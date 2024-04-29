@file:OptIn(DelicateCoroutinesApi::class)

package com.example.simpletranslateapp


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import com.example.simpletranslateapp.ui.theme.SimpleTranslateAppTheme
import kotlinx.coroutines.DelicateCoroutinesApi


@OptIn(DelicateCoroutinesApi::class)
class MainActivity : ComponentActivity() {
    private lateinit var viewModel: MainViewModel
    private lateinit var mainSharedPreferences : SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this, MainViewModel.factory).get(MainViewModel::class.java)
        viewModel.startConnectivityObserve(this)

        mainSharedPreferences = getSharedPreferences("main_preferences", Context.MODE_PRIVATE)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        viewModel.getActivityContext(this)

        //get info from shared preferences
        val sourceLanguageFromIntent = intent.getStringExtra("sourceLanguage")
        val targetLanguageFromIntent = intent.getStringExtra("targetLanguage")

        val sourceLanguageFromPrefs = mainSharedPreferences.getString("sourceLanguage", "").orEmpty()
        val targetLanguageFromPrefs = mainSharedPreferences.getString("targetLanguage", "").orEmpty()

        if(sourceLanguageFromPrefs != "") viewModel.changeSourceLanguage(sourceLanguageFromPrefs)
        if(targetLanguageFromPrefs != "") viewModel.changeTargetLanguage(targetLanguageFromPrefs)

        if(sourceLanguageFromIntent!=null) viewModel.changeSourceLanguage(sourceLanguageFromIntent)
        if(targetLanguageFromIntent!=null) viewModel.changeTargetLanguage(targetLanguageFromIntent)

        setContent {
            SimpleTranslateAppTheme {
                UI(viewModel)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.saveDataToPrefs(mainSharedPreferences)
    }
}



