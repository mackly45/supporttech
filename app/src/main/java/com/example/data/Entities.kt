package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(tableName = "tickets")
data class Ticket(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val contactEmail: String = "",
    val priority: TicketPriority = TicketPriority.MEDIUM,
    val status: TicketStatus = TicketStatus.OPEN,
    val internalNotes: String = "",
    val attachmentUri: String? = null,
    val assignedAgent: String? = null,
    val category: String = "Général",
    val tags: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

enum class TicketStatus {
    OPEN, IN_PROGRESS, CLOSED
}

enum class TicketPriority {
    LOW, MEDIUM, HIGH
}

@Entity(
    tableName = "ticket_messages",
    foreignKeys = [
        ForeignKey(
            entity = Ticket::class,
            parentColumns = ["id"],
            childColumns = ["ticketId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("ticketId")]
)
data class TicketMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val ticketId: Int,
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class MessageSender {
    USER, SUPPORT, SYSTEM
}

@Entity(tableName = "knowledge_base")
data class KbArticle(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val tags: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "ticket_templates")
data class TicketTemplate(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val category: String,
    val priority: TicketPriority
)
