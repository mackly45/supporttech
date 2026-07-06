package com.example.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Context
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Ticket
import com.example.data.TicketStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter

import com.example.data.TicketPriority

import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import kotlinx.coroutines.delay

import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.BarChart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketListScreen(
    viewModel: TicketViewModel,
    onNavigateToDetail: (Int) -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToKnowledgeBase: () -> Unit,
    onNavigateToDashboard: () -> Unit
) {
    val tickets by viewModel.tickets.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var selectedPriorityFilter by remember { mutableStateOf<TicketPriority?>(null) }
    var priorityFilterExpanded by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                writeCsvToUri(context, it, tickets)
            }
        }
    }

    val filteredTickets = remember(tickets, searchQuery, selectedPriorityFilter) {
        var filtered = tickets
        if (searchQuery.isNotBlank()) {
            filtered = filtered.filter { ticket ->
                ticket.title.contains(searchQuery, ignoreCase = true) ||
                ticket.id.toString().contains(searchQuery)
            }
        }
        if (selectedPriorityFilter != null) {
            filtered = filtered.filter { it.priority == selectedPriorityFilter }
        }
        filtered
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SupportTech") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = onNavigateToDashboard) {
                        Icon(Icons.Default.BarChart, contentDescription = "Tableau de bord")
                    }
                    IconButton(onClick = onNavigateToKnowledgeBase) {
                        Icon(Icons.Default.Book, contentDescription = "Base de connaissances")
                    }
                    IconButton(onClick = { exportLauncher.launch("tickets.csv") }) {
                        Icon(Icons.Default.Share, contentDescription = "Exporter CSV")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreate,
                modifier = Modifier.testTag("create_ticket_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nouveau Ticket")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            DashboardSummary(tickets = tickets)
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Rechercher") },
                    modifier = Modifier.weight(1f)
                )
                
                ExposedDropdownMenuBox(
                    expanded = priorityFilterExpanded,
                    onExpandedChange = { priorityFilterExpanded = !priorityFilterExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = when (selectedPriorityFilter) {
                            null -> "Toutes"
                            TicketPriority.LOW -> "Faible"
                            TicketPriority.MEDIUM -> "Moyenne"
                            TicketPriority.HIGH -> "Haute"
                        },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Priorité") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = priorityFilterExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = priorityFilterExpanded,
                        onDismissRequest = { priorityFilterExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Toutes") },
                            onClick = {
                                selectedPriorityFilter = null
                                priorityFilterExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Faible") },
                            onClick = {
                                selectedPriorityFilter = TicketPriority.LOW
                                priorityFilterExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Moyenne") },
                            onClick = {
                                selectedPriorityFilter = TicketPriority.MEDIUM
                                priorityFilterExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Haute") },
                            onClick = {
                                selectedPriorityFilter = TicketPriority.HIGH
                                priorityFilterExpanded = false
                            }
                        )
                    }
                }
            }
            
            if (filteredTickets.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (tickets.isEmpty()) "Aucun ticket. Appuyez sur + pour en créer un." else "Aucun ticket trouvé.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    items(filteredTickets, key = { it.id }) { ticket ->
                        TicketItem(
                            ticket = ticket,
                            onClick = { onNavigateToDetail(ticket.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TicketItem(ticket: Ticket, onClick: () -> Unit) {
    val urgencyColor = when (ticket.priority) {
        TicketPriority.HIGH -> Color(0xFFD32F2F) // Rouge pour Haute
        TicketPriority.MEDIUM -> Color(0xFFF57C00) // Orange pour Moyenne
        TicketPriority.LOW -> Color(0xFF1976D2) // Bleu pour Faible
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("ticket_item_${ticket.id}")
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(8.dp)
                    .background(urgencyColor)
            )
            Column(
                modifier = Modifier.padding(16.dp).weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "#${ticket.id}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = ticket.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    PriorityBadge(priority = ticket.priority)
                    Spacer(modifier = Modifier.width(4.dp))
                    StatusBadge(status = ticket.status)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = ticket.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                ticket.assignedAgent?.let { agent ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Assigné à: $agent",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = "Catégorie: ${ticket.category}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    if (ticket.tags.isNotBlank()) {
                        Text(
                            text = "Tags: ${ticket.tags}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.tertiary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false).padding(start = 8.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatDate(ticket.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (ticket.status != TicketStatus.CLOSED) {
                        SlaTimerBadge(ticket = ticket)
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: TicketStatus) {
    val (backgroundColor, textColor, text) = when (status) {
        TicketStatus.OPEN -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), "Ouvert")
        TicketStatus.IN_PROGRESS -> Triple(Color(0xFFFFF3E0), Color(0xFFEF6C00), "En cours")
        TicketStatus.CLOSED -> Triple(Color(0xFFFFEBEE), Color(0xFFC62828), "Fermé")
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun PriorityBadge(priority: TicketPriority) {
    val (backgroundColor, textColor, text) = when (priority) {
        TicketPriority.LOW -> Triple(Color(0xFFE3F2FD), Color(0xFF1565C0), "Faible")
        TicketPriority.MEDIUM -> Triple(Color(0xFFFFF3E0), Color(0xFFE65100), "Moyenne")
        TicketPriority.HIGH -> Triple(Color(0xFFFFEBEE), Color(0xFFC62828), "Haute")
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

fun formatDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

@Composable
fun SlaTimerBadge(ticket: Ticket) {
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(60000)
            currentTime = System.currentTimeMillis()
        }
    }
    
    val slaDuration = when (ticket.priority) {
        TicketPriority.HIGH -> 4L * 60 * 60 * 1000
        TicketPriority.MEDIUM -> 24L * 60 * 60 * 1000
        TicketPriority.LOW -> 48L * 60 * 60 * 1000
    }
    val deadline = ticket.timestamp + slaDuration
    val remainingTime = deadline - currentTime
    val isBreached = remainingTime < 0
    val isApproaching = remainingTime > 0 && remainingTime < (slaDuration * 0.2) // 20%
    
    val hoursLeft = Math.abs(remainingTime) / (1000 * 60 * 60)
    val minsLeft = (Math.abs(remainingTime) / (1000 * 60)) % 60
    
    val timeString = if (isBreached) {
        "SLA dépassé de ${hoursLeft}h ${minsLeft}m"
    } else {
        "SLA : ${hoursLeft}h ${minsLeft}m restants"
    }
    
    val (iconColor, textColor, icon) = when {
        isBreached -> Triple(MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.error, Icons.Default.Warning)
        isApproaching -> Triple(Color(0xFFF57F17), Color(0xFFF57F17), Icons.Default.Timer)
        else -> Triple(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onSurfaceVariant, Icons.Default.Timer)
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = "SLA",
            tint = iconColor,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = timeString,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = if (isBreached || isApproaching) FontWeight.Bold else FontWeight.Normal
        )
    }
}

suspend fun writeCsvToUri(context: Context, uri: Uri, tickets: List<Ticket>) {
    withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write("ID,Titre,Description,Email,Catégorie,Tags,Priorite,Statut,Date\n")
                    tickets.forEach { ticket ->
                        val id = ticket.id
                        val title = ticket.title.replace("\"", "\"\"")
                        val desc = ticket.description.replace("\"", "\"\"")
                        val email = ticket.contactEmail.replace("\"", "\"\"")
                        val category = ticket.category
                        val tags = ticket.tags.replace("\"", "\"\"")
                        val priority = ticket.priority.name
                        val status = ticket.status.name
                        val date = formatDate(ticket.timestamp)
                        writer.write("$id,\"$title\",\"$desc\",\"$email\",\"$category\",\"$tags\",$priority,$status,\"$date\"\n")
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
