package com.cacutler.cardgamepointtracker.navigation
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cacutler.cardgamepointtracker.ui.screens.*
import com.cacutler.cardgamepointtracker.ui.viewmodels.*
import com.cacutler.cardgamepointtracker.repository.GameRepository
sealed class Screen(val route: String) {
    object Main: Screen("main")
    object Game: Screen("game/{gameId}") {
        fun createRoute(gameId: String) = "game/$gameId"
    }
    object RoundHistory: Screen("history/{gameId}") {
        fun createRoute(gameId: String) = "history/$gameId"
    }
}
@Composable
fun AppNavigation(repository: GameRepository, navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Screen.Main.route) {
        composable(Screen.Main.route) {
            val viewModel: MainViewModel = viewModel(factory = MainViewModelFactory(repository))
            MainScreen(viewModel = viewModel, onNavigateToGame = {gameId -> navController.navigate(Screen.Game.createRoute(gameId)) })
        }
        composable(route = Screen.Game.route, arguments = listOf(navArgument("gameId") { type = NavType.StringType })) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId") ?: return@composable
            val viewModel: GameViewModel = viewModel(factory = GameViewModelFactory(repository, gameId))
            GameScreen(viewModel = viewModel, onNavigateBack = {navController.popBackStack()}, onNavigateToHistory = {navController.navigate(Screen.RoundHistory.createRoute(gameId))}, repository = repository)
        }
        composable(route = Screen.RoundHistory.route, arguments = listOf(navArgument("gameId") {type = NavType.StringType})) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId") ?: return@composable
            RoundHistoryScreen(repository = repository, gameId = gameId, onNavigateBack = {navController.popBackStack()})
        }
    }
}