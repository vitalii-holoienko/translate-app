package com.example.simpletranslateapp

import android.app.Activity
import android.content.Context
import android.content.Context.CAMERA_SERVICE
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.example.simpletranslateapp.ui.theme.SimpleTranslateAppTheme
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import java.io.IOException
class TranslatedImageActivity : ComponentActivity() {
    private lateinit var viewModel: TranslatedImageViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Connecting viewmodel
        viewModel = ViewModelProvider(this, TranslatedImageViewModel.factory).get(TranslatedImageViewModel::class.java)
        val uriString = intent.getStringExtra("imageUri")
        val uri = uriString?.let { Uri.parse(it) }


        setContent {
            SimpleTranslateAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    ProcessAndDisplayImage(uri!!, viewModel)
                }
            }
        }
    }
}
@Composable
fun OverlayTextOnImage(uri: Uri, translatedBlocks: List<CameraScreenViewModel.RecognizedTextBlock>, translatedImageViewModel: TranslatedImageViewModel) {
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


                        // Paint for the white box
                        val boxPaint = Paint().apply {
                            color = android.graphics.Color.WHITE // White color for the background box
                            style = Paint.Style.FILL
                        }


                        // Paint for the text
                        val textPaint = Paint().apply {
                            color = android.graphics.Color.BLACK // Use a contrasting color for the text
                            textSize = 50f
                            typeface = Typeface.DEFAULT_BOLD
                            textAlign = Paint.Align.LEFT
                        }

                        translatedBlocks.forEach { block ->
                            block.boundingBox?.let { box ->
                                // Adjust coordinates for better visibility
                                val x = box.left.toFloat()
                                val y = box.bottom.toFloat()

                                // Calculate the size of the text
                                val textBounds = Rect()
                                textPaint.getTextBounds(block.text, 0, block.text.length, textBounds)

                                // Calculate the position of the white box
                                val padding = 10
                                val boxLeft = box.left.toFloat() - padding
                                val boxTop = box.bottom.toFloat() - textBounds.height() - padding
                                val boxRight = box.left.toFloat() + textBounds.width() + padding
                                val boxBottom = box.bottom.toFloat() + padding

                                // Draw the white box first
                                canvas.drawRect(boxLeft, boxTop, boxRight, boxBottom, boxPaint)

                                // Draw the text on top of the box
                                if (x >= 0 && x <= mutableBitmap.width && y >= 0 && y <= mutableBitmap.height) {
                                    canvas.drawText(block.text, x, y, textPaint)
                                } else {
                                }
                            }
                        }
                        // Set the modified Bitmap to the ImageView
                        imageView.setImageBitmap(mutableBitmap)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        imageView.setImageDrawable(placeholder)
                    }
                })
            imageView
        }
    )
}
@OptIn(DelicateCoroutinesApi::class)
@Composable
fun ProcessAndDisplayImage(uri: Uri, translatedImageViewModel: TranslatedImageViewModel) {
    var translatedTextBlocks by remember { mutableStateOf<List<CameraScreenViewModel.RecognizedTextBlock>>(emptyList()) }
    var shouldOverlay by remember { mutableStateOf(false) }
    val context = LocalContext.current
        translatedImageViewModel.recognizeTextFromImage(context, uri) { textBlocks ->
            var translatedText = ""
            // Perform translation asynchronously
//                val translations = textBlocks.map { block ->
////                        translateText(block.text){
////                            Log.d("GAGA", it + " TRANSLATED TEXT \n")
////                            translatedText = it
////                            block.text = translatedText
////                        } // Assuming translateText is a suspend function
//                    Log.d("GAGA", "AAA")
//                  CoroutineScope(Dispatchers.IO).launch {
//                      Log.d("GAGA", "BBB")
//                    translatedImageViewModel.getTranslatedText(block.text, context)
//                      Log.d("GAGA", "CCC")
//                      block.text = translatedImageViewModel.translatedText.value!!
//                  }
//
//                    CameraScreenViewModel.RecognizedTextBlock(
//                        text = block.text,
//                        boundingBox = block.boundingBox
//                    )
//                }
            CoroutineScope(Dispatchers.IO).launch {
                val translations = textBlocks.map { block ->
                    Log.d("GAGA", "AAA")
                    CoroutineScope(Dispatchers.IO).async {
                        Log.d("GAGA", "BBB")
                        val translated = translatedImageViewModel.getTranslatedText(block.text, context)
                        Log.d("GAGA", "CCC")
                        CameraScreenViewModel.RecognizedTextBlock(
                            text = translated!!, // Use the translated text here
                            boundingBox = block.boundingBox
                        )
                    }
                }
                translatedTextBlocks = translations.awaitAll()
                shouldOverlay = translations.isNotEmpty()
            }
        }


    if (shouldOverlay) {
        OverlayTextOnImage(uri, translatedTextBlocks, translatedImageViewModel)
    }
}

