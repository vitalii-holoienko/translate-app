package com.example.simpletranslateapp

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChooseLanguageViewModel : ViewModel() {
    var from = MutableLiveData<String>()
    var savedInputString = MutableLiveData<String>()
    var camera = MutableLiveData<Boolean>()
    //Logic for text searching
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

    fun changeSearchText(text:String){
        _searchText.value = text
    }

}