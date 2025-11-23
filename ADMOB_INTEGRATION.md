# Google AdMob 插页式广告集成说明

本文档说明如何完成 Google AdMob 插页式广告的集成和配置。

## 📋 已完成的工作

### 1. Gradle 配置
- ✅ 在 `gradle/libs.versions.toml` 中添加了 AdMob 和 UMP 依赖版本
- ✅ 在 `app/build.gradle` 中添加了依赖（仅 play flavor）

### 2. AndroidManifest 配置
- ✅ 在 `AndroidManifest.xml` 中添加了 AdMob App ID 元数据（当前为测试 ID）

### 3. 核心类实现
- ✅ `GoogleMobileAdsConsentManager.kt` - UMP 同意管理器
- ✅ `InterstitialAdManager.kt` - 插页式广告管理器
- ✅ `AdMobInitializer.kt` - AdMob SDK 初始化器
- ✅ `MainActivityAdMobHelper.kt` - MainActivity 的 AdMob 集成辅助类
- ✅ `AppConfigAdMob.kt` - AppConfig 的 AdMob 扩展

### 4. 集成点
- ✅ `AppConfig.onCreate()` - 初始化 MobileAds SDK
- ✅ `MainActivity.onCreate()` - 初始化 UMP 同意流程和广告加载
- ✅ `MainActivity.goToMain()` - 在用户返回到主界面时显示广告
- ✅ `MainActivity.onDestroy()` - 清理广告资源

## 🔧 需要替换的配置

### 1. AdMob App ID（必需）

**位置：** `app/src/main/AndroidManifest.xml`

```xml
<meta-data
    android:name="com.google.android.gms.ads.APPLICATION_ID"
    android:value="ca-app-pub-3940256099942544~3347511713"/>
```

