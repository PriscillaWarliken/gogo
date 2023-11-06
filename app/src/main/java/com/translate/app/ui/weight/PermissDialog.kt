package com.translate.app.ui.weight

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.translate.app.R

@Composable
fun PermissDialog(dissmissBlock:()->Unit) {
    val context = LocalContext.current as Activity
    Dialog(onDismissRequest = {
        dissmissBlock.invoke()
    }, properties = DialogProperties(dismissOnClickOutside = true)) {
        Box(
            modifier = Modifier
                .width(305.dp)
                .height(260.dp)
        ){
            CoilImage(modifier = Modifier.fillMaxSize(), data = R.mipmap.bg_dialog)

            Column(modifier = Modifier
                .padding(top = 32.dp)
                .fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally)
            {

                Text(text = "Request permission", fontSize = 20.sp)
                
                Text(text = "The app really needs this permission to perform photo recognition. Please authorize!",
                    fontSize = 16.sp,
                    color = Color(0xFF666666),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(top = 20.dp, bottom = 11.dp)
                        .padding(horizontal = 27.dp))
            }

            Button(
                onClick = {
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    val uri = Uri.fromParts("package", context.packageName, null)
                    intent.data = uri
                    context.startActivity(intent)
                }, modifier = Modifier
                    .padding(bottom = 30.dp)
                    .align(Alignment.BottomCenter)
                    .size(245.dp, 50.dp),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6ACAFF))
            ) {
                Text(text = "Go Settings", fontSize = 18.sp, color = Color.White)
            }
        }
    }
}