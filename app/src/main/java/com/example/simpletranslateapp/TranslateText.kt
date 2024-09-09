package com.example.simpletranslateapp

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.cloud.translate.Translate
import com.google.cloud.translate.TranslateOptions
import com.google.cloud.translate.Translation

class TranslateText {
    companion object{
        private val API_KEY = "AIzaSyD6BNc6OpDunxIUv99ciWYdKt_loiJkqOY"
        private var sourceLanguage : Translate.TranslateOption = Translate.TranslateOption.targetLanguage("en")
        private var targetLanguage : Translate.TranslateOption = Translate.TranslateOption.sourceLanguage("en")
        private var translate: Translate = TranslateOptions.newBuilder().setApiKey(API_KEY).build().service

        private lateinit var targetLanguageCode : String
        private lateinit var sourceLanguageCode : String



        suspend fun translate(input:String) : String{
            return try {
                Log.d("GAGA", "Translating input: $input")
                val translation: Translation = translate.translate(input, sourceLanguage, targetLanguage)
                Log.d("GAGA", "Translation successful")
                translation.translatedText.also {
                    Log.d("GAGA", "EEE - Translation result: $it")
                }
            } catch (e: Exception) {
                Log.e("GAGA", "Error during translation: ${e.message}", e)
                ""  // Return an empty string or some default value if translation fails
            }
        }

        public fun setSourceLanguage(language:String){
            val code = Languages.languages.get(language)
            if(!(language == "Detect automatically")){
                sourceLanguage = Translate.TranslateOption.sourceLanguage(code)
            }


        }
        public fun setTargetLanguage(language:String){
            val code = Languages.languages.get(language)
            targetLanguage = Translate.TranslateOption.targetLanguage(code)
        }
    }


}