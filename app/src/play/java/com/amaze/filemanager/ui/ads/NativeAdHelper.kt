/*
 * Copyright (C) 2014-2024 Arpit Khurana <arpitkh96@gmail.com>, Vishal Nehra <vishalmeham2@gmail.com>,
 * Emmanuel Messulam<emmanuelbendavid@gmail.com>, Raymond Lai <airwave209gt at gmail.com> and Contributors.
 *
 * This file is part of Amaze File Manager.
 *
 * Amaze File Manager is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.amaze.filemanager.ui.ads

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.amaze.filemanager.R
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.VideoController
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 原生广告辅助类
 * 负责加载和显示 Google AdMob 原生广告
 */
class NativeAdHelper(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner? = null
) {
    private var currentNativeAd: NativeAd? = null
    private var adLoader: AdLoader? = null

    companion object {
        private const val TAG = "NativeAdHelper"
        // 测试广告单元 ID，发布前需要替换为实际 ID
        private const val TEST_AD_UNIT_ID = "ca-app-pub-3940256099942544/2247696110"
        
        // Logcat 筛选关键字
        const val LOG_TAG = "NativeAdHelper"
    }

    /**
     * 初始化 AdMob SDK
     */
    fun initialize(onInitializationComplete: () -> Unit = {}) {
        Log.d(TAG, "========== 开始初始化 AdMob SDK ==========")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "正在后台线程初始化 AdMob SDK...")
                MobileAds.initialize(context) { initializationStatus ->
                    Log.d(TAG, "✅ AdMob SDK 初始化完成")
                    Log.d(TAG, "初始化状态: ${initializationStatus.adapterStatusMap}")
                    Log.d(TAG, "========== AdMob SDK 初始化流程结束 ==========")
                    onInitializationComplete()
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ AdMob SDK 初始化失败", e)
                Log.e(TAG, "错误详情: ${e.message}", e)
            }
        }
    }

    /**
     * 加载原生广告
     * @param adUnitId 广告单元 ID，如果为 null 则使用测试 ID
     * @param onAdLoaded 广告加载成功回调
     * @param onAdFailed 广告加载失败回调
     */
    fun loadNativeAd(
        adUnitId: String? = null,
        onAdLoaded: (NativeAd) -> Unit,
        onAdFailed: (String) -> Unit = {}
    ) {
        Log.d(TAG, "========== 开始加载原生广告 ==========")
        Log.d(TAG, "loadNativeAd: AD_UNIT_ID=${adUnitId ?: TEST_AD_UNIT_ID}")
        
        // 检查是否可以请求广告（同意状态）
        val consentManager = GoogleMobileAdsConsentManager.getInstance()
        val canRequest = consentManager.canRequestAds()
        Log.d(TAG, "loadNativeAd: 同意状态检查 - canRequestAds=$canRequest")
        
        if (!canRequest) {
            val errorMsg = "Cannot request ads: consent not granted"
            Log.w(TAG, "❌ $errorMsg")
            Log.w(TAG, "同意状态: ${consentManager.getConsentStatus()}")
            onAdFailed(errorMsg)
            return
        }

        // 销毁之前的广告
        if (currentNativeAd != null) {
            Log.d(TAG, "销毁之前的广告")
            currentNativeAd?.destroy()
        }
        currentNativeAd = null

        val unitId = adUnitId ?: TEST_AD_UNIT_ID
        Log.d(TAG, "使用广告单元 ID: $unitId")

        Log.d(TAG, "创建 AdLoader.Builder...")
        adLoader = AdLoader.Builder(context, unitId)
            .forNativeAd { nativeAd ->
                Log.d(TAG, "✅✅✅ onAdLoaded - 原生广告加载成功！")
                Log.d(TAG, "广告标题: ${nativeAd.headline}")
                Log.d(TAG, "广告正文: ${nativeAd.body}")
                Log.d(TAG, "行动号召: ${nativeAd.callToAction}")
                Log.d(TAG, "广告主: ${nativeAd.advertiser}")
                currentNativeAd = nativeAd
                onAdLoaded(nativeAd)
            }
            .withAdListener(object : com.google.android.gms.ads.AdListener() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    val error = "Failed to load native ad: ${loadAdError.message} (Code: ${loadAdError.code})"
                    Log.e(TAG, "❌❌❌ onAdFailedToLoad - 原生广告加载失败")
                    Log.e(TAG, "错误代码: ${loadAdError.code}")
                    Log.e(TAG, "错误域: ${loadAdError.domain}")
                    Log.e(TAG, "错误消息: ${loadAdError.message}")
                    Log.e(TAG, "响应信息: ${loadAdError.responseInfo}")
                    onAdFailed(error)
                }

                override fun onAdClicked() {
                    Log.d(TAG, "✅ 原生广告被点击")
                }

                override fun onAdImpression() {
                    Log.d(TAG, "✅ 原生广告展示已记录")
                }
            })
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    .setVideoOptions(
                        com.google.android.gms.ads.VideoOptions.Builder()
                            .setStartMuted(true)
                            .build()
                    )
                    .setRequestMultipleImages(false)
                    .build()
            )
            .build()

        Log.d(TAG, "创建 AdRequest...")
        val adRequest = AdRequest.Builder().build()
        Log.d(TAG, "调用 adLoader.loadAd()...")
        adLoader?.loadAd(adRequest)
        Log.d(TAG, "loadAd() 已调用，等待回调")
    }

    /**
     * 填充原生广告视图
     * @param nativeAdView 原生广告视图容器
     * @param nativeAd 原生广告对象
     */
    fun populateNativeAdView(nativeAdView: NativeAdView, nativeAd: NativeAd) {
        Log.d(TAG, "========== 开始填充原生广告视图 ==========")
        Log.d(TAG, "populateNativeAdView: 广告标题=${nativeAd.headline}")
        
        // 设置媒体视图
        val mediaView = nativeAdView.findViewById<MediaView>(R.id.ad_media)
        if (mediaView != null) {
            nativeAdView.mediaView = mediaView
            Log.d(TAG, "✅ MediaView 已设置")
        } else {
            Log.w(TAG, "⚠️ MediaView 未找到")
        }

        // 设置标题
        val headlineView = nativeAdView.findViewById<TextView>(R.id.ad_headline)
        headlineView.text = nativeAd.headline
        nativeAdView.headlineView = headlineView

        // 设置正文
        val bodyView = nativeAdView.findViewById<TextView>(R.id.ad_body)
        if (nativeAd.body != null) {
            bodyView.text = nativeAd.body
            bodyView.visibility = View.VISIBLE
            nativeAdView.bodyView = bodyView
        } else {
            bodyView.visibility = View.GONE
        }

        // 设置行动号召按钮
        val callToActionView = nativeAdView.findViewById<Button>(R.id.ad_call_to_action)
        if (nativeAd.callToAction != null) {
            callToActionView.text = nativeAd.callToAction
            callToActionView.visibility = View.VISIBLE
            nativeAdView.callToActionView = callToActionView
        } else {
            callToActionView.visibility = View.GONE
        }

        // 设置应用图标（可选）
        val appIconView = nativeAdView.findViewById<ImageView>(R.id.ad_app_icon)
        if (nativeAd.icon != null) {
            appIconView.setImageDrawable(nativeAd.icon?.drawable)
            appIconView.visibility = View.VISIBLE
            nativeAdView.iconView = appIconView
        } else {
            appIconView.visibility = View.GONE
        }

        // 设置广告主名称（可选）
        val advertiserView = nativeAdView.findViewById<TextView>(R.id.ad_advertiser)
        if (nativeAd.advertiser != null) {
            advertiserView.text = nativeAd.advertiser
            advertiserView.visibility = View.VISIBLE
            nativeAdView.advertiserView = advertiserView
        } else {
            advertiserView.visibility = View.GONE
        }

        // 设置评分（可选）
        val ratingBar = nativeAdView.findViewById<RatingBar>(R.id.ad_stars)
        if (nativeAd.starRating != null) {
            ratingBar.rating = nativeAd.starRating!!.toFloat()
            ratingBar.visibility = View.VISIBLE
            nativeAdView.starRatingView = ratingBar
        } else {
            ratingBar.visibility = View.GONE
        }

        // 设置价格（可选）
        val priceView = nativeAdView.findViewById<TextView>(R.id.ad_price)
        if (nativeAd.price != null) {
            priceView.text = nativeAd.price
            priceView.visibility = View.VISIBLE
            nativeAdView.priceView = priceView
        } else {
            priceView.visibility = View.GONE
        }

        // 设置商店名称（可选）
        val storeView = nativeAdView.findViewById<TextView>(R.id.ad_store)
        if (nativeAd.store != null) {
            storeView.text = nativeAd.store
            storeView.visibility = View.VISIBLE
            nativeAdView.storeView = storeView
        } else {
            storeView.visibility = View.GONE
        }

        // 绑定原生广告到视图
        nativeAdView.setNativeAd(nativeAd)
        Log.d(TAG, "✅ 原生广告已绑定到视图")
        Log.d(TAG, "========== 原生广告视图填充完成 ==========")

        // 处理视频广告生命周期
        val videoController = nativeAd.mediaContent?.videoController
        videoController?.videoLifecycleCallbacks = object : VideoController.VideoLifecycleCallbacks() {
            override fun onVideoEnd() {
                Log.d(TAG, "Video ad ended")
                super.onVideoEnd()
            }

            override fun onVideoMute(isMuted: Boolean) {
                Log.d(TAG, "Video ad muted: $isMuted")
                super.onVideoMute(isMuted)
            }

            override fun onVideoPlay() {
                Log.d(TAG, "Video ad started playing")
                super.onVideoPlay()
            }

            override fun onVideoPause() {
                Log.d(TAG, "Video ad paused")
                super.onVideoPause()
            }
        }

        // 监听生命周期事件
        lifecycleOwner?.lifecycle?.addObserver(LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    videoController?.pause()
                }
                Lifecycle.Event.ON_RESUME -> {
                    videoController?.play()
                }
                Lifecycle.Event.ON_DESTROY -> {
                    destroy()
                }
                else -> {}
            }
        })
    }

    /**
     * 销毁当前广告
     */
    fun destroy() {
        currentNativeAd?.destroy()
        currentNativeAd = null
        adLoader = null
    }

    /**
     * 获取当前广告
     */
    fun getCurrentNativeAd(): NativeAd? = currentNativeAd
}

