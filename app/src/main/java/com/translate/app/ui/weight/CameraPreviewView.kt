package com.translate.app.ui.weight

import android.annotation.SuppressLint
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.translate.app.R

@Composable
fun CoilImage(
    modifier: Modifier,
    data: Any,
    contentScale: ContentScale = ContentScale.Crop,
    filterQuality: FilterQuality = FilterQuality.High,
    backgroundColor: Color = Color.Gray.copy(alpha = 0f)
) {
    AsyncImage(
        modifier = modifier
            .background(color = backgroundColor),
        model = data,
        contentScale = contentScale,
        filterQuality = filterQuality,
        contentDescription = null
    )
}

class TriangleShape() : Shape {

    private val path = Path()

    override fun createOutline(
        size: Size, layoutDirection: LayoutDirection, density: Density
    ): Outline {
        path.reset()
        val width = size.width
        val height = size.height
        path.moveTo(0f,height)
        path.lineTo(width / 2, 0f)
        path.lineTo(width, height)
        path.close()
        return Outline.Generic(path = path)
    }

    override fun toString(): String = "BezierShape"

}

@SuppressLint("RestrictedApi")
@Composable
fun PreViewMainLayout(
    lensFacing:Int = CameraSelector.LENS_FACING_BACK,
    takePhotoClick: (imageCapture: ImageCapture) -> Unit,
    switchFacing:()->Unit,
    openAlbum:()->Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture by lazy { ProcessCameraProvider.getInstance(context) }
    val cameraProvider by lazy { cameraProviderFuture.get() }
    val preview = Preview.Builder().build()
    val previewView = remember { PreviewView(context) }
    val cameraSelector = CameraSelector.Builder()
        .requireLensFacing(lensFacing)
        .build()

    val imageCapture = remember {
        ImageCapture.Builder().build()
    } //实例化拍照ImageCapture，

    LaunchedEffect(lensFacing) { //compose中使用协程进行camera绑定生命周期
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageCapture
        )
        preview.setSurfaceProvider(previewView.surfaceProvider)
    }


    Column(modifier = Modifier.fillMaxWidth()) {
        AndroidView( //由于PreviewView是Android View非compose，所以需要AndroidView
            { previewView },
            modifier = Modifier
                .fillMaxWidth()
                .height(460.dp)
                .clip(shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(color = Color(0xFF315177)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            CoilImage(modifier = Modifier
                .size(36.dp)
                .click {
                    openAlbum.invoke()
                }, data = R.drawable.photo)
            CoilImage(modifier = Modifier
                .size(70.dp)
                .click {
                    takePhotoClick.invoke(imageCapture)
                }, data = R.drawable.photograph)
            CoilImage(modifier = Modifier
                .size(24.dp)
                .click {
                    switchFacing.invoke()
                }, data = R.drawable.transform)
        }
    }
}
