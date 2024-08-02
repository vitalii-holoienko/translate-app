package com.example.simpletranslateapp

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ChooseLanguageViewModel : ViewModel() {
    var from = MutableLiveData<String>()
    var previousLanguage = MutableLiveData<String>()
    var savedInputString = MutableLiveData<String>()

}