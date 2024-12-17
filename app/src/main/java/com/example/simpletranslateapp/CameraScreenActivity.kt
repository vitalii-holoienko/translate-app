package com.example.simpletranslateapp

import android.app.Activity
import android.content.Intent
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row


import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.simpletranslateapp.ui.theme.SimpleTranslateAppTheme
import java.io.File

class CameraScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SimpleTranslateAppTheme {
                // A surface container using the 'background' color from the theme

                UI()

            }
        }
    }
}

@Composable
fun UI() {
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current;
    val imageCapture: ImageCapture = remember {
        ImageCapture.Builder().build()
    }
    Scaffold(
        topBar = {
            Header()
        },
        bottomBar = {
            Footer(

            )
        },
        content = {padding->
            CameraCapture(
                onImageCaptured = { uri,  ->
                    imageUri = uri
                    val intent = Intent(context, TranslatedImageActivity::class.java).also {
                        it.putExtra("imageUri", imageUri.toString());
                    }
                    context.startActivity(intent)
                },
                onError = { exc ->
                    Log.e("CameraX", "Error capturing image: ${exc.localizedMessage}")
                },
                padding,
                imageCapture
            )

        }
    )

}

@Composable
fun Header(){
    val context = LocalContext.current
    val activity = context as? Activity

    Column(){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(Color(43, 40, 43)),
            horizontalArrangement = Arrangement.SpaceBetween

        ){
            Box(modifier = Modifier.fillMaxWidth()){
                Image(
                    painter = painterResource(id = R.drawable.cancel_arrow),
                    contentDescription = null,
                    modifier = Modifier
                        .size(47.dp)
                        .padding(6.dp, 5.dp, 0.dp, 6.dp)
                        .align(alignment = Alignment.CenterStart)
                        .scale(1.1f)
                        .clickable {
                            activity?.finish()
                        }
                )
                Text(
                    text = stringResource(R.string.simpletranslate),
                    color = Color(224, 224, 224),
                    fontSize = 27.sp,
                    fontFamily = FontFamily(Font(R.font.salsa_regular)),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(alignment = Alignment.Center)
                )
                Image(
                    painter = painterResource(id = R.drawable.search_icon),
                    contentDescription = null,
                    modifier = Modifier
                        .size(50.dp)
                        .padding(6.dp, 5.dp, 6.dp, 6.dp)
                        .align(alignment = Alignment.CenterEnd)
                        .scale(1f),
                )

            }


        }

    }


}

@Composable
fun CameraCapture(
    onImageCaptured: (Uri) -> Unit,
    onError: (ImageCaptureException) -> Unit,
    paddingValues: PaddingValues,
    imageCapture : ImageCapture
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(paddingValues)
            .background(Color(43, 40, 43))
    ){
        val lifecycleOwner = LocalLifecycleOwner.current
        val context = LocalContext.current
        Box(
            modifier = Modifier.fillMaxSize()
        ){
            AndroidView(
                modifier = Modifier
                    .clip(shape = RoundedCornerShape(0.dp, 0.dp, 20.dp, 20.dp))
                    .background(Color.Red, shape = RoundedCornerShape(0.dp))
                    .align(Alignment.TopCenter)

                ,
                factory = { ctx ->
                    val previewView = PreviewView(ctx)

                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = androidx.camera.core.Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageCapture
                            )
                        } catch (exc: Exception) {
                            Log.e("CameraX", "Use case binding failed", exc)
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                }
            )

            Button(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(20.dp)
                    .size(80.dp),


                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(contentColor = Color.White, containerColor = Color.White),


                onClick = {
                    val outputOptions = ImageCapture.OutputFileOptions.Builder(
                        File(context.externalCacheDir, "${System.currentTimeMillis()}.jpg")
                    ).build()

                    imageCapture.takePicture(
                        outputOptions,
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                val savedUri = outputFileResults.savedUri
                                onImageCaptured(outputFileResults.savedUri!!)
                            }

                            override fun onError(exception: ImageCaptureException) {
                                onError(exception)
                            }
                        }
                    )
                },

                ) {
                Icon(
                    imageVector = Icons.Default.Favorite, // Пример иконки
                    contentDescription = "Favorite",
                    tint = Color.White
                )

            }
        }


   }
}
@Composable
fun Footer(
){
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(Color(43, 40, 43)),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Row(modifier = Modifier.padding(0.dp,10.dp,0.dp,2.dp)){
            Box(
                modifier = Modifier
                    .width(130.dp)
                    .height(50.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(53, 50, 53))
                    .clickable {
//                    val intent = Intent(context, ChooseLanguageActivity::class.java).also {
//                        it.putExtra("source", mainViewModel.sourceLanguage.value)
//                        it.putExtra("from", "source")
//                        it.putExtra("input", mainViewModel.inputText.value)
//                    }
//
//                    context.startActivity(intent)
                    },
                contentAlignment = Alignment.Center
            ){
                Text(
                    text = "sourceLanguage",
                    color = Color(224, 224, 224),
                    fontFamily = FontFamily(Font(R.font.poppins_regular)),
                    textAlign = TextAlign.Center,
                    fontSize = 15.sp,
                )
            }
            val interactionSource = remember { MutableInteractionSource() }
            Image(
                painter = painterResource(id = R.drawable.arrows_icon),
                contentDescription = null,
                modifier = Modifier
                    .size(50.dp)
                    .scale(2f)
//                .clickable(indication = null, onClick = {
//                    mainViewModel.swapSourceAndTargetLanguages()
//                }, interactionSource = interactionSource)

            )

            Box(
                modifier = Modifier
                    .width(130.dp)
                    .height(50.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(53, 50, 53))
                    .clickable {
//                    val intent =
//                        Intent(context, ChooseLanguageActivity::class.java).also {
//                            it.putExtra("target", mainViewModel.targetLanguage.value)
//                            it.putExtra("from", "target")
//                            it.putExtra("input", mainViewModel.inputText.value)
//                        }
//
//
//                    context.startActivity(intent)

                    },
                contentAlignment = Alignment.Center
            ){ Text(
                text = "it",
                color = Color(224, 224, 224),
                fontFamily = FontFamily(Font(R.font.poppins_regular)),
                textAlign = TextAlign.Center,
                fontSize = 15.sp,
            )
            }
        }
    }
}