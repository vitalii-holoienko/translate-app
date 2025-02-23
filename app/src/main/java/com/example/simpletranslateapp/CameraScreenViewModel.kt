package com.example.simpletranslateapp

import android.content.Context
import android.graphics.Rect
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException

class CameraScreenViewModel : ViewModel() {
    data class RecognizedTextBlock(
        var text: String,
        val boundingBox: Rect?

    )

    val translateText = TranslateText()
    val translatedText =                 MutableLiveData<String>()
    var sourceLanguage =                 MutableLiveData<String>()
    var targetLanguage =                 MutableLiveData<String>()

    init {
        sourceLanguage.value = "Detect Automatically"
        targetLanguage.value = "English"

        changeSourceLanguage(sourceLanguage.value!!)
        changeTargetLanguage(targetLanguage.value!!)
    }


    fun changeSourceLanguage(language:String){
        TranslateText.setSourceLanguage(language)
        sourceLanguage.value = language
    }

    fun changeTargetLanguage(language:String){
        TranslateText.setTargetLanguage(language)
        targetLanguage.value = language
    }











}