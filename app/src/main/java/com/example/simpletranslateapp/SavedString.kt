package com.example.simpletranslateapp

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "SavedString")
data class SavedString(
    val sourceText:String,
    val translatedText:String,
    val sourceLanguage:String,
    val targetLanguage:String,
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
