package com.example.simpletranslateapp

import android.app.Activity
import android.content.Context
import android.content.Context.CAMERA_SERVICE
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.Image
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.transition.Transition
import android.util.Log
import android.util.SparseIntArray
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.example.simpletranslateapp.ui.theme.SimpleTranslateAppTheme
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException

val translateText = TranslateText()
private val ORIENTATIONS = SparseIntArray()
class TranslatedImageActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val uriString = intent.getStringExtra("imageUri")
        val uri = uriString?.let { Uri.parse(it) }
        translateText.setTargetLanguage("English")

        setContent {
            SimpleTranslateAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    ProcessAndDisplayImage(uri!!)
                }
            }
        }
    }
}

@Composable
fun Greeting2() {

}

@Composable
fun GlideImageFromUri(uri: Uri) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            val imageView = ImageView(context)

            Glide.with(context)
                .asBitmap()
                .load(uri)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?
                    ) {
                        imageView.setImageBitmap(resource)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        imageView.setImageDrawable(placeholder)
                    }
                })
            imageView
        }
    )
}
@Composable
fun OverlayTextOnImage(uri: Uri, translatedBlocks: List<CameraScreenViewModel.RecognizedTextBlock>) {
    Log.d("GAGA", "5")
    // Load the image using Glide or any other image loading library
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            val imageView = ImageView(context)
            Glide.with(context)
                .asBitmap()
                .load(uri)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?
                    ) {
                                                // Draw the text on the image
                        val mutableBitmap = resource.copy(Bitmap.Config.ARGB_8888, true)
                        val canvas = Canvas(mutableBitmap)
                        val paint = Paint().apply {
                            color = android.graphics.Color.RED // Use a contrasting color for visibility
                            textSize = 50f
                            typeface = Typeface.DEFAULT_BOLD
                            style = Paint.Style.FILL
                            textAlign = Paint.Align.LEFT
                        }

                        translatedBlocks.forEach { block ->
                            Log.d("GAGA", "Processing block with text: ${block.text}")
                            block.boundingBox?.let { box ->
                                // Adjust coordinates for better visibility
                                val x = box.left.toFloat()
                                val y = box.bottom.toFloat() // Ensure this is within the Bitmap bounds

                                // Check Bitmap dimensions
                                if (x >= 0 && x <= mutableBitmap.width && y >= 0 && y <= mutableBitmap.height) {
                                    Log.d("GAGA", "Drawing text at x: $x, y: $y")
                                    canvas.drawText(block.text, x, y, paint)
                                } else {
                                    Log.d("GAGA", "Text coordinates out of bounds: x: $x, y: $y")
                                }
                            }
                        }
                        // Set the modified Bitmap to the ImageView
                        Log.d("GAGA", "Updating ImageView with the modified Bitmap")
                        imageView.setImageBitmap(mutableBitmap)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        Log.d("GAGA", "CANCEL")
                        imageView.setImageDrawable(placeholder)
                    }
                })
            imageView
        }
    )
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



// Function to handle image rotation
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

@OptIn(DelicateCoroutinesApi::class)
@Composable
fun ProcessAndDisplayImage(uri: Uri) {
    var translatedTextBlocks by remember { mutableStateOf<List<CameraScreenViewModel.RecognizedTextBlock>>(emptyList()) }
    var shouldOverlay by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val c = LocalContext.current
    LaunchedEffect(uri) {
        Log.d("GAGA", "IN LAUNCHED_EFFECT")
        recognizeTextFromImage(context, uri) { textBlocks ->
                var translatedText = ""
            Log.d("GAGA", "2")
                // Perform translation asynchronously
                val translations = textBlocks.map { block ->
                    Log.d("GAGA", "3")
//                    translateText(block.text){
//                        Log.d("GAGA", it + " TRANSLATED TEXT \n")
//                        translatedText = it
//                    } // Assuming translateText is a suspend function
                    CameraScreenViewModel.RecognizedTextBlock(
                        text = block.text,
                        boundingBox = block.boundingBox
                    )
                }
            Log.d("GAGA", "4")
                translatedTextBlocks = translations
                shouldOverlay = translations.isNotEmpty()
            Log.d("GAGA", shouldOverlay.toString())
        }
    }

    if (shouldOverlay) {
        OverlayTextOnImage(uri, translatedTextBlocks)
    }
//    OverlayTextOnImage(uri, translatedTextBlocks)


}

fun translateText(text: String, onTranslationDone: (String) -> Unit) {
    var translatedText = ""
    try {
//        if (Tools.isInternetAvailable(context)) {
//            GlobalScope.launch {
//                translatedText.postValue(translateText.translate(text))
//            }.invokeOnCompletion {
//                onTranslationDone(translatedText.value!!)
//            }
//        }
        GlobalScope.launch {
                translatedText=translateText.translate(text)
            }.invokeOnCompletion {
                onTranslationDone(translatedText)
            }
    } catch (_: IOException) {

    } catch (_: Exception) {

    }

}

