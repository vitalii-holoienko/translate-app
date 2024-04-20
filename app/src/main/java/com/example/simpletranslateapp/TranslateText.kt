package com.example.simpletranslateapp

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.cloud.translate.Translate
import com.google.cloud.translate.TranslateOptions
import com.google.cloud.translate.Translation

class TranslateText {
    private val API_KEY = "AIzaSyD6BNc6OpDunxIUv99ciWYdKt_loiJkqOY"
    private var sourceLanguage : Translate.TranslateOption = Translate.TranslateOption.targetLanguage("en")
    private var targetLanguage : Translate.TranslateOption = Translate.TranslateOption.sourceLanguage("en")
    private var translate: Translate = TranslateOptions.newBuilder().setApiKey(API_KEY).build().service

    private lateinit var targetLanguageCode : String
    private lateinit var sourceLanguageCode : String



    suspend fun translate(input:String) : String{
        val translation: Translation = translate.translate(input, sourceLanguage, targetLanguage)
        return translation.translatedText
    }
    public fun setSourceLanguage(language:String){
        val code = Languages.languages.get(language)
        if(language == "Detect automatically"){

        }else{
            sourceLanguage = Translate.TranslateOption.sourceLanguage(code)
        }


    }
    public fun setTargetLanguage(language:String){
        val code = Languages.languages.get(language)
        targetLanguage = Translate.TranslateOption.targetLanguage(code)
    }

}