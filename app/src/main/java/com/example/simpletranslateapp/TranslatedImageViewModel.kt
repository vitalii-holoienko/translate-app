package com.example.simpletranslateapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.IOException

class TranslatedImageViewModel(val database:DataBase) : ViewModel() {
    private val translateText = TranslateText()
    var translatedText =        MutableLiveData<String>()

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

    suspend fun getTranslatedText(input: String, context: Context) : String {
        Log.d("GAGA", "DDD")
        return TranslateText.translate(input).also {
            Log.d("GAGA", "EEE")
        }
    }

    fun recognizeTextFromImage(
        context: Context,
        uri: Uri,
        onTextRecognized: (List<CameraScreenViewModel.RecognizedTextBlock>) -> Unit
    ) {
        val inputStream = context.contentResolver.openInputStream(uri)
        val imageBitmap = BitmapFactory.decodeStream(inputStream)

        // Rotate the image if necessary
        val rotatedBitmap = rotateBitmapIfNeeded(preprocessBitmap(imageBitmap), uri, context)

        val image = InputImage.fromBitmap(rotatedBitmap, 0)

        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val textBlocks = visionText.textBlocks.map { block ->
                    CameraScreenViewModel.RecognizedTextBlock(
                        text = block.text,
                        boundingBox = block.boundingBox?.let { box ->
                            // Adjust bounding box coordinates if needed
                            // For example: adjustBoundingBox(box, rotation)
                            box
                        }
                    )
                }
                onTextRecognized(textBlocks)
            }
            .addOnFailureListener { e ->
                Log.e("TextRecognition", "Text recognition failed: ${e.localizedMessage}")
            }
    }

    fun rotateBitmapIfNeeded(bitmap: Bitmap, uri: Uri, context: Context): Bitmap {
        val exifInterface = androidx.exifinterface.media.ExifInterface(context.contentResolver.openInputStream(uri)!!)
        val rotation = when (exifInterface.getAttributeInt(androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION, androidx.exifinterface.media.ExifInterface.ORIENTATION_UNDEFINED)) {
            androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_90 -> 90
            androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_180 -> 180
            androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
        return when (rotation) {
            90 -> rotateBitmap(bitmap, 90f)
            180 -> rotateBitmap(bitmap, 180f)
            270 -> rotateBitmap(bitmap, 270f)
            else -> bitmap
        }
    }

    // Function to rotate the bitmap
    fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = android.graphics.Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    fun preprocessBitmap(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val newBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(newBitmap)
        val paint = Paint()
        paint.color = Color.WHITE
        paint.textSize = 50f
        paint.style = Paint.Style.FILL

        // Draw bitmap on canvas with adjustments
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return newBitmap
    }
}