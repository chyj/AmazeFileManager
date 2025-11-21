package com.amaze.filemanager.ads

import android.app.Activity
import android.app.Application
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import java.util.Date

/**
 * App Open Ad Manager
 * 管理 App Open 广告的加载、显示、过期判断和生命周期回调
 * 
 * Logcat 筛选关键字: AppOpenAdManager
 */
class AppOpenAdManager(private val application: Application) {
    
    companion object {
        private const val TAG = "AppOpenAdManager"
        // App Open Ad 测试广告单元 ID
        // 注意：确保在 AdMob 控制台中创建的是 App Open Ad 类型的广告单元
        // 如果仍然报错，请检查：
        // 1. AdMob App ID 是否正确配置在 AndroidManifest.xml 中
        // 2. 广告单元类型是否为 App Open Ad（不是 Banner 或其他类型）
        // 3. 网络连接是否正常
        private const val AD_UNIT_ID = "ca-app-pub-3940256099942544/9257395921" // Test App Open Ad Unit ID (updated for play-services-ads:24.4.0)
        private const val AD_EXPIRY_HOURS = 4L // 广告过期时间（小时）
    }
    
    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAd = false
    private var isShowingAd = false
    private var loadTime: Date? = null
    private var onAdLoadListener: OnAdLoadListener? = null
    
    /**
     * 广告加载监听器
     */
    interface OnAdLoadListener {
        fun onAdLoaded()
        fun onAdFailedToLoad()
    }
    
    /**
     * 设置广告加载监听器
     */
    fun setOnAdLoadListener(listener: OnAdLoadListener?) {
        this.onAdLoadListener = listener
    }
    
    /**
     * 检查广告是否已过期
     */
    private fun isAdExpired(): Boolean {
        val loadTime = this.loadTime ?: return true
        val dateDifference = Date().time - loadTime.time
        val millisecondsPerHour = 3600000L
        return dateDifference > (AD_EXPIRY_HOURS * millisecondsPerHour)
    }
    
    /**
     * 检查广告是否可用
     */
    fun isAdAvailable(): Boolean {
        val hasAd = appOpenAd != null
        val expired = isAdExpired()
        val result = hasAd && !expired
        Log.d(TAG, "isAdAvailable: 检查结果 - hasAd=$hasAd, expired=$expired, result=$result")
        if (hasAd) {
            Log.d(TAG, "isAdAvailable: loadTime=$loadTime")
        }
        return result
    }
    
