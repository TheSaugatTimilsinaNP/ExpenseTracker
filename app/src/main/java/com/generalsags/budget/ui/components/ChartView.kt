package com.generalsags.budget.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.generalsags.budget.R
import com.generalsags.budget.data.model.Expense

@Composable
fun ExpenseChartView(
    expenses: List<Expense>,
    modifier: Modifier = Modifier
) {
    if (expenses.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = stringResource(R.string.no_expenses))
        }
        return
    }

    val categoryTotals = expenses.groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.amount.toDouble() }.toFloat() }
    
    val total = categoryTotals.values.sum()
    val colors = listOf(
        Color(0xFF2196F3), Color(0xFFF44336), Color(0xFF4CAF50), 
        Color(0xFFFFEB3B), Color(0xFF9C27B0), Color(0xFF00BCD4)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Box(modifier = Modifier.size(250.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                var startAngle = 0f
                categoryTotals.values.forEachIndexed { index, amount ->
                    val sweepAngle = (amount / total) * 360f
                    drawArc(
                        color = colors[index % colors.size],
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        size = Size(size.width, size.height),
                        style = Stroke(width = 40.dp.toPx())
                    )
                    startAngle += sweepAngle
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Total", style = MaterialTheme.typography.labelMedium)
                Text(text = "Rs. $total", style = MaterialTheme.typography.titleLarge)
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Legend
        categoryTotals.keys.forEachIndexed { index, category ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(12.dp).padding(2.dp)) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawRect(color = colors[index % colors.size])
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = category, modifier = Modifier.weight(1f))
                Text(text = "Rs. ${categoryTotals[category]}")
            }
        }
    }
}
