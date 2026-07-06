package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TicketDao {
    @Query("SELECT * FROM tickets ORDER BY timestamp DESC")
    fun getAllTickets(): Flow<List<Ticket>>

    @Query("SELECT * FROM tickets WHERE id = :id")
    fun getTicketById(id: Int): Flow<Ticket?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: Ticket): Long

    @Query("UPDATE tickets SET status = :status WHERE id = :ticketId")
    suspend fun updateTicketStatus(ticketId: Int, status: TicketStatus)
    
    @Query("UPDATE tickets SET internalNotes = :notes WHERE id = :ticketId")
    suspend fun updateTicketInternalNotes(ticketId: Int, notes: String)
    
    @Query("DELETE FROM tickets WHERE id = :id")
    suspend fun deleteTicketById(id: Int)
}

@Dao
interface TicketMessageDao {
    @Query("SELECT * FROM ticket_messages WHERE ticketId = :ticketId ORDER BY timestamp ASC")
    fun getMessagesForTicket(ticketId: Int): Flow<List<TicketMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: TicketMessage)
}

@Dao
interface KbArticleDao {
    @Query("SELECT * FROM knowledge_base ORDER BY timestamp DESC")
    fun getAllArticles(): Flow<List<KbArticle>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticle(article: KbArticle)

    @Query("SELECT * FROM knowledge_base WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchArticles(query: String): Flow<List<KbArticle>>
}

@Dao
interface TicketTemplateDao {
    @Query("SELECT * FROM ticket_templates ORDER BY title ASC")
    fun getAllTemplates(): Flow<List<TicketTemplate>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: TicketTemplate)
}
