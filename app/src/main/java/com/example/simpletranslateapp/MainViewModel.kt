package com.example.simpletranslateapp
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.text.BoringLayout
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.edit
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.cloud.translate.Translate
import com.google.cloud.translate.TranslateOptions
import com.google.cloud.translate.Translation
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.IOException


@DelicateCoroutinesApi
class MainViewModel(val database:DataBase) : ViewModel() {

    //ViewModel custom constructor, that allowing to use database
    companion object{
        @Suppress("UNCHECKED_CAST")
        val factory: ViewModelProvider.Factory = object: ViewModelProvider.Factory{
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val database= (checkNotNull(extras[APPLICATION_KEY]) as App).database
                return MainViewModel(database) as T
            }
        }
    }



    var displayInternetConnectionError = MutableLiveData<Boolean>()

    val inputTextSize =                  MutableLiveData<Int>()

    var inputText =                      MutableLiveData<String>()

    var savedInputText =                 MutableLiveData<String>()

    var translatedText =                 MutableLiveData<String>()

    var sourceLanguage =                 MutableLiveData<String>()

    var targetLanguage =                 MutableLiveData<String>()

    var stringInFavourite =              MutableLiveData<Boolean>()

    val translateText = TranslateText()

    val MAX_AMOUNT_OF_LINES_IN_HISTORY_PAGE = 10



    @SuppressLint("StaticFieldLeak")

    lateinit var context: Context
    lateinit var connectivityObserver: NetworkConnectivityObserver

    init {
        sourceLanguage.value = "Detect Automatically"
        targetLanguage.value = "English"

        changeSourceLanguage(sourceLanguage.value!!)
        changeTargetLanguage(targetLanguage.value!!)

        inputText.value = ""
        translatedText.value = ""
    }

    fun getActivityContext(context: Context){
        this.context = context
    }

    //Database operations
    fun upsertSavedString(){
        GlobalScope.launch {
            val savedString = SavedString(inputText.value!!, translatedText.value!!, sourceLanguage.value!!, targetLanguage.value!!)
            database.savedStringDao.upsertString(savedString)
            stringInFavourite.postValue(true)
        }
    }
    fun deleteSavedString(){
        GlobalScope.launch {
            val savedString = database.savedStringDao.getBySourceText(inputText.value!!)
            database.savedStringDao.deleteString(savedString!!)
            stringInFavourite.postValue(false)
        }
    }

    fun upsertHistoryString(){
        GlobalScope.launch {
            if(!database.historyStringDao.exists(inputText.value!!)){
                val amount = database.historyStringDao.getItemCount()

                if(amount >= MAX_AMOUNT_OF_LINES_IN_HISTORY_PAGE){
                    database.historyStringDao.deleteOldestString()
                }

                val historyString = HistoryString(inputText.value!!, translatedText.value!!, sourceLanguage.value!!, targetLanguage.value!!)

                database.historyStringDao.upsertString(historyString)
            }

        }
    }
    fun deleteHistoryString(){
        GlobalScope.launch {
            val historyString = database.historyStringDao.getBySourceText(inputText.value!!)
            database.historyStringDao.deleteString(historyString!!)

        }
    }

    fun clearHistory(){
        GlobalScope.launch {
            database.historyStringDao.clearHistory()
        }
    }



    fun getAllHistoryStrings(){
        val a = database.historyStringDao.getAllHistoryStrings()
        Log.d("GAGA", a.toString())
        GlobalScope.launch {
            a.collect { list ->
                list.forEach { string ->
                    Log.d("GAGA", string.sourceText.toString())
                }
                Log.d("GAGA", list.size.toString())

            }
        }

    }
    fun checkIfStringInDB() : Boolean{
        return database.savedStringDao.exists(inputText.value!!)
    }

    //observing internet connection
    fun startConnectivityObserve(context:Context){
        connectivityObserver = NetworkConnectivityObserver(context)
    }


    //languages changing

    fun changeSourceLanguage(language:String){
        TranslateText.setSourceLanguage(language)
        sourceLanguage.value = language
    }

    fun changeTargetLanguage(language:String){
        TranslateText.setTargetLanguage(language)
        targetLanguage.value = language
    }

    fun swapSourceAndTargetLanguages(){
        if(sourceLanguage.value == "Detect automatically")return

        sourceLanguage.value = targetLanguage.value.also { targetLanguage.value = sourceLanguage.value }

        TranslateText.setSourceLanguage(sourceLanguage.value!!)

        TranslateText.setTargetLanguage(targetLanguage.value!!)

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

    //processing and translating input text
    fun processingInput(input:String, context: Context){
        inputText.value = input

        changeInputUI(input)

        GlobalScope.launch {
            stringInFavourite.postValue(checkIfStringInDB())
            getTranslatedText(input, context)
        }
    }

    fun clearAllText(){
        inputText.value = ""
        translatedText.value = ""
    }



    private suspend fun getTranslatedText(input: String, context: Context) {
        try {
            if (Tools.isInternetAvailable(context)) {
                translatedText.postValue(TranslateText.translate(input))
                displayInternetConnectionError.value = false;
            } else
                displayInternetConnectionError.value = true;

        } catch (_: IOException) {

        } catch (_: Exception) {

        }
    }
    //UI
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