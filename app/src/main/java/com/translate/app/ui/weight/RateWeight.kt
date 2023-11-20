package com.translate.app.ui.weight

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import com.translate.app.R

@Composable
fun RateWeight(modifier:Modifier,block:(Int)->Unit={}) {
    var selectedRateIndex by remember {
        mutableStateOf(value = -1)
    }
    val animateValue by rememberInfiniteTransition(label = "").animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ), label = ""
    )
    Row(modifier = modifier) {
        var thenModifier= Modifier.scale(1f)
        for (i in 0 until 5) {
            if (i == 4) {
                thenModifier = Modifier.scale(animateValue)
            }
            CoilImage(
                modifier = Modifier
                    .padding(horizontal = 5.dp)
                    .size(44.dp)
                    .then(thenModifier)
                    .click {
                        selectedRateIndex = i
                        block.invoke(i)
                    },
                data = if (selectedRateIndex < i) {
                    R.drawable.rate_2
                } else {
                    R.drawable.rate_1
                }
            )
        }

    }
}