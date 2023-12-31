package com.translate.app.ui.ocrPage

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import android.os.Bundle
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.google.android.gms.ads.nativead.NativeAd
import com.google.gson.JsonArray
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.translate.app.App
import com.translate.app.Const
import com.translate.app.ads.AdManager
import com.translate.app.ads.base.AdWrapper
import com.translate.app.ads.callback.IntAdCallback
import com.translate.app.ads.callback.NavAdCallback
import com.translate.app.repository.Repository
import com.translate.app.repository.ServiceCreator
import com.translate.app.repository.bean.Data
import com.translate.app.ui.BaseActivity
import com.translate.app.ui.pointLog
import com.translate.app.ui.weight.CoilImage
import com.translate.app.ui.weight.NativeAdsView
import com.translate.app.ui.weight.SmallNavView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.math.sqrt


class OCRActivity : BaseActivity(),IntAdCallback,NavAdCallback {

    companion object{
        var resultBitmap: Bitmap? = null
        var resultStr:String = ""

        fun clearResultStr() {
            resultStr = ""
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        resultBitmap = null
        var path = intent.getStringExtra("PATH") ?: return

        pointLog("Cameraanimation_And","照片翻译动效曝光（相机的和相册的是同1个）")

        if (path.contains("jpg").not()) {
            path = "content://media${path}"
        }
        setContent {
            BackHandler (enabled = true){}
            Box(modifier = Modifier.fillMaxSize()){
                PreViewImageLayout(
                    path, modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .fillMaxHeight(0.73f)
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                        ),
                    )

                val animateValue by rememberInfiniteTransition(label = "").animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 1000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ), label = ""
                )
                Box(modifier = Modifier
                    .padding(bottom = 50.dp)
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(0.9f)
                    .height(15.dp)
                    .background(
                        color = Color(0xFFEEEFEF),
                        shape = RoundedCornerShape(90.dp)
                    )
                ){
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth(animateValue)
                            .fillMaxHeight()
                            .background(
                                color = Color(0xFF6ACAFF),
                                shape = RoundedCornerShape(90.dp)
                            )
                    )
                }

                val composition by rememberLottieComposition(LottieCompositionSpec.Asset("anim/扫描.json"))
                LottieAnimation(
                    composition = composition,
                    iterations = LottieConstants.IterateForever,
                    modifier = Modifier
                        .padding(bottom = 100.dp)
                        .align(Alignment.BottomCenter),
                    contentScale = ContentScale.None
                )

