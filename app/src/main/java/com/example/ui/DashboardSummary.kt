package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.Ticket
import com.example.data.TicketPriority
import com.example.data.TicketStatus

@Composable
fun DashboardSummary(tickets: List<Ticket>, modifier: Modifier = Modifier) {
    val totalTickets = tickets.size
    if (totalTickets == 0) return

    val statusCounts = mapOf(
        TicketStatus.OPEN to tickets.count { it.status == TicketStatus.OPEN },
        TicketStatus.IN_PROGRESS to tickets.count { it.status == TicketStatus.IN_PROGRESS },
        TicketStatus.CLOSED to tickets.count { it.status == TicketStatus.CLOSED }
    )

    val priorityCounts = mapOf(
        TicketPriority.LOW to tickets.count { it.priority == TicketPriority.LOW },
        TicketPriority.MEDIUM to tickets.count { it.priority == TicketPriority.MEDIUM },
        TicketPriority.HIGH to tickets.count { it.priority == TicketPriority.HIGH }
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Tableau de Bord",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Status Chart
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Par Statut",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    BarChart(
                        data = listOf(
                            BarData("Ouvert", statusCounts[TicketStatus.OPEN] ?: 0, Color(0xFF2E7D32)),
                            BarData("En cours", statusCounts[TicketStatus.IN_PROGRESS] ?: 0, Color(0xFFEF6C00)),
                            BarData("Résolu", statusCounts[TicketStatus.CLOSED] ?: 0, Color(0xFFC62828))
                        ),
                        maxValue = totalTickets
                    )
                }

                // Priority Chart
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Par Priorité",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    BarChart(
                        data = listOf(
                            BarData("Faible", priorityCounts[TicketPriority.LOW] ?: 0, Color(0xFF1565C0)),
                            BarData("Moyenne", priorityCounts[TicketPriority.MEDIUM] ?: 0, Color(0xFFE65100)),
                            BarData("Haute", priorityCounts[TicketPriority.HIGH] ?: 0, Color(0xFFC62828))
                        ),
                        maxValue = totalTickets
                    )
                }
            }
        }
    }
}

data class BarData(val label: String, val value: Int, val color: Color)

@Composable
fun BarChart(data: List<BarData>, maxValue: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        data.forEach { item ->
            val fraction = if (maxValue > 0) item.value.toFloat() / maxValue else 0f
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.width(56.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraction)
                            .clip(RoundedCornerShape(6.dp))
                            .background(item.color)
                    )
                }
                Text(
                    text = item.value.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(start = 8.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
