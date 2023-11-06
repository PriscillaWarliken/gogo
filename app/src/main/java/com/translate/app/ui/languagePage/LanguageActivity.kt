package com.translate.app.ui.languagePage

import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.currentRecomposeScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.ads.nativead.NativeAd
import com.translate.app.App
import com.translate.app.Const
import com.translate.app.R
import com.translate.app.ads.AdManager
import com.translate.app.ads.base.AdWrapper
import com.translate.app.ads.callback.SmallAdCallback
import com.translate.app.repository.Repository
import com.translate.app.repository.bean.LanguageBeanItem
import com.translate.app.ui.BaseActivity
import com.translate.app.ui.ImagePickerActivity
import com.translate.app.ui.theme.grey
import com.translate.app.ui.weight.CoilImage
import com.translate.app.ui.weight.NativeAdsView
import com.translate.app.ui.weight.SearchEdit
import com.translate.app.ui.weight.click
import com.translate.app.ui.weight.saveSP

class LanguageActivity : BaseActivity(),SmallAdCallback {

    companion object{
        var sourceSelectState by mutableStateOf(value = true)

        private var languageChangeListener:LanguageChangeListener ?= null

        fun setLanguageChangeListener(listener: LanguageChangeListener?) {
            languageChangeListener = listener
        }
    }

