package com.one.memorymatch.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.one.memorymatch.data.model.AppSettings
import com.one.memorymatch.data.model.GameLevel
import com.one.memorymatch.data.model.PackProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = "kids_memory_prefs")

class ProgressRepository(
    private val dataStore: DataStore<Preferences>
) {
    constructor(context: Context) : this(context.userPreferencesDataStore)

    private val safePreferencesFlow: Flow<Preferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                System.err.println("KidsMemory: IOException reading preferences: ${exception.message}")
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }

    val settingsFlow: Flow<AppSettings> = safePreferencesFlow.map { prefs ->
        AppSettings(
            soundOn = prefs[KEY_SOUND_ON] ?: true,
            musicOn = prefs[KEY_MUSIC_ON] ?: true,
            language = prefs[KEY_LANGUAGE] ?: "en"
        )
    }

    suspend fun getSettings(): AppSettings = settingsFlow.first()

    suspend fun setSoundEnabled(enabled: Boolean) {
        editWithRetry { prefs ->
            prefs[KEY_SOUND_ON] = enabled
        }
    }

    suspend fun setMusicEnabled(enabled: Boolean) {
        editWithRetry { prefs ->
            prefs[KEY_MUSIC_ON] = enabled
        }
    }

    fun getProgressFlow(packId: String, level: GameLevel): Flow<PackProgress> {
        val key = progressKey(packId, level)
        return safePreferencesFlow.map { prefs ->
            parseProgress(packId, level, prefs[key])
        }
    }

    suspend fun getProgress(packId: String, level: GameLevel): PackProgress {
        return getProgressFlow(packId, level).first()
    }

    suspend fun getStarsForPack(packId: String): Int {
        val prefs = safePreferencesFlow.first()
        var total = 0
        for (level in GameLevel.entries) {
            val progress = parseProgress(packId, level, prefs[progressKey(packId, level)])
            total += progress.stars
        }
        return total
    }

    suspend fun getTotalStars(packIds: List<String>): Int {
        val prefs = safePreferencesFlow.first()
        var total = 0
        for (packId in packIds) {
            for (level in GameLevel.entries) {
                val progress = parseProgress(packId, level, prefs[progressKey(packId, level)])
                total += progress.stars
            }
        }
        return total
    }

    fun getAllPacksStarsFlow(packIds: List<String>): Flow<Map<String, Int>> {
        return safePreferencesFlow.map { prefs ->
            packIds.associateWith { packId ->
                GameLevel.entries.sumOf { level ->
                    parseProgress(packId, level, prefs[progressKey(packId, level)]).stars
                }
            }
        }
    }

    /**
     * Records game results ensuring best-only-improves rule:
     * - Stars: max(currentStars, newStars)
     * - Best moves: min(currentBestMoves, newMoves)
     * - Best time: min(currentBestTime, newTimeSec)
     *
     * Returns true if any best metric improved.
     */
    suspend fun saveGameResult(
        packId: String,
        level: GameLevel,
        starsEarned: Int,
        moves: Int,
        timeSec: Int
    ): Boolean {
        var improved = false
        val key = progressKey(packId, level)

        editWithRetry { prefs ->
            val current = parseProgress(packId, level, prefs[key])

            val newStars = starsEarned.coerceIn(0, 3).coerceAtLeast(current.stars)
            val newBestMoves = if (current.bestMoves == Int.MAX_VALUE || moves < current.bestMoves) {
                moves
            } else {
                current.bestMoves
            }
            val newBestTime = if (current.bestTimeSec <= 0 || (timeSec in 1 until current.bestTimeSec)) {
                timeSec
            } else {
                current.bestTimeSec
            }

            if (newStars > current.stars || newBestMoves < current.bestMoves || (newBestTime < current.bestTimeSec && newBestTime > 0)) {
                improved = true
            }

            prefs[key] = "$newStars,$newBestMoves,$newBestTime"
        }

        return improved
    }

    suspend fun resetAllProgress() {
        editWithRetry { prefs ->
            val keysToRemove = prefs.asMap().keys.filter { it.name.startsWith("progress_") }
            for (key in keysToRemove) {
                prefs.remove(key)
            }
        }
    }

    private suspend fun editWithRetry(action: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        try {
            dataStore.edit(action)
        } catch (e: IOException) {
            System.err.println("KidsMemory: DataStore write failed, retrying once: ${e.message}")
            try {
                dataStore.edit(action)
            } catch (retryException: Exception) {
                System.err.println("KidsMemory: DataStore retry also failed: ${retryException.message}")
            }
        }
    }

    companion object {
        val KEY_SOUND_ON = booleanPreferencesKey("settings_sound")
        val KEY_MUSIC_ON = booleanPreferencesKey("settings_music")
        val KEY_LANGUAGE = stringPreferencesKey("settings_language")
        val KEY_ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")

        fun progressKey(packId: String, level: GameLevel): Preferences.Key<String> {
            return stringPreferencesKey("progress_${packId}_${level.name.lowercase()}")
        }

        fun parseProgress(packId: String, level: GameLevel, raw: String?): PackProgress {
            val default = PackProgress(
                packId = packId,
                level = level,
                stars = 0,
                bestMoves = Int.MAX_VALUE,
                bestTimeSec = 0
            )

            if (raw.isNullOrBlank()) return default

            return try {
                val parts = raw.split(",")
                if (parts.size != 3) return default

                val stars = parts[0].trim().toInt().coerceIn(0, 3)
                val bestMoves = parts[1].trim().toInt().let { if (it <= 0) Int.MAX_VALUE else it }
                val bestTimeSec = parts[2].trim().toInt().coerceAtLeast(0)

                PackProgress(
                    packId = packId,
                    level = level,
                    stars = stars,
                    bestMoves = bestMoves,
                    bestTimeSec = bestTimeSec
                )
            } catch (e: Exception) {
                // Corruption -> defaults (per ERROR_HANDLING.md §3)
                System.err.println("KidsMemory: Corrupt progress data '$raw' for $packId $level: ${e.message}")
                default
            }
        }
    }
}
