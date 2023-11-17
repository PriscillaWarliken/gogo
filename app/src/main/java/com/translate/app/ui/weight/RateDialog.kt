package com.translate.app.ui.weight

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.translate.app.R

@Composable
fun RateDialog(dissBlock:()->Unit) {
    val context = LocalContext.current as Activity
    Dialog(onDismissRequest = {
        dissBlock.invoke()
    }, properties = DialogProperties(dismissOnClickOutside = true)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .width(305.dp)
                    .height(180.dp)
                    .clip(shape = RoundedCornerShape(20.dp))
            ){
                CoilImage(modifier = Modifier.fillMaxSize(), data = R.mipmap.bg_dialog)

                Column(modifier = Modifier
                    .padding(top = 32.dp)
                    .fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally)
                {

                    Text(
                        text = "Enjoying our app? Leave us a review!",
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )

                    RateWeight(modifier = Modifier.padding(top = 15.dp)){
                        if (it >= 3) {
                            val googlePlayUrl = "https://play.google.com/store/apps/details?id=${context.packageName}"
                            context.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(googlePlayUrl)
                                )
                            )
                        }
                        dissBlock.invoke()
                    }
                }


            }
            CoilImage(modifier = Modifier
                .click {
                    dissBlock.invoke()
                }
                .padding(top = 15.dp)
                .size(36.dp),
                data = R.mipmap.close)
        }
    }
}