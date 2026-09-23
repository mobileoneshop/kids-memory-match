package com.one.memorymatch

import com.one.memorymatch.ui.navigation.NavRoutes
import org.junit.Assert.assertEquals
import org.junit.Test

class ScaffoldTest {
    @Test
    fun splashRoute_isConfigured() {
        assertEquals("splash", NavRoutes.SPLASH)
    }
}
