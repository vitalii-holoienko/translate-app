package com.example.simpletranslateapp

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "SavedString")
data class SavedString(
    val sourceText:String,
    val translatedText:String,
    @PrimaryKey(autoGenerate = true)
    val id:Int? = null,
)
