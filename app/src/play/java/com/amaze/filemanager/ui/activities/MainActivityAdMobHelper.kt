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

package com.amaze.filemanager.ui.activities

import android.app.Activity
import android.util.Log
import com.amaze.filemanager.utils.InterstitialAdManager

/**
 * MainActivity 的 AdMob 集成辅助类
 * 
 * 负责：
 * 1. 初始化并加载插页式广告
 * 2. 在合适的时机显示广告
 */
class MainActivityAdMobHelper(private val activity: Activity) {
    
    private val interstitialAdManager = InterstitialAdManager.getInstance(activity)
    private var isInitialized = false
    
    companion object {
        private const val TAG = "MainActivityAdMob"
    }
    
    /**
     * 初始化 AdMob 并加载插页式广告
     * 应在 MainActivity.onCreate() 中调用
     */
    fun initialize() {
        if (isInitialized) {
            Log.d(TAG, "AdMob 已初始化，跳过重复初始化")
            return
        }
        
        Log.d(TAG, "开始初始化 AdMob...")
        
        // 直接加载插页式广告（无需 UMP 同意流程）
        loadInterstitialAd()
        
        isInitialized = true
    }
    
    /**
     * 加载插页式广告
     */
    private fun loadInterstitialAd() {
        interstitialAdManager.loadAd(
            onAdLoaded = {
                Log.d(TAG, "插页式广告加载成功")
            },
            onAdFailedToLoad = { loadAdError ->
                Log.e(TAG, "插页式广告加载失败: ${loadAdError.message}")
            }
        )
    }
    
    /**
     * 尝试显示插页式广告
     * 
     * 应在合适的业务触发点调用，例如：
     * - 用户点击文件夹时
     * - 完成文件操作后
     * 
     * @return true 如果广告已显示，false 如果广告未准备好
     */
    fun tryShowInterstitialAd(): Boolean {
        return interstitialAdManager.showAd {
            Log.d(TAG, "广告未准备好，无法显示")
        }
    }
    
    /**
     * 清理资源（在 Activity 销毁时调用）
     */
    fun cleanup() {
        interstitialAdManager.clearAd()
    }
}

