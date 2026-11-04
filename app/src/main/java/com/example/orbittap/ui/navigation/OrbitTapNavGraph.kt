package com.example.orbittap.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.orbittap.game.GameViewModel
import com.example.orbittap.ui.screens.AboutScreen
import com.example.orbittap.ui.screens.GameScreen
import com.example.orbittap.ui.screens.HomeScreen
import com.example.orbittap.ui.screens.ResultScreen
import com.example.orbittap.ui.screens.SettingsScreen
import com.example.orbittap.ui.screens.SplashScreen

@Composable
fun OrbitTapNavGraph(
    viewModel: GameViewModel,
    navController: NavHostController = rememberNavController()
) {
    val preferences by viewModel.userPreferences.collectAsStateWithLifecycle()
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        enterTransition = { fadeIn(animationSpec = tween(280)) },
        exitTransition = { fadeOut(animationSpec = tween(280)) },
        popEnterTransition = { fadeIn(animationSpec = tween(280)) },
        popExitTransition = { fadeOut(animationSpec = tween(280)) }
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                preferences = preferences,
                onPlayClick = {
                    viewModel.audioEngine.playUiClick()
                    viewModel.startNewGame()
                    navController.navigate(Screen.Game.route)
                },
                onSettingsClick = {
                    viewModel.audioEngine.playUiClick()
                    navController.navigate(Screen.Settings.route)
                },
                onAboutClick = {
                    viewModel.audioEngine.playUiClick()
                    navController.navigate(Screen.About.route)
                }
            )
        }

        composable(Screen.Game.route) {
            GameScreen(
                viewModel = viewModel,
                onGameOver = {
                    navController.navigate(Screen.Result.route) {
                        popUpTo(Screen.Game.route) { inclusive = true }
                    }
                },
                onNavigateHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Result.route) {
            ResultScreen(
                gameState = gameState,
                onPlayAgain = {
                    viewModel.audioEngine.playUiClick()
                    viewModel.startNewGame()
                    navController.navigate(Screen.Game.route) {
                        popUpTo(Screen.Result.route) { inclusive = true }
                    }
                },
                onNavigateHome = {
                    viewModel.audioEngine.playUiClick()
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Result.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                preferences = preferences,
                onThemeSelect = { mode ->
                    viewModel.audioEngine.playUiClick()
                    viewModel.setThemeMode(mode)
                },
                onSoundToggle = { enabled ->
                    viewModel.setSoundEnabled(enabled)
                },
                onHapticsToggle = { enabled ->
                    viewModel.setHapticsEnabled(enabled)
                },
                onResetStats = {
                    viewModel.resetBestScore()
                },
                onNavigateBack = {
                    viewModel.audioEngine.playUiClick()
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.About.route) {
            AboutScreen(
                onNavigateBack = {
                    viewModel.audioEngine.playUiClick()
                    navController.popBackStack()
                }
            )
        }
    }
}
