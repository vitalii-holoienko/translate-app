package com.example.simpletranslateapp
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException


class MainViewModel : ViewModel() {
    var displayInternetConnectionError = MutableLiveData<Boolean>()
    val inputTextSize =  MutableLiveData<Int>()
    var inputText = MutableLiveData<String>()
    var translatedText = MutableLiveData<String>()
    var sourceLanguage = MutableLiveData<String>()
    var targetLanguage = MutableLiveData<String>()
    private lateinit var context: Context
    private val translateText = TranslateText()
    private val TAG = "dain"
    lateinit var connectivityObserver: NetworkConnectivityObserver

    init {
        sourceLanguage.value = "Detect Automatically"
        targetLanguage.value = "English"
        inputText.value = ""
        translatedText.value = ""
    }
    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "end")
    }

    fun getActivityContext(context: Context){
        this.context = context
    }
    fun startConnectivityObserve(context:Context){
        connectivityObserver = NetworkConnectivityObserver(context)
    }
    fun changeSourceLanguage(language:String){

        translateText.setSourceLanguage(language)
        sourceLanguage.value = language
    }
    fun changeTargetLanguage(language:String){
        translateText.setTargetLanguage(language)
        targetLanguage.value = language
    }

    fun swapSourceAndTargetLanguages(){
        if(sourceLanguage.value == "Detect automatically")return
        sourceLanguage.value = targetLanguage.value.also { targetLanguage.value = sourceLanguage.value }
        translateText.setSourceLanguage(sourceLanguage.value!!)
        translateText.setTargetLanguage(targetLanguage.value!!)

        inputText.value = translatedText.value
        translatedText.value = ""
        GlobalScope.launch {
            getTranslatedText(inputText.value!!, context)
        }
    }
    public fun saveDataToPrefs(sharedPreferences: SharedPreferences){
        val editor = sharedPreferences.edit()
        editor.putString("targetLanguage", targetLanguage.value)
        editor.putString("sourceLanguage", sourceLanguage.value)
        editor.apply()
    }
    fun processingInput(input:String, context: Context){
        inputText.value = input

        changeInputUI(input)

        GlobalScope.launch {
            getTranslatedText(input, context)
        }
    }

    private suspend fun getTranslatedText(input: String, context: Context) {
        try {
            if (isInternetAvailable(context)) {
                translatedText.postValue(translateText.translate(input))
                displayInternetConnectionError.value = false;
            } else
                displayInternetConnectionError.value = true;

        } catch (ex: IOException) {
            ex.message?.let { Log.d(TAG, it) }
        } catch (ex: Exception) {
            ex.message?.let { Log.d(TAG, it) }
        }
    }
    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
        return actNw.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
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