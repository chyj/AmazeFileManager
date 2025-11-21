# AdMob App Open Ad 集成说明

## 概述

本项目已集成 Google AdMob App Open Ad，按照官方文档要求实现。

## 关键组件

### 1. AppOpenAdManager (`com.amaze.filemanager.ads.AppOpenAdManager`)
- 管理 App Open 广告的加载、显示、过期判断
- 实现 FullScreenContentCallback 处理广告生命周期
- 维护状态位（isLoadingAd, isShowingAd）
- 记录详细的日志和 ResponseInfo

**Logcat 关键字**: `AppOpenAdManager`

### 2. GoogleMobileAdsConsentManager (`com.amaze.filemanager.ads.GoogleMobileAdsConsentManager`)
- 管理 UMP (User Messaging Platform) 同意表单
- 处理同意信息的初始化和更新
- 提供同意表单的加载和显示功能

**Logcat 关键字**: `ConsentManager`

### 3. MyApplication (`com.amaze.filemanager.ads.MyApplication`)
- 继承自 AppConfig
- 实现 ActivityLifecycleCallbacks 和 ProcessLifecycleObserver
- 管理应用生命周期，自动显示 App Open Ad

**Logcat 关键字**: `MyApplication`

### 4. SplashActivity (`com.amaze.filemanager.ui.activities.SplashActivity`)
- 作为应用的启动 Activity
- 串联整个流程：consent -> MobileAds.initialize -> loadAd -> showAdIfAvailable
- 提供倒计时和冷启动体验

**Logcat 关键字**: `SplashActivity`

## Logcat 筛选关键字

在 Android Studio 的 Logcat 中，可以使用以下关键字筛选相关日志：

### 主要关键字（推荐使用）
- `AppOpenAdManager` - 广告管理器相关日志
- `ConsentManager` - 同意表单管理器相关日志
- `MyApplication` - Application 生命周期相关日志
- `SplashActivity` - 启动页面相关日志

### 组合筛选
可以使用多个关键字组合筛选：
- `AppOpenAdManager | ConsentManager` - 同时查看广告和同意表单日志
- `MyApplication | SplashActivity` - 同时查看应用和启动页面日志

### 完整筛选表达式（推荐）
```
tag:AppOpenAdManager | tag:ConsentManager | tag:MyApplication | tag:SplashActivity
```

## 配置说明

### 1. AdMob App ID
在 `AndroidManifest.xml` 中配置了测试 App ID：
```xml
<meta-data
    android:name="com.google.android.gms.ads.APPLICATION_ID"
    android:value="ca-app-pub-3940256099942544~3347511713"/>
```

**重要**: 需要替换为实际的 AdMob App ID。

### 2. 广告单元 ID
在 `AppOpenAdManager.kt` 中配置了测试广告单元 ID：
```kotlin
private const val AD_UNIT_ID = "ca-app-pub-3940256099942544/3419835294"
```

**重要**: 需要替换为实际的 App Open Ad 单元 ID。

### 3. 测试设备 ID
在 `GoogleMobileAdsConsentManager.kt` 中配置了测试设备 ID：
```kotlin
private const val TEST_DEVICE_ID = "TEST_DEVICE_ID_HERE"
```

**重要**: 需要替换为实际的测试设备 ID（可通过 Logcat 查看）。

## 功能说明

### 1. 启动流程
1. SplashActivity 启动
2. 初始化 GoogleMobileAdsConsentManager
3. 检查并显示同意表单（如需要）
4. 初始化 MobileAds SDK
5. 加载 App Open Ad
6. 显示广告（如果可用）
7. 广告关闭后进入 MainActivity

### 2. 应用生命周期管理
- MyApplication 监听应用进入前台事件
- 当应用从后台返回前台时，自动显示 App Open Ad（如果可用）

### 3. 隐私设置
- 在主界面菜单中提供"隐私设置"入口
- 可以重置同意状态并重新显示同意表单

### 4. Ad Inspector
- 在主界面菜单中提供"Ad Inspector"调试按钮
- 可以查看广告调试信息

## 注意事项

1. **Flavor 区分**: 仅在 `play` flavor 中启用 AdMob，`fdroid` flavor 中不包含相关功能
2. **广告过期**: 广告加载后 4 小时过期，过期后会自动重新加载
3. **最小显示时间**: SplashActivity 至少显示 2 秒，确保良好的用户体验
4. **错误处理**: 所有关键操作都有错误处理和日志记录

## 调试建议

1. 使用 Logcat 关键字筛选相关日志
2. 检查同意表单是否正确显示
3. 确认 MobileAds SDK 初始化成功
4. 查看广告加载和显示的状态日志
5. 使用 Ad Inspector 查看广告调试信息

## 常见问题

### Q: 广告不显示？
A: 检查以下几点：
- 确认使用的是 play flavor
- 查看 Logcat 中的错误日志
- 确认同意表单已同意
- 检查网络连接
- 确认 AdMob App ID 和广告单元 ID 配置正确

### Q: 如何查看测试设备 ID？
A: 在 Logcat 中搜索 "TEST_DEVICE_ID" 或查看 AdMob 控制台。

### Q: 同意表单不显示？
A: 检查以下几点：
- 确认测试设备 ID 配置正确
- 检查 DebugGeography 设置
- 查看 ConsentManager 相关日志

## 相关文档

- [AdMob Android 快速开始](https://developers.google.com/admob/android/quick-start)
- [App Open Ad 集成指南](https://developers.google.com/admob/android/app-open)

