# AdMob 开屏广告问题排查指南

## 当前问题：广告加载失败

### 错误信息
```
错误代码: 3
错误消息: Ad unit doesn't match format
```

### 问题分析

这个错误通常表示：
1. **广告单元 ID 类型不匹配**：使用的广告单元 ID 不是 App Open Ad 类型
2. **测试广告单元 ID 可能无效**：Google 的测试 ID 可能已过期或需要更新

### 解决方案

#### 方案 1：在 AdMob 控制台创建 App Open Ad（推荐）

1. **登录 AdMob 控制台**
   - 访问：https://apps.admob.com
   - 使用你的 Google 账号登录

2. **创建应用（如果还没有）**
   - 点击 "应用" → "添加应用"
   - 选择 "Android"
   - 输入应用名称和包名：`com.amaze.filemanager.openad.debug`

3. **创建 App Open Ad 广告单元**
   - 在应用详情页面，点击 "广告单元" → "添加广告单元"
   - **重要**：选择 **"App Open Ad"** 类型（不是 Banner、Interstitial 等）
   - 输入广告单元名称（如：App Open Ad - Main）
   - 点击 "创建广告单元"

4. **获取广告单元 ID**
   - 创建成功后，复制广告单元 ID（格式：`ca-app-pub-xxxxxxxxxxxxxxxx/yyyyyyyyyy`）
   - 替换 `AppOpenAdManager.kt` 中的 `AD_UNIT_ID`

5. **更新代码**
   ```kotlin
   // 在 AppOpenAdManager.kt 中
   private const val AD_UNIT_ID = "ca-app-pub-你的发布商ID/你的广告单元ID"
   ```

6. **更新 AndroidManifest.xml**
   - 确保 `app/src/play/AndroidManifest.xml` 中的 `APPLICATION_ID` 是你的 AdMob App ID
   - App ID 格式：`ca-app-pub-xxxxxxxxxxxxxxxx~yyyyyyyyyy`

#### 方案 2：使用正确的测试广告单元 ID

如果测试 ID 无效，可以尝试：
1. 等待几分钟让 AdMob 系统更新
2. 检查网络连接
3. 确保设备/模拟器可以访问 Google 服务

### 验证步骤

1. **检查 AdMob App ID**
   ```bash
   # 在 AndroidManifest.xml 中确认
   <meta-data
       android:name="com.google.android.gms.ads.APPLICATION_ID"
       android:value="ca-app-pub-xxxxxxxxxxxxxxxx~yyyyyyyyyy"/>
   ```

2. **检查广告单元 ID**
   - 确认 `AppOpenAdManager.kt` 中的 `AD_UNIT_ID` 是正确的 App Open Ad 类型
   - 格式：`ca-app-pub-xxxxxxxxxxxxxxxx/yyyyyyyyyy`

3. **检查日志输出**
   - 运行应用后，查看 Logcat 中的详细错误信息
   - 使用筛选：`tag:AppOpenAdManager`

### 常见错误代码

- **错误代码 0**: ERROR_CODE_INTERNAL_ERROR - AdMob SDK 内部错误
- **错误代码 1**: ERROR_CODE_INVALID_REQUEST - 请求参数无效
- **错误代码 2**: ERROR_CODE_NETWORK_ERROR - 网络连接问题
- **错误代码 3**: ERROR_CODE_NO_FILL - 无广告填充（或格式不匹配）
- **错误代码 8**: ERROR_CODE_INVALID_AD_SIZE - 广告尺寸无效

### 调试建议

1. **使用 Ad Inspector**
   - 在主界面菜单中点击 "Ad Inspector"
   - 可以查看广告状态和调试信息

2. **检查网络连接**
   - 确保设备可以访问互联网
   - 如果在中国大陆，可能需要 VPN

3. **等待广告填充**
   - 新创建的广告单元可能需要几分钟到几小时才能开始填充
   - 测试阶段建议使用测试广告单元 ID

4. **查看完整日志**
   ```
   tag:SplashActivity | tag:AppOpenAdManager | tag:MyApplication | tag:ConsentManager
   ```

### 下一步

1. 在 AdMob 控制台创建 App Open Ad 广告单元
2. 更新代码中的广告单元 ID
3. 重新编译运行
4. 查看日志确认广告是否加载成功

