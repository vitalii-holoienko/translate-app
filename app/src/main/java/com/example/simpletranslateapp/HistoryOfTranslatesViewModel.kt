package com.example.simpletranslateapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class HistoryOfTranslatesViewModel(val database:DataBase) : ViewModel() {
    companion object{
        @Suppress("UNCHECKED_CAST")
        val factory: ViewModelProvider.Factory = object: ViewModelProvider.Factory{
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val database= (checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as App).database
                return HistoryOfTranslatesViewModel(database) as T
            }
        }
    }
    val allHistoryStrings = database.historyStringDao.getAllHistoryStrings()


    //UI
    private var _isSearching = MutableStateFlow(false)
    var isSearching = _isSearching.asStateFlow()

    fun startTypingSearchQuery(){
        _isSearching.value = true
    }

    fun cancelTypingSearchQuery(){
        _isSearching.value = false
    }

    fun getDataOfStringCreation(historyString: HistoryString) : String{
        return Tools.formatTimestampToDateString(historyString.timestamp)
    }
    //filter items by user input query
    private var _searchText = MutableStateFlow("")
    var searchText = _searchText.asStateFlow()

    val filteredItemsFlow: Flow<List<HistoryString>> = _searchText.flatMapLatest { query ->
        allHistoryStrings.map { items ->
            items.filter { it.doesMatchSearchQuery(query) }
        }
    }

    fun changeSearchText(text:String){
        _searchText.value = text
    }
}