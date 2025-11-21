package com.amaze.filemanager.ads

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.amaze.filemanager.application.AppConfig

/**
 * MyApplication
 * 实现 ActivityLifecycleCallbacks + ProcessLifecycleObserver
 * 管理 App Open Ad 的生命周期
 * 
 * Logcat 筛选关键字: MyApplication
 */
class MyApplication : AppConfig(), Application.ActivityLifecycleCallbacks, DefaultLifecycleObserver {
    
    companion object {
        private const val TAG = "MyApplication"
    }
    
    private var appOpenAdManager: AppOpenAdManager? = null
    private var currentActivity: Activity? = null
    
    override fun onCreate() {
        super<AppConfig>.onCreate()
        Log.d(TAG, "onCreate: MyApplication 初始化")
        
        registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }
    
    /**
     * ProcessLifecycleObserver - 应用进入前台
     */
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        Log.d(TAG, "onStart: 应用进入前台")
        currentActivity?.let { activity ->
            appOpenAdManager?.showAdIfAvailable(activity)
        }
    }
    
    /**
     * ProcessLifecycleObserver - 应用进入后台
     */
    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        Log.d(TAG, "onStop: 应用进入后台")
    }
    
    /**
     * ActivityLifecycleCallbacks - Activity 创建
     */
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        Log.d(TAG, "onActivityCreated: ${activity.javaClass.simpleName}")
    }
    
    /**
     * ActivityLifecycleCallbacks - Activity 开始
     */
    override fun onActivityStarted(activity: Activity) {
        Log.d(TAG, "onActivityStarted: ${activity.javaClass.simpleName}")
        currentActivity = activity
    }
    
    /**
     * ActivityLifecycleCallbacks - Activity 恢复
     */
    override fun onActivityResumed(activity: Activity) {
        Log.d(TAG, "onActivityResumed: ${activity.javaClass.simpleName}")
        currentActivity = activity
    }
    
    /**
     * ActivityLifecycleCallbacks - Activity 暂停
     */
    override fun onActivityPaused(activity: Activity) {
        Log.d(TAG, "onActivityPaused: ${activity.javaClass.simpleName}")
    }
    
    /**
     * ActivityLifecycleCallbacks - Activity 停止
     */
    override fun onActivityStopped(activity: Activity) {
        Log.d(TAG, "onActivityStopped: ${activity.javaClass.simpleName}")
        if (currentActivity == activity) {
            currentActivity = null
        }
    }
    
    /**
     * ActivityLifecycleCallbacks - Activity 保存状态
     */
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        Log.d(TAG, "onActivitySaveInstanceState: ${activity.javaClass.simpleName}")
    }
    
    /**
     * ActivityLifecycleCallbacks - Activity 销毁
     */
    override fun onActivityDestroyed(activity: Activity) {
        Log.d(TAG, "onActivityDestroyed: ${activity.javaClass.simpleName}")
        if (currentActivity == activity) {
            currentActivity = null
        }
    }
    
    /**
     * 初始化 AppOpenAdManager
     */
    fun initializeAppOpenAdManager() {
        Log.d(TAG, "initializeAppOpenAdManager: ========== 初始化 AppOpenAdManager ==========")
        if (appOpenAdManager == null) {
            Log.d(TAG, "initializeAppOpenAdManager: 创建新的 AppOpenAdManager 实例")
            appOpenAdManager = AppOpenAdManager(this)
            Log.d(TAG, "initializeAppOpenAdManager: ✅ AppOpenAdManager 创建成功")
        } else {
            Log.d(TAG, "initializeAppOpenAdManager: ⚠️ AppOpenAdManager 已存在，跳过初始化")
        }
    }
    
    /**
     * 加载广告
     */
    fun loadAd(onAdLoadListener: AppOpenAdManager.OnAdLoadListener? = null) {
        Log.d(TAG, "loadAd: ========== MyApplication.loadAd() ==========")
        if (appOpenAdManager == null) {
            Log.e(TAG, "loadAd: ❌ AppOpenAdManager 为空，无法加载广告")
            return
        }
        Log.d(TAG, "loadAd: 调用 AppOpenAdManager.loadAd()")
        appOpenAdManager?.setOnAdLoadListener(onAdLoadListener)
        appOpenAdManager?.loadAd()
    }
    
    /**
     * 显示广告（如果可用）
     */
    fun showAdIfAvailable(activity: Activity) {
        Log.d(TAG, "showAdIfAvailable: ========== MyApplication.showAdIfAvailable() ==========")
        Log.d(TAG, "showAdIfAvailable: Activity=${activity.javaClass.simpleName}")
        if (appOpenAdManager == null) {
            Log.e(TAG, "showAdIfAvailable: ❌ AppOpenAdManager 为空，无法显示广告")
            return
        }
        Log.d(TAG, "showAdIfAvailable: 调用 AppOpenAdManager.showAdIfAvailable()")
        appOpenAdManager?.showAdIfAvailable(activity)
    }
    
    /**
     * 获取 AppOpenAdManager（用于调试）
     */
    fun getAppOpenAdManager(): AppOpenAdManager? {
        Log.d(TAG, "getAppOpenAdManager: 返回 AppOpenAdManager，是否为 null=${appOpenAdManager == null}")
        return appOpenAdManager
    }
}

