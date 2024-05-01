package com.example.simpletranslateapp

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

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

    //UI
    private var _isSearching = MutableStateFlow(false)
    var isSearching = _isSearching.asStateFlow()

    fun startTypingSearchQuery(){
        _isSearching.value = true
    }

    fun cancelTypingSearchQuery(){
        _isSearching.value = false
    }

    //filter items by user input query
    private var _searchText = MutableStateFlow("")
    var searchText = _searchText.asStateFlow()

    val filteredItemsFlow: Flow<List<SavedString>> = _searchText.flatMapLatest { query ->
        allSavedStrings.map { items ->
            items.filter { it.doesMatchSearchQuery(query) }
        }
    }

    fun changeSearchText(text:String){
        _searchText.value = text
    }





}