@DelicateCoroutinesApi
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
            .background(Color(43, 40, 43))
            .height(70.dp)
            .padding(0.dp, 25.dp, 0.dp, 0.dp)

    ){
        val context = LocalContext.current;
        Image(
            painter = painterResource(id = R.drawable.new_star_icon),
            contentDescription = null,
            modifier = Modifier
                .size(50.dp)
                .align(alignment = Alignment.CenterStart)
                .padding(6.dp, 7.dp, 0.dp, 6.dp)
                .scale(0.90f)
                .clickable {
                    val intent = Intent(context, FavouritePageActivity::class.java)
                    context.startActivity(intent)
                },
            contentScale = ContentScale.Crop
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
            painter = painterResource(id = R.drawable.cup_icon),
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
            .background(Color(43, 40, 43))
            .padding(5.dp),
    ){
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .clip(RoundedCornerShape(15.dp, 15.dp, 15.dp, 15.dp))
                .background(Color(53, 50, 53))
                .verticalScroll(rememberScrollState())
        ){
            var textSize by remember {
                mutableStateOf(30)
            }

            var translatedText by remember {
                mutableStateOf("")
            }

            var inputText by remember{
                mutableStateOf("")
            }

            textSize = mainViewModel.inputTextSize.observeAsState(30).value;
            inputText = mainViewModel.inputText.observeAsState("").value
            Column(
                modifier = Modifier
                    .padding(10.dp)

            ) {
                val enabled by remember { mutableStateOf(true) }
                val interactionSource = remember { MutableInteractionSource() }
                val localClipboardManager = LocalClipboardManager.current
                val focusManager = LocalFocusManager.current
                val keyboardController = LocalSoftwareKeyboardController.current
                val context = LocalContext.current

                BasicTextField(
                    value = inputText,
                    onValueChange = {
                        mainViewModel.processingInput(it, context)
                    },
                    interactionSource = interactionSource,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .animateContentSize()
                        .padding(0.dp, 12.5f.dp, 0.dp, 12.5f.dp),

                    decorationBox = { innerTextField ->
                        TextFieldDefaults.OutlinedTextFieldDecorationBox(
                            value = inputText,
                            innerTextField = innerTextField,
                            enabled = enabled,
                            singleLine = false,
                            contentPadding = PaddingValues(0.dp),
                            visualTransformation = VisualTransformation.None,
                            interactionSource = interactionSource,
                            placeholder = { Text(
                                text = "Enter text",
                                fontSize = textSize.sp,
                                fontFamily = FontFamily(Font(R.font.inter_regular)),
                                color = Color(224, 224, 224),
                            ) },
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
                        color = Color(244, 241, 242)
                    ) ,

                )
                translatedText = mainViewModel.translatedText.observeAsState("").value

                if(translatedText.length > 0){
                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ){
                        Divider(
                            modifier = Modifier
                                .width(180.dp)
                                .padding(0.dp, 0.dp, 0.dp, 12.5f.dp)
                                .align(Alignment.Center),
                            color = Color(43,40,43)
                        )
                    }
                }

                    BasicText(
                        text = translatedText,
                        modifier = Modifier
                            .wrapContentHeight()
                            .animateContentSize()
                            .padding(0.dp, 0.dp, 0.dp, 0.dp),
                        TextStyle(
                            fontSize = textSize.sp,
                            fontFamily = FontFamily(Font(R.font.inter_regular)),
                            color = Color(244, 241, 242)))



                if(translatedText.length > 0){
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(0.dp, 10.dp, 0.dp, 0.dp)
                            .height(35.dp),
                        horizontalArrangement = Arrangement.End
                    ){
                        Image(
                            painter = painterResource(id = R.drawable.copy_text),
                            contentDescription = null,
                            modifier = Modifier
                                .size(50.dp)
                                .scale(0.8f)
                                .clickable {
                                    localClipboardManager.setText(AnnotatedString(translatedText))
                                    Toast
                                        .makeText(context, "Translation copied", Toast.LENGTH_LONG)
                                        .show()
                                }
                        )
                        val inFavourite = mainViewModel.stringInFavourite.observeAsState().value

                        if(!inFavourite!!){
                            Image(
                                painter = painterResource(id = R.drawable.add_to_favourite_icon),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(50.dp)
                                    .scale(0.8f)
                                    .clickable {
                                        mainViewModel.upsertSavedString()
                                    }
                            )
                        }else{
                            Image(
                                painter = painterResource(id = R.drawable.add_to_favourite_icon_filled),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(50.dp)
                                    .scale(0.8f)
                                    .clickable {
                                        mainViewModel.deleteSavedString()
                                    }
                            )
                        }

                    }
                }
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
            .background(Color(43, 40, 43)),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        val context = LocalContext.current;
        var sourceLanguage by remember{
            mutableStateOf("")
        }
        var targetLanguage by remember{
            mutableStateOf("")
        }
        sourceLanguage = mainViewModel.sourceLanguage.observeAsState("").value
        targetLanguage = mainViewModel.targetLanguage.observeAsState("").value
        Row(
            modifier = Modifier.padding(0.dp,10.dp,0.dp,2.dp)
        ){
            Box(
                modifier = Modifier
                    .width(130.dp)
                    .height(50.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(53, 50, 53))
                    .clickable {
                        val intent = Intent(context, ChooseLanguageActivity::class.java).also {
                            it.putExtra("source", mainViewModel.sourceLanguage.value)
                            it.putExtra("from", "source")
                        }
                        context.startActivity(intent)
                    },
                contentAlignment = Alignment.Center
                ){
                    Text(
                        text = sourceLanguage,
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
                            .clickable(indication = null, onClick = {
                                mainViewModel.swapSourceAndTargetLanguages()
                            }, interactionSource = interactionSource)

                    )

                    Box(
                        modifier = Modifier
                            .width(130.dp)
                            .height(50.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(53, 50, 53))
                            .clickable {
                                val intent =
                                    Intent(context, ChooseLanguageActivity::class.java).also {
                                        it.putExtra("target", mainViewModel.targetLanguage.value)
                                        it.putExtra("from", "target")
                                    }
                                context.startActivity(intent)
                            },
                        contentAlignment = Alignment.Center
                    ){ mainViewModel.targetLanguage.value?.let { Text(
                        text = it,
                        color = Color(224, 224, 224),
                        fontFamily = FontFamily(Font(R.font.poppins_regular)),
                        textAlign = TextAlign.Center,
                        fontSize = 15.sp,
                    ) } }
            }

            Box(modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 0.dp))
            {
                Image(
                    painter = painterResource(id = R.drawable.folder), //163 3x
                    contentDescription = null,
                    modifier = Modifier
                        .padding(0.dp, 0.dp, 250.dp, 0.dp)
                        .align(alignment = Alignment.Center)
                        .size(65.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(53, 50, 53))
                        .shadow(
                            50.dp,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .scale(0.8f)
                        .padding(4.dp)

                )

                Image(
                    painter = painterResource(id = R.drawable.camera_icon),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(0.dp, 0.dp, 0.dp, 10.dp)
                        .align(alignment = Alignment.Center)
                        .size(90.dp)
                        .clip(RoundedCornerShape(60.dp))
                        .background(Color(53, 50, 53))
                        .shadow(
                            50.dp,
                            shape = RoundedCornerShape(40.dp)
                        )
                        .padding(4.dp)
                        .scale(0.7f),


//                    contentScale = ContentScale.Crop
                )
                Image(
                    painter = painterResource(id = R.drawable.clock_icon),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(250.dp, 0.dp, 0.dp, 0.dp)
                        .align(alignment = Alignment.Center)
                        .size(65.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(53, 50, 53))
                        .shadow(
                            50.dp,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(4.dp)
                        .scale(0.83f)
                )
            }
        }



}

@Composable
fun InternetConnectionErrorWarning(padding: PaddingValues){
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(43, 40, 43))
            .padding(5.dp),
    ){
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .clip(RoundedCornerShape(15.dp, 15.dp, 15.dp, 15.dp))
                .background(Color(53, 50, 53))
                .padding(5.dp)
        ){
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .align(Alignment.Center)
            ){
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(0.dp,50.dp,0.dp,0.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    Image(
                        painter = painterResource(id = R.drawable.no_internet_warning2),
                        contentDescription = null,
                        modifier = Modifier
                            .scale(1.4f),
                    )
                    BasicText(
                        text = "Please check\nyour internet connection",
                        modifier = Modifier.padding(0.dp,70.dp),
                        TextStyle(
                            fontSize = 25.sp,
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            color = Color(224, 224, 224),
                            textAlign = TextAlign.Center
                        ))
                }

            }

        }

    }
}

