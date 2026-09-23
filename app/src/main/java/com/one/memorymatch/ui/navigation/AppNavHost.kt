package com.one.memorymatch.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.one.memorymatch.ui.game.GameScreen
import com.one.memorymatch.ui.home.HomeScreen
import com.one.memorymatch.ui.level.LevelSelectScreen
import com.one.memorymatch.ui.preview.DesignSystemPreviewScreen
import com.one.memorymatch.ui.settings.SettingsScreen
import com.one.memorymatch.ui.splash.SplashScreen

object NavRoutes {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val LEVEL_SELECT = "level_select/{packId}"
    const val GAME = "game/{packId}/{level}"
    const val SETTINGS = "settings"
    const val PREVIEW = "preview"

    fun levelSelect(packId: String) = "level_select/$packId"
    fun game(packId: String, level: String) = "game/$packId/$level"
}

// Slide-in from right (forward navigation)
private val slideInFromRight = slideInHorizontally(initialOffsetX = { it }) + fadeIn()
// Slide-out to left (when a new screen pushes current one)
private val slideOutToLeft = slideOutHorizontally(targetOffsetX = { -it / 3 }) + fadeOut()
// Slide-in from left (back pop — the screen we're returning to re-enters)
private val slideInFromLeft = slideInHorizontally(initialOffsetX = { -it / 3 }) + fadeIn()
// Slide-out to right (popping — current screen exits right)
private val slideOutToRight = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = NavRoutes.SPLASH
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(
            route = NavRoutes.SPLASH,
            exitTransition = { fadeOut() },
            popEnterTransition = { fadeIn() }
        ) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.SPLASH) { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = NavRoutes.HOME,
            enterTransition = { fadeIn() },
            exitTransition = { slideOutToLeft },
            popEnterTransition = { slideInFromLeft },
            popExitTransition = { slideOutToRight }
        ) {
            HomeScreen(
                onPackSelected = { packId ->
                    navController.navigate(NavRoutes.levelSelect(packId))
                },
                onOpenSettings = {
                    navController.navigate(NavRoutes.SETTINGS)
                }
            )
        }
        composable(
            route = NavRoutes.LEVEL_SELECT,
            arguments = listOf(navArgument("packId") { type = NavType.StringType }),
            enterTransition = { slideInFromRight },
            exitTransition = { slideOutToLeft },
            popEnterTransition = { slideInFromLeft },
            popExitTransition = { slideOutToRight }
        ) { backStackEntry ->
            val packId = backStackEntry.arguments?.getString("packId") ?: ""
            LevelSelectScreen(
                packId = packId,
                onLevelSelected = { level ->
                    navController.navigate(NavRoutes.game(packId, level.name.lowercase()))
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = NavRoutes.GAME,
            arguments = listOf(
                navArgument("packId") { type = NavType.StringType },
                navArgument("level") { type = NavType.StringType }
            ),
            enterTransition = { slideInFromRight },
            exitTransition = { slideOutToLeft },
            popEnterTransition = { slideInFromLeft },
            popExitTransition = { slideOutToRight }
        ) { backStackEntry ->
            val packId = backStackEntry.arguments?.getString("packId") ?: ""
            val level = backStackEntry.arguments?.getString("level") ?: ""
            GameScreen(
                packId = packId,
                level = level,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = NavRoutes.SETTINGS,
            enterTransition = { slideInFromRight },
            exitTransition = { slideOutToRight },
            popEnterTransition = { slideInFromLeft },
            popExitTransition = { slideOutToRight }
        ) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(NavRoutes.PREVIEW) {
            DesignSystemPreviewScreen()
        }
    }
}
