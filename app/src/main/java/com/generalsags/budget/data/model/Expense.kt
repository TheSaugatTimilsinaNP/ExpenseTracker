package com.generalsags.budget.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val date: LocalDate,
    val time: LocalTime,
    val name: String,
    val category: String,
    val amount: Float,
    val remarks: String? = null
)
