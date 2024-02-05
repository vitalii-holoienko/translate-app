package com.example.simpletranslateapp

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.ViewModelProvider
import com.example.simpletranslateapp.ui.theme.SimpleTranslateAppTheme

class ChooseLanguageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel: ChooseLanguageViewModel= ViewModelProvider(this)
            .get(ChooseLanguageViewModel::class.java)
        val from = intent.getStringExtra("from").orEmpty()
        val language = intent.getStringExtra(from)
        viewModel.from.value = from
        viewModel.previousLanguage.value = language

        setContent {
            SimpleTranslateAppTheme {
                // A surface container using the 'background' color from the theme
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
        bottomBar = {
            Footer(viewModel)
        },
        content = {padding->
            MainContent(padding, viewModel)
        }
    )
}

@Composable
fun Header(viewModel : ChooseLanguageViewModel){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(color = Color(99, 67, 48)),
        horizontalArrangement = Arrangement.SpaceBetween

    ){
        Image(
            painter = painterResource(id = R.drawable.cancel_arrow),
            contentDescription = null,
            modifier = Modifier
                .size(50.dp)
                .padding(6.dp, 5.dp, 0.dp, 6.dp)
                .scale(1.2f),
        )
        Image(
            painter = painterResource(id = R.drawable.search_icon),
            contentDescription = null,
            modifier = Modifier
                .size(50.dp)
                .padding(6.dp, 5.dp, 6.dp, 6.dp)
                .scale(1f),
        )
    }
}
@Composable
fun MainContent(padding: PaddingValues, viewModel : ChooseLanguageViewModel){
    var recentLanguagesCounter by remember {
        mutableStateOf(0)
    }
    val list = remember{Languages.languages.toList()}

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(246, 228, 217))
            .padding(padding))

    {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
        ) {
            list.forEach{
                languageBox(key = it.first, value = it.second, viewModel)
            }
        }


    }
}
@Composable
fun languageBox(key : String, value : String, viewModel: ChooseLanguageViewModel){ //where key is name of a language, and value is a language code
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clickable {
                val intent = Intent(context, MainActivity::class.java).also {
                    if(viewModel.from.value == "source") it.putExtra("sourceLanguage", key)
                    else                                 it.putExtra("targetLanguage", key)
                }
                context.startActivity(intent)
            },
        contentAlignment = Alignment.CenterStart
    ){
        Text(
            modifier = Modifier.padding(10.dp),
            text=key,
            fontFamily = FontFamily(Font(R.font.inter_regular)),
            color = Color(99, 67, 4),
            fontSize = 18.sp

        )
    }
}
@Composable
fun Footer(viewModel : ChooseLanguageViewModel){

}

