package com.example.data

import kotlinx.coroutines.flow.Flow

class TicketRepository(
    private val ticketDao: TicketDao,
    private val messageDao: TicketMessageDao,
    private val kbArticleDao: KbArticleDao,
    private val ticketTemplateDao: TicketTemplateDao
) {
    val allTickets: Flow<List<Ticket>> = ticketDao.getAllTickets()
    val allKbArticles: Flow<List<KbArticle>> = kbArticleDao.getAllArticles()
    val allTemplates: Flow<List<TicketTemplate>> = ticketTemplateDao.getAllTemplates()

    fun searchKbArticles(query: String): Flow<List<KbArticle>> = kbArticleDao.searchArticles(query)

    suspend fun createKbArticle(title: String, content: String, tags: String) {
        kbArticleDao.insertArticle(KbArticle(title = title, content = content, tags = tags))
    }

    suspend fun createTemplate(title: String, description: String, category: String, priority: TicketPriority) {
        ticketTemplateDao.insertTemplate(TicketTemplate(title = title, description = description, category = category, priority = priority))
    }

    fun getTicketById(id: Int): Flow<Ticket?> = ticketDao.getTicketById(id)
    
    fun getMessagesForTicket(ticketId: Int): Flow<List<TicketMessage>> = messageDao.getMessagesForTicket(ticketId)

    suspend fun createTicket(title: String, description: String, contactEmail: String, priority: TicketPriority, category: String, tags: String, attachmentUri: String?) {
        val assignedAgent = when (priority) {
            TicketPriority.HIGH -> "Alice (Niveau 2)"
            TicketPriority.MEDIUM -> "Bob (Niveau 1)"
            TicketPriority.LOW -> "Charlie (Support Général)"
        }
        
        val ticketId = ticketDao.insertTicket(Ticket(title = title, description = description, contactEmail = contactEmail, priority = priority, category = category, tags = tags, attachmentUri = attachmentUri, assignedAgent = assignedAgent))
        
        messageDao.insertMessage(TicketMessage(
            ticketId = ticketId.toInt(),
            sender = MessageSender.SYSTEM,
            text = "Ticket créé avec le statut OPEN et la priorité ${priority.name}."
        ))
        
        messageDao.insertMessage(TicketMessage(ticketId = ticketId.toInt(), sender = MessageSender.USER, text = description))
        
        messageDao.insertMessage(TicketMessage(
            ticketId = ticketId.toInt(),
            sender = MessageSender.SUPPORT,
            text = "Ce ticket a été assigné automatiquement à $assignedAgent en fonction de sa priorité."
        ))
    }

    suspend fun addMessage(ticketId: Int, text: String, sender: MessageSender) {
        messageDao.insertMessage(TicketMessage(ticketId = ticketId, text = text, sender = sender))
        messageDao.insertMessage(TicketMessage(
            ticketId = ticketId,
            sender = MessageSender.SYSTEM,
            text = "Commentaire ajouté par ${if (sender == MessageSender.USER) "l'utilisateur" else "le support"}."
        ))
        if (sender == MessageSender.USER) {
            updateStatus(ticketId, TicketStatus.OPEN)
        }
    }
    
    suspend fun updateStatus(ticketId: Int, status: TicketStatus) {
        ticketDao.updateTicketStatus(ticketId, status)
        messageDao.insertMessage(TicketMessage(
            ticketId = ticketId,
            sender = MessageSender.SYSTEM,
            text = "Le statut du ticket est passé à : ${status.name}"
        ))
    }
    
    suspend fun updateInternalNotes(ticketId: Int, notes: String) {
        ticketDao.updateTicketInternalNotes(ticketId, notes)
    }
    
    suspend fun simulateSupportReply(ticketId: Int, text: String) {
        addMessage(ticketId, text, MessageSender.SUPPORT)
        ticketDao.updateTicketStatus(ticketId, TicketStatus.IN_PROGRESS)
    }
}
