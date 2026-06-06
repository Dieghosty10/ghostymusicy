package com.dieghosty10.ghostymusicy

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.lifecycleScope
import com.dieghosty10.ghostymusicy.db.MusicDatabase
import com.dieghosty10.ghostymusicy.playback.MusicService
import com.dieghosty10.ghostymusicy.playback.PlayerConnection
import com.dieghosty10.ghostymusicy.ui.navigation.MainNavGraph
import com.dieghosty10.ghostymusicy.ui.navigation.Routes
import com.dieghosty10.ghostymusicy.ui.screens.MiniPlayer
import com.dieghosty10.ghostymusicy.ui.theme.ghostymusicyTheme
import com.dieghosty10.ghostymusicy.utils.PreferenceStore
import dagger.hilt.android.AndroidEntryPoint
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import javax.inject.Inject

val LocalPlayerConnection = staticCompositionLocalOf<PlayerConnection?> { null }

data class NavItem(val route: String, val icon: ImageVector, val label: String)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var database: MusicDatabase

    private var playerConnection by mutableStateOf<PlayerConnection?>(null)

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            if (service is MusicService.MusicBinder) {
                playerConnection = PlayerConnection(
                    context  = this@MainActivity,
                    binder   = service,
                    database = database,
                    scope    = lifecycleScope,
                )
            }
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            playerConnection?.dispose()
            playerConnection = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        PreferenceStore.start(this)

        val intent = Intent(this, MusicService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)

        setContent {
            ghostymusicyTheme {
                val navController = rememberNavController()
                val hazeState     = remember { HazeState() }

                val navItems = listOf(
                    NavItem(Routes.HOME,     Icons.Rounded.Home,     "Inicio"),
                    NavItem(Routes.SEARCH,   Icons.Rounded.Search,   "Buscar"),
                    NavItem(Routes.SETTINGS, Icons.Rounded.Settings,  "Ajustes"),
                )

                val isFirstTime by com.dieghosty10.ghostymusicy.utils.rememberPreference(com.dieghosty10.ghostymusicy.constants.IsFirstTimeAppLaunchKey, true)
                val startDest = if (isFirstTime) Routes.ONBOARDING else Routes.HOME

                CompositionLocalProvider(LocalPlayerConnection provides playerConnection) {
                    Scaffold(
                        bottomBar = {
                            val backStack by navController.currentBackStackEntryAsState()
                            val current   = backStack?.destination?.route

                            // Ocultar bottom bar cuando está en el reproductor o en el onboarding
                            if (current != Routes.PLAYER && current != Routes.ONBOARDING) {
                                Column {
                                    MiniPlayer(
                                        onClick = {
                                            navController.navigate(Routes.PLAYER) {
                                                popUpTo(Routes.HOME)
                                            }
                                        }
                                    )
                                    NavigationBar(
                                        modifier       = Modifier.hazeChild(
                                            state = hazeState,
                                            style = HazeStyle(
                                                backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                                tint            = HazeTint(Color.Transparent),
                                                blurRadius      = 28.dp,
                                            )
                                        ),
                                        containerColor = Color.Transparent,
                                        tonalElevation = 0.dp,
                                    ) {
                                        navItems.forEach { item ->
                                            val selected = current == item.route
                                            NavigationBarItem(
                                                selected  = selected,
                                                onClick   = {
                                                    navController.navigate(item.route) {
                                                        popUpTo(Routes.HOME) { saveState = true }
                                                        launchSingleTop = true
                                                        restoreState    = true
                                                    }
                                                },
                                                icon      = {
                                                    Icon(
                                                        item.icon,
                                                        contentDescription = item.label,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                },
                                                label     = { Text(item.label) },
                                                colors    = NavigationBarItemDefaults.colors(
                                                    selectedIconColor   = MaterialTheme.colorScheme.primary,
                                                    selectedTextColor   = MaterialTheme.colorScheme.primary,
                                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    indicatorColor      = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        },
                    ) { innerPadding ->
                        MainNavGraph(
                            navController = navController,
                            startDestination = startDest,
                            hazeState     = hazeState,
                            modifier      = Modifier.padding(innerPadding),
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnection)
        playerConnection?.dispose()
    }
}
