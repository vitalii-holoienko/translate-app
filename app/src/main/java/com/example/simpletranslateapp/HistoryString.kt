package com.example.simpletranslateapp

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "HistoryString")
data class HistoryString(
    val sourceText:String,
    val translatedText:String,
    @PrimaryKey(autoGenerate = true)
    val id:Int? = null,
){
    fun doesMatchSearchQuery(query:String) : Boolean{
        val matchingCombinations = listOf(
            "$sourceText",
            "$translatedText",
        )
        return matchingCombinations.any {
            it.contains(query, ignoreCase = true)
        }
    }
}