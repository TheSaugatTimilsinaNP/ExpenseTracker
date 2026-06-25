package com.generalsags.budget.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.generalsags.budget.R
import com.generalsags.budget.data.model.Expense
import com.generalsags.budget.ui.viewmodel.ExpenseViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataEntryScreen(
    viewModel: ExpenseViewModel,
    expenseId: Int? = null,
    onNavigateBack: () -> Unit
) {
    val expenses by viewModel.filteredExpenses.collectAsStateWithLifecycle()
    val existingExpense = remember(expenseId) {
        expenses.find { it.id == expenseId }
    }

    val categories = listOf(
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

    var name by remember(existingExpense) { mutableStateOf(existingExpense?.name ?: "") }
    var amount by remember(existingExpense) { mutableStateOf(existingExpense?.amount?.toString() ?: "") }
    var category by remember(existingExpense, categories) { 
        mutableStateOf(existingExpense?.category ?: categories[0]) 
    }
    var date by remember(existingExpense) { mutableStateOf(existingExpense?.date ?: LocalDate.now()) }
    var time by remember(existingExpense) { mutableStateOf(existingExpense?.time ?: LocalTime.now()) }
    var remarks by remember(existingExpense) { mutableStateOf(existingExpense?.remarks ?: "") }

    val focusManager = LocalFocusManager.current

    var expanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )
    
    val timePickerState = rememberTimePickerState(
        initialHour = time.hour,
        initialMinute = time.minute
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        date = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    time = LocalTime.of(timePickerState.hour, timePickerState.minute)
                    showTimePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text(stringResource(R.string.add_expense)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = date.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.date)) },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        Icon(Icons.Default.DateRange, contentDescription = null)
                    }
                )
                // Invisible button to open date picker
                IconButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.matchParentSize()
                ) {}
            }

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = time.format(DateTimeFormatter.ofPattern("HH:mm")),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.time)) },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        Icon(Icons.Default.History, contentDescription = null)
                    }
                )
                // Invisible button to open time picker
                IconButton(
                    onClick = { showTimePicker = true },
                    modifier = Modifier.matchParentSize()
                ) {}
            }

            Box(modifier = Modifier.fillMaxWidth()) {
                @OptIn(ExperimentalMaterial3Api::class)
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.category)) },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    }
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat) },
                            onClick = {
                                category = cat
                                expanded = false
                            }
                        )
                    }
                }
                // Invisible button to open dropdown
                IconButton(
                    onClick = { expanded = true },
                    modifier = Modifier.matchParentSize()
                ) {}
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.expense_name)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text(stringResource(R.string.amount)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )

            OutlinedTextField(
                value = remarks,
                onValueChange = { remarks = it },
                label = { Text(stringResource(R.string.remarks)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                )
            )

            Button(
                onClick = {
                    if (name.isNotEmpty() && amount.isNotEmpty()) {
                        val expense = Expense(
                            id = expenseId ?: 0,
                            date = date,
                            time = time,
                            name = name,
                            category = category,
                            amount = amount.toFloatOrNull() ?: 0f,
                            remarks = remarks
                        )
                        if (expenseId == null) {
                            viewModel.insert(expense)
                        } else {
                            viewModel.update(expense)
                        }
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }
}
