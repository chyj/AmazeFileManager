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

package com.amaze.filemanager.utils

import android.app.Activity
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

/**
 * 插页式广告管理器
 * 
 * 负责加载和显示插页式广告，确保：
 * 1. 避免重复加载
 * 2. 正确处理加载和显示回调
 * 3. 在广告关闭后清空引用
 * 
 * 使用说明：
 * 1. 在应用启动时调用 loadAd() 预加载广告
 * 2. 在合适的业务触发点调用 showAd() 显示广告
 * 3. 确保在显示广告前检查 canShowAd()
 */
class InterstitialAdManager private constructor(private val activity: Activity) {
    
    private var interstitialAd: InterstitialAd? = null
    private var isLoadingAd = false
    private var isShowingAd = false
    
    // TODO: 替换为您的插页式广告位 ID。格式：ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX
    // 测试广告位 ID：ca-app-pub-3940256099942544/1033173712
    private val adUnitId = "ca-app-pub-3940256099942544/1033173712"
    
    companion object {
        private const val TAG = "InterstitialAd"
        
        @Volatile
        private var INSTANCE: InterstitialAdManager? = null
        
        /**
         * 获取单例实例
         */
        fun getInstance(activity: Activity): InterstitialAdManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: InterstitialAdManager(activity).also { INSTANCE = it }
            }
        }
    }
    
    /**
     * 检查是否可以显示广告
     */
    fun canShowAd(): Boolean {
        return interstitialAd != null && !isShowingAd
    }
    
    /**
     * 加载插页式广告
     * 
     * @param onAdLoaded 广告加载成功回调（可选）
     * @param onAdFailedToLoad 广告加载失败回调（可选）
     */
    fun loadAd(
        onAdLoaded: (() -> Unit)? = null,
        onAdFailedToLoad: ((LoadAdError) -> Unit)? = null
    ) {
        // 如果正在加载或已有广告，则跳过
        if (isLoadingAd || interstitialAd != null) {
            Log.d(TAG, "广告正在加载或已存在，跳过加载")
            return
        }
        
        isLoadingAd = true
        Log.d(TAG, "开始加载插页式广告...")
        
        val adRequest = AdRequest.Builder().build()
        
        InterstitialAd.load(
            activity,
            adUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "插页式广告加载成功")
                    interstitialAd = ad
                    isLoadingAd = false
                    
                    // 设置全屏内容回调
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            Log.d(TAG, "广告已关闭")
                            interstitialAd = null
                            isShowingAd = false
                            // 广告关闭后，可以立即加载下一个广告
                            loadAd()
                        }
                        
                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            Log.e(TAG, "广告显示失败: ${adError.message} (错误代码: ${adError.code})")
                            interstitialAd = null
                            isShowingAd = false
                            // 显示失败后，可以尝试加载下一个广告
                            loadAd()
                        }
                        
                        override fun onAdShowedFullScreenContent() {
                            Log.d(TAG, "广告已显示")
                            isShowingAd = true
                        }
                        
                        override fun onAdClicked() {
                            Log.d(TAG, "用户点击了广告")
                        }
                        
                        override fun onAdImpression() {
                            Log.d(TAG, "广告已展示给用户")
                        }
                    }
                    
                    onAdLoaded?.invoke()
                }
                
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.e(
                        TAG,
                        "插页式广告加载失败: ${loadAdError.message} (错误代码: ${loadAdError.code}, " +
                            "域: ${loadAdError.domain}, 原因: ${loadAdError.cause})"
                    )
                    interstitialAd = null
                    isLoadingAd = false
                    onAdFailedToLoad?.invoke(loadAdError)
                }
            }
        )
    }
    
    /**
     * 显示插页式广告
     * 
     * @param onAdNotReady 广告未准备好时的回调（可选）
     * @return true 如果广告已显示，false 如果广告未准备好
     */
    fun showAd(onAdNotReady: (() -> Unit)? = null): Boolean {
        if (!canShowAd()) {
            Log.d(TAG, "广告未准备好，无法显示")
            onAdNotReady?.invoke()
            return false
        }
        
        interstitialAd?.let { ad ->
            Log.d(TAG, "显示插页式广告")
            ad.show(activity)
            return true
        }
        
        onAdNotReady?.invoke()
        return false
    }
    
    /**
     * 清空当前广告引用（用于清理资源）
     */
    fun clearAd() {
        interstitialAd = null
        isLoadingAd = false
        isShowingAd = false
        Log.d(TAG, "已清空广告引用")
    }
}

