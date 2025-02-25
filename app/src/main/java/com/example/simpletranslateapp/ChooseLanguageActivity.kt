package com.example.simpletranslateapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.lifecycle.ViewModelProvider
import com.example.simpletranslateapp.ui.theme.SimpleTranslateAppTheme

class ChooseLanguageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel: ChooseLanguageViewModel= ViewModelProvider(this).get(ChooseLanguageViewModel::class.java)

        val from = intent.getStringExtra("from").orEmpty()

        var inputText = ""
        if(intent.getStringExtra("input") != null){
            inputText = intent.getStringExtra("input")!!
        }
        viewModel.camera.value = intent.getBooleanExtra("camera", false)

        viewModel.from.value = from

        viewModel.savedInputString.value = inputText


        setContent {
            SimpleTranslateAppTheme {
                UI(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UI(viewModel: ChooseLanguageViewModel){
    Scaffold(
        topBar = {
            Header(viewModel)
        },
        content = {padding->
            MainContent(padding, viewModel)
        },
    )
}

@Composable
fun Header(viewModel : ChooseLanguageViewModel){
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
fun MainContent(padding: PaddingValues, viewModel : ChooseLanguageViewModel){
    val list = remember{Languages.languages.toList()}
    var text = ""

    if (viewModel.from.value == "source") text = "Choose source language:"
    else                                  text = "Choose target language:"

    Column (modifier = Modifier.verticalScroll(rememberScrollState())){
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(43, 40, 43))
                .height(150.dp)
                .padding(padding)

        ){
            Text(
                modifier = Modifier.padding(6.dp,50.dp,0.dp,6.dp),
                text = text ,
                fontFamily = FontFamily(Font(R.font.inter_regular)),
                color = Color(224, 224, 224),
                fontSize = 26.sp,

            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(43, 40, 43))
                .padding())

        {
            Column(

            ) {
                var i = 0;
                list.forEach{
                    if(viewModel.from.value == "target" &&(i == 0))
                        else languageBox(key = it.first, value = it.second, viewModel)
                    i++;

                }
            }


        }
    }

}
@Composable
fun languageBox(key : String, value : String, viewModel: ChooseLanguageViewModel){ //where key is name of a language, and value is a language code
    val context = LocalContext.current
    Divider(
        color = Color(53, 50, 53)
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clickable {
                if(viewModel.camera.value == false){
                    val intent = Intent(context, MainActivity::class.java).also {
                        if (viewModel.from.value == "source") it.putExtra("sourceLanguage", key)
                        else                                  it.putExtra("targetLanguage", key)

                        it.putExtra("input", viewModel.savedInputString.value)
                    }
                    context.startActivity(intent)
                }else{
                    val intent = Intent(context, CameraScreenActivity::class.java).also {
                        Log.d("TEKKEN", "FROM CLA")
                        if (viewModel.from.value == "source") it.putExtra("choseSourceLanguage", key)
                        else                                  it.putExtra("choseTargetLanguage", key)
                    }
                    context.startActivity(intent)
                }

            },
        contentAlignment = Alignment.CenterStart
    ){
        Text(
            modifier = Modifier.padding(10.dp),
            text=key,
            fontFamily = FontFamily(Font(R.font.inter_regular)),
            color = Color(224, 224, 224),
            fontSize = 18.sp
        )
    }

}