    private var searchResultList = mutableListOf<LanguageBeanItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val focusManager = LocalFocusManager.current
            val scrollState = rememberScrollState()
            Column(
                Modifier
                    .statusBarsPadding()
                    .fillMaxWidth()
                    .click {
                        focusManager.clearFocus()
                    }, horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {

                    Text(text = "Source language",
                        fontSize = if (sourceSelectState) 20.sp else 16.sp,
                        color = if (sourceSelectState) Color.Black else Color(0xFF666666),
                        modifier = Modifier.click {
                            sourceSelectState = true
                        })

                    Text(
                        text = "Target language",
                        fontSize = if (sourceSelectState.not()) 20.sp else 16.sp,
                        color = if (sourceSelectState.not()) Color.Black else Color(0xFF666666),
                        modifier = Modifier.click {
                            sourceSelectState = false
                        }
                    )
                    CoilImage(modifier = Modifier
                        .size(22.dp)
                        .click { finish() }, data = R.mipmap.ic_return)
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(top = 30.dp)
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .clip(shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        .background(
                            color = Color.White,
                        )
                        .verticalScroll(scrollState)
                ) {
                    val recompose = currentRecomposeScope
                    SearchEdit(modifier = Modifier.padding(top = 30.dp)) {
                        searchResultList.clear()
                        val result = Repository.getSearchResult(it)
                        searchResultList.addAll(result)
                        recompose.invalidate()
                    }
                    if (searchResultList.isEmpty()) {
                        LanguageListView()
                    }else{
                        SearchResultList()
                    }
                }
            }
        }
    }

    @Composable
    fun SearchResultList() {
        Column(modifier = Modifier
            .padding(top = 12.dp)
            .fillMaxWidth(0.9f)) {
            Text(text = "Search Result language", fontSize = 14.sp,color = grey)
            searchResultList.forEachIndexed  { index,it->
                LanguageItemView(index,it)
            }
        }
    }

    @Composable
    private fun LanguageListView() {
        Column(modifier = Modifier
            .padding(top = 12.dp)
            .fillMaxWidth(0.9f)) {
            Text(text = "Recently used language", fontSize = 14.sp,color = grey)
            Repository.recentLanguageList.forEachIndexed {index,it->
                LanguageItemView(index, it)
            }
            Text(text = "All languages", fontSize = 14.sp,color = grey, modifier = Modifier.padding(vertical = 12.dp))
            Column(modifier = Modifier
                .fillMaxWidth()
                .background(color = Color(0xFFF4F8FA), shape = RoundedCornerShape(16.dp))){
                Repository.allLanguageList.forEach {
                    LanguageItemView2(it)
                }
            }
        }
    }

    @Composable
    private fun LanguageItemView(index: Int, languageBeanItem: LanguageBeanItem) {
        var selectState = false
        val selectModifier = if (sourceSelectState) {
            if (Repository.sourceLanguage!! == languageBeanItem) {
                selectState = true
                Modifier
                    .background(color = Color(0xFFEDF9FF), shape = RoundedCornerShape(8.dp))
                    .border(
                        width = 1.dp,
                        color = Color(0xFF58B9EE),
                        shape = RoundedCornerShape(8.dp)
                    )
            }else{
                Modifier.border(
                    width = 1.dp,
                    color = Color(0xFFE9E9E9),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }else{
            if (Repository.targetLanguage!! == languageBeanItem) {
                selectState = true
                Modifier
                    .background(color = Color(0xFFEDF9FF), shape = RoundedCornerShape(8.dp))
                    .border(
                        width = 1.dp,
                        color = Color(0xFF58B9EE),
                        shape = RoundedCornerShape(8.dp)
                    )
            }else{
                Modifier.border(
                    width = 1.dp,
                    color = Color(0xFFE9E9E9),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }
        Box(
            modifier = Modifier
                .padding(top = 12.dp)
                .fillMaxWidth()
                .height(60.dp)
                .then(selectModifier)
                .click {
                    setLanguageClick(languageBeanItem)
                    Repository.setRecentLanguage(languageBeanItem)
                    finish()
                }
        ) {
            Text(
                text = languageBeanItem.languageEn, fontSize = 18.sp, color = Color.Black,
                modifier = Modifier
                    .padding(start = 27.dp)
                    .align(Alignment.CenterStart)
            )

            if (selectState) {
                CoilImage(modifier = Modifier
                    .padding(end = 18.dp)
                    .align(Alignment.CenterEnd)
                    .size(24.dp), data = R.mipmap.selected)
            }
        }

        if (index == 0) {
            adWrapper.value?.let {
                NativeAdsView(
                    isBig = false, mAdInstance = it, modifier = Modifier
                        .padding(top = 12.dp)
                        .fillMaxWidth(1f)
                )
            }
        }
    }

    @Composable
    private fun LanguageItemView2(languageBeanItem: LanguageBeanItem){
        Box(
            modifier = Modifier
                .padding(top = 12.dp)
                .fillMaxWidth()
                .height(60.dp)
                .click {
                    setLanguageClick(languageBeanItem)
                    Repository.setRecentLanguage(languageBeanItem)
                    finish()
                },
        ) {
            Text(
                text = languageBeanItem.languageEn, fontSize = 18.sp, color = Color.Black,
                modifier = Modifier
                    .padding(start = 27.dp)
                    .align(Alignment.CenterStart)
            )
            Spacer(modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(0.9f)
                .height(1.dp)
                .background(color = Color(0xFFC8C8C8)))
        }
    }

    private fun setLanguageClick(languageBeanItem: LanguageBeanItem) {
        if (sourceSelectState) {
            languageBeanItem.saveSP(Const.SOURCE_LANGUAGE)
            Repository.sourceLanguage = languageBeanItem
            ImagePickerActivity.setSourceLanguageMethod(languageBeanItem.languageEn)
        } else {
            languageBeanItem.saveSP(Const.TARGET_LANGUAGE)
            Repository.targetLanguage = languageBeanItem
            ImagePickerActivity.setTargetLanguageMethod(languageBeanItem.languageEn)
        }
        languageChangeListener?.changeLanguage(Repository.sourceLanguage!!.languageEn,Repository.targetLanguage!!.languageEn)
    }

    override fun onStart() {
        super.onStart()
        if (App.isBackground.not()) {
            AdManager.setSmallCallBack(this, Const.AdConst.AD_TEXT)
            AdManager.getAdObjFromPool(Const.AdConst.AD_TEXT)
        }
    }

    var adWrapper= mutableStateOf<NativeAd?>(null)
    override fun getSmallFromPool(adWrapper: AdWrapper) {
        this.adWrapper.value = adWrapper.getAdInstance() as NativeAd
    }
}