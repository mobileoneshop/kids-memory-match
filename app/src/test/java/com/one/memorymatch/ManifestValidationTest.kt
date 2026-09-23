package com.one.memorymatch

import com.one.memorymatch.data.repository.PackRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

/**
 * ManifestValidationTest verifies the presence, validity, and file integrity of
 * all assets declared in packs.json (all 96 card WebP images, 8 pack icons, UI sound effects,
 * and background music).
 */
class ManifestValidationTest {

    private lateinit var repository: PackRepository
    private lateinit var assetsDir: File

    @Before
    fun setUp() {
        val primary = File("src/main/assets")
        assetsDir = if (primary.exists()) primary else File("../app/src/main/assets")
        assertTrue("Assets directory must exist at ${assetsDir.absolutePath}", assetsDir.exists())

        val openAsset: (String) -> InputStream = { path ->
            FileInputStream(File(assetsDir, path))
        }
        repository = PackRepository(openAsset)
    }

    @Test
    fun manifest_allPacksAndItemsExistOnDiskAsValidWebp() {
        val packs = repository.packs
        assertEquals("Expected 8 packs in manifest", 8, packs.size)

        var totalItems = 0
        for (pack in packs) {
            // Verify pack icon
            val iconFile = File(assetsDir, pack.iconAsset)
            assertTrue("Pack icon for ${pack.id} must exist at ${iconFile.path}", iconFile.exists())
            assertTrue("Pack icon for ${pack.id} must not be empty", iconFile.length() > 500)
            verifyWebpHeader(iconFile)

            val items = repository.getItemsForPack(pack.id)
            assertEquals("Pack ${pack.id} must have 12 items", 12, items.size)
            totalItems += items.size

            for (item in items) {
                // Verify card image asset
                assertTrue("Item ${item.id} imageAsset must be set", item.imageAsset.isNotBlank())
                assertTrue("Item ${item.id} imageAsset must end with .webp", item.imageAsset.endsWith(".webp"))

                val imageFile = File(assetsDir, item.imageAsset)
                assertTrue(
                    "Card art for ${pack.id}/${item.id} must exist at ${imageFile.path}",
                    imageFile.exists()
                )
                assertTrue(
                    "Card art for ${pack.id}/${item.id} must be > 1KB (was ${imageFile.length()} bytes)",
                    imageFile.length() > 1024
                )
                verifyWebpHeader(imageFile)
            }
        }

        assertEquals("Total validated card images must be 96", 96, totalItems)
    }

    @Test
    fun audio_allUiSoundsAndMusicExistOnDisk() {
        val expectedSounds = listOf(
            "sounds/ui/click.mp3",
            "sounds/ui/flip.mp3",
            "sounds/ui/match.mp3",
            "sounds/ui/mismatch.mp3",
            "sounds/ui/star.mp3",
            "sounds/ui/win.mp3"
        )

        for (soundPath in expectedSounds) {
            val soundFile = File(assetsDir, soundPath)
            assertTrue("UI sound must exist at ${soundFile.path}", soundFile.exists())
            assertTrue("UI sound $soundPath must not be empty", soundFile.length() > 500)
        }

        val musicFile = File(assetsDir, "music/bg_loop.mp3")
        assertTrue("Background music must exist at ${musicFile.path}", musicFile.exists())
        assertTrue("Background music must be > 10KB (was ${musicFile.length()} bytes)", musicFile.length() > 10000)
    }

    private fun verifyWebpHeader(file: File) {
        val bytes = ByteArray(12)
        FileInputStream(file).use { it.read(bytes) }
        // RIFF....WEBP
        val riff = String(bytes, 0, 4, Charsets.US_ASCII)
        val webp = String(bytes, 8, 4, Charsets.US_ASCII)
        assertEquals("File ${file.name} must start with RIFF", "RIFF", riff)
        assertEquals("File ${file.name} must have WEBP magic bytes", "WEBP", webp)
    }
}
