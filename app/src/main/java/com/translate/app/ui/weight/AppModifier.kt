package com.translate.app.ui.weight

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import com.translate.app.Const
import com.translate.app.repository.Repository
import com.translate.app.repository.bean.LanguageBeanItem
import java.util.Objects

/**
 * View的click方法的两次点击间隔时间
 */
const val VIEW_CLICK_INTERVAL_TIME = 800

/**
 * 防止重复点击(有的人可能会手抖连点两次,造成奇怪的bug)
 */
@Composable
inline fun Modifier.click(
    time: Int = VIEW_CLICK_INTERVAL_TIME,
    crossinline onClick: () -> Unit
): Modifier {
    var lastClickTime = remember { 0L }//使用remember函数记录上次点击的时间
    return clickable(
        onClick = {
            val currentTimeMillis = System.currentTimeMillis()
            if (currentTimeMillis - time >= lastClickTime) {//判断点击间隔,如果在间隔内则不回调
                onClick()
                lastClickTime = currentTimeMillis
            }
        },
        // 去除点击效果
        indication = null,
        interactionSource = remember {
            MutableInteractionSource()
        }
    )
}

fun Any.saveSP(tag: String) {
    val json = Gson().toJson(this)
    Repository.sharedPreferences.edit {
        putString(tag,json)
    }
}

inline fun<reified T> String.getSpByTag(): LanguageBeanItem? {
    Repository.sharedPreferences.getString(this,"")?.let {
        return Gson().fromJson<LanguageBeanItem>(it, T::class.java)
    }
    return null
}