**操作：**
- 将 `ca-app-pub-3940256099942544~3347511713` 替换为您的实际 AdMob App ID
- App ID 格式：`ca-app-pub-XXXXXXXXXXXXXXXX~XXXXXXXXXX`
- 在 [AdMob 控制台](https://apps.admob.com/) 获取您的 App ID

### 2. 插页式广告位 ID（必需）

**位置：** `app/src/play/java/com/amaze/filemanager/utils/InterstitialAdManager.kt`

```kotlin
// TODO: 替换为您的插页式广告位 ID。格式：ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX
// 测试广告位 ID：ca-app-pub-3940256099942544/1033173712
private val adUnitId = "ca-app-pub-3940256099942544/1033173712"
```

**操作：**
- 将测试广告位 ID 替换为您的实际插页式广告位 ID
- 广告位 ID 格式：`ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX`
- 在 AdMob 控制台创建插页式广告单元并获取广告位 ID

### 3. 测试设备 ID（可选，用于开发测试）

**位置：** `app/src/play/java/com/amaze/filemanager/utils/GoogleMobileAdsConsentManager.kt`

```kotlin
// TODO: 替换为您的测试设备 ID（通过 logcat 查看 "To get test ads on this device, set" 消息）
val debugSettings = ConsentDebugSettings.Builder(activity)
    // .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA) // 仅用于测试
    // .addTestDeviceHashedId("YOUR_TEST_DEVICE_ID") // 替换为您的测试设备 ID
    .build()
```

**操作：**
1. 运行应用并查看 logcat
2. 查找包含 "To get test ads on this device, set" 的日志消息
3. 复制设备 ID 并取消注释相关代码行

**位置：** `app/src/play/java/com/amaze/filemanager/application/AppConfigAdMob.kt`

```kotlin
// TODO: 添加您的测试设备 ID（通过 logcat 查看 "To get test ads on this device, set" 消息）
val testDeviceIds = listOf(
    // "YOUR_TEST_DEVICE_ID_HERE"
)
```

## 📱 QA 验证步骤

### 1. 测试设备配置

1. **获取测试设备 ID：**
   - 运行应用
   - 查看 logcat，查找包含 "To get test ads on this device" 的消息
   - 复制设备 ID

2. **配置测试设备：**
   - 在 `GoogleMobileAdsConsentManager.kt` 和 `AppConfigAdMob.kt` 中添加测试设备 ID
   - 重新编译并运行应用

### 2. UMP 同意流程测试

1. **测试同意表单显示：**
   - 首次启动应用
   - 应该看到 UMP 同意表单（如果适用）
   - 完成同意流程

2. **测试隐私选项：**
   - 在设置中提供入口调用 `showPrivacyOptionsForm()`
   - 验证用户可以更改同意选择

### 3. 插页式广告测试

1. **测试广告加载：**
   - 查看 logcat，确认看到 "插页式广告加载成功" 消息
   - 如果加载失败，检查错误信息

2. **测试广告显示：**
   - 返回到主界面（调用 `goToMain()`）
   - 应该看到插页式广告显示
   - 验证广告关闭后正常返回应用

3. **测试广告关闭：**
   - 关闭广告后，验证应用流程正常
   - 验证广告关闭后自动加载下一个广告

### 4. Ad Inspector 测试（推荐）

1. **启用 Ad Inspector：**
   - 在 AdMob 控制台启用 Ad Inspector
   - 在测试设备上长按应用图标
   - 选择 "Ad Inspector" 选项

2. **验证广告：**
   - 使用 Ad Inspector 验证广告是否正确加载
   - 检查广告请求和响应

### 5. 生产环境测试

1. **使用真实广告位 ID：**
   - 替换所有测试广告位 ID 为真实 ID
   - 移除或注释测试设备配置

2. **验证同意流程：**
   - 在真实环境中测试 UMP 同意流程
   - 验证不同地区的同意表单显示

3. **监控广告性能：**
   - 在 AdMob 控制台监控广告展示和收入
   - 检查错误报告

## 📝 关键实现细节

### 1. 单例模式
- `GoogleMobileAdsConsentManager` 和 `InterstitialAdManager` 使用单例模式
- 确保每个应用会话只有一个实例

### 2. 反射调用
- 由于 AdMob 代码仅在 play flavor 中，main 代码使用反射调用
- 如果类不存在（fdroid flavor），会优雅地跳过

### 3. 广告显示时机
- 当前实现在用户返回到主界面时显示广告
- 避免首次启动就显示广告（使用 `isFirstLaunch` 标志）
- 可以根据业务需求调整显示逻辑

### 4. 错误处理
- 所有 AdMob 操作都有错误处理
- 错误信息记录到 logcat，便于调试

## 🚨 注意事项

1. **仅 play flavor：**
   - 所有 AdMob 相关代码仅在 play flavor 中编译
   - fdroid flavor 不会包含 AdMob 代码

2. **网络权限：**
   - 确保 `AndroidManifest.xml` 中有 `INTERNET` 权限（已存在）

3. **同意流程：**
   - 必须在用户同意后才能加载广告
   - 遵循 GDPR、CCPA 等隐私法规要求

4. **广告频率：**
   - 避免过于频繁地显示广告，影响用户体验
   - 当前实现避免首次启动就显示广告

5. **测试广告：**
   - 在开发阶段使用测试广告位 ID
   - 发布前替换为真实广告位 ID

## 📚 参考资源

- [Google AdMob 快速入门](https://developers.google.com/admob/android/quick-start)
- [插页式广告指南](https://developers.google.com/admob/android/interstitial)
- [UMP SDK 文档](https://developers.google.com/admob/ump/android/quick-start)
- [Ad Inspector 使用指南](https://support.google.com/admob/answer/9691433)

## 🔍 调试技巧

1. **查看日志：**
   - 使用 logcat 过滤 "AdMob"、"InterstitialAd"、"GoogleMobileAdsConsent" 标签
   - 查看详细的加载和显示日志

2. **常见问题：**
   - **广告不显示：** 检查是否已同意、广告是否加载成功、广告位 ID 是否正确
   - **同意表单不显示：** 检查网络连接、UMP 配置是否正确
   - **广告加载失败：** 检查网络权限、App ID 是否正确、广告位 ID 是否有效

3. **测试命令：**
   ```bash
   # 查看 AdMob 相关日志
   adb logcat | grep -E "AdMob|InterstitialAd|GoogleMobileAdsConsent"
   ```

## ✅ 检查清单

在发布前，请确认：

- [ ] 已替换 AdMob App ID
- [ ] 已替换插页式广告位 ID
- [ ] 已配置测试设备 ID（开发阶段）
- [ ] 已测试 UMP 同意流程
- [ ] 已测试广告加载和显示
- [ ] 已测试广告关闭后的应用流程
- [ ] 已移除或注释测试设备配置（生产环境）
- [ ] 已使用 Ad Inspector 验证广告
- [ ] 已检查 logcat 无错误信息

---

**最后更新：** 2024年
**维护者：** Android 开发团队

