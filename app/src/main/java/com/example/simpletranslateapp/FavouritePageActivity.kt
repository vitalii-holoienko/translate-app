package com.example.simpletranslateapp

import android.content.Context
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.modifier.modifierLocalConsumer
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

class FavouritePageActivity : ComponentActivity() {
    private lateinit var viewModel: FavouritePageViewModel
    private lateinit var sharedPreferences : SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, FavouritePageViewModel.factory).get(FavouritePageViewModel::class.java)
        sharedPreferences = getSharedPreferences("favourite_preferences", Context.MODE_PRIVATE)

        setContent {
            SimpleTranslateAppTheme {
                UI(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UI(viewModel: FavouritePageViewModel){
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
            .background(Color(43, 40, 43)),
        horizontalArrangement = Arrangement.SpaceBetween

    ){
        Box(modifier = Modifier.fillMaxWidth()){
            Image(
                painter = painterResource(id = R.drawable.cancel_arrow),
                contentDescription = null,
                modifier = Modifier
                    .size(50.dp)
                    .padding(6.dp, 5.dp, 0.dp, 6.dp)
                    .align(alignment = Alignment.CenterStart)
                    .scale(1.1f),
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
@Composable
fun MainContent(padding: PaddingValues, viewModel : FavouritePageViewModel){
    val savedStrings =  viewModel.allSavedStrings.collectAsState(initial = emptyList())
    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color(43, 40, 43))){
        Column (){
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(43, 40, 43))
                    .height(150.dp)
                    .padding(padding)

            ){
                Text(
                    modifier = Modifier.padding(6.dp,50.dp,0.dp,6.dp),
                    text = "Saved translations",
                    fontFamily = FontFamily(Font(R.font.inter_regular)),
                    color = Color(224, 224, 224),
                    fontSize = 28.sp,

                    )
            }
            Box()
            {
                LazyColumn(){
                    items(savedStrings.value){
                        SavedString(it, viewModel)
                    }
                }
            }
        }
    }
}
@Composable
fun SavedString(savedString: SavedString, viewModel: FavouritePageViewModel){
    Divider(
        color = Color(53, 50, 53)
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(65.dp)
            .clickable {},
        contentAlignment = Alignment.CenterStart
    ){
        Row(){
            Column(){
                Text(
                    modifier = Modifier.padding(6.dp,3.dp, 6.dp, 0.dp),
                    text=savedString.sourceText,
                    fontFamily = FontFamily(Font(R.font.inter_regular)),
                    color = Color(224, 224, 224),
                    fontSize = 18.sp
                )
                Text(
                    modifier = Modifier.padding(6.dp,0.dp, 6.dp, 6.dp),
                    text=savedString.translatedText,
                    fontFamily = FontFamily(Font(R.font.inter_regular)),
                    color = Color(124, 124, 124),
                    fontSize = 18.sp
                )
            }
            Box(
                modifier=Modifier.fillMaxSize().padding(6.dp)
            ){
                Image(
                    painter = painterResource(id = R.drawable.add_to_favourite_icon3),
                    contentDescription = null,
                    modifier = Modifier
                        .size(35.dp)
                        .scale(0.8f)
                        .align(Alignment.CenterEnd)
                        .clickable {
                            viewModel.deleteSavedString(savedString)
                        }
                )
            }

        }


    }
    Divider(
        color = Color(53, 50, 53)
    )

}
@Composable
fun Footer(viewModel : FavouritePageViewModel){

}


