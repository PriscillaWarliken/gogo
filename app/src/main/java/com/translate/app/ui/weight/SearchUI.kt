package com.translate.app.ui.weight

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.RecomposeScope
import androidx.compose.runtime.currentRecomposeScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.translate.app.R
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchEdit(
    searchContent:String="",
    modifier:Modifier,
    onSearch: (String) -> Unit,
) {

    var searchContent by remember { mutableStateOf(searchContent) }

    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        SearchEditView(
            searchContent,
            "Search...",
            Modifier.fillMaxWidth(),
            onValueChanged = {
                searchContent = it
                onSearch.invoke(searchContent)
            },
            onDeleteClick = {
                searchContent = ""
                onSearch.invoke("")
            },
            onSearch = {
                onSearch(searchContent)
            }
        )
    }
}


@OptIn(ExperimentalComposeUiApi::class)
@ExperimentalMaterial3Api
@Composable
fun SearchEditView(
    text: String,
    hintText: String,
    modifier: Modifier = Modifier,
    onValueChanged: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onSearch: () -> Unit,

    ) {
    val keyboardService = LocalTextInputService.current
    val editFocusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val focusState = remember {
        mutableStateOf(value = false)
    }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        delay(300)
        focusRequester.requestFocus()
    }

    TextField(
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .onFocusChanged {
                if (it.isFocused) {
                    focusState.value = true
                    keyboardController?.show()
                }else{
                    focusState.value = false
                }
            }
            .padding(vertical = 3.dp, horizontal = 12.dp),
        value = text,
        textStyle = TextStyle.Default.copy(fontSize = 16.sp),
        onValueChange = { onValueChanged.invoke(it) },
        placeholder = {
            TextContent(hintText,color = Color(0x4DFFFFFF))
        },
        colors = TextFieldDefaults.textFieldColors(
            cursorColor = Color.White,
            textColor = Color.White,
            containerColor = Color(0xFF58B9EE),
            focusedIndicatorColor = Color.White, // 有焦点时的颜色，透明
            unfocusedIndicatorColor = Color.White, // 无焦点时的颜色，绿色
        ),
        leadingIcon = if (focusState.value.not()) {
            @Composable{
                CoilImage(modifier = Modifier.size(20.dp), data = R.mipmap.search)
            }
        }else{
            null
        },
        trailingIcon =
        if (focusState.value) {
            @Composable{
                Box(
                    modifier = Modifier
                        .padding(end = 5.dp)
                        .size(64.dp, 40.dp)
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 36.dp,
                                bottomStart = 16.dp,
                                bottomEnd = 36.dp
                            )
                        )
                        .click {
                            onDeleteClick()
                        }
                ) {
                    Text(text = "Cancel", modifier = Modifier.align(Alignment.Center))
                }
            }
        }else{
            null
        }
        ,
        shape = RoundedCornerShape(36.dp),
        keyboardActions = KeyboardActions {
            editFocusManager.clearFocus()
            onSearch.invoke()
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Search
        )
    )
}

