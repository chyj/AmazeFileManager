package com.amaze.filemanager.ads

import android.app.Activity
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ConsentManagerHelper
 * 辅助类，用于从 Java 代码调用 Kotlin 协程函数
 * 
 * Logcat 筛选关键字: ConsentManagerHelper
 */
object ConsentManagerHelper {
    private const val TAG = "ConsentManagerHelper"
    
    /**
     * 显示同意表单（从 Java 调用）
     */
    @JvmStatic
    fun showConsentForm(activity: Activity, consentManager: GoogleMobileAdsConsentManager) {
        Log.d(TAG, "showConsentForm: 开始显示同意表单")
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // 加载同意表单
                val loadResult = consentManager.loadConsentForm()
                
                if (loadResult.isSuccess) {
                    // 显示同意表单
                    val showResult = consentManager.showConsentForm()
                    
                    if (showResult.isFailure) {
                        Log.e(TAG, "showConsentForm: 显示同意表单失败", showResult.exceptionOrNull())
                    }
                } else {
                    Log.e(TAG, "showConsentForm: 加载同意表单失败", loadResult.exceptionOrNull())
                }
            } catch (e: Exception) {
                Log.e(TAG, "showConsentForm: 异常", e)
            }
        }
    }
}

