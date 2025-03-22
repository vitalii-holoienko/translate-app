package com.example.simpletranslateapp

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color.rgb
import android.graphics.Color as LegacyColor
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column



import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.example.simpletranslateapp.ui.theme.SimpleTranslateAppTheme
import com.github.chrisbanes.photoview.PhotoView
import com.google.mlkit.vision.text.Text
import kotlinx.coroutines.launch
class TranslatedImageActivity : ComponentActivity() {
    private lateinit var viewModel: TranslatedImageViewModel
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this, TranslatedImageViewModel.factory).get(TranslatedImageViewModel::class.java)

        val uriString = intent.getStringExtra("imageUri")
        val uri = uriString?.let { Uri.parse(it) }


        viewModel.sourceLanguage = intent.getStringExtra("sourceLanguage")!!
        viewModel.targetLanguage = intent.getStringExtra("targetLanguage")!!

        setContent {
            SimpleTranslateAppTheme {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(43, 40, 43))
                ) {

                    Top()

                    Divider(
                        color = Color(67, 67, 67),
                        modifier = Modifier
                            .height(1.dp)
                            .fillMaxWidth()
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clipToBounds()

                    ) {
                        ProcessAndDisplayImage(uri!!, viewModel)
                    }

                    Divider(
                        color = Color(67, 67, 67),
                        modifier = Modifier
                            .height(1.dp)
                            .fillMaxWidth()
                    )


                    Bottom(viewModel)
                }


            }
        }
    }
}
@Composable
fun Top() {
    val context = LocalContext.current
    val activity = context as? Activity
    Column(modifier = Modifier.background(Color(43, 40, 43))) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(Color(43, 40, 43)),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Левая иконка (Cancel)
                Image(
                    painter = painterResource(id = R.drawable.cancel_arrow),
                    contentDescription = null,
                    modifier = Modifier
                        .size(35.dp)
                        .clickable {
                            activity?.finish()
                        }
                )

                Spacer(modifier = Modifier.weight(1f)) // Раздвигаем текст в центр

                // Текст в центре
                Text(
                    text = stringResource(R.string.simpletranslate),
                    color = Color(224, 224, 224),
                    fontSize = 27.sp,
                    fontFamily = FontFamily(Font(R.font.salsa_regular)),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.weight(1f)) // Раздвигаем иконку вправо

                // Правая иконка (Home)
                Image(
                    painter = painterResource(id = R.drawable.home_icon),
                    contentDescription = null,
                    modifier = Modifier
                        .size(35.dp)
                        .clickable {
                            val intent = Intent(context, MainActivity::class.java)
                            context.startActivity(intent)

                        }
                )
            }
        }
    }

}
@Composable
fun Bottom(translatedImageViewModel: TranslatedImageViewModel){

    var isLoading by remember{ mutableStateOf(true) }
    isLoading = translatedImageViewModel.isLoading.observeAsState(true).value
    val selectedText = remember { mutableStateOf(2) }
    if(!isLoading){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp, 0.dp,0.dp,0.dp)
                .background(Color(43, 40, 43))
                .height(120.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Source text",
                color = if (selectedText.value == 1) Color(rgb(255, 183, 115)) else Color(224, 224, 224),
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                fontFamily = FontFamily(Font(R.font.poppins_regular)),
                modifier = Modifier.weight(1f).clickable {
                    selectedText.value = 1
                    translatedImageViewModel.showTranslatedText.value = false
                }
            )
            Divider(
                color = Color.Gray,
                modifier = Modifier
                    .height(30.dp)
                    .width(2.dp)
                    .clip(CircleShape)
            )
            Text(
                text = "Translated text",
                color = if (selectedText.value == 2) Color(rgb(255, 183, 115)) else Color(224, 224, 224),
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                fontFamily = FontFamily(Font(R.font.poppins_regular)),
                modifier = Modifier.weight(1f).clickable {
                    selectedText.value = 2
                    translatedImageViewModel.showTranslatedText.value = true
                }
            )
        }
    }
}



@Composable
fun OverlayTextOnImage(
    uri: Uri,
    translatedBlocks: List<CameraScreenViewModel.RecognizedTextBlock>
) {
    translatedBlocks.map{
        Log.d("TEKKEN", it.text)
    }

        Column(modifier=Modifier.background(Color(43, 40, 43))){
            AndroidView(
                modifier = Modifier
                    .height(600.dp)// Ограничивает размер по содержимому
                    .padding(0.dp)
                    .background(Color(43, 40, 43)),

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
                                        color = LegacyColor.argb(100, 150, 150, 150)
                                        style = Paint.Style.FILL
                                    }
                                    canvas.drawRect(0f, 0f, mutableBitmap.width.toFloat(), mutableBitmap.height.toFloat(), overlayPaint)

                                    val boxPaint = Paint().apply {
                                        color = LegacyColor.WHITE
                                        style = Paint.Style.FILL
                                    }
                                    val textPaint = Paint().apply {
                                        color = LegacyColor.BLACK
                                        textSize = 50f
                                        typeface = Typeface.DEFAULT_BOLD
                                    }

                                    translatedBlocks.forEach { block ->

                                        block.boundingBox?.let { box ->

                                            drawTextScaledToWidth(canvas, block.text, box, boxPaint, textPaint, block.lines)
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
                        Log.d("main", e.message.toString())
                    }
                    photoView.apply {
                        maximumScale = 5.0f
                        mediumScale = 2.5f
                        minimumScale = 1.0f
                    }
                }
            )
        }
}

