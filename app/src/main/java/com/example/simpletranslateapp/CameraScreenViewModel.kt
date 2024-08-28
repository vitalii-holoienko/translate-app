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
        val text: String,
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
        translateText.setSourceLanguage(language)
        sourceLanguage.value = language
    }

    fun changeTargetLanguage(language:String){
        translateText.setTargetLanguage(language)
        targetLanguage.value = language
    }


    fun recognizeTextFromImage(image: InputImage, onTextRecognized: (List<RecognizedTextBlock>) -> Unit) {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val textBlocks = visionText.textBlocks.map { block ->
                    RecognizedTextBlock(
                        text = block.text,
                        boundingBox = block.boundingBox
                    )
                }
                onTextRecognized(textBlocks)
            }
            .addOnFailureListener { e ->
                Log.e("TextRecognition", "Text recognition failed: ${e.localizedMessage}")
            }
    }



    fun translateText(text: String, targetLanguage: String, onTranslationDone: (String) -> Unit, context: Context) {
        try {
            if (Tools.isInternetAvailable(context)) {
                GlobalScope.launch {
                    translatedText.postValue(translateText.translate(text))
                }.invokeOnCompletion {
                    onTranslationDone(translatedText.value!!)
                }
            }
        } catch (_: IOException) {

        } catch (_: Exception) {

        }

    }



}