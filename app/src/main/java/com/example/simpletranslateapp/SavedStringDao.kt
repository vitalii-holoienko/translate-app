package com.example.simpletranslateapp

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import org.checkerframework.checker.index.qual.SearchIndexBottom

@Dao
interface SavedStringDao {
    @Upsert
    suspend fun upsertString(savedString: SavedString)
    @Delete
    suspend fun deleteString(savedString: SavedString)

    @Query("SELECT * FROM SavedString")
    fun getAllSavedStrings(): Flow<List<SavedString>>

    @Query("SELECT EXISTS (SELECT 1 FROM SavedString WHERE sourceText = :sourceText)")
    fun exists(sourceText: String): Boolean

    @Query("SELECT * FROM SavedString WHERE sourceText = :sourceText LIMIT 1")
    fun getBySourceText(sourceText: String): SavedString?
    //TODO functions to sort stings by date of adding, or in alphabet order
}