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
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class TranslatedImageViewModel(val database:DataBase) : ViewModel() {
    private val translateText = TranslateText()
    private var textRecognizer : TextRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    var translatedText =        MutableLiveData<String>()


    private val ORIENTATIONS = SparseIntArray().apply {
        append(Surface.ROTATION_0, 0)
        append(Surface.ROTATION_90, 90)
        append(Surface.ROTATION_180, 180)
        append(Surface.ROTATION_270, 270)
    }
    init{
        TranslateText.setSourceLanguage("English")
        TranslateText.setTargetLanguage("Russian")
    }
    companion object{
        @Suppress("UNCHECKED_CAST")
        val factory: ViewModelProvider.Factory = object: ViewModelProvider.Factory{
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val database= (checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as App).database
                return TranslatedImageViewModel(database) as T
            }
        }
    }

    suspend fun getTranslatedText(input: String, context: Context): String {
        Log.d("TextTranslation", "Translating text...")
        return TranslateText.translate(input).also {
            Log.d("TextTranslation", "Translation completed")
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    @Throws(CameraAccessException::class)
    private fun getRotationCompensation(
        cameraId: String,
        windowManager: WindowManager,
        cameraManager: CameraManager,
        isFrontFacing: Boolean
    ): Int {
        val deviceRotation = windowManager.defaultDisplay.rotation
        val rotationCompensation = ORIENTATIONS.get(deviceRotation)
        val sensorOrientation = cameraManager
            .getCameraCharacteristics(cameraId)
            .get(CameraCharacteristics.SENSOR_ORIENTATION)!!

        return if (isFrontFacing) {
            (sensorOrientation + rotationCompensation) % 360
        } else {
            (sensorOrientation - rotationCompensation + 360) % 360
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
            var bitmap = ImageDecoder.decodeBitmap(source)
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            textRecognizer.process(inputImage)
                    .addOnSuccessListener {
                        val textBlocks = it.textBlocks.map { block ->

                            CameraScreenViewModel.RecognizedTextBlock(
                                text = block.text,
                                boundingBox = block.boundingBox
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

    fun translateBlocks(
        textBlocks: List<CameraScreenViewModel.RecognizedTextBlock>,
        context: Context, // Контекст передается извне
        onTranslationComplete: (List<CameraScreenViewModel.RecognizedTextBlock>) -> Unit
    ) {
        viewModelScope.launch {
            val translations = textBlocks.map { block ->
                async(Dispatchers.IO) {
                    val translatedText = getTranslatedText(block.text, context)
                    CameraScreenViewModel.RecognizedTextBlock(
                        text = translatedText,
                        boundingBox = block.boundingBox
                    )
                }
            }
            val translatedBlocks = translations.awaitAll()
            onTranslationComplete(translatedBlocks)
        }
    }



    // Suspend function for asynchronous task handling
    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun <TResult> Task<TResult>.await(): TResult = suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { continuation.resume(it) }
        addOnFailureListener { continuation.resumeWithException(it) }
    }
}