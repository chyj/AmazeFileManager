# Logcat 筛选关键字

## 原生广告相关日志筛选

### 方法 1: 使用单个标签筛选（推荐）
```bash
adb logcat -s NativeAdHelper:* MainActivity:* GoogleMobileAdsConsentManager:*
```

### 方法 2: 使用包名筛选
```bash
adb logcat | grep -E "(NativeAdHelper|MainActivity|GoogleMobileAdsConsentManager|Ads)"
```

### 方法 3: 使用关键词筛选（最全面）
```bash
adb logcat | grep -E "(原生广告|NativeAdHelper|AdMob|loadNativeAd|initializeNativeAd|onAdLoaded|onAdFailed)"
```

### 方法 4: 组合筛选（包含所有相关信息）
```bash
adb logcat -s NativeAdHelper:D MainActivity:I GoogleMobileAdsConsentManager:D Ads:I *:S
```

### 方法 5: 仅显示错误和警告
```bash
adb logcat -s NativeAdHelper:E MainActivity:W GoogleMobileAdsConsentManager:E Ads:W
```

## 详细日志级别说明

- `V` = Verbose（详细）
- `D` = Debug（调试）
- `I` = Info（信息）
- `W` = Warning（警告）
- `E` = Error（错误）

## 常用筛选命令

### 查看所有原生广告相关日志（包含调试信息）
```bash
adb logcat -s NativeAdHelper:D MainActivity:I GoogleMobileAdsConsentManager:D
```

### 实时查看并保存到文件
```bash
adb logcat -s NativeAdHelper:D MainActivity:I GoogleMobileAdsConsentManager:D > native_ad_logs.txt
```

### 清除日志缓冲区后重新查看
```bash
adb logcat -c && adb logcat -s NativeAdHelper:D MainActivity:I GoogleMobileAdsConsentManager:D
```

## 关键日志标签

1. **NativeAdHelper** - 原生广告辅助类日志
   - 广告加载流程
   - SDK 初始化
   - 广告视图填充

2. **MainActivity** - MainActivity 中的广告相关日志
   - 初始化流程
   - 反射调用
   - 广告显示

3. **GoogleMobileAdsConsentManager** - 用户同意管理日志
   - 同意状态检查
   - 同意表单显示

4. **Ads** - Google AdMob SDK 内部日志
   - 广告请求
   - 广告响应
   - 错误信息

## 快速调试命令

### 查看广告初始化日志
```bash
adb logcat -s NativeAdHelper:D MainActivity:I | grep -E "(初始化|initialize|开始)"
```

### 查看广告加载日志
```bash
adb logcat -s NativeAdHelper:D MainActivity:I | grep -E "(加载|load|onAdLoaded|onAdFailed)"
```

### 查看错误日志
```bash
adb logcat -s NativeAdHelper:E MainActivity:E GoogleMobileAdsConsentManager:E Ads:E
```

## Android Studio Logcat 筛选器设置

在 Android Studio 的 Logcat 窗口中，可以使用以下筛选表达式：

```
tag:NativeAdHelper OR tag:MainActivity OR tag:GoogleMobileAdsConsentManager OR tag:Ads
```

或者使用包名筛选：
```
package:com.amaze.filemanager
```

## 示例输出

正常流程的日志应该包含：
1. `========== MainActivity: 开始初始化原生广告 ==========`
2. `MainActivity: ✅ Ad container found`
3. `========== 开始初始化 AdMob SDK ==========`
4. `✅ AdMob SDK 初始化完成`
5. `========== 开始加载原生广告 ==========`
6. `✅✅✅ onAdLoaded - 原生广告加载成功！`
7. `✅✅✅ 原生广告已显示在容器中`

