package com.example.simpletranslateapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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

        val viewModel: ChooseLanguageViewModel = ViewModelProvider(this).get(ChooseLanguageViewModel::class.java)
        val from = intent.getStringExtra("from").orEmpty()
        viewModel.camera.value = intent.getBooleanExtra("camera", false)
        viewModel.from.value = from
        viewModel.savedInputString.value = intent.getStringExtra("input") ?: ""

        setContent {
            SimpleTranslateAppTheme {
                UI(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UI(viewModel: ChooseLanguageViewModel) {
    Scaffold(
        topBar = {
            Header(viewModel)
        },
        content = { padding ->
            MainContent(padding, viewModel)
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Header(viewModel: ChooseLanguageViewModel) {
    val context = LocalContext.current
    val activity = context as? Activity
    val searchText by viewModel.searchText.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val focusRequester = remember { FocusRequester() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(Color(43, 40, 43)),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
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

        if (isSearching) {
            TextField(
                modifier = Modifier.background(Color(43, 40, 43)).focusRequester(focusRequester),
                value = searchText,
                onValueChange = { viewModel.changeSearchText(it) },
                placeholder = { Text(text = "Search") },
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color(43, 40, 43),
                    focusedTextColor = Color(244, 244, 244),
                    unfocusedTextColor = Color(244, 244, 244),
                    unfocusedIndicatorColor = Color(53, 50, 53),
                    unfocusedPlaceholderColor = Color(123, 120, 123),
                    focusedIndicatorColor = Color(53, 50, 53)
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
                    .clickable { viewModel.cancelTypingSearchQuery() }
                    .scale(0.6f),
            )
        } else {
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
                    .clickable { viewModel.startTypingSearchQuery() }
            )
        }
    }
}

@Composable
fun MainContent(padding: PaddingValues, viewModel: ChooseLanguageViewModel) {
    val list = remember { Languages.languages.toList() }
    val searchText by viewModel.searchText.collectAsState()
    val filteredList = list.filter { it.first.contains(searchText, ignoreCase = true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(43, 40, 43))
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .padding(padding)
        ) {
            Text(
                modifier = Modifier.padding(6.dp, 50.dp, 0.dp, 6.dp),
                text = if (viewModel.from.value == "source") "Choose source language:" else "Choose target language:",
                fontFamily = FontFamily(Font(R.font.inter_regular)),
                color = Color(224, 224, 224),
                fontSize = 26.sp,
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth().background(Color(43, 40, 43))
        ) {
            filteredList.forEach {
                languageBox(it.first, it.second, viewModel)
            }
        }
    }
}

@Composable
fun languageBox(key: String, value: String, viewModel: ChooseLanguageViewModel) {
    val context = LocalContext.current
    Divider(color = Color(53, 50, 53))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(Color(43, 40, 43))
            .clickable {
                val intent = Intent(context, if (viewModel.camera.value == true) CameraScreenActivity::class.java else MainActivity::class.java).apply {
                    putExtra(if (viewModel.from.value == "source") "sourceLanguage" else "targetLanguage", key)
                    putExtra("input", viewModel.savedInputString.value)
                }
                context.startActivity(intent)
            },
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            modifier = Modifier.padding(10.dp),
            text = key,
            fontFamily = FontFamily(Font(R.font.inter_regular)),
            color = Color(224, 224, 224),
            fontSize = 18.sp
        )
    }
    Divider(color = Color(53, 50, 53))
}



