package com.example.simpletranslateapp

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.launch

class FavouritePageViewModel(val database:SavedStringDataBase) : ViewModel(){

    //ViewModel custom constructor, that allowing to use database
    companion object{
        @Suppress("UNCHECKED_CAST")
        val factory: ViewModelProvider.Factory = object: ViewModelProvider.Factory{
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val database= (checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as App).database
                return FavouritePageViewModel(database) as T
            }
        }
    }

    //Database
    val allSavedStrings = database.dao.getAllSavedStrings()

    fun deleteSavedString(savedString:SavedString) = viewModelScope.launch{
        database.dao.deleteString(savedString)
    }




}