    /**
     * 加载广告
     */
    fun loadAd() {
        Log.d(TAG, "loadAd: ========== 开始加载广告流程 ==========")
        Log.d(TAG, "loadAd: AD_UNIT_ID=$AD_UNIT_ID")
        Log.d(TAG, "loadAd: 当前状态 - isLoadingAd=$isLoadingAd, isAdAvailable=${isAdAvailable()}")
        
        // 如果正在加载或已有可用广告，则跳过
        if (isLoadingAd) {
            Log.d(TAG, "loadAd: ⚠️ 正在加载中，跳过")
            return
        }
        
        if (isAdAvailable()) {
            Log.d(TAG, "loadAd: ⚠️ 广告已可用，跳过加载")
            return
        }
        
        isLoadingAd = true
        Log.d(TAG, "loadAd: ✅ 开始加载广告，调用 AppOpenAd.load()")
        
        val request = AdRequest.Builder().build()
        Log.d(TAG, "loadAd: AdRequest 已创建")
        
        AppOpenAd.load(
            application,
            AD_UNIT_ID,
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    Log.d(TAG, "loadAd: ✅✅✅ onAdLoaded - 广告加载成功！")
                    appOpenAd = ad
                    loadTime = Date()
                    isLoadingAd = false
                    Log.d(TAG, "loadAd: 广告已保存，loadTime=$loadTime")
                    Log.d(TAG, "loadAd: 当前状态=${getAdStatus()}")
                    
                    // 打印 ResponseInfo
                    ad.responseInfo?.let { responseInfo ->
                        Log.d(TAG, "loadAd: ResponseInfo - " +
                            "responseId=${responseInfo.responseId}, " +
                            "mediationAdapterClassName=${responseInfo.mediationAdapterClassName}, " +
                            "adapterResponses=${responseInfo.adapterResponses?.map { it.adapterClassName }}")
                    } ?: run {
                        Log.w(TAG, "loadAd: ResponseInfo 为空")
                    }
                    
                    // 通知监听器
                    onAdLoadListener?.onAdLoaded()
                }
                
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.e(TAG, "loadAd: ❌❌❌ onAdFailedToLoad - 广告加载失败")
                    Log.e(TAG, "loadAd: 错误代码=${loadAdError.code}")
                    Log.e(TAG, "loadAd: 错误域=${loadAdError.domain}")
                    Log.e(TAG, "loadAd: 错误消息=${loadAdError.message}")
                    Log.e(TAG, "loadAd: 错误原因=${loadAdError.cause}")
                    
                    // 错误代码说明
                    when (loadAdError.code) {
                        0 -> Log.e(TAG, "loadAd: ERROR_CODE_INTERNAL_ERROR - 内部错误")
                        1 -> Log.e(TAG, "loadAd: ERROR_CODE_INVALID_REQUEST - 请求无效")
                        2 -> Log.e(TAG, "loadAd: ERROR_CODE_NETWORK_ERROR - 网络错误")
                        3 -> Log.e(TAG, "loadAd: ERROR_CODE_NO_FILL - 无广告填充")
                        8 -> Log.e(TAG, "loadAd: ERROR_CODE_INVALID_AD_SIZE - 广告尺寸无效")
                        else -> Log.e(TAG, "loadAd: 未知错误代码")
                    }
                    
                    // 如果是格式错误，提供详细说明
                    if (loadAdError.message.contains("format", ignoreCase = true) || 
                        loadAdError.message.contains("doesn't match", ignoreCase = true)) {
                        Log.e(TAG, "loadAd: ⚠️⚠️⚠️ 广告单元 ID 格式/类型不匹配 ⚠️⚠️⚠️")
                        Log.e(TAG, "loadAd: 当前使用的 AD_UNIT_ID=$AD_UNIT_ID")
                        Log.e(TAG, "loadAd: 可能的原因：")
                        Log.e(TAG, "loadAd: 1. ❌ 广告单元 ID 类型不正确（必须是 App Open Ad 类型，不是 Banner/Interstitial）")
                        Log.e(TAG, "loadAd: 2. ❌ AdMob App ID 配置错误或未正确设置")
                        Log.e(TAG, "loadAd: 3. ❌ 测试广告单元 ID 可能已过期或无效")
                        Log.e(TAG, "loadAd: 解决方案：")
                        Log.e(TAG, "loadAd: 1. 在 AdMob 控制台 (https://apps.admob.com) 创建 App Open Ad 类型的广告单元")
                        Log.e(TAG, "loadAd: 2. 确保 AndroidManifest.xml 中的 APPLICATION_ID 正确")
                        Log.e(TAG, "loadAd: 3. 使用新创建的广告单元 ID 替换当前测试 ID")
                        Log.e(TAG, "loadAd: 4. 等待几分钟让 AdMob 系统更新配置")
                    }
                    
                    // 打印 ResponseInfo（如果有）
                    loadAdError.responseInfo?.let { responseInfo ->
                        Log.d(TAG, "loadAd: ResponseInfo - responseId=${responseInfo.responseId}")
                        Log.d(TAG, "loadAd: ResponseInfo - mediationAdapterClassName=${responseInfo.mediationAdapterClassName}")
                    } ?: run {
                        Log.d(TAG, "loadAd: ResponseInfo - responseId=null")
                        Log.d(TAG, "loadAd: ResponseInfo - mediationAdapterClassName=")
                    }
                    
                    isLoadingAd = false
                    appOpenAd = null
                    
                    // 通知监听器
                    onAdLoadListener?.onAdFailedToLoad()
                }
            }
        )
        Log.d(TAG, "loadAd: AppOpenAd.load() 已调用，等待回调")
    }
    
    /**
     * 显示广告（如果可用）
     */
    fun showAdIfAvailable(activity: Activity) {
        Log.d(TAG, "showAdIfAvailable: ========== 尝试显示广告 ==========")
        Log.d(TAG, "showAdIfAvailable: Activity=${activity.javaClass.simpleName}")
        Log.d(TAG, "showAdIfAvailable: Activity 是否 finishing=${activity.isFinishing}")
        Log.d(TAG, "showAdIfAvailable: 当前状态=${getAdStatus()}")
        
        // 检查 Activity 是否已经 finish 或 destroy
        if (activity.isFinishing || activity.isDestroyed) {
            Log.w(TAG, "showAdIfAvailable: ⚠️⚠️⚠️ Activity 已销毁，无法显示广告 ⚠️⚠️⚠️")
            return
        }
        
        // 如果正在显示广告，则跳过
        if (isShowingAd) {
            Log.d(TAG, "showAdIfAvailable: ⚠️ 正在显示广告，跳过")
            return
        }
        
        // 如果广告不可用，尝试加载
        if (!isAdAvailable()) {
            Log.d(TAG, "showAdIfAvailable: ⚠️ 广告不可用，开始加载")
            loadAd()
            return
        }
        
        appOpenAd?.let { ad ->
            Log.d(TAG, "showAdIfAvailable: ✅ 广告可用，准备显示")
            isShowingAd = true
            Log.d(TAG, "showAdIfAvailable: 设置 FullScreenContentCallback")
            
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "showAdIfAvailable: ✅✅✅ onAdDismissedFullScreenContent - 广告已关闭")
                    appOpenAd = null
                    isShowingAd = false
                    loadAd() // 预加载下一个广告
                }
                
                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.e(TAG, "showAdIfAvailable: ❌❌❌ onAdFailedToShowFullScreenContent - 广告显示失败")
                    Log.e(TAG, "showAdIfAvailable: 错误代码=${adError.code}")
                    Log.e(TAG, "showAdIfAvailable: 错误域=${adError.domain}")
                    Log.e(TAG, "showAdIfAvailable: 错误消息=${adError.message}")
                    appOpenAd = null
                    isShowingAd = false
                    loadAd() // 尝试加载下一个广告
                }
                
                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "showAdIfAvailable: ✅✅✅ onAdShowedFullScreenContent - 广告已显示！")
                }
                
                override fun onAdImpression() {
                    Log.d(TAG, "showAdIfAvailable: ✅ onAdImpression - 广告展示已记录")
                }
            }
            
            Log.d(TAG, "showAdIfAvailable: 调用 ad.show(activity)")
            try {
                ad.show(activity)
                Log.d(TAG, "showAdIfAvailable: ✅ ad.show() 调用成功")
            } catch (e: Exception) {
                Log.e(TAG, "showAdIfAvailable: ❌ ad.show() 调用异常", e)
                isShowingAd = false
            }
        } ?: run {
            Log.e(TAG, "showAdIfAvailable: ❌ appOpenAd 为空，无法显示")
        }
    }
    
    /**
     * 获取当前广告状态（用于调试）
     */
    fun getAdStatus(): String {
        return "AppOpenAdManager Status: " +
            "isLoadingAd=$isLoadingAd, " +
            "isShowingAd=$isShowingAd, " +
            "isAdAvailable=${isAdAvailable()}, " +
            "loadTime=$loadTime, " +
            "isAdExpired=${isAdExpired()}"
    }
}

