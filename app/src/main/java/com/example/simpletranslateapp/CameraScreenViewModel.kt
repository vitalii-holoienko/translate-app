package com.example.simpletranslateapp

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Rect
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException

class CameraScreenViewModel(val database:DataBase) : ViewModel() {
    data class RecognizedTextBlock(
        var text: String,
        val boundingBox: Rect?

    )

    companion object{
        @Suppress("UNCHECKED_CAST")
        val factory: ViewModelProvider.Factory = object: ViewModelProvider.Factory{
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val database= (checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as App).database
                return CameraScreenViewModel(database) as T
            }
        }
    }
    var sourceLanguage =                 MutableLiveData<String>()
    var targetLanguage =                 MutableLiveData<String>()


    init {
        Log.d("TEKKEN", "INIT")
        sourceLanguage.value = "English"
        targetLanguage.value = "Russian"

        changeSourceLanguage(sourceLanguage.value!!)
        changeTargetLanguage(targetLanguage.value!!)
    }

    fun changeSourceLanguage(language:String){
        TranslateText.setSourceLanguage(language)
        sourceLanguage.value = language
    }

    fun changeTargetLanguage(language:String){
        Log.d("TEKKEN","language" )
        TranslateText.setTargetLanguage(language)
        targetLanguage.value = language
    }
    fun swapSourceAndTargetLanguages(){

        if(sourceLanguage.value == "Detect automatically")return

        sourceLanguage.value = targetLanguage.value.also { targetLanguage.value = sourceLanguage.value }
        Log.d("TEKKEN", sourceLanguage.value!!)

        TranslateText.setSourceLanguage(sourceLanguage.value!!)

        TranslateText.setTargetLanguage(targetLanguage.value!!)
    }

    public fun saveDataToPrefs(sharedPreferences: SharedPreferences){

        val editor = sharedPreferences.edit()

        editor.putString("targetLanguage", targetLanguage.value)

        editor.putString("sourceLanguage", sourceLanguage.value)

        editor.apply()
    }











}