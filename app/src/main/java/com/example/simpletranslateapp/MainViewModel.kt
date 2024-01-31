package com.example.simpletranslateapp
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.cloud.translate.Translate
import com.google.cloud.translate.TranslateOptions
import com.google.cloud.translate.Translation
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException


class MainViewModel : ViewModel() {
    val inputIsValid = MutableLiveData<Boolean>()
    var displayInternetConnectionError = MutableLiveData<Boolean>()
    val inputTextSize = MutableLiveData<Int>()
    var translatedText = MutableLiveData<String>()

    val API_KEY = "AIzaSyD6BNc6OpDunxIUv99ciWYdKt_loiJkqOY"
    val TAG = "dain"

    private var translate: Translate = TranslateOptions.newBuilder().setApiKey(API_KEY).build().service

    lateinit var connectivityObserver: NetworkConnectivityObserver

    fun startConnectivityObserve(context:Context){
        connectivityObserver = NetworkConnectivityObserver(context)
    }

    fun processingInput(input:String, context: Context){
        changeInputUI(input)
        GlobalScope.launch {
            translateText(input, context)
        }
    }

    private suspend fun translateText(input: String, context: Context) {
        try {
            if (isInternetAvailable(context)) {
                val options = Translate.TranslateOption.targetLanguage("es")
                val translation: Translation = translate.translate(input, options)
                translatedText.postValue(translation.getTranslatedText())
                displayInternetConnectionError.value = false;
            } else {
                displayInternetConnectionError.value = true;
            }
        } catch (ex: IOException) {
            ex.message?.let { Log.d(TAG, it) }
            // Handle other IO exceptions if needed
        } catch (ex: Exception) {
            ex.message?.let { Log.d(TAG, it) }
            // Handle other exceptions if needed
        }
    }
    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
        return actNw.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    val validateInput = {input:String->inputIsValid.value = input.length <= 300}

    fun changeInputUI(input:String){
        if(input.length < 50){
            inputTextSize.value = 30;
        }
        if(input.length >= 100){
            inputTextSize.value = 20;
            return
        }
        if(input.length >= 50){
            inputTextSize.value = 25;
            return
        }
    }


}