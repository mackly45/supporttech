package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.MessageSender
import com.example.data.Ticket
import com.example.data.TicketMessage
import com.example.data.TicketRepository
import com.example.data.TicketStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

import kotlinx.coroutines.flow.firstOrNull

import com.example.data.TicketPriority

class TicketViewModel(private val repository: TicketRepository) : ViewModel() {

    val tickets: StateFlow<List<Ticket>> = repository.allTickets.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _highPriorityAlerts = MutableSharedFlow<String>()
    val highPriorityAlerts: SharedFlow<String> = _highPriorityAlerts.asSharedFlow()

    fun getTicket(id: Int): Flow<Ticket?> {
        return repository.getTicketById(id)
    }

    fun getMessages(ticketId: Int): Flow<List<TicketMessage>> {
        return repository.getMessagesForTicket(ticketId)
    }

    fun createTicket(title: String, description: String, contactEmail: String, priority: TicketPriority, category: String, tags: String, attachmentUri: String?, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.createTicket(title, description, contactEmail, priority, category, tags, attachmentUri)
            onComplete()
            
            if (priority == TicketPriority.HIGH) {
                _highPriorityAlerts.emit("Alerte: Nouveau ticket Haute priorité créé : $title")
            }
            
            // Simulating an auto-reply
            delay(1500)
            val latestTickets = tickets.value
            val newTicket = latestTickets.firstOrNull { it.title == title && it.description == description }
            if (newTicket != null) {
                repository.simulateSupportReply(
                    newTicket.id, 
                    "Bonjour,\n\nMerci de nous avoir contacté. Un agent de support a été assigné à votre ticket et vous répondra sous peu.\n\nCordialement,\nL'équipe SupportTech"
                )
            }
        }
    }

    fun addMessage(ticketId: Int, text: String) {
        viewModelScope.launch {
            repository.addMessage(ticketId, text, MessageSender.USER)
            // Simulate agent reply
            delay(2000)
            repository.simulateSupportReply(
                ticketId,
                "Merci pour ces informations supplémentaires. Nous sommes en train d'analyser le problème."
            )
        }
    }

    fun closeTicket(ticketId: Int, onEmailSent: (String) -> Unit = {}) {
        viewModelScope.launch {
            repository.updateStatus(ticketId, TicketStatus.CLOSED)
            repository.addMessage(ticketId, "Le ticket a été clôturé.", MessageSender.SUPPORT)
            
            // Simulation d'envoi d'email
            val ticket = repository.getTicketById(ticketId).firstOrNull()
            if (ticket != null && ticket.contactEmail.isNotBlank()) {
                delay(1500) // Simuler un délai réseau
                repository.addMessage(
                    ticketId,
                    "Simulation : Un email de confirmation de résolution a été envoyé à ${ticket.contactEmail}",
                    MessageSender.SUPPORT
                )
                onEmailSent(ticket.contactEmail)
            }
        }
    }

    fun updateInternalNotes(ticketId: Int, notes: String) {
        viewModelScope.launch {
            repository.updateInternalNotes(ticketId, notes)
        }
    }

    val kbArticles: StateFlow<List<com.example.data.KbArticle>> = repository.allKbArticles.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun searchKbArticles(query: String): Flow<List<com.example.data.KbArticle>> {
        return repository.searchKbArticles(query)
    }

    fun createKbArticle(title: String, content: String, tags: String) {
        viewModelScope.launch {
            repository.createKbArticle(title, content, tags)
        }
    }

    val ticketTemplates: StateFlow<List<com.example.data.TicketTemplate>> = repository.allTemplates.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun createTemplate(title: String, description: String, category: String, priority: TicketPriority) {
        viewModelScope.launch {
            repository.createTemplate(title, description, category, priority)
        }
    }
}

class TicketViewModelFactory(private val repository: TicketRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TicketViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TicketViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
