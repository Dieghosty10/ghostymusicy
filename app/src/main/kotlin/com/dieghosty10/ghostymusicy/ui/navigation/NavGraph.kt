package com.dieghosty10.ghostymusicy.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.dieghosty10.ghostymusicy.ui.screens.HomeScreen
import com.dieghosty10.ghostymusicy.ui.screens.PlayerScreen
import com.dieghosty10.ghostymusicy.ui.screens.SearchScreen
import com.dieghosty10.ghostymusicy.ui.screens.SettingsScreen
import dev.chrisbanes.haze.HazeState

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val VERIFICATION = "verification"
    const val ADMIN = "admin"
    const val ONBOARDING = "onboarding"
    const val HOME     = "home"
    const val SEARCH   = "search"
    const val PLAYER   = "player"
    const val SETTINGS = "settings"
    const val LIBRARY  = "library"
    const val DOWNLOADS = "downloads"
    
    fun Album(id: String) = "album/$id"
    fun Artist(id: String) = "artist/$id"
}

@Composable
fun MainNavGraph(
    navController: NavHostController,
    startDestination: String = Routes.HOME,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController   = navController,
        startDestination = startDestination,
        modifier        = modifier,
        enterTransition = { androidx.compose.animation.EnterTransition.None },
        exitTransition = { androidx.compose.animation.ExitTransition.None },
        popEnterTransition = { androidx.compose.animation.EnterTransition.None },
        popExitTransition = { androidx.compose.animation.ExitTransition.None }
    ) {
        composable(Routes.LOGIN) {
            com.dieghosty10.ghostymusicy.ui.screens.auth.LoginScreen(
                onNavigateToHome = { isFirstTime ->
                    val nextRoute = if (isFirstTime) Routes.ONBOARDING else Routes.HOME
                    navController.navigate(nextRoute) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER)
                }
            )
        }
        composable(Routes.REGISTER) {
            com.dieghosty10.ghostymusicy.ui.screens.auth.RegisterScreen(
                onNavigateToVerification = {
                    navController.navigate(Routes.VERIFICATION) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToHome = { isFirstTime ->
                    val nextRoute = if (isFirstTime) Routes.ONBOARDING else Routes.HOME
                    navController.navigate(nextRoute) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateBackToLogin = { navController.popBackStack() }
            )
        }
        composable(Routes.VERIFICATION) {
            com.dieghosty10.ghostymusicy.ui.screens.auth.VerificationScreen(
                onNavigateToHome = {
                    val isFirstTime = com.dieghosty10.ghostymusicy.utils.PreferenceStore.get(com.dieghosty10.ghostymusicy.constants.IsFirstTimeAppLaunchKey) ?: true
                    val nextRoute = if (isFirstTime) Routes.ONBOARDING else Routes.HOME
                    navController.navigate(nextRoute) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.ADMIN) {
            com.dieghosty10.ghostymusicy.ui.screens.AdminScreen(navController = navController)
        }
        composable(Routes.ONBOARDING) {
            com.dieghosty10.ghostymusicy.ui.screens.OnboardingScreen(
                onFinish = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.HOME) {
            HomeScreen(hazeState = hazeState, navController = navController)
        }
        composable(Routes.SEARCH) {
            SearchScreen(navController = navController)
        }
        composable(Routes.PLAYER) {
            PlayerScreen(navController = navController)
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(navController = navController)
        }
        composable(Routes.LIBRARY) {
            com.dieghosty10.ghostymusicy.ui.screens.LibraryScreen(navController = navController)
        }
        composable(Routes.DOWNLOADS) {
            com.dieghosty10.ghostymusicy.ui.screens.DownloadsScreen(navController = navController)
        }
        composable("album/{albumId}") {
            com.dieghosty10.ghostymusicy.ui.screens.AlbumScreen(navController = navController)
        }
        composable("artist/{artistId}") {
            com.dieghosty10.ghostymusicy.ui.screens.ArtistScreen(navController = navController)
        }
    }
}
