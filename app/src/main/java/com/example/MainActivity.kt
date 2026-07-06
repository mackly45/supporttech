package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.runtime.LaunchedEffect
import android.widget.Toast
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.AppDatabase
import com.example.data.TicketRepository
import com.example.ui.CreateTicketScreen
import com.example.ui.TicketDetailScreen
import com.example.ui.TicketListScreen
import com.example.ui.TicketViewModel
import com.example.ui.TicketViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val database = AppDatabase.getDatabase(context)
                    val repository = TicketRepository(database.ticketDao(), database.ticketMessageDao(), database.kbArticleDao(), database.ticketTemplateDao())
                    val factory = TicketViewModelFactory(repository)
                    val viewModel: TicketViewModel = viewModel(factory = factory)

                    LaunchedEffect(Unit) {
                        viewModel.highPriorityAlerts.collect { alertMessage ->
                            Toast.makeText(context, alertMessage, Toast.LENGTH_LONG).show()
                        }
                    }

                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "ticket_list") {
                        composable("ticket_list") {
                            TicketListScreen(
                                viewModel = viewModel,
                                onNavigateToDetail = { ticketId ->
                                    navController.navigate("ticket_detail/$ticketId")
                                },
                                onNavigateToCreate = {
                                    navController.navigate("create_ticket")
                                },
                                onNavigateToKnowledgeBase = {
                                    navController.navigate("knowledge_base")
                                },
                                onNavigateToDashboard = {
                                    navController.navigate("dashboard")
                                }
                            )
                        }
                        composable("create_ticket") {
                            CreateTicketScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(
                            "ticket_detail/{ticketId}",
                            arguments = listOf(navArgument("ticketId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val ticketId = backStackEntry.arguments?.getInt("ticketId") ?: return@composable
                            TicketDetailScreen(
                                ticketId = ticketId,
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable("knowledge_base") {
                            com.example.ui.KnowledgeBaseScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable("dashboard") {
                            com.example.ui.DashboardScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
