package com.example.simpletranslateapp

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
@Database(
    entities = [SavedString::class],
    version = 1
)
abstract class SavedStringDataBase : RoomDatabase(){
    abstract val dao : SavedStringDao
    companion object{
        fun createDataBase(context: Context):SavedStringDataBase{
            return Room.databaseBuilder(
                context,
                SavedStringDataBase::class.java,
                "database.db"
            ).build()
        }
    }
}