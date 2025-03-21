package com.example.simpletranslateapp

import android.R.attr.label
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.room.util.query
import com.example.simpletranslateapp.ui.theme.SimpleTranslateAppTheme
import com.google.android.material.search.SearchBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking


class FavouritePageActivity : ComponentActivity() {
    private lateinit var viewModel: FavouritePageViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, FavouritePageViewModel.factory).get(FavouritePageViewModel::class.java)
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
        content = {padding->
            MainContent(padding, viewModel)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Header(viewModel : FavouritePageViewModel){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(Color(43, 40, 43)),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically

    ){
        val focusRequester = remember { FocusRequester() }
        val context = LocalContext.current
        val activity = context as? Activity

        Image(
            painter = painterResource(id = R.drawable.cancel_arrow),
            contentDescription = null,
            modifier = Modifier
                .size(47.dp)
                .padding(6.dp, 5.dp, 0.dp, 6.dp)
                .scale(1.1f)
                .clickable {
                    activity?.finish()
                }
        )


        val searchText = viewModel.searchText.collectAsState()
        val isSearching = viewModel.isSearching.collectAsState()

        if(isSearching.value){
            TextField(
                modifier = Modifier
                    .background(color = Color(43,40,43))
                    .focusRequester(focusRequester),
                value = searchText.value,
                onValueChange = {
                    viewModel.changeSearchText(it)
                },
                placeholder = { Text(text = "Search")},
                colors = TextFieldDefaults.
                textFieldColors(containerColor = Color(43,40,43),
                    focusedTextColor = Color(244,244,244),
                    unfocusedTextColor = Color(244,244,244),
                    unfocusedIndicatorColor = Color(53,50,53),
                    unfocusedPlaceholderColor = Color(123,120,123),
                    focusedIndicatorColor = Color(53,50,53)
                )
            )

            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }

            Image(
                painter = painterResource(id = R.drawable.cancel_cross_icon),
                contentDescription = null,
                modifier = Modifier
                    .size(50.dp)
                    .padding(6.dp, 5.dp, 6.dp, 6.dp)
                    .clickable {
                        viewModel.cancelTypingSearchQuery()
                    }
                    .scale(0.6f),
                )
            }else{
                Text(
                    text = stringResource(R.string.simpletranslate),
                    color = Color(224, 224, 224),
                    fontSize = 27.sp,
                    fontFamily = FontFamily(Font(R.font.salsa_regular)),
                    textAlign = TextAlign.Center,
                )

                Image(
                    painter = painterResource(id = R.drawable.search_icon),
                    contentDescription = null,
                    modifier = Modifier
                        .size(50.dp)
                        .padding(6.dp, 5.dp, 6.dp, 6.dp)
                        .scale(1f)
                        .clickable {
                            viewModel.startTypingSearchQuery()
                        }
                )
            }
    }
}

@Composable
fun MainContent(padding: PaddingValues, viewModel : FavouritePageViewModel){
    val filteredItemsFlow = viewModel.filteredItemsFlow.collectAsState(initial = emptyList())

    Box(
        modifier = Modifier
        .fillMaxSize()
        .background(Color(43, 40, 43))){
        Column (){
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(43, 40, 43))
                    .height(150.dp)
                    .padding(padding),

            ){
                Text(
                    modifier = Modifier.padding(10.dp,50.dp,0.dp,6.dp),
                    text = "Saved translations",
                    fontFamily = FontFamily(Font(R.font.inter_regular)),
                    color = Color(224, 224, 224),
                    fontSize = 28.sp,

                    )
            }
            Box()
            {
                LazyColumn(){
                    items(filteredItemsFlow.value){
                        SavedString(it, viewModel)
                    }
                }

            }
        }
    }
}
@Composable
fun SavedString(savedString: SavedString, viewModel: FavouritePageViewModel){
    val context = LocalContext.current
    Divider(
        color = Color(53, 50, 53)
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(65.dp)
            .clickable {
                val intent = Intent(context, MainActivity::class.java).also {
                    it.putExtra("favouriteSourceText", savedString.sourceText)
                    it.putExtra("sourceLanguage", savedString.sourceLanguage)
                    it.putExtra("targetLanguage", savedString.targetLanguage)

                }
                context.startActivity(intent)
            },
        contentAlignment = Alignment.CenterStart
    ){
        Row(
            verticalAlignment = Alignment.CenterVertically
        ){
            Column(){
                Text(
                    modifier = Modifier.padding(10.dp,3.dp, 6.dp, 0.dp),
                    text=Tools.TruncateTextIfNeeded(savedString.sourceText),
                    fontFamily = FontFamily(Font(R.font.inter_regular)),
                    color = Color(224, 224, 224),
                    fontSize = 18.sp
                )
                Text(
                    modifier = Modifier.padding(10.dp,0.dp, 6.dp, 6.dp),
                    text=Tools.TruncateTextIfNeeded(savedString.translatedText),
                    fontFamily = FontFamily(Font(R.font.inter_regular)),
                    color = Color(124, 124, 124),
                    fontSize = 18.sp
                )
            }
            Box(
                modifier= Modifier
                    .fillMaxSize()
                    .padding(10.dp)
            ){
                Image(
                    painter = painterResource(id = R.drawable.add_to_favourite_icon_filled),
                    contentDescription = null,
                    modifier = Modifier
                        .size(30.dp)
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



