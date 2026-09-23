package com.one.memorymatch.data.repository

import android.content.Context
import com.one.memorymatch.R
import com.one.memorymatch.data.model.CardItem
import com.one.memorymatch.data.model.CardPack
import com.one.memorymatch.data.util.JsonElement
import java.io.InputStream

class PackRepository(
    private val openAsset: (String) -> InputStream
) {
    constructor(context: Context) : this({ path -> context.assets.open(path) })

    private val packsMap = LinkedHashMap<String, CardPack>()
    private val itemsMap = LinkedHashMap<String, CardItem>()
    private val packItemsMap = LinkedHashMap<String, MutableList<CardItem>>()

    val packs: List<CardPack>
        get() = packsMap.values.toList()

    val allItems: List<CardItem>
        get() = itemsMap.values.toList()

    init {
        loadManifest()
    }

    private fun loadManifest() {
        try {
            val jsonString = openAsset("packs.json").bufferedReader().use { it.readText() }
            val root = JsonElement.parse(jsonString) as JsonElement.JsonObject
            val packsArray = root.getArray("packs")

            for (i in 0 until packsArray.size) {
                val packObj = packsArray[i] as JsonElement.JsonObject
                val packId = packObj.getString("id")
                val packName = packObj.getString("name")
                val icon = packObj.getString("icon")
                val primaryColor = parseHexColor(packObj.getString("primaryColor"))
                val darkColor = parseHexColor(packObj.getString("darkColor"))
                val itemsArray = packObj.getArray("items")

                val itemIds = mutableListOf<String>()
                val packItems = mutableListOf<CardItem>()

                for (j in 0 until itemsArray.size) {
                    val itemObj = itemsArray[j] as JsonElement.JsonObject
                    val itemId = itemObj.getString("id")
                    val itemName = itemObj.getString("name")
                    val image = itemObj.getString("image")
                    val sound = itemObj.optString("sound")?.takeIf { it.isNotBlank() }
                    val speak = itemObj.getString("speak")

                    val cardItem = CardItem(
                        id = itemId,
                        packId = packId,
                        name = itemName,
                        imageAsset = image,
                        soundAsset = sound,
                        speakText = speak
                    )

                    itemIds.add(itemId)
                    packItems.add(cardItem)
                    itemsMap[itemId] = cardItem
                }

                val nameRes = getPackNameRes(packId)
                val cardPack = CardPack(
                    id = packId,
                    name = packName,
                    nameRes = nameRes,
                    iconAsset = icon,
                    primaryColor = primaryColor,
                    darkColor = darkColor,
                    itemIds = itemIds,
                    isVirtual = false
                )

                packsMap[packId] = cardPack
                packItemsMap[packId] = packItems
            }
        } catch (e: Exception) {
            System.err.println("KidsMemory: Failed to load packs.json: ${e.message}")
        }
    }

    fun getAllPacks(includeVirtual: Boolean = true): List<CardPack> {
        val list = packsMap.values.toMutableList()
        if (includeVirtual && list.isNotEmpty()) {
            list.add(
                CardPack(
                    id = ALL_MIX_ID,
                    name = "All Mix",
                    nameRes = R.string.pack_allmix,
                    iconAsset = "cards/allmix/shuffle.webp",
                    primaryColor = 0xFF7C4DFF,
                    darkColor = 0xFF512DA8,
                    itemIds = itemsMap.keys.toList(),
                    isVirtual = true
                )
            )
        }
        return list
    }

    fun getPack(id: String): CardPack? = getAllPacks(includeVirtual = true).find { it.id == id }

    fun getItemsForPack(packId: String): List<CardItem> {
        if (packId == ALL_MIX_ID) return itemsMap.values.toList()
        return packItemsMap[packId] ?: emptyList()
    }

    fun getItem(itemId: String): CardItem? = itemsMap[itemId]

    private fun getPackNameRes(packId: String): Int {
        return when (packId) {
            "zoo" -> R.string.pack_zoo
            "farm" -> R.string.pack_farm
            "sea" -> R.string.pack_sea
            "birds" -> R.string.pack_birds
            "fruits" -> R.string.pack_fruits
            "vegetables" -> R.string.pack_vegetables
            "vehicles" -> R.string.pack_vehicles
            "shapes_colors" -> R.string.pack_shapes_colors
            ALL_MIX_ID -> R.string.pack_allmix
            else -> 0
        }
    }

    companion object {
        const val ALL_MIX_ID = "allmix"

        fun parseHexColor(hex: String): Long {
            val clean = hex.removePrefix("#")
            val value = clean.toLong(16)
            return if (clean.length == 6) {
                0xFF000000L or value
            } else {
                value
            }
        }
    }
}
