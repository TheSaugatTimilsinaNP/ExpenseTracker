package com.generalsags.budget.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.generalsags.budget.R
import com.generalsags.budget.ui.components.ExpenseListView
import com.generalsags.budget.ui.viewmodel.ExpenseViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: ExpenseViewModel,
    onNavigateToAdd: (Int?) -> Unit,
    onNavigateToHistory: () -> Unit,
    currentLanguage: String,
    onLanguageChange: (String) -> Unit
) {
    val expenses by viewModel.currentMonthExpenses.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    // Language Toggle Button
                    TextButton(onClick = {
                        val nextLang = if (currentLanguage == "en") "ne" else "en"
                        onLanguageChange(nextLang)
                    }) {
                        Text(
                            text = if (currentLanguage == "en") "NE" else "EN",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = onNavigateToHistory) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.List,
                            contentDescription = stringResource(R.string.list_view)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToAdd(null) }) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${stringResource(R.string.month)}: ${LocalDate.now().month}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            ExpenseListView(
                expenses = expenses,
                onEditExpense = { onNavigateToAdd(it.id) },
                onDeleteExpense = { viewModel.delete(it) }
            )
        }
    }
}
