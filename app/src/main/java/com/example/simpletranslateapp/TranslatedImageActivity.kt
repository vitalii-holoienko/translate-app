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

        viewModel = ViewModelProvider(this, TranslatedImageViewModel.factory).get(TranslatedImageViewModel::class.java)

        val uriString = intent.getStringExtra("imageUri")
        val uri = uriString?.let { Uri.parse(it) }




        setContent {
            SimpleTranslateAppTheme {
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
            val photoView = PhotoView(context)
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

                            // 🔥 gray filter
                            val overlayPaint = Paint().apply {
                                color = Color.argb(100, 150, 150, 150) // Серый цвет с прозрачностью
                                style = Paint.Style.FILL
                            }
                            canvas.drawRect(0f, 0f, mutableBitmap.width.toFloat(), mutableBitmap.height.toFloat(), overlayPaint)

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
                            photoView.setImageBitmap(mutableBitmap)
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
                maximumScale = 5.0f
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
        val textWidth = textPaint.measureText(line) + padding * 2
        val textHeight = textPaint.textSize + padding
        val backgroundLeft = box.left.toFloat()
        val backgroundTop = currentTop - padding / 2
        val backgroundRight = backgroundLeft + textWidth
        val backgroundBottom = backgroundTop + textHeight

        canvas.drawRect(
            backgroundLeft, backgroundTop, backgroundRight, backgroundBottom, boxPaint
        )

        val textX = backgroundLeft + padding
        val textY = backgroundTop + textPaint.textSize
        canvas.drawText(line, textX, textY, textPaint)

        // 📌 Сдвигаем `currentTop`, чтобы учесть новую высоту текста
        currentTop = backgroundBottom + padding
    }
}
@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun ProcessAndDisplayImage(uri: Uri, translatedImageViewModel: TranslatedImageViewModel) {
    var translatedTextBlocks by remember { mutableStateOf<List<CameraScreenViewModel.RecognizedTextBlock>>(emptyList()) }
    var shouldOverlay by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(uri) {
        try {
            translatedImageViewModel.recognizeTextFromImage(context, uri) { textBlocks ->
                coroutineScope.launch {
                    val translatedBlocks = textBlocks.map { block ->
                        val translatedText = TranslateText.translate(block.text)
                        block.copy(text = translatedText)
                    }
                    translatedTextBlocks = translatedBlocks
                    shouldOverlay = translatedTextBlocks.isNotEmpty()
                }
            }
        } catch (e: Exception) {
            Log.d("TEKKEN", e.message.toString())
        }
    }

    if (shouldOverlay) {
        OverlayTextOnImage(uri, translatedTextBlocks)
    }
}