private fun drawTextScaledToWidth(
    canvas: Canvas,
    text: String,
    box: Rect,
    boxPaint: Paint,
    textPaint: Paint,
    lines: List<Text.Line>
) {
    val padding = 12
    val lineSpacing = 6

    val translatedLines = text.split("\n")

    // Определяем минимальный размер шрифта, который подойдет всем строкам
    var adjustedTextPaint = Paint(textPaint)
    var minTextSize = adjustedTextPaint.textSize

    lines.forEachIndexed { index, line ->
        val boundingBox = line.getBoundingBox() ?: return@forEachIndexed
        val translatedText = translatedLines.getOrNull(index) ?: ""

        // Определяем, насколько нужно уменьшить шрифт
        while (adjustedTextPaint.measureText(translatedText) > boundingBox.width() - padding * 2 && adjustedTextPaint.textSize > 10) {
            adjustedTextPaint.textSize -= 1
        }

        // Запоминаем минимальный размер текста
        minTextSize = minOf(minTextSize, adjustedTextPaint.textSize)
    }

    // Применяем одинаковый размер шрифта ко всем строкам
    adjustedTextPaint.textSize = minTextSize

    lines.forEachIndexed { index, line ->
        val angle = line.getAngle()
        val boundingBox = line.getBoundingBox() ?: return@forEachIndexed
        val topLeft = PointF(boundingBox.left.toFloat(), boundingBox.top.toFloat())

        canvas.save()

        val centerX = topLeft.x + boundingBox.width() / 2
        val centerY = topLeft.y + boundingBox.height() / 2 + index * lineSpacing

        canvas.rotate(angle, centerX, centerY)

        val translatedText = translatedLines.getOrNull(index) ?: ""

        val textWidth = adjustedTextPaint.measureText(translatedText) + padding * 2
        val textHeight = adjustedTextPaint.textSize + padding

        val backgroundLeft = centerX - textWidth / 2
        val backgroundTop = centerY - textHeight / 2
        val backgroundRight = backgroundLeft + textWidth
        val backgroundBottom = backgroundTop + textHeight

        // Рисуем фон
        canvas.drawRect(backgroundLeft, backgroundTop, backgroundRight, backgroundBottom, boxPaint)

        // Рисуем текст
        val textX = centerX - textWidth / 2 + padding
        val textY = centerY + adjustedTextPaint.textSize / 3
        canvas.drawText(translatedText, textX, textY, adjustedTextPaint)

        canvas.restore()
    }
}
@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun ProcessAndDisplayImage(uri: Uri, translatedImageViewModel: TranslatedImageViewModel) {
    Column(

    ){
        var translatedTextBlocks by remember { mutableStateOf<List<CameraScreenViewModel.RecognizedTextBlock>>(emptyList()) }
        var shouldOverlay by remember { mutableStateOf(false) }
        var isLoading by remember { mutableStateOf(true) }
        var showTranslatedText by remember {
            mutableStateOf(true)
        }
        isLoading = translatedImageViewModel.isLoading.observeAsState(true).value
        showTranslatedText = translatedImageViewModel.showTranslatedText.observeAsState(true).value
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(uri) {
            try {
                translatedImageViewModel.recognizeTextFromImage(context, uri) { textBlocks ->
                    coroutineScope.launch {
                        var translatedBlocks = textBlocks.map { block ->
                            val translatedText = TranslateText.translate(block.text)
                            block.copy(text = translatedText)
                        }
                        translatedTextBlocks = translatedBlocks
                        Log.d("TEKKEN", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")
                        translatedImageViewModel.upsertHistoryString(translatedBlocks)
                        translatedTextBlocks.map{
                            Log.d("TEKKEN", it.text)
                        }
                        Log.d("TEKKEN", "-----------------------------------------------------")
                        shouldOverlay = translatedTextBlocks.isNotEmpty()

                        translatedImageViewModel.isLoading.value = false
                    }
                }


            } catch (e: Exception) {
                translatedImageViewModel.isLoading.value = false
            }

        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(43, 40, 43)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        else if (showTranslatedText) {
            OverlayTextOnImage(uri, translatedTextBlocks)
            Log.d("TEKKEN", "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB")
        } else if (!showTranslatedText){
            SourceImage(uri)
        }
    }

}
    @Composable
    fun SourceImage(uri: Uri) {
        val context = LocalContext.current
        AndroidView(
            modifier = Modifier
                .height(600.dp)
                .padding(0.dp)
                .background(Color(43, 40, 43)),

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
                    Log.d("main", e.message.toString())
                }
                photoView.apply {
                    maximumScale = 5.0f
                    mediumScale = 2.5f
                    minimumScale = 1.0f
                }
            }
        )
    }



