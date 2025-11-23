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

package com.amaze.filemanager.application

import android.content.Context
import com.amaze.filemanager.utils.AdMobInitializer

/**
 * AppConfig 的 AdMob 扩展
 * 
 * 仅在 play flavor 中可用，用于初始化 AdMob SDK
 */
object AppConfigAdMob {
    
    /**
     * 初始化 AdMob（仅在 play flavor 中调用）
     * 
     * @param context 应用上下文
     */
    fun initializeAdMob(context: Context) {
        // TODO: 添加您的测试设备 ID（通过 logcat 查看 "To get test ads on this device, set" 消息）
        val testDeviceIds: List<String> = listOf(
            // "YOUR_TEST_DEVICE_ID_HERE"
        )
        
        AdMobInitializer.initialize(
            context = context,
            testDeviceIds = if (testDeviceIds.isEmpty()) null else testDeviceIds
        ) { initializationStatus ->
            // 初始化完成后的回调（可选）
        }
    }
}

