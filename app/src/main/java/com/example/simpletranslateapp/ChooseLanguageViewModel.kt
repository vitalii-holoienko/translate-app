package com.example.simpletranslateapp

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ChooseLanguageViewModel : ViewModel() {
    var fromLanguage = MutableLiveData<String>()
    var toLanguage = MutableLiveData<String>()
    var languages = buildMap<String, String> {

    }
}