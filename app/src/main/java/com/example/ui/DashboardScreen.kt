package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TicketPriority

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: TicketViewModel,
    onNavigateBack: () -> Unit
) {
    val tickets by viewModel.tickets.collectAsState()

    val lowCount = tickets.count { it.priority == TicketPriority.LOW }
    val mediumCount = tickets.count { it.priority == TicketPriority.MEDIUM }
    val highCount = tickets.count { it.priority == TicketPriority.HIGH }

    val onBackgroundColor = MaterialTheme.colorScheme.onBackground.toArgb()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tableau de bord") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                "Répartition par priorité",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            val maxCount = maxOf(lowCount, mediumCount, highCount, 1)

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val barWidth = canvasWidth / 5f
                val spacing = canvasWidth / 5f

                val lowColor = Color(0xFF4CAF50)
                val mediumColor = Color(0xFFFF9800)
                val highColor = Color(0xFFF44336)

                val bottomOffset = 40.dp.toPx()
                val graphHeight = canvasHeight - bottomOffset

                val drawBar = { index: Int, count: Int, color: Color, label: String ->
                    val barHeight = (count.toFloat() / maxCount) * graphHeight
                    val startX = (spacing / 2) + index * (barWidth + spacing)
                    
                    drawRoundRect(
                        color = color,
                        topLeft = Offset(startX, graphHeight - barHeight),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )

                    drawIntoCanvas { canvas ->
                        val textPaint = android.graphics.Paint().apply {
                            this.color = onBackgroundColor
                            textSize = 14.sp.toPx()
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                        
                        // Draw label at the bottom
                        canvas.nativeCanvas.drawText(
                            label,
                            startX + barWidth / 2,
                            canvasHeight - 10.dp.toPx(),
                            textPaint
                        )
                        
                        // Draw value on top of the bar
                        canvas.nativeCanvas.drawText(
                            count.toString(),
                            startX + barWidth / 2,
                            graphHeight - barHeight - 10.dp.toPx(),
                            textPaint
                        )
                    }
                }

                drawBar(0, lowCount, lowColor, "Faible")
                drawBar(1, mediumCount, mediumColor, "Moyenne")
                drawBar(2, highCount, highColor, "Haute")
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                "Total des tickets : ${tickets.size}",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
