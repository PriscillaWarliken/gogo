package com.translate.app.ui.translatePage

import android.app.Activity
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonArray
import com.translate.app.Const
import com.translate.app.repository.ServiceCreator
import com.translate.app.repository.bean.ResultBean
import com.translate.app.ui.BaseActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TranslateViewModel : ViewModel() {



    suspend fun execTranslateApi(result: JsonArray, sourceLanguage:String, targetLanguage:String) = withContext(Dispatchers.IO) {
        try {
//            delay(5000)
            val json = Const.baseParam.deepCopy().apply {
                addProperty("langFrom", sourceLanguage)
                addProperty("langTo",targetLanguage)
                add("words",result)
                srcText = result.first().asString
                dstText = result.first().asString
            }
            val result = ServiceCreator.api.getTranslateResult(json)
            reusltLiveData.postValue(result)
            srcText = result.data.first().src
            dstText = result.data.first().dst
        } catch (e: Exception) {
            e.printStackTrace()
            reusltLiveData.postValue(null)
        }
    }

    companion object{
        var srcText by mutableStateOf(value = "")
        var dstText by mutableStateOf(value = "")
        var reusltLiveData = MutableLiveData<ResultBean?>()
    }
}