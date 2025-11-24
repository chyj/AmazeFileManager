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

import android.app.Activity
import android.util.Log
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.FormError
import com.google.android.ump.UserMessagingPlatform

/**
 * 用户消息平台 (UMP) SDK 的同意管理类
 * 用于处理 GDPR 等隐私合规要求
 */
class GoogleMobileAdsConsentManager private constructor() {

    private var consentInformation: ConsentInformation? = null
    private var consentForm: ConsentForm? = null

    companion object {
        private const val TAG = "ConsentManager"
        private const val DEBUG_DEVICE_HASHED_ID = "TEST-DEVICE-HASHED-ID"

        @Volatile
        private var instance: GoogleMobileAdsConsentManager? = null

        fun getInstance(): GoogleMobileAdsConsentManager {
            return instance ?: synchronized(this) {
                instance ?: GoogleMobileAdsConsentManager().also { instance = it }
            }
        }
    }

    /**
     * 初始化同意信息
     */
    fun initialize(activity: Activity) {
        // 在开发阶段，可以设置测试设备 ID
        val debugSettings = ConsentDebugSettings.Builder(activity)
            .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
            .addTestDeviceHashedId(DEBUG_DEVICE_HASHED_ID)
            .build()

        val params = ConsentRequestParameters.Builder()
            .setConsentDebugSettings(debugSettings)
            .build()

        consentInformation = UserMessagingPlatform.getConsentInformation(activity)
        consentInformation?.requestConsentInfoUpdate(
            activity,
            params,
            {
                // 同意信息更新成功
                Log.d(TAG, "Consent info updated successfully")
                if (consentInformation?.isConsentFormAvailable == true) {
                    loadConsentForm(activity)
                }
            },
            { formError: FormError ->
                // 同意信息更新失败
                Log.e(TAG, "Consent info update failed: ${formError.message}")
            }
        )
    }

    /**
     * 加载同意表单
     */
    private fun loadConsentForm(activity: Activity) {
        UserMessagingPlatform.loadConsentForm(
            activity,
            { consentForm ->
                this.consentForm = consentForm
                if (consentInformation?.consentStatus == ConsentInformation.ConsentStatus.REQUIRED) {
                    consentForm.show(activity) { formError ->
                        if (formError != null) {
                            Log.e(TAG, "Consent form error: ${formError.message}")
                        } else {
                            Log.d(TAG, "Consent form shown successfully")
                        }
                    }
                }
            },
            { formError ->
                Log.e(TAG, "Load consent form error: ${formError.message}")
            }
        )
    }

    /**
     * 检查是否可以加载广告
     */
    fun canRequestAds(): Boolean {
        return consentInformation?.canRequestAds() ?: false
    }

    /**
     * 获取同意状态
     */
    fun getConsentStatus(): Int? {
        return consentInformation?.consentStatus
    }

  /**
   * 显示隐私选项表单（如果可用）
   */
  fun showPrivacyOptionsForm(activity: Activity, onComplete: () -> Unit) {
    if (consentForm != null) {
      UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError ->
        if (formError != null) {
          Log.e(TAG, "Privacy options form error: ${formError.message}")
        } else {
          Log.d(TAG, "Privacy options form shown successfully")
        }
        onComplete()
      }
    } else {
      Log.w(TAG, "Consent form not available")
      onComplete()
    }
  }
}


