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
import com.github.chrisbanes.photoview.PhotoView
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
import java.io.File
import java.io.IOException

class TranslatedImageActivity : ComponentActivity() {
    private lateinit var viewModel: TranslatedImageViewModel
    @RequiresApi(Build.VERSION_CODES.P)
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
fun OverlayTextOnImage(
    uri: Uri,
    translatedBlocks: List<CameraScreenViewModel.RecognizedTextBlock>
) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            val photoView = PhotoView(context) // Используем PhotoView вместо ImageView
            try {
                Glide.with(context)
                    .asBitmap()
                    .load(uri)
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?
                        ) {
                            val mutableBitmap = resource.copy(Bitmap.Config.ARGB_8888, true)
                            val canvas = Canvas(mutableBitmap)

                            val boxPaint = Paint().apply {
                                color = Color.WHITE
                                style = Paint.Style.FILL
                            }
                            val textPaint = Paint().apply {
                                color = Color.BLACK
                                textSize = 50f
                                typeface = Typeface.DEFAULT_BOLD
                            }
                            translatedBlocks.forEach { block ->
                                block.boundingBox?.let { box ->
                                    drawTextScaledToWidth(canvas, block.text, box, boxPaint, textPaint)
                                }
                            }

                            Log.d("TEKKEN", "4")
                            photoView.setImageBitmap(mutableBitmap) // Устанавливаем в PhotoView
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                            photoView.setImageDrawable(placeholder)
                        }

                        override fun onLoadFailed(errorDrawable: Drawable?) {
                            Log.e("OverlayTextOnImage", "Failed to load image")
                            photoView.setImageResource(android.R.color.transparent)
                        }
                    })
            } catch (e: Exception) {
                Log.d("TEKKEN", e.message.toString())
            }
            photoView.apply {
                maximumScale = 5.0f // Максимальный зум
                mediumScale = 2.5f
                minimumScale = 1.0f
            }
        }
    )
}

private fun drawTextScaledToWidth(
    canvas: Canvas,
    text: String,
    box: Rect,
    boxPaint: Paint,
    textPaint: Paint
) {
    val lines = text.split("\n")
    val padding = 8

    var currentTop = box.top.toFloat()

    lines.forEachIndexed { index, line ->
        val textX = box.left.toFloat() + padding
        val textY = currentTop + textPaint.textSize

        // Белый прямоугольник вокруг текста
        val backgroundTop = currentTop - padding / 2
        val backgroundBottom = backgroundTop + textPaint.textSize + padding
        canvas.drawRect(
            box.left.toFloat(),
            backgroundTop,
            box.right.toFloat(),
            backgroundBottom,
            boxPaint
        )

        // Рисуем текст
        canvas.drawText(line, textX, textY, textPaint)

        // Если это не последняя строка, пересчитаем отступ
        if (index < lines.size - 1) {
            val nextBoxTop = box.top + ((box.height() / lines.size) * (index + 1))
            currentTop = nextBoxTop.toFloat()
        } else {
            currentTop = backgroundBottom
        }
    }
}
@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun ProcessAndDisplayImage(uri: Uri, translatedImageViewModel: TranslatedImageViewModel) {
    var translatedTextBlocks by remember { mutableStateOf<List<CameraScreenViewModel.RecognizedTextBlock>>(emptyList()) }
    var shouldOverlay by remember { mutableStateOf(false) }
    val context = LocalContext.current
    // Обработка распознавания текста
    try{

        translatedImageViewModel.recognizeTextFromImage(context, uri) {
                textBlocks ->
                translatedTextBlocks = textBlocks
                shouldOverlay = translatedTextBlocks.isNotEmpty()
        }
    }catch (e:Exception){
        Log.d("TEKKEN", e.message.toString())
    }
    if (shouldOverlay) {
        OverlayTextOnImage(uri, translatedTextBlocks)
    }
}



