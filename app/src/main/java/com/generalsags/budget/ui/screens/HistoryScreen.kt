package com.generalsags.budget.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.generalsags.budget.R
import com.generalsags.budget.ui.components.ExpenseChartView
import com.generalsags.budget.ui.components.ExpenseListView
import com.generalsags.budget.ui.viewmodel.ExpenseViewModel
import com.generalsags.budget.utils.ExportUtils
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: ExpenseViewModel,
    onNavigateToEdit: (Int) -> Unit,
    onNavigateBack: () -> Unit
) {
    var isChartView by remember { mutableStateOf(false) }
    val expenses by viewModel.filteredExpenses.collectAsStateWithLifecycle()
    val dateRange by viewModel.dateRange.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val csvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv"),
        onResult = { uri ->
            uri?.let {
                ExportUtils.exportToCsv(context, it, expenses)
                Toast.makeText(context, context.getString(R.string.csv_exported), Toast.LENGTH_SHORT).show()
            }
        }
    )

    val jsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
        onResult = { uri ->
            uri?.let {
                ExportUtils.exportToJson(context, it, expenses)
                Toast.makeText(context, context.getString(R.string.json_exported), Toast.LENGTH_SHORT).show()
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.list_view)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { isChartView = !isChartView }) {
                        Icon(
                            imageVector = if (isChartView) Icons.Default.List else Icons.Default.PieChart,
                            contentDescription = null
                        )
                    }
                    var showExportMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { showExportMenu = true }) {
                        Icon(Icons.Default.Download, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = showExportMenu,
                        onDismissRequest = { showExportMenu = false }
                    ) {
                    DropdownMenuItem(
                        text = { Text("Export to CSV") },
                        onClick = {
                            val timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd - HH-mm-ss"))
                            csvLauncher.launch("Budget Tracker - $timestamp.csv")
                            showExportMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Export to JSON") },
                        onClick = {
                            val timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd - HH-mm-ss"))
                            jsonLauncher.launch("Budget Tracker - $timestamp.json")
                            showExportMenu = false
                        }
                    )
                    }
                }
            )
        },
        bottomBar = {
            val total = expenses.sumOf { it.amount.toDouble() }
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Expense",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Rs. ${String.format("%.2f", total)}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            HistoryFilterBar(viewModel)
            
            if (isChartView) {
                ExpenseChartView(expenses = expenses)
            } else {
                ExpenseListView(
                    expenses = expenses,
                    onEditExpense = { onNavigateToEdit(it.id) },
                    onDeleteExpense = { viewModel.delete(it) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryFilterBar(viewModel: ExpenseViewModel) {
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    // Need to handle localization of the "All" string in VM or convert here
    val displayCategory = if (selectedCategory == "All") stringResource(R.string.all) else selectedCategory
    val dateRange by viewModel.dateRange.collectAsStateWithLifecycle()
    val categories = listOf(
        stringResource(R.string.all),
        stringResource(R.string.category_food),
        stringResource(R.string.category_transport),
        stringResource(R.string.category_entertainment),
        stringResource(R.string.category_rent),
        stringResource(R.string.category_education),
        stringResource(R.string.category_health),
        stringResource(R.string.category_clothes),
        stringResource(R.string.category_utilities),
        stringResource(R.string.category_miscellaneous)
    )
    
    var showDateRangePicker by remember { mutableStateOf(false) }
    val dateRangePickerState = rememberDateRangePickerState()
    
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        // Category Dropdown
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = displayCategory,
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.category)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                categories.forEach { category ->
                    val allText = stringResource(R.string.all)
                    DropdownMenuItem(
                        text = { Text(category) },
                        onClick = {
                            val vmValue = if (category == allText) "All" else category
                            viewModel.updateCategoryFilter(vmValue)
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Date Range Picker Card
        OutlinedCard(
            onClick = { showDateRangePicker = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                
                val dateText = if (dateRange.first != null && dateRange.second != null) {
                    "${dateRange.first?.format(DateTimeFormatter.ofPattern("MMM dd"))} - ${dateRange.second?.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))}"
                } else {
                    "All Time"
                }
                
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (dateRange.first != null) {
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = { viewModel.updateDateRange(null, null) },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Clear")
                    }
                }
            }
        }
    }

    if (showDateRangePicker) {
        DatePickerDialog(
            onDismissRequest = { showDateRangePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val start = dateRangePickerState.selectedStartDateMillis?.let {
                        Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    val end = dateRangePickerState.selectedEndDateMillis?.let {
                        Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    if (start != null && end != null) {
                        viewModel.updateDateRange(start, end)
                    }
                    showDateRangePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDateRangePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DateRangePicker(
                state = dateRangePickerState,
                modifier = Modifier.weight(1f),
                title = { Text(stringResource(R.string.select_date), modifier = Modifier.padding(16.dp)) },
                headline = { 
                    val start = dateRangePickerState.selectedStartDateMillis
                    val end = dateRangePickerState.selectedEndDateMillis
                    Text(
                        text = if (start != null && end != null) "Selected Range" else "Select Range",
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                },
                showModeToggle = false
            )
        }
    }
}
