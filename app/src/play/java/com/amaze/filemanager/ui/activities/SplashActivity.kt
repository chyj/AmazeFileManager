package com.amaze.filemanager.ui.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.amaze.filemanager.R
import com.amaze.filemanager.ads.AppOpenAdManager
import com.amaze.filemanager.ads.GoogleMobileAdsConsentManager
import com.amaze.filemanager.ads.MyApplication
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.InitializationStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * SplashActivity
 * 串联 consent -> MobileAds.initialize -> loadAd -> showAdIfAvailable 的流程
 * 提供倒计时/冷启动体验
 * 
 * Logcat 筛选关键字: SplashActivity
 */
class SplashActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "SplashActivity"
        private const val MIN_SPLASH_DURATION_MS = 2000L // 最小启动画面显示时间（毫秒）
        private const val COUNTDOWN_DURATION_MS = 3000L // 倒计时总时长（毫秒）
    }
    
    private var consentManager: GoogleMobileAdsConsentManager? = null
    private var startTime: Long = 0
    private var isAdShown = false
    private var countdownHandler: Handler? = null
    private var countdownRunnable: Runnable? = null
    private var adLoadListener: AppOpenAdManager.OnAdLoadListener? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "========================================")
        Log.d(TAG, "onCreate: ✅✅✅ PLAY FLAVOR SplashActivity 启动 ✅✅✅")
        Log.d(TAG, "onCreate: 这是包含 AdMob 的完整版本")
        Log.d(TAG, "========================================")
        
        setContentView(R.layout.activity_splash)
        
        startTime = System.currentTimeMillis()
        
        Log.d(TAG, "onCreate: Play flavor，开始 AdMob 初始化流程")
        initializeAdMob()
    }
    
    /**
     * 初始化 AdMob
     */
    private fun initializeAdMob() {
        Log.d(TAG, "initializeAdMob: 开始初始化")
        
        consentManager = GoogleMobileAdsConsentManager(this)
        consentManager?.initializeConsentInformation()
        
        // 等待同意信息更新后，检查是否需要显示同意表单
        Handler(Looper.getMainLooper()).postDelayed({
            checkConsentAndInitialize()
        }, 500) // 给一点时间让同意信息更新
    }
    
    /**
     * 检查同意状态并初始化 MobileAds
     */
    private fun checkConsentAndInitialize() {
        Log.d(TAG, "checkConsentAndInitialize: 检查同意状态")
        
        val consentManager = this.consentManager ?: run {
            Log.e(TAG, "checkConsentAndInitialize: consentManager 为空")
            navigateToMainActivity()
            return
        }
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // 如果需要显示同意表单（已启用绕过模式时会跳过）
                if (consentManager.isConsentFormAvailable()) {
                    Log.d(TAG, "checkConsentAndInitialize: 需要显示同意表单")
                    
                    // 加载同意表单
                    val loadResult = withContext(Dispatchers.IO) {
                        consentManager.loadConsentForm()
                    }
                    
                    if (loadResult.isSuccess) {
                        // 显示同意表单
                        val showResult = withContext(Dispatchers.IO) {
                            consentManager.showConsentForm()
                        }
                        
                        if (showResult.isFailure) {
                            Log.e(TAG, "checkConsentAndInitialize: 显示同意表单失败", showResult.exceptionOrNull())
                        }
                    } else {
                        Log.e(TAG, "checkConsentAndInitialize: 加载同意表单失败", loadResult.exceptionOrNull())
                    }
                } else {
                    Log.d(TAG, "checkConsentAndInitialize: 跳过同意表单显示（绕过模式或不需要）")
                }
                
                // 等待同意信息更新（绕过模式下直接继续）
                Handler(Looper.getMainLooper()).postDelayed({
                    initializeMobileAds()
                }, 500)
                
            } catch (e: Exception) {
                Log.e(TAG, "checkConsentAndInitialize: 异常", e)
                initializeMobileAds()
            }
        }
    }
    
    /**
     * 初始化 MobileAds SDK
     */
    private fun initializeMobileAds() {
        Log.d(TAG, "initializeMobileAds: ========== 开始初始化 MobileAds SDK ==========")
        
        // 检查是否可以请求广告
        val canRequestAds = consentManager?.canRequestAds() ?: false
        Log.d(TAG, "initializeMobileAds: canRequestAds=$canRequestAds")
        
        if (!canRequestAds) {
            Log.w(TAG, "initializeMobileAds: ❌ 无法请求广告，直接进入主界面")
            navigateToMainActivity()
            return
        }
        
        Log.d(TAG, "initializeMobileAds: 调用 MobileAds.initialize()")
        MobileAds.initialize(this) { initializationStatus: InitializationStatus ->
            Log.d(TAG, "initializeMobileAds: ✅ MobileAds 初始化完成")
            
            val adapterStatuses = initializationStatus.adapterStatusMap
            Log.d(TAG, "initializeMobileAds: Adapter 数量=${adapterStatuses.size}")
            adapterStatuses.forEach { (adapter, status) ->
                Log.d(TAG, "initializeMobileAds: Adapter $adapter - " +
                    "state=${status.initializationState}, " +
                    "description=${status.description}")
            }
            
            // 检查 Application 类型
            Log.d(TAG, "initializeMobileAds: 检查 Application 类型")
            Log.d(TAG, "initializeMobileAds: application 类型=${application.javaClass.name}")
            val myApplication = application as? MyApplication
            if (myApplication == null) {
                Log.e(TAG, "initializeMobileAds: ❌ Application 不是 MyApplication 类型！实际类型=${application.javaClass.name}")
                Log.e(TAG, "initializeMobileAds: 请检查 AndroidManifest.xml 中的 android:name 配置")
                navigateToMainActivity()
                return@initialize
            }
            Log.d(TAG, "initializeMobileAds: ✅ Application 类型转换成功")
            
            // 初始化 AppOpenAdManager
            Log.d(TAG, "initializeMobileAds: 初始化 AppOpenAdManager")
            myApplication.initializeAppOpenAdManager()
            val appOpenAdManager = myApplication.getAppOpenAdManager()
            if (appOpenAdManager == null) {
                Log.e(TAG, "initializeMobileAds: ❌ AppOpenAdManager 初始化失败")
                navigateToMainActivity()
                return@initialize
            }
            Log.d(TAG, "initializeMobileAds: ✅ AppOpenAdManager 初始化成功")
            Log.d(TAG, "initializeMobileAds: 广告状态=${appOpenAdManager.getAdStatus()}")
            
            // 加载广告，并设置回调监听
            Log.d(TAG, "initializeMobileAds: 开始加载广告")
            adLoadListener = object : AppOpenAdManager.OnAdLoadListener {
                override fun onAdLoaded() {
                    Log.d(TAG, "initializeMobileAds: ✅ 广告加载完成回调，尝试显示广告")
                    // 检查 Activity 是否已经 finish
                    if (isFinishing || isDestroyed) {
                        Log.w(TAG, "initializeMobileAds: ⚠️ Activity 已销毁，忽略广告加载回调")
                        return
                    }
                    // 广告加载成功，立即尝试显示
                    Handler(Looper.getMainLooper()).post {
                        if (!isFinishing && !isDestroyed) {
                            showAdIfAvailable()
                        }
                    }
                }
                
                override fun onAdFailedToLoad() {
                    Log.d(TAG, "initializeMobileAds: ❌ 广告加载失败回调，直接进入主界面")
                    // 检查 Activity 是否已经 finish
                    if (isFinishing || isDestroyed) {
                        Log.w(TAG, "initializeMobileAds: ⚠️ Activity 已销毁，忽略广告加载失败回调")
                        return
                    }
                    // 广告加载失败，直接进入主界面
                    Handler(Looper.getMainLooper()).post {
                        if (!isFinishing && !isDestroyed) {
                            navigateToMainActivity()
                        }
                    }
                }
            }
            myApplication.loadAd(adLoadListener)
            
            // 设置超时：如果 5 秒内广告还没加载完成，也进入主界面
            Handler(Looper.getMainLooper()).postDelayed({
                val currentAdManager = myApplication.getAppOpenAdManager()
                val isAdAvailable = currentAdManager?.isAdAvailable() ?: false
                val isLoading = currentAdManager?.let { 
                    // 通过反射或添加方法检查是否正在加载
                    false // 简化处理，如果超时且广告不可用，就进入主界面
                } ?: false
                
                Log.d(TAG, "initializeMobileAds: ⏰ 超时检查 - isAdAvailable=$isAdAvailable")
                if (!isAdAvailable && !isAdShown) {
                    Log.w(TAG, "initializeMobileAds: ⚠️ 广告加载超时，进入主界面")
                    navigateToMainActivity()
                }
            }, 5000) // 5 秒超时
        }
    }
    
    /**
     * 显示广告（如果可用）
     */
    private fun showAdIfAvailable() {
        Log.d(TAG, "showAdIfAvailable: ========== 尝试显示广告 ==========")
        
        val myApplication = application as? MyApplication
        if (myApplication == null) {
            Log.e(TAG, "showAdIfAvailable: ❌ Application 不是 MyApplication 类型")
            navigateToMainActivity()
            return
        }
        
        val appOpenAdManager = myApplication.getAppOpenAdManager()
        if (appOpenAdManager == null) {
            Log.e(TAG, "showAdIfAvailable: ❌ AppOpenAdManager 为空")
            navigateToMainActivity()
            return
        }
        
        Log.d(TAG, "showAdIfAvailable: AppOpenAdManager 状态=${appOpenAdManager.getAdStatus()}")
        val isAdAvailable = appOpenAdManager.isAdAvailable()
        Log.d(TAG, "showAdIfAvailable: isAdAvailable=$isAdAvailable")
        
        if (isAdAvailable) {
            Log.d(TAG, "showAdIfAvailable: ✅ 广告可用，准备显示")
            Log.d(TAG, "showAdIfAvailable: 当前 Activity=${this.javaClass.simpleName}")
            Log.d(TAG, "showAdIfAvailable: Activity 是否 finishing=${isFinishing}")
            isAdShown = true
            myApplication.showAdIfAvailable(this)
            Log.d(TAG, "showAdIfAvailable: 已调用 showAdIfAvailable，等待广告显示")
        } else {
            Log.w(TAG, "showAdIfAvailable: ⚠️ 广告不可用")
            Log.d(TAG, "showAdIfAvailable: 广告状态详情=${appOpenAdManager.getAdStatus()}")
            Log.d(TAG, "showAdIfAvailable: 直接进入主界面")
            navigateToMainActivity()
        }
    }
    
    /**
     * 导航到主界面
     */
    private fun navigateToMainActivity() {
        val elapsedTime = System.currentTimeMillis() - startTime
        val remainingTime = MIN_SPLASH_DURATION_MS - elapsedTime
        
        if (remainingTime > 0) {
            Log.d(TAG, "navigateToMainActivity: 等待最小显示时间 ${remainingTime}ms")
            Handler(Looper.getMainLooper()).postDelayed({
                doNavigateToMainActivity()
            }, remainingTime)
        } else {
            doNavigateToMainActivity()
        }
    }
    
    /**
     * 执行导航到主界面
     */
    private fun doNavigateToMainActivity() {
        Log.d(TAG, "doNavigateToMainActivity: 进入主界面")
        
        // 停止倒计时
        stopCountdown()
        
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
    
    /**
     * 开始倒计时
     */
    private fun startCountdown() {
        val countdownText = findViewById<TextView>(R.id.splash_countdown)
        countdownText?.visibility = View.VISIBLE
        
        var remainingSeconds = (COUNTDOWN_DURATION_MS / 1000).toInt()
        
        countdownRunnable = object : Runnable {
            override fun run() {
                if (remainingSeconds > 0) {
                    countdownText?.text = getString(R.string.splash_countdown, remainingSeconds)
                    remainingSeconds--
                    countdownHandler?.postDelayed(this, 1000)
                } else {
                    countdownText?.visibility = View.GONE
                }
            }
        }
        
        countdownHandler = Handler(Looper.getMainLooper())
        countdownHandler?.post(countdownRunnable!!)
    }
    
    /**
     * 停止倒计时
     */
    private fun stopCountdown() {
        countdownHandler?.removeCallbacks(countdownRunnable ?: return)
        countdownRunnable = null
        countdownHandler = null
        
        findViewById<TextView>(R.id.splash_countdown)?.visibility = View.GONE
    }
    
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: SplashActivity")
        
        // 如果广告已显示并关闭，进入主界面
        if (isAdShown) {
            val myApplication = application as? MyApplication
            val appOpenAdManager = myApplication?.getAppOpenAdManager()
            
            if (appOpenAdManager?.isAdAvailable() != true) {
                Log.d(TAG, "onResume: 广告已关闭，进入主界面")
                navigateToMainActivity()
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: SplashActivity")
        stopCountdown()
        
        // 清除广告加载回调，防止在 Activity 销毁后仍然触发回调
        val myApplication = application as? MyApplication
        myApplication?.getAppOpenAdManager()?.setOnAdLoadListener(null)
        adLoadListener = null
        Log.d(TAG, "onDestroy: 已清除广告加载回调")
    }
}

