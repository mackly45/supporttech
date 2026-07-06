package com.example.ui

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.data.TicketPriority
import java.io.File

fun createImageUri(context: Context): Uri {
    val file = File(context.filesDir, "images").apply { mkdirs() }
    val image = File(file, "ticket_img_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", image)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTicketScreen(
    viewModel: TicketViewModel,
    onNavigateBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var contactEmail by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(TicketPriority.MEDIUM) }
    var category by remember { mutableStateOf("Général") }
    val categories = listOf("Hardware", "Software", "Network", "Billing", "Général")
    var tagsInput by remember { mutableStateOf("") }
    var priorityExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var templateExpanded by remember { mutableStateOf(false) }
    var showSaveTemplateDialog by remember { mutableStateOf(false) }
    
    val templates by viewModel.ticketTemplates.collectAsState()
    
    var attachmentUri by remember { mutableStateOf<Uri?>(null) }
    var currentPhotoUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            attachmentUri = currentPhotoUri
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nouveau Ticket") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    TextButton(onClick = { showSaveTemplateDialog = true }) {
                        Text("Sauver comme Modèle")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (templates.isNotEmpty()) {
                ExposedDropdownMenuBox(
                    expanded = templateExpanded,
                    onExpandedChange = { templateExpanded = !templateExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = "Sélectionner un modèle",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Modèle") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = templateExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = templateExpanded,
                        onDismissRequest = { templateExpanded = false }
                    ) {
                        templates.forEach { template ->
                            DropdownMenuItem(
                                text = { Text(template.title) },
                                onClick = {
                                    title = template.title
                                    description = template.description
                                    category = template.category
                                    priority = template.priority
                                    templateExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Sujet") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ticket_title_input")
            )

            OutlinedTextField(
                value = contactEmail,
                onValueChange = { contactEmail = it },
                label = { Text("Email de contact") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ticket_email_input")
            )

            ExposedDropdownMenuBox(
                expanded = priorityExpanded,
                onExpandedChange = { priorityExpanded = !priorityExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = when (priority) {
                        TicketPriority.LOW -> "Faible"
                        TicketPriority.MEDIUM -> "Moyenne"
                        TicketPriority.HIGH -> "Haute"
                    },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Priorité") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = priorityExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = priorityExpanded,
                    onDismissRequest = { priorityExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Faible") },
                        onClick = {
                            priority = TicketPriority.LOW
                            priorityExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Moyenne") },
                        onClick = {
                            priority = TicketPriority.MEDIUM
                            priorityExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Haute") },
                        onClick = {
                            priority = TicketPriority.HIGH
                            priorityExpanded = false
                        }
                    )
                }
            }

            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Catégorie") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    categories.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                category = selectionOption
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description du problème") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("ticket_desc_input"),
                maxLines = 10
            )

            OutlinedTextField(
                value = tagsInput,
                onValueChange = { tagsInput = it },
                label = { Text("Étiquettes (séparées par des virgules)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ticket_tags_input")
            )

            if (attachmentUri != null) {
                AsyncImage(
                    model = attachmentUri,
                    contentDescription = "Image attachée",
                    modifier = Modifier.fillMaxWidth().height(150.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = {
                        val newUri = createImageUri(context)
                        currentPhotoUri = newUri
                        cameraLauncher.launch(newUri)
                    }
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Prendre une photo")
                    Spacer(Modifier.width(8.dp))
                    Text(if (attachmentUri == null) "Ajouter une photo" else "Changer de photo")
                }
            }

            Button(
                onClick = {
                    if (title.isNotBlank() && description.isNotBlank() && contactEmail.isNotBlank()) {
                        viewModel.createTicket(title, description, contactEmail, priority, category, tagsInput, attachmentUri?.toString()) {
                            onNavigateBack()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("submit_ticket_button"),
                enabled = title.isNotBlank() && description.isNotBlank() && contactEmail.isNotBlank()
            ) {
                Text("Créer le ticket")
            }
        }
        
        if (showSaveTemplateDialog) {
            var templateTitle by remember { mutableStateOf(title) }
            AlertDialog(
                onDismissRequest = { showSaveTemplateDialog = false },
                title = { Text("Sauver comme Modèle") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = templateTitle,
                            onValueChange = { templateTitle = it },
                            label = { Text("Nom du modèle") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text("Sujet: $title")
                        Text("Catégorie: $category")
                        Text("Priorité: ${priority.name}")
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.createTemplate(templateTitle, description, category, priority)
                            showSaveTemplateDialog = false
                        },
                        enabled = templateTitle.isNotBlank() && description.isNotBlank()
                    ) {
                        Text("Sauver")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSaveTemplateDialog = false }) {
                        Text("Annuler")
                    }
                }
            )
        }
    }
}

