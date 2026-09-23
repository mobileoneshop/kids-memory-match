package com.one.memorymatch

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import com.one.memorymatch.ui.theme.CardShape
import com.one.memorymatch.ui.theme.PackColors
import com.one.memorymatch.ui.theme.PillShape
import com.one.memorymatch.ui.theme.StarEmpty
import com.one.memorymatch.ui.theme.StarGold
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class DesignSystemTest {

    @Test
    fun shapes_haveCorrectCornerRadii() {
        assertEquals(RoundedCornerShape(24.dp), CardShape)
        assertEquals(RoundedCornerShape(32.dp), PillShape)
    }

    @Test
    fun packColors_allV1PacksAreConfigured() {
        // 8 packs
        assertNotNull(PackColors.ZooPrimary)
        assertNotNull(PackColors.FarmPrimary)
        assertNotNull(PackColors.SeaPrimary)
        assertNotNull(PackColors.BirdsPrimary)
        assertNotNull(PackColors.FruitsPrimary)
        assertNotNull(PackColors.VegetablesPrimary)
        assertNotNull(PackColors.VehiclesPrimary)
        assertNotNull(PackColors.ShapesColorsPrimary)
        // Virtual pack
        assertNotNull(PackColors.AllMixPrimary)
    }

    @Test
    fun starColors_areDefined() {
        assertNotNull(StarGold)
        assertNotNull(StarEmpty)
    }
}
