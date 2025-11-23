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
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.initialization.InitializationStatus
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener

/**
 * AdMob 初始化器
 * 
 * 负责初始化 Google Mobile Ads SDK，确保：
 * 1. 每个应用会话只初始化一次
 * 2. 记录 SDK 版本信息
 * 3. 配置测试设备 ID
 * 
 * 使用说明：
 * 在 Application.onCreate() 中调用 initialize()
 */
object AdMobInitializer {
    
    private const val TAG = "AdMobInitializer"
    private var isInitialized = false
    
    /**
     * 初始化 MobileAds SDK
     * 
     * @param context 应用上下文
     * @param testDeviceIds 测试设备 ID 列表（可选）
     * @param onInitializationComplete 初始化完成回调（可选）
     */
    fun initialize(
        context: Context,
        testDeviceIds: List<String>? = null,
        onInitializationComplete: ((InitializationStatus) -> Unit)? = null
    ) {
        if (isInitialized) {
            Log.d(TAG, "MobileAds SDK 已初始化，跳过重复初始化")
            return
        }
        
        Log.d(TAG, "开始初始化 MobileAds SDK...")
        
        // 配置测试设备（仅用于开发测试）
        testDeviceIds?.let { ids ->
            val requestConfiguration = RequestConfiguration.Builder()
                .setTestDeviceIds(ids)
                .build()
            MobileAds.setRequestConfiguration(requestConfiguration)
            Log.d(TAG, "已配置测试设备: ${ids.joinToString()}")
        }
        
        // 初始化 MobileAds SDK
        MobileAds.initialize(context) { initializationStatus ->
            isInitialized = true
            
            // 记录 SDK 版本
            val adapterStatusMap = initializationStatus.adapterStatusMap
            Log.d(TAG, "MobileAds SDK 初始化完成")
            Log.d(TAG, "SDK 版本信息:")
            adapterStatusMap.forEach { (adapterClass, status) ->
                Log.d(
                    TAG,
                    "  适配器: $adapterClass, 状态: ${status.initializationState}, " +
                        "延迟: ${status.latency}ms, 描述: ${status.description}"
                )
            }
            
            onInitializationComplete?.invoke(initializationStatus)
        }
    }
    
    /**
     * 检查是否已初始化
     */
    fun isInitialized(): Boolean {
        return isInitialized
    }
}

