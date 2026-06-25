package com.generalsags.budget.utils

import android.content.Context
import android.net.Uri
import com.generalsags.budget.data.model.Expense
import com.google.gson.GsonBuilder
import com.opencsv.CSVWriter
import java.io.OutputStreamWriter
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object ExportUtils {
    fun exportToCsv(context: Context, uri: Uri, expenses: List<Expense>) {
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            OutputStreamWriter(outputStream).use { writer ->
                val csvWriter = CSVWriter(writer)
                val header = arrayOf("ID", "Date", "Time", "Name", "Category", "Amount", "Remarks")
                csvWriter.writeNext(header)

                expenses.forEach { expense ->
                    val row = arrayOf(
                        expense.id.toString(),
                        expense.date.toString(),
                        expense.time.toString(),
                        expense.name,
                        expense.category,
                        expense.amount.toString(),
                        expense.remarks ?: ""
                    )
                    csvWriter.writeNext(row)
                }
                csvWriter.close()
            }
        }
    }

    fun exportToJson(context: Context, uri: Uri, expenses: List<Expense>) {
        val gson = GsonBuilder()
            .registerTypeAdapter(LocalDate::class.java, com.google.gson.JsonSerializer<LocalDate> { src, _, _ ->
                com.google.gson.JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE))
            })
            .registerTypeAdapter(LocalTime::class.java, com.google.gson.JsonSerializer<LocalTime> { src, _, _ ->
                com.google.gson.JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_TIME))
            })
            .setPrettyPrinting()
            .create()

        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            OutputStreamWriter(outputStream).use { writer ->
                gson.toJson(expenses, writer)
            }
        }
    }
}
