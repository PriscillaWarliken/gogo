package com.translate.app.ui.weight

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.placeholder.material.placeholder
import com.translate.app.R
import com.translate.app.ui.pointLog
import com.translate.app.ui.theme.grey1


@Composable
fun TranslateEditView(
    text: String,
    hintText: String,
    modifier: Modifier = Modifier,
    onNext: (String) -> Unit
) {
    var fullTag by remember { mutableStateOf(value = true) }
    var textContent by remember { mutableStateOf(
        value = TextFieldValue(text)
    ) }
    val clipboard: ClipboardManager? = LocalContext.current.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?

    Box(modifier = modifier){
        CommentEditView(
            textContent,
            hintText,
            modifier = Modifier,
            onValueChanged = {
                if (fullTag && it.text.isNotEmpty()) {
                    pointLog("Texthas_And","文本输入 有 数据曝光")
                    fullTag = false
                }
                textContent = it
            }
        ) {
            onNext(textContent.text)
        }

        if (textContent.text.isEmpty()) {
            Row(
                modifier = Modifier
                    .padding(bottom = 22.dp)
                    .padding(horizontal = 10.dp)
                    .fillMaxWidth()
                    .align(Alignment.BottomStart),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Box(
                    modifier = Modifier
                        .size(247.dp, 54.dp)
                        .background(color = Color(0x33000000), shape = RoundedCornerShape(12.dp))
                ) {
                    Row(modifier = Modifier.align(Alignment.Center)) {
                        CoilImage(modifier = Modifier.size(24.dp), data = R.drawable.home_translate)
                        Text(text = "Translate", fontSize = 18.sp, color = Color(0x996584C2))
                    }
                }

                CoilImage(
                    modifier = Modifier
                        .size(38.dp)
                        .click {
                            pointLog("paste_And", "文本输入无数据点击粘贴")
                            val clipData = clipboard!!.primaryClip
                            if (clipData != null && clipData.itemCount > 0) {
                                val text = clipData.getItemAt(0).text
                                if (text != null) {
                                    textContent = TextFieldValue(
                                        text = text.toString(),
                                        selection = TextRange(text.length)
                                    )
                                }
                            }
                        }, data = R.drawable.home_paste
                )
            }
        }
        else{
            CoilImage(modifier = Modifier
                .align(Alignment.TopEnd)
                .size(36.dp, 29.dp)
                .click {
                    textContent = TextFieldValue(text = "")
                }, data = R.drawable.home_eliminate)

            Box(
                modifier = Modifier
                    .click {
                        onNext(textContent.text)
                        pointLog("clicktranslate_And", "文本输入有数据点击翻译按钮")
                    }
                    .padding(bottom = 22.dp)
                    .align(Alignment.BottomCenter)
                    .size(295.dp, 54.dp)
                    .background(color = Color(0xFFFFFFFF), shape = RoundedCornerShape(12.dp))
            ) {
                Row(modifier = Modifier.align(Alignment.Center)) {
                    CoilImage(modifier = Modifier.size(24.dp), data = R.drawable.home_translate2)
                    Text(text = "Translate", fontSize = 18.sp, color = Color.Black)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun CommentEditView(
    text: TextFieldValue,
    hintText: String,
    modifier: Modifier = Modifier,
    onValueChanged: (TextFieldValue) -> Unit,
    onNext: () -> Unit
    ) {
    val keyboardService = LocalTextInputService.current
    val editFocusManager = LocalFocusManager.current


    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    TextField(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 70.dp)
            .focusRequester(focusRequester)
            .onFocusChanged {
                if (it.isFocused) {
                    keyboardController?.show()
                }
            },
        value = text,
        textStyle = TextStyle.Default.copy(fontSize = 22.sp),
        onValueChange = {
            onValueChanged.invoke(it)
        },
        placeholder = {
            TextContent(hintText,color = Color(0xFFBECAE1), fontSize = 22.sp)
        },
        colors = TextFieldDefaults.textFieldColors(
            textColor = Color.White,
            containerColor = Color.Transparent,
            cursorColor = Color.White,
            focusedIndicatorColor = Color.Transparent, // 有焦点时的颜色，透明
            unfocusedIndicatorColor = Color.Transparent, // 无焦点时的颜色，绿色
        ),
        trailingIcon = {
        },
        keyboardActions = KeyboardActions {
            editFocusManager.clearFocus()
            onNext.invoke()
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Send
        )
    )
}


@Composable
fun TextContent(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    fontSize:TextUnit = 16.sp,
    maxLines: Int = 99,
    textAlign: TextAlign = TextAlign.Start,
    isLoading: Boolean = false
) {
    Title(
        title = text,
        modifier = modifier,
        fontSize = fontSize,
        color = color,
        maxLine = maxLines,
        textAlign = textAlign,
        isLoading = isLoading
    )
}

@Composable
fun Title(
    title: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 16.sp,
    color: Color = Color.White,
    fontWeight: FontWeight = FontWeight.Bold,
    maxLine: Int = 1,
    textAlign: TextAlign = TextAlign.Start,
    isLoading: Boolean = false
) {
    Text(
        text = title,
        modifier = modifier
            .placeholder(
                visible = isLoading,
                color = grey1
            ),
        fontWeight = fontWeight,
        fontSize = fontSize,
        color = color,
        maxLines = maxLine,
        overflow = TextOverflow.Ellipsis,
        textAlign = textAlign
    )
}