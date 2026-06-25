package com.generalsags.budget.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.generalsags.budget.data.local.AppDatabase
import com.generalsags.budget.data.model.Expense
import com.generalsags.budget.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ExpenseRepository
    private val _allExpenses = MutableStateFlow<List<Expense>>(emptyList())
    
    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory

    private val _selectedMonth = MutableStateFlow<LocalDate?>(null)
    val selectedMonth: StateFlow<LocalDate?> = _selectedMonth

    private val _dateRange = MutableStateFlow<Pair<LocalDate?, LocalDate?>>(null to null)
    val dateRange: StateFlow<Pair<LocalDate?, LocalDate?>> = _dateRange

    init {
        val expenseDao = AppDatabase.getDatabase(application).expenseDao()
        repository = ExpenseRepository(expenseDao)
        
        viewModelScope.launch {
            repository.allExpenses
                .catch { e -> 
                    // Log error or handle gracefully
                    _allExpenses.value = emptyList()
                }
                .collect {
                    _allExpenses.value = it
                }
        }
    }

    val filteredExpenses: StateFlow<List<Expense>> = combine(
        _allExpenses,
        _selectedCategory,
        _dateRange
    ) { expenses, category, range ->
        expenses.filter { expense ->
            val matchesCategory = if (category == "All") true else expense.category == category
            val matchesRange = if (range.first == null || range.second == null) true else {
                !expense.date.isBefore(range.first) && !expense.date.isAfter(range.second)
            }
            matchesCategory && matchesRange
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentMonthExpenses: StateFlow<List<Expense>> = _allExpenses.map { expenses ->
        val now = LocalDate.now()
        expenses.filter { 
            it.date.month == now.month && it.date.year == now.year
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun insert(expense: Expense) = viewModelScope.launch {
        repository.insert(expense)
    }

    fun delete(expense: Expense) = viewModelScope.launch {
        repository.delete(expense)
    }

    fun update(expense: Expense) = viewModelScope.launch {
        repository.update(expense)
    }

    fun updateCategoryFilter(category: String) {
        _selectedCategory.value = category
    }

    fun updateMonthFilter(date: LocalDate?) {
        // We can keep this for backward compatibility or repurpose it to set a range for the whole month
        if (date == null) {
            _dateRange.value = null to null
        } else {
            val start = date.withDayOfMonth(1)
            val end = date.withDayOfMonth(date.lengthOfMonth())
            _dateRange.value = start to end
        }
        _selectedMonth.value = date
    }

    fun updateDateRange(start: LocalDate?, end: LocalDate?) {
        _dateRange.value = start to end
        _selectedMonth.value = null // Clear specific month if manual range is set
    }
}
