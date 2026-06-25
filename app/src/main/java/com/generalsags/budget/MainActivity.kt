package com.generalsags.budget

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.generalsags.budget.ui.screens.DataEntryScreen
import com.generalsags.budget.ui.screens.MainScreen
import com.generalsags.budget.ui.screens.HistoryScreen
import com.generalsags.budget.ui.theme.BudgetTrackerTheme
import com.generalsags.budget.ui.viewmodel.ExpenseViewModel
import com.generalsags.budget.utils.LocaleHelper
import com.generalsags.budget.utils.PreferenceManager
import java.util.*

class MainActivity : ComponentActivity() {
    private lateinit var preferenceManager: PreferenceManager
    private var currentLocaleCode by mutableStateOf("en")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferenceManager = PreferenceManager(this)
        currentLocaleCode = preferenceManager.getLanguage()

        setContent {
            // Provide a localized context to the entire Compose hierarchy
            val context = LocalContext.current
            val localizedContext = remember(currentLocaleCode) {
                LocaleHelper.setLocale(context, currentLocaleCode)
            }

            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalActivityResultRegistryOwner provides this
            ) {
                BudgetTrackerTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        BudgetAppNavigation(
                            currentLanguage = currentLocaleCode,
                            onLanguageChange = { newCode ->
                                currentLocaleCode = newCode
                                preferenceManager.saveLanguage(newCode)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BudgetAppNavigation(
    currentLanguage: String,
    onLanguageChange: (String) -> Unit
) {
    val navController = rememberNavController()
    val viewModel: ExpenseViewModel = viewModel()

    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            MainScreen(
                viewModel = viewModel,
                onNavigateToAdd = { id -> 
                    val route = if (id == null) "add" else "add?id=$id"
                    navController.navigate(route)
                },
                onNavigateToHistory = { navController.navigate("history") },
                currentLanguage = currentLanguage,
                onLanguageChange = onLanguageChange
            )
        }
        composable(
            route = "add?id={id}",
            arguments = listOf(navArgument("id") { 
                type = NavType.IntType
                defaultValue = -1 
            })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("id")?.takeIf { it != -1 }
            DataEntryScreen(
                viewModel = viewModel,
                expenseId = id,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("history") {
            HistoryScreen(
                viewModel = viewModel,
                onNavigateToEdit = { id -> navController.navigate("add?id=$id") },
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