                if (adWrapper.value == null) {
                    SmallNavView(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 50.dp)
                            .padding(horizontal = 20.dp)
                    )
                }else{
                    NativeAdsView(
                        isBig = false, mAdInstance = adWrapper.value!!, modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 50.dp)
                            .padding(horizontal = 20.dp)
                    )
                }

            }
        }
        startRecognizer(path)
    }
    private fun startRecognizer(path: String) {
        clearResultStr()
        val startTime = System.currentTimeMillis()
        val recognizer = when (Repository.sourceLanguage!!.language) {
            "zh-CN","zh-TW" -> {
                TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
            }
            "hi"->{
                TextRecognition.getClient(DevanagariTextRecognizerOptions.Builder().build())
            }
            "ja"->{
                TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())
            }
            "ko"->{
                TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
            }else->{
                TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            }
        }
        val image: InputImage
        try {
            val bitmap:Bitmap = if (path.contains("jpg").not()) {
                val inputStream = contentResolver.openInputStream(Uri.parse(path))
                BitmapFactory.decodeStream(inputStream)
            }else{
                BitmapFactory.decodeFile(path)
            }

            val bmp = bitmap.copy(Bitmap.Config.ARGB_8888, true)
            val canvas = Canvas(bmp)
            image = InputImage.fromBitmap(bitmap, 0)
            //将图片传递给 process 方法
            recognizer.process(image)
                .addOnSuccessListener { result ->
                    // Task completed successfully
                    lifecycleScope.launch(Dispatchers.IO) {
                        if (result.textBlocks.isNullOrEmpty()) {
                            val differTime = System.currentTimeMillis() - startTime
                            if (differTime < 3000L) {
                                delay(3000L - differTime)
                            }
                            resultBitmap = bmp
                            withContext(Dispatchers.Main){
                                showIntAd()
                            }
                            return@launch
                        }
                        val degress = result.textBlocks[0].lines[0].angle

                        val resultArr = execTranslateApi(result.textBlocks)

                        for (block in result.textBlocks){
                            draw(
                                bmp,
                                resultArr,
                                block,
                                canvas,
                                Paint().apply {
                                    this.color = android.graphics.Color.parseColor("#FFFFFFFF")
                                },
                                degress
                            )
                        }

                        resultBitmap = bmp
                        if (resultArr.isNullOrEmpty()) {
                            result.textBlocks.forEach {block->
                                resultStr += "${block.text}\n"
                            }
                        }else{
                            parseResultString(resultArr)
                        }
                        withContext(Dispatchers.Main){
                            showIntAd()
                        }
                    }

                }
                .addOnFailureListener { e ->
                    lifecycleScope.launch {
                        resultBitmap = bmp
                        val differTime = System.currentTimeMillis() - startTime
                        if (differTime < 3000L) {
                            delay(3000L - differTime)
                        }
                        withContext(Dispatchers.Main){
                            showIntAd()
                        }
                    }
                }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun parseResultString(resultArr: List<Data>?) {
        resultArr?.forEach {
            resultStr += "${it.dst}\n"
        }
    }

    private suspend fun execTranslateApi(textBlocks: List<Text.TextBlock>) = withContext(Dispatchers.IO) {
        val arr = JsonArray()
        textBlocks.forEach {
            arr.add(it.text)
        }
        try {
            val json = Const.baseParam.deepCopy().apply {
                addProperty("langFrom", Repository.sourceLanguage!!.language)
                addProperty("langTo",Repository.targetLanguage!!.language)
                add("words",arr)
            }
            val result = ServiceCreator.api.getTranslateResult(json)
            return@withContext result.data
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }

    private suspend fun draw(
        bmp: Bitmap,
        resultArr: List<Data>?,
        textBlock: Text.TextBlock,
        canvas: Canvas,
        paint: Paint,
        angle: Float = 0f
    ) = suspendCancellableCoroutine<Boolean> { res ->

        textBlock.boundingBox?.let { rect ->

            val startX = rect.left
            val startY = rect.top
            val endX = rect.right
            val endY = rect.bottom
            val pixelColors = ArrayList<Int>()
            for (x in startX until endX) {
                for (y in startY until endY) {
                    try {
                        val pixelColor = bmp.getPixel(x, y)
                        pixelColors.add(pixelColor)
                    }catch (_:Exception){}
                }
            }

            val sameColor = calculateAverageColor(pixelColors)
            paint.color = sameColor

            val afterTT:String
            if (resultArr.isNullOrEmpty().not()) {
                afterTT = resultArr!!.find { it.src.equals(textBlock.text,true) }?.dst ?: ""
            }else{
                afterTT = textBlock.text
            }

            val textPaint = TextPaint()
            textPaint.textSize = calculateTextSizeToFitRect(afterTT,rect)
            textPaint.isAntiAlias = true
            textPaint.bgColor = android.graphics.Color.WHITE
            val drawTextWidth = if (rect.width() >= rect.height()) rect.width() else rect.height()

//            if (drawTextWidth <= 50f) {
//                textPaint.textSize = 30f
//            }
            val layout = StaticLayout(
                afterTT,
                textPaint,
                drawTextWidth,
                Layout.Alignment.ALIGN_CENTER,
                1.0f,
                0.0f,
                true
            )
            canvas.save()


            //高度根据翻译后的文本高度
            val maxHeight = if (layout.height >= rect.height()) {
                rect.top + layout.height
            }else{
                rect.bottom
            }

            val bgRectF = RectF(
                rect.left.toFloat(),
                rect.top.toFloat(),
                rect.right.toFloat(),
                rect.bottom.toFloat()
            )

            val pointX = bgRectF.right - bgRectF.width() / 2f
            val pointY = bgRectF.top + (bgRectF.width() / 2f)
            if (afterTT.isEmpty().not()) {
                canvas.drawRect(bgRectF, paint)
            }
            canvas.rotate(angle,pointX,pointY)
            canvas.translate(bgRectF.left.toFloat(), bgRectF.top)
            layout.draw(canvas);
            canvas.restore()
            res.resume(true)
        }
    }

    private fun calculateTextSizeToFitRect(text: String, rect: Rect): Float {
        if (text.isEmpty()) {
            return 70f
        }
        var textSize = 10000f // 初始文本大小，可以根据实际情况进行调整
        val space = rect.width() * rect.height()
        val leng = space/text.length

        textSize = sqrt(leng.toFloat())

        return textSize
    }
    private fun calculateAverageColor(colors: List<Int>): Int {
        var totalRed = 0
        var totalGreen = 0
        var totalBlue = 0

        for (color in colors) {
            totalRed += android.graphics.Color.red(color)
            totalGreen += android.graphics.Color.green(color)
            totalBlue += android.graphics.Color.blue(color)
        }

        val avgRed = totalRed / colors.size
        val avgGreen = totalGreen / colors.size
        val avgBlue = totalBlue / colors.size

        return android.graphics.Color.rgb(avgRed, avgGreen, avgBlue)
    }

    private fun showIntAd() {
        AdManager.setIntAdCallBack(this)
        AdManager.getAdObjFromPool(Const.AdConst.AD_INSERT)
    }

    override fun getIntAdFromPool(adWrapper: AdWrapper?) {
        adWrapper?.let {
            it.showAdInstance(this)
            return
        }
        navActivity<ResultActivity>()
        finish()
    }

    override fun onCloseIntAd() {
        navActivity<ResultActivity>()
        finish()
    }


    override fun onStart() {
        super.onStart()
        if (App.isBackground.not()) {
            AdManager.setNativeCallBack(this, Const.AdConst.AD_OTHER)
            AdManager.getAdObjFromPool(Const.AdConst.AD_OTHER)
        }
    }

    override fun getNavAdFromPool(adWrapper: AdWrapper) {
        this.adWrapper.value = adWrapper.getAdInstance() as NativeAd
    }
}

@Composable
fun PreViewImageLayout(
    path: Any,
    modifier: Modifier,
    shareView:@Composable ()->Unit={},
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .padding(top = 11.dp, bottom = 20.dp)
                .fillMaxWidth(0.9f)
                .height(460.dp)
        ){
            CoilImage(
                data = path,
                modifier = Modifier.fillMaxSize()
            )
            Box(modifier = Modifier.align(Alignment.BottomCenter)){
                shareView()
            }
        }
    }
}