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
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.FormError
import com.google.android.ump.UserMessagingPlatform

/**
 * Google Mobile Ads 同意管理器
 * 
 * 负责处理 UMP (User Messaging Platform) 同意流程，确保仅在用户同意后才加载广告。
 * 
 * 使用说明：
 * 1. 在应用启动时调用 requestConsentInfoUpdate() 请求同意信息
 * 2. 在同意流程完成后，检查 canRequestAds() 以确定是否可以加载广告
 * 3. 如果需要显示隐私选项，调用 showPrivacyOptionsForm()
 */
class GoogleMobileAdsConsentManager private constructor(private val activity: Activity) {
    
    private val consentInformation: ConsentInformation = UserMessagingPlatform.getConsentInformation(activity)
    private var consentForm: ConsentForm? = null
    
    companion object {
        private const val TAG = "GoogleMobileAdsConsent"
        
        @Volatile
        private var INSTANCE: GoogleMobileAdsConsentManager? = null
        
        /**
         * 获取单例实例
         */
        fun getInstance(activity: Activity): GoogleMobileAdsConsentManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: GoogleMobileAdsConsentManager(activity).also { INSTANCE = it }
            }
        }
    }
    
    /**
     * 检查是否可以请求广告
     */
    fun canRequestAds(): Boolean {
        return consentInformation.canRequestAds()
    }
    
    /**
     * 检查是否需要显示同意表单
     */
    fun isConsentFormAvailable(): Boolean {
        return consentInformation.isConsentFormAvailable
    }
    
    /**
     * 请求同意信息更新
     * 
     * @param onConsentInfoUpdateSuccess 同意信息更新成功回调
     * @param onConsentInfoUpdateFailure 同意信息更新失败回调
     */
    fun requestConsentInfoUpdate(
        onConsentInfoUpdateSuccess: () -> Unit,
        onConsentInfoUpdateFailure: (FormError) -> Unit
    ) {
        // 在调试模式下，可以设置测试设备 ID
        // TODO: 替换为您的测试设备 ID（通过 logcat 查看 "To get test ads on this device, set" 消息）
        val debugSettings = ConsentDebugSettings.Builder(activity)
            // .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA) // 仅用于测试
            // .addTestDeviceHashedId("YOUR_TEST_DEVICE_ID") // 替换为您的测试设备 ID
            .build()
        
        val params = ConsentRequestParameters.Builder()
            .setConsentDebugSettings(debugSettings)
            .build()
        
        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            object : ConsentInformation.OnConsentInfoUpdateSuccessListener {
                override fun onConsentInfoUpdateSuccess() {
                    Log.d(TAG, "同意信息更新成功")
                    onConsentInfoUpdateSuccess()
                }
            },
            object : ConsentInformation.OnConsentInfoUpdateFailureListener {
                override fun onConsentInfoUpdateFailure(formError: FormError) {
                    Log.e(TAG, "同意信息更新失败: ${formError.message} (错误代码: ${formError.errorCode})")
                    onConsentInfoUpdateFailure(formError)
                }
            }
        )
    }
    
    /**
     * 加载同意表单
     * 
     * @param onConsentFormLoadSuccess 表单加载成功回调
     * @param onConsentFormLoadFailure 表单加载失败回调
     */
    fun loadConsentForm(
        onConsentFormLoadSuccess: () -> Unit,
        onConsentFormLoadFailure: (FormError) -> Unit
    ) {
        UserMessagingPlatform.loadConsentForm(
            activity,
            object : UserMessagingPlatform.OnConsentFormLoadSuccessListener {
                override fun onConsentFormLoadSuccess(form: ConsentForm) {
                    Log.d(TAG, "同意表单加载成功")
                    consentForm = form
                    onConsentFormLoadSuccess()
                }
            },
            object : UserMessagingPlatform.OnConsentFormLoadFailureListener {
                override fun onConsentFormLoadFailure(formError: FormError) {
                    Log.e(TAG, "同意表单加载失败: ${formError.message} (错误代码: ${formError.errorCode})")
                    onConsentFormLoadFailure(formError)
                }
            }
        )
    }
    
    /**
     * 显示同意表单
     * 
     * @param onConsentFormDismissed 表单关闭回调（无论用户是否同意）
     */
    fun showConsentForm(onConsentFormDismissed: (FormError?) -> Unit) {
        val form = consentForm ?: return
        
        // 使用反射调用 ConsentForm.show()，因为接口类型在编译时无法解析
        try {
            // 获取 ConsentForm.OnConsentFormDismissedListener 接口类
            val listenerInterfaceClass = Class.forName("com.google.android.ump.ConsentForm\$OnConsentFormDismissedListener")
            
            // 创建代理对象实现接口
            val listener = java.lang.reflect.Proxy.newProxyInstance(
                listenerInterfaceClass.classLoader,
                arrayOf(listenerInterfaceClass)
            ) { _, method, args ->
                if (method.name == "onConsentFormDismissed") {
                    val formError = args?.get(0) as? FormError
                    if (formError != null) {
                        Log.e(TAG, "同意表单显示错误: ${formError.message} (错误代码: ${formError.errorCode})")
                    } else {
                        Log.d(TAG, "同意表单已关闭。可以请求广告: ${canRequestAds()}")
                    }
                    onConsentFormDismissed(formError)
                }
                null
            }
            
            // 使用反射调用 show 方法
            val showMethod = form.javaClass.getMethod("show", Activity::class.java, listenerInterfaceClass)
            showMethod.invoke(form, activity, listener)
        } catch (e: Exception) {
            Log.e(TAG, "显示同意表单失败: ${e.message}", e)
            // 如果反射失败，记录错误但不抛出异常
        }
    }
    
    /**
     * 显示隐私选项表单（用于允许用户更改同意选择）
     * 
     * @param onPrivacyOptionsFormDismissed 表单关闭回调
     */
    fun showPrivacyOptionsForm(onPrivacyOptionsFormDismissed: () -> Unit) {
        // UserMessagingPlatform.showPrivacyOptionsForm() 也使用 ConsentForm.OnConsentFormDismissedListener
        // 使用反射调用以避免编译时类型检查问题
        try {
            val listenerInterfaceClass = Class.forName("com.google.android.ump.ConsentForm\$OnConsentFormDismissedListener")
            
            val listener = java.lang.reflect.Proxy.newProxyInstance(
                listenerInterfaceClass.classLoader,
                arrayOf(listenerInterfaceClass)
            ) { _, method, args ->
                if (method.name == "onConsentFormDismissed") {
                    val formError = args?.get(0) as? FormError
                    if (formError != null) {
                        Log.e(TAG, "隐私选项表单显示错误: ${formError.message} (错误代码: ${formError.errorCode})")
                    } else {
                        Log.d(TAG, "隐私选项表单已关闭。可以请求广告: ${canRequestAds()}")
                    }
                    onPrivacyOptionsFormDismissed()
                }
                null
            }
            
            val showMethod = UserMessagingPlatform::class.java.getMethod(
                "showPrivacyOptionsForm",
                Activity::class.java,
                listenerInterfaceClass
            )
            showMethod.invoke(null, activity, listener)
        } catch (e: Exception) {
            Log.e(TAG, "显示隐私选项表单失败: ${e.message}", e)
        }
    }
    
    /**
     * 重置同意状态（仅用于测试）
     */
    fun reset() {
        consentInformation.reset()
        Log.d(TAG, "同意状态已重置")
    }
}

