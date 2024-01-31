package com.example.simpletranslateapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.example.simpletranslateapp.ui.theme.SimpleTranslateAppTheme

class ChooseLanguageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel: ChooseLanguageViewModel= ViewModelProvider(this)
            .get(ChooseLanguageViewModel::class.java)

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
//        Spacer(modifier = Modifier.width(250.dp))
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(246, 228, 217))
            .padding(padding))

}
@Composable
fun Footer(viewModel : ChooseLanguageViewModel){

}

