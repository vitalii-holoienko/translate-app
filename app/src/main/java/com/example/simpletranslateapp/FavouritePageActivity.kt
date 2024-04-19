package com.example.simpletranslateapp

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.example.simpletranslateapp.ui.theme.SimpleTranslateAppTheme

class FavouritePageActivity : ComponentActivity() {
    private lateinit var viewModel: FavouritePageViewModel
    private lateinit var sharedPreferences : SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(FavouritePageViewModel::class.java)

        setContent {
            SimpleTranslateAppTheme {
                UI(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UI(viewModel: FavouritePageViewModel) {
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
fun Header(viewModel : FavouritePageViewModel){
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
fun MainContent(padding: PaddingValues, viewModel : FavouritePageViewModel){
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

    }
}

@Composable
fun Footer(viewModel : FavouritePageViewModel){

}

