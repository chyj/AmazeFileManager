package com.amaze.filemanager.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.amaze.filemanager.R

/**
 * SplashActivity (F-Droid 版本)
 * 简单的启动页面，直接跳转到主界面
 * 
 * Logcat 筛选关键字: SplashActivity
 */
class SplashActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "SplashActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "========================================")
        Log.d(TAG, "onCreate: ⚠️⚠️⚠️ F-DROID FLAVOR SplashActivity 启动 ⚠️⚠️⚠️")
        Log.d(TAG, "onCreate: 这是简单版本，不包含 AdMob")
        Log.d(TAG, "onCreate: 如果要测试广告，请切换到 playDebug 构建变体")
        Log.d(TAG, "========================================")
        
        setContentView(R.layout.activity_splash)
        
        // 直接跳转到主界面
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}

