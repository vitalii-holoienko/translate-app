package com.example.simpletranslateapp

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.cloud.translate.Translate
import com.google.cloud.translate.TranslateOptions
import com.google.cloud.translate.Translation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TranslateText {
    companion object{
        private val API_KEY = "AIzaSyD6BNc6OpDunxIUv99ciWYdKt_loiJkqOY"
        private var sourceLanguage : Translate.TranslateOption = Translate.TranslateOption.targetLanguage("en")
        private var targetLanguage : Translate.TranslateOption = Translate.TranslateOption.sourceLanguage("en")
        private var translate: Translate = TranslateOptions.newBuilder().setApiKey(API_KEY).build().service

        private lateinit var targetLanguageCode : String
        private lateinit var sourceLanguageCode : String



        suspend fun translate(input: String): String {
            return withContext(Dispatchers.IO) {
                try {
                    val lines = input.split("\n").map { it.trim() }
                    val translatedLines = lines.map { line ->
                        if (line.isNotEmpty()) translate.translate(line, sourceLanguage, targetLanguage).translatedText
                        else ""
                    }
                    translatedLines.joinToString("\n")
                } catch (e: Exception) {
                    input
                }
            }
        }

        fun setSourceLanguage(language:String){
            val code = Languages.languages.get(language)
            if(!(language == "Detect automatically")){
                sourceLanguage = Translate.TranslateOption.sourceLanguage(code)
            }


        }
        fun setTargetLanguage(language:String){
            val code = Languages.languages.get(language)
            targetLanguage = Translate.TranslateOption.targetLanguage(code)
        }
    }


}