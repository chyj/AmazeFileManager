package com.amaze.filemanager.ads

import android.app.Activity
import android.util.Log
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.FormError
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Google Mobile Ads Consent Manager
 * 管理 UMP (User Messaging Platform) 同意表单
 * 
 * Logcat 筛选关键字: ConsentManager
 */
class GoogleMobileAdsConsentManager(private val activity: Activity) {
    
    companion object {
        private const val TAG = "ConsentManager"
        // 测试设备 ID，用于调试（需要替换为实际设备 ID）
        private const val TEST_DEVICE_ID = "TEST_DEVICE_ID_HERE"
        // 是否绕过同意表单（用于测试）
        private const val BYPASS_CONSENT_FORM = true
    }
    
    private var consentInformation: ConsentInformation? = null
    private var consentForm: ConsentForm? = null
    
    /**
     * 初始化同意信息
     */
    fun initializeConsentInformation() {
        Log.d(TAG, "initializeConsentInformation: 开始初始化同意信息")
        
        // 使用非 EEA 地理位置来绕过同意表单（仅用于测试）
        val debugSettings = ConsentDebugSettings.Builder(activity)
            .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_NOT_EEA) // 非欧洲地区，不需要同意表单
            .addTestDeviceHashedId(TEST_DEVICE_ID)
            .build()
        
        val requestParameters = ConsentRequestParameters.Builder()
            .setConsentDebugSettings(debugSettings)
            .build()
        
        consentInformation = UserMessagingPlatform.getConsentInformation(activity)
        consentInformation?.requestConsentInfoUpdate(
            activity,
            requestParameters,
            object : ConsentInformation.OnConsentInfoUpdateSuccessListener {
                override fun onConsentInfoUpdateSuccess() {
                    Log.d(TAG, "initializeConsentInformation: onConsentInfoUpdateSuccess - 同意信息更新成功")
                }
            },
            object : ConsentInformation.OnConsentInfoUpdateFailureListener {
                override fun onConsentInfoUpdateFailure(formError: FormError) {
                    Log.e(TAG, "initializeConsentInformation: onConsentInfoUpdateFailure - 同意信息更新失败: " +
                        "code=${formError.errorCode}, " +
                        "message=${formError.message}")
                }
            }
        )
    }
    
    /**
     * 检查是否可以请求广告
     */
    fun canRequestAds(): Boolean {
        // 如果启用绕过模式，直接返回 true
        if (BYPASS_CONSENT_FORM) {
            Log.d(TAG, "canRequestAds: 绕过模式，返回 true")
            return true
        }
        val canRequest = consentInformation?.canRequestAds() ?: false
        Log.d(TAG, "canRequestAds: $canRequest")
        return canRequest
    }
    
    /**
     * 检查是否需要显示同意表单
     */
    fun isConsentFormAvailable(): Boolean {
        // 如果启用绕过模式，直接返回 false（不显示同意表单）
        if (BYPASS_CONSENT_FORM) {
            Log.d(TAG, "isConsentFormAvailable: 绕过模式，返回 false")
            return false
        }
        val isAvailable = consentInformation?.isConsentFormAvailable() ?: false
        Log.d(TAG, "isConsentFormAvailable: $isAvailable")
        return isAvailable
    }
    
    /**
     * 加载同意表单
     */
    suspend fun loadConsentForm(): Result<Unit> = suspendCancellableCoroutine { continuation ->
        Log.d(TAG, "loadConsentForm: 开始加载同意表单")
        
        UserMessagingPlatform.loadConsentForm(
            activity,
            { form ->
                Log.d(TAG, "loadConsentForm: 同意表单加载成功")
                consentForm = form
                continuation.resume(Result.success(Unit))
            },
            { formError ->
                Log.e(TAG, "loadConsentForm: 同意表单加载失败: " +
                    "code=${formError.errorCode}, " +
                    "message=${formError.message}")
                continuation.resume(Result.failure(Exception(formError.message)))
            }
        )
    }
    
    /**
     * 显示同意表单
     */
    suspend fun showConsentForm(): Result<Unit> = suspendCancellableCoroutine { continuation ->
        Log.d(TAG, "showConsentForm: 准备显示同意表单")
        
        consentForm?.show(
            activity,
            object : ConsentForm.OnConsentFormDismissedListener {
                override fun onConsentFormDismissed(formError: FormError?) {
                    if (formError != null) {
                        Log.e(TAG, "showConsentForm: 同意表单关闭时出错: " +
                            "code=${formError.errorCode}, " +
                            "message=${formError.message}")
                        continuation.resume(Result.failure(Exception(formError.message)))
                    } else {
                        Log.d(TAG, "showConsentForm: 同意表单已关闭")
                        val canRequest = canRequestAds()
                        Log.d(TAG, "showConsentForm: 关闭后 canRequestAds=$canRequest")
                        continuation.resume(Result.success(Unit))
                    }
                }
            }
        ) ?: run {
            Log.e(TAG, "showConsentForm: 同意表单未加载")
            continuation.resume(Result.failure(Exception("Consent form not loaded")))
        }
    }
    
    /**
     * 获取同意状态（用于调试）
     */
    fun getConsentStatus(): String {
        val status = consentInformation?.consentStatus ?: -1
        val canRequest = canRequestAds()
        val isFormAvailable = isConsentFormAvailable()
        
        return "ConsentManager Status: " +
            "consentStatus=$status, " +
            "canRequestAds=$canRequest, " +
            "isConsentFormAvailable=$isFormAvailable"
    }
    
    /**
     * 重置同意状态（用于测试）
     */
    fun resetConsent() {
        Log.d(TAG, "resetConsent: 重置同意状态")
        consentInformation?.reset()
    }
}

