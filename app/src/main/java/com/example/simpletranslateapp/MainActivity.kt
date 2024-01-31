package com.example.simpletranslateapp


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ViewTreeObserver
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.simpletranslateapp.ui.theme.SimpleTranslateAppTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        val viewModel: MainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        viewModel.startConnectivityObserve(this)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            SimpleTranslateAppTheme {
                UI(viewModel)
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UI(mainViewModel: MainViewModel) {
    val connectivityStatus by mainViewModel.connectivityObserver.observe().collectAsState(initial = ConnectivityObserver.Status.Available)
    Scaffold(
        topBar = {
            Header(mainViewModel)
        },
        bottomBar = {
            Footer(mainViewModel)
        },
        content = {padding->
            if(connectivityStatus == ConnectivityObserver.Status.Available) MainContent(padding, mainViewModel)
            else InternetConnectionErrorWarning(padding = padding)
        }
    )


}

@Composable
fun Header(mainViewModel: MainViewModel){
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(99, 67, 48))
            .height(70.dp)
            .padding(0.dp, 25.dp, 0.dp, 0.dp)

    ){
        Image(
            painter = painterResource(id = R.drawable.star_icon1),
            contentDescription = null,
            modifier = Modifier
                .size(50.dp)
                .align(alignment = Alignment.CenterStart)
                .padding(6.dp, 5.dp, 0.dp, 6.dp)
                .scale(1.2f),
            contentScale = ContentScale.Crop
        )
        Text(
            text = stringResource(R.string.simpletranslate),
            color = Color(255, 255, 255),
            fontSize = 27.sp,
            fontFamily = FontFamily(Font(R.font.salsa_regular)),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(alignment = Alignment.Center)
        )
        Image(
            painter = painterResource(id = R.drawable.cup_icon1),
            contentDescription = null,
            modifier = Modifier
                .size(50.dp)
                .align(alignment = Alignment.CenterEnd)
                .padding(0.dp, 5.dp, 0.dp, 6.dp),
            contentScale = ContentScale.Crop
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun MainContent(padding: PaddingValues, mainViewModel: MainViewModel){
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(99, 67, 48))
            .padding(5.dp)
        ,
    ){
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .clip(RoundedCornerShape(15.dp, 15.dp, 15.dp, 15.dp))
                .background(Color(246, 228, 217))
        ){
            var text by remember {
                mutableStateOf("")
            }
            var textSize by remember {
                mutableStateOf(30)
            }

            var translatedText by remember {
                mutableStateOf("")
            }

            textSize = mainViewModel.inputTextSize.observeAsState(30).value;
            Column(
                modifier = Modifier.padding(10.dp)
            ) {
                val enabled by remember { mutableStateOf(true) }
                val interactionSource = remember { MutableInteractionSource() }
                val isFocused by interactionSource.collectIsFocusedAsState()
                val focusManager = LocalFocusManager.current
                val keyboardController = LocalSoftwareKeyboardController.current
                val context = LocalContext.current

                BasicTextField(
                    value = text,
                    onValueChange = {
                        text = it
                        mainViewModel.processingInput(it, context)
                    },
                    interactionSource = interactionSource,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .animateContentSize()
                        .heightIn(0.dp, 250.dp)
                        .padding(0.dp, 12.5f.dp, 0.dp, 22.5f.dp),

                    decorationBox = { innerTextField ->
                        TextFieldDefaults.OutlinedTextFieldDecorationBox(
                            value = text,
                            innerTextField = innerTextField,
                            enabled = enabled,
                            singleLine = false,
                            contentPadding = PaddingValues(0.dp),
                            visualTransformation = VisualTransformation.None,
                            interactionSource = interactionSource,
                            placeholder = { Text(text = "Enter text",
                                fontSize = textSize.sp,
                                fontFamily = FontFamily(Font(R.font.inter_regular)),
                                color = Color(99, 67, 4)) },
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent,
                            ),
                        )
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        }
                    ),
                    textStyle = TextStyle(
                        fontSize = textSize.sp,
                        fontFamily = FontFamily(Font(R.font.inter_regular)),
                        color = Color(99, 67, 4)
                    ) ,

                )
                if(isFocused){
                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ){
                        Divider(
                            modifier = Modifier
                                .width(180.dp)
                                .align(Alignment.Center),
                            color = Color(99, 67, 4)
                        )
                    }
                }

                var translatedText by remember {
                    mutableStateOf("")
                }
                var displayError by remember{
                    mutableStateOf(false)
                }
                translatedText = mainViewModel.translatedText.observeAsState("").value
                BasicText(
                    text = translatedText,
                    modifier = Modifier
                        .height(200.dp)
                        .padding(0.dp, 22.5f.dp, 0.dp, 0.dp),
                    TextStyle(
                        fontSize = textSize.sp,
                        fontFamily = FontFamily(Font(R.font.inter_regular)),
                        color = Color(99, 67, 4),))
            }
        }
    }

}
@Composable
fun Footer(mainViewModel: MainViewModel){
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
            .background(Color(99, 67, 48)),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        val context = LocalContext.current

            Row(
                modifier = Modifier.padding(0.dp,10.dp,0.dp,2.dp)
            ){
                Box(
                    modifier = Modifier
                        .width(130.dp)
                        .height(50.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(246, 228, 217)).clickable {
                                val intent = Intent(context, ChooseLanguageActivity::class.java).also{
                                    it.putExtra("source", "source")
                                }
                                context.startActivity(intent)
                        },
                    contentAlignment = Alignment.Center
                ){ Text(text = "Language", color = Color(99, 67, 48), fontFamily = FontFamily(Font(R.font.poppins_regular)),) }


                Image(
                    painter = painterResource(id = R.drawable.arrows1_icon),
                    contentDescription = null,
                    modifier = Modifier
                        .size(50.dp)
                        .scale(2f)
                )

                Box(
                    modifier = Modifier
                        .width(130.dp)
                        .height(50.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(246, 228, 217)),
                    contentAlignment = Alignment.Center
                ){ Text(text = "Language", color = Color(99, 67, 48), fontFamily = FontFamily(Font(R.font.poppins_regular))) }


            }

            Box(modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 0.dp))
            {
                Image(
                    painter = painterResource(id = R.drawable.folder1_icon),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(0.dp, 0.dp, 250.dp, 0.dp)
                        .align(alignment = Alignment.Center)
                        .size(65.dp)
                        .clip(RoundedCornerShape(16.dp)) // Adjust the corner radius as needed
                        .background(Color(246, 228, 217)) // Set the background color
                        .shadow(
                            50.dp,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(4.dp)

                )

                Image(
                    painter = painterResource(id = R.drawable.camera1_icon),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(0.dp, 0.dp, 0.dp, 10.dp)
                        .align(alignment = Alignment.Center)
                        .size(90.dp)
                        .clip(RoundedCornerShape(60.dp)) // Adjust the corner radius as needed
                        .background(Color(246, 228, 217)) // Set the background color
                        .shadow(
                            50.dp,
                            shape = RoundedCornerShape(40.dp)
                        )
                        .padding(4.dp)
                        .scale(1.5f),


                    contentScale = ContentScale.Crop
                )
                Image(
                    painter = painterResource(id = R.drawable.clock1_icon),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(250.dp, 0.dp, 0.dp, 0.dp)
                        .align(alignment = Alignment.Center)
                        .size(65.dp)
                        .clip(RoundedCornerShape(16.dp)) // Adjust the corner radius as needed
                        .background(Color(246, 228, 217)) // Set the background color
                        .shadow(
                            50.dp,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(4.dp)
                        .scale(1.3f)

                )
            }
        }



}

@Composable
fun InternetConnectionErrorWarning(padding: PaddingValues){
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(99, 67, 48))
            .padding(5.dp)
        ,
    ){
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .clip(RoundedCornerShape(15.dp, 15.dp, 15.dp, 15.dp))
                .background(Color(246, 228, 217))
        ){
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .align(Alignment.Center)
            ){
                Column(
                    modifier = Modifier.align(Alignment.Center)
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    Image(
                        painter = painterResource(id = R.drawable.no_internet_icon),
                        contentDescription = null,
                        modifier = Modifier
                            .scale(1.8f),
                    )
                    BasicText(
                        text = "Please check\nyour internet connection",
                        modifier = Modifier.padding(0.dp,50.dp),
                        TextStyle(
                            fontSize = 25.sp,
                            fontFamily = FontFamily(Font(R.font.inter_regular)),
                            color = Color(99, 67, 4),
                            textAlign = TextAlign.Center
                        ))
                }

            }

        }

    }
}

