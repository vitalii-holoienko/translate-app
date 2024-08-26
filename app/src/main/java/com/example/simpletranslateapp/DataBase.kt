package com.example.simpletranslateapp

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
@Database(
    entities = [SavedString::class, HistoryString::class],
    version = 1
)
abstract class DataBase : RoomDatabase(){
    abstract val savedStringDao : SavedStringDao
    abstract val historyStringDao : HistoryStringDao
    companion object{
        fun createDataBase(context: Context):DataBase{
            return Room.databaseBuilder(
                context,
                DataBase::class.java,
                "database.db"
            ).build()
        }
    }
}