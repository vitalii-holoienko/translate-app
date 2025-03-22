package com.example.simpletranslateapp

import android.app.Activity
import android.content.Context
import android.content.Context.CAMERA_SERVICE
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.graphics.Paint
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.util.SparseIntArray
import android.view.Surface
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class TranslatedImageViewModel(val database:DataBase) : ViewModel() {
    private var textRecognizer : TextRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    var showTranslatedText = MutableLiveData<Boolean>(true)
    var isLoading = MutableLiveData<Boolean>(true)
    var wholeRecognizedTextString = MutableLiveData<String>("")
    var translatedTextString = MutableLiveData<String>("")
    var sourceLanguage : String = ""
    var targetLanguage : String = ""
    val MAX_AMOUNT_OF_LINES_IN_HISTORY_PAGE = 10
    companion object{
        @Suppress("UNCHECKED_CAST")
        val factory: ViewModelProvider.Factory = object: ViewModelProvider.Factory{
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val database= (checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as App).database
                return TranslatedImageViewModel(database) as T
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.P)
    fun recognizeTextFromImage(
        context: Context,
        uri: Uri,
        onTextRecognized: (List<CameraScreenViewModel.RecognizedTextBlock>) -> Unit
    ) {
        try{
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            val bitmap = ImageDecoder.decodeBitmap(source)
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            textRecognizer.process(inputImage)
                    .addOnSuccessListener {
                        val textBlocks = it.textBlocks.map { block ->
                            wholeRecognizedTextString.value += block.text
                            CameraScreenViewModel.RecognizedTextBlock(
                                text = block.text,
                                boundingBox = block.boundingBox,
                                lines = block.lines
                            )
                        }

                        onTextRecognized(textBlocks)

                    }
                    .addOnFailureListener(){
                        Log.d("TEKKEN", it.message.toString())
                    }
        } catch (e : Exception){
            Log.d("TEKKEN", e.message.toString())
        }
    }

    fun upsertHistoryString(translations : List<CameraScreenViewModel.RecognizedTextBlock>){
        translations.map{block->
            translatedTextString.value+=block.text
        }




        GlobalScope.launch {
            if(!database.historyStringDao.exists(wholeRecognizedTextString.value!!)){
                val amount = database.historyStringDao.getItemCount()

                if(amount >= MAX_AMOUNT_OF_LINES_IN_HISTORY_PAGE){
                    database.historyStringDao.deleteOldestString()
                }

                val historyString = HistoryString(wholeRecognizedTextString.value!!, translatedTextString.value!!, sourceLanguage, targetLanguage)

                database.historyStringDao.upsertString(historyString)
            }

        }
    }
}