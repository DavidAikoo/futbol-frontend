package com.futbol.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.futbol.app.ui.screens.*

sealed class Screen(val route: String) {
    object Home         : Screen("home")
    object Equipos      : Screen("equipos")
    object EquipoDetail : Screen("equipos/{id}") {
        fun createRoute(id: Int) = "equipos/$id"
    }
    object Jugadores      : Screen("jugadores")
    object JugadorDetail  : Screen("jugadores/{id}") {
        fun createRoute(id: Int) = "jugadores/$id"
    }
    object Partidos      : Screen("partidos")
    object Estadisticas  : Screen("estadisticas")
    object Entrenadores  : Screen("entrenadores")
}

@Composable
fun FutbolNavGraph(
    navController: NavHostController,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                navController = navController,
                darkTheme = darkTheme,
                onToggleTheme = onToggleTheme
            )
        }

        composable(Screen.Equipos.route) {
            EquiposScreen(
                navController = navController,
                darkTheme = darkTheme,
                onToggleTheme = onToggleTheme
            )
        }

        composable(
            route = Screen.EquipoDetail.route,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("id") ?: return@composable
            EquipoDetailScreen(
                equipoId = id,
                navController = navController
            )
        }

        composable(Screen.Jugadores.route) {
            JugadoresScreen(
                navController = navController,
                darkTheme = darkTheme,
                onToggleTheme = onToggleTheme
            )
        }

        composable(
            route = Screen.JugadorDetail.route,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("id") ?: return@composable
            JugadorDetailScreen(
                jugadorId = id,
                navController = navController
            )
        }

        composable(Screen.Partidos.route) {
            PartidosScreen(
                navController = navController,
                darkTheme = darkTheme,
                onToggleTheme = onToggleTheme
            )
        }

        composable(Screen.Estadisticas.route) {
            EstadisticasScreen(
                navController = navController,
                darkTheme = darkTheme,
                onToggleTheme = onToggleTheme
            )
        }

        composable(Screen.Entrenadores.route) {
            EntrenadoresScreen(
                navController = navController,
                darkTheme = darkTheme,
                onToggleTheme = onToggleTheme
            )
        }
    }
}
