package com.example.simpletranslateapp

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import org.checkerframework.checker.index.qual.SearchIndexBottom

@Dao
interface HistoryStringDao {
    @Upsert
    suspend fun upsertString(historyString: HistoryString)
    @Delete
    suspend fun deleteString(historyString: HistoryString)

    @Query("DELETE FROM HistoryString")
    suspend fun clearHistory()

    @Query("SELECT * FROM HistoryString")
    fun getAllHistoryStrings(): Flow<List<HistoryString>>

    @Query("DELETE FROM HistoryString WHERE id = (SELECT id FROM HistoryString ORDER BY timestamp ASC LIMIT 1)")
    suspend fun deleteOldestString()

    @Query("SELECT COUNT(*) FROM HistoryString")
    suspend fun getItemCount(): Int

    @Query("SELECT EXISTS (SELECT 1 FROM HistoryString WHERE sourceText = :sourceText)")
    fun exists(sourceText: String): Boolean

    @Query("SELECT * FROM HistoryString WHERE sourceText = :sourceText LIMIT 1")
    fun getBySourceText(sourceText: String): HistoryString?
    //TODO functions to sort stings by date of adding, or in alphabet order
}