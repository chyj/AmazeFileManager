# Logcat 筛选器修复指南

## 问题分析

从截图看到筛选器已设置，但没有看到广告日志。可能的原因：

1. **MainActivity 使用 SLF4J Logger**，不是 Android Log，可能需要包名筛选
2. **日志级别**可能设置为 INFO 或更高，需要调整
3. **应用可能还没执行到广告初始化代码**

## 推荐的筛选器设置

### 方法 1: 使用包名筛选（最可靠）
```
package:com.amaze.filemanager
```

### 方法 2: 组合筛选（包含所有可能）
```
package:com.amaze.filemanager OR tag:NativeAdHelper OR tag:ConsentManager OR tag:Ads OR tag:MainActivity
```

### 方法 3: 使用关键词筛选
```
原生广告 OR NativeAdHelper OR AdMob OR loadNativeAd OR initializeNativeAd
```

### 方法 4: 查看所有日志（调试时使用）
```
*:D
```
然后手动搜索关键词

## Android Studio Logcat 设置步骤

1. **清除当前筛选器**，先查看所有日志
2. **设置日志级别为 Verbose**（显示所有级别）
3. **使用包名筛选**：`package:com.amaze.filemanager`
4. **启动应用**，观察日志输出

## 验证日志是否输出

运行以下命令验证：
```bash
adb logcat -c
adb logcat | grep -i "nativeadhelper\|原生广告\|admob"
```

## 如果仍然没有日志

可能的原因和解决方案：

1. **应用还没启动到 MainActivity**
   - 检查是否真的进入了 MainActivity
   - 查看是否有其他错误阻止了初始化

2. **BuildConfig.IS_VERSION_FDROID 为 true**
   - 检查是否编译的是 play flavor
   - 确认广告代码在 play flavor 中

3. **日志被过滤**
   - 尝试移除所有筛选器
   - 查看是否有其他错误日志

4. **SLF4J 日志桥接问题**
   - MainActivity 的日志可能不会直接显示
   - 主要关注 NativeAdHelper 和 ConsentManager 的日志

## 最简化的筛选器（推荐）

```
tag:NativeAdHelper OR tag:ConsentManager OR tag:Ads
```

这个筛选器会显示：
- ✅ NativeAdHelper 的所有日志（使用 Android Log.d/e）
- ✅ ConsentManager 的所有日志（使用 Android Log）
- ✅ Ads SDK 的内部日志

MainActivity 的日志（使用 SLF4J）可能需要包名筛选才能看到。

