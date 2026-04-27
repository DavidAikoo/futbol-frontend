package com.futbol.app

import SplashScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.futbol.app.navigation.Screen
import com.futbol.app.ui.screens.*
import com.futbol.app.ui.theme.FutbolAppTheme
import com.futbol.app.viewmodel.DarkModeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val darkModeViewModel: DarkModeViewModel = viewModel()
            val systemDark = isSystemInDarkTheme()
            LaunchedEffect(Unit) { darkModeViewModel.setDark(systemDark) }

            FutbolAppTheme(darkTheme = darkModeViewModel.isDarkMode) {
                FutbolAppRoot(darkModeViewModel = darkModeViewModel)
            }
        }
    }
}

@Composable
fun FutbolAppRoot(darkModeViewModel: DarkModeViewModel) {
    var splashDone by remember { mutableStateOf(false) }

    AnimatedContent(
        targetState = splashDone,
        transitionSpec = {
            fadeIn(tween(500)) togetherWith fadeOut(tween(300))
        },
        label = "splash_transition"
    ) { done ->
        if (!done) {
            SplashScreen(onFinished = { splashDone = true })
        } else {
            FutbolAppContent(darkModeViewModel = darkModeViewModel)
        }
    }
}

@Composable
fun FutbolAppContent(darkModeViewModel: DarkModeViewModel) {
    val navController = rememberNavController()
    val darkTheme = darkModeViewModel.isDarkMode
    val onToggle = { darkModeViewModel.toggle() }

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        enterTransition = {
            slideInHorizontally(tween(300)) { it / 2 } + fadeIn(tween(300))
        },
        exitTransition = {
            slideOutHorizontally(tween(300)) { -it / 2 } + fadeOut(tween(200))
        },
        popEnterTransition = {
            slideInHorizontally(tween(300)) { -it / 2 } + fadeIn(tween(300))
        },
        popExitTransition = {
            slideOutHorizontally(tween(300)) { it / 2 } + fadeOut(tween(200))
        }
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController = navController, darkTheme = darkTheme, onToggleTheme = onToggle)
        }
        composable(Screen.Equipos.route) {
            EquiposScreen(navController = navController, darkTheme = darkTheme, onToggleTheme = onToggle)
        }
        composable(
            route = Screen.EquipoDetail.route,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { back ->
            EquipoDetailScreen(
                equipoId = back.arguments?.getInt("id") ?: 0,
                navController = navController
            )
        }
        composable(Screen.Jugadores.route) {
            JugadoresScreen(navController = navController, darkTheme = darkTheme, onToggleTheme = onToggle)
        }
        composable(
            route = Screen.JugadorDetail.route,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { back ->
            JugadorDetailScreen(
                jugadorId = back.arguments?.getInt("id") ?: 0,
                navController = navController
            )
        }
        composable(Screen.Partidos.route) {
            PartidosScreen(navController = navController, darkTheme = darkTheme, onToggleTheme = onToggle)
        }
        composable(Screen.Estadisticas.route) {
            EstadisticasScreen(navController = navController, darkTheme = darkTheme, onToggleTheme = onToggle)
        }
        composable(Screen.Entrenadores.route) {
            EntrenadoresScreen(navController = navController, darkTheme = darkTheme, onToggleTheme = onToggle)
        }
    }
}
