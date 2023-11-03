package com.translate.app.ui.weight

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role

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