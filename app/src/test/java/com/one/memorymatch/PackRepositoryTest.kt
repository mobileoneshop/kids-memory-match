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

class PackRepositoryTest {

    private lateinit var repository: PackRepository

    @Before
    fun setUp() {
        // Provide asset stream from local test directory
        val openAsset: (String) -> InputStream = { path ->
            val file = File("src/main/assets", path)
            if (file.exists()) {
                FileInputStream(file)
            } else {
                File("../app/src/main/assets", path).inputStream()
            }
        }
        repository = PackRepository(openAsset)
    }

    @Test
    fun manifest_parsesEightRealPacksAndNinetySixItems() {
        val packs = repository.packs
        assertEquals("Expected 8 real packs in manifest", 8, packs.size)

        val items = repository.allItems
        assertEquals("Expected exactly 96 items across 8 packs", 96, items.size)
    }

    @Test
    fun packs_allFieldsAreNonBlankAndTwelveItemsEach() {
        val expectedPackIds = listOf(
            "zoo", "farm", "sea", "birds", "fruits", "vegetables", "vehicles", "shapes_colors"
        )

        for (packId in expectedPackIds) {
            val pack = repository.getPack(packId)
            assertNotNull("Pack $packId must exist", pack)
            checkNotNull(pack)

            assertTrue("Pack id must not be blank", pack.id.isNotBlank())
            assertTrue("Pack name must not be blank", pack.name.isNotBlank())
            assertTrue("Pack iconAsset must not be blank", pack.iconAsset.isNotBlank())
            assertTrue("Pack primaryColor must be non-zero", pack.primaryColor != 0L)
            assertTrue("Pack darkColor must be non-zero", pack.darkColor != 0L)
            assertEquals("Pack $packId must have exactly 12 itemIds", 12, pack.itemIds.size)

            val packItems = repository.getItemsForPack(packId)
            assertEquals("Pack $packId must resolve to 12 CardItem instances", 12, packItems.size)
        }
    }

    @Test
    fun items_allFieldsAreNonBlankAndUnique() {
        val items = repository.allItems
        val seenIds = mutableSetOf<String>()

        for (item in items) {
            assertTrue("Item id must not be blank", item.id.isNotBlank())
            assertTrue("Duplicate item id found: ${item.id}", seenIds.add(item.id))
            assertTrue("Item packId must not be blank", item.packId.isNotBlank())
            assertTrue("Item name must not be blank", item.name.isNotBlank())
            assertTrue("Item imageAsset must not be blank", item.imageAsset.isNotBlank())
            assertTrue("Item speakText must not be blank", item.speakText.isNotBlank())
        }
    }

    @Test
    fun allMix_isVirtualAndContainsAllNinetySixItems() {
        val allMix = repository.getPack(PackRepository.ALL_MIX_ID)
        assertNotNull("All Mix pack must exist", allMix)
        checkNotNull(allMix)

        assertTrue("All Mix must be marked virtual", allMix.isVirtual)
        assertEquals("All Mix must include all 96 item ids", 96, allMix.itemIds.size)

        val items = repository.getItemsForPack(PackRepository.ALL_MIX_ID)
        assertEquals("All Mix items list must contain 96 items", 96, items.size)
    }

    @Test
    fun hexColorParser_handlesSixAndEightDigitHex() {
        val color6 = PackRepository.parseHexColor("#4CAF50")
        assertEquals(0xFF4CAF50L, color6)

        val color8 = PackRepository.parseHexColor("#804CAF50")
        assertEquals(0x804CAF50L, color8)
    }
}
