#!/usr/bin/env bash

# 安装 Android NDK r25c (版本号: 25.2.9519653)
# 使用方法: ./scripts/install_ndk_r25c.sh

set -e

echo "=========================================="
echo "安装 Android NDK r25c"
echo "=========================================="
echo ""

# 检测 SDK 目录
if [ -n "$ANDROID_SDK_ROOT" ]; then
    SDK_DIR="$ANDROID_SDK_ROOT"
elif [ -n "$ANDROID_HOME" ]; then
    SDK_DIR="$ANDROID_HOME"
else
    SDK_DIR="$HOME/Library/Android/sdk"
fi

echo "SDK 目录: $SDK_DIR"
echo ""

SDKMANAGER="$SDK_DIR/cmdline-tools/latest/bin/sdkmanager"

# 方法 1: 使用 sdkmanager（如果可用）
if [ -f "$SDKMANAGER" ]; then
    echo "使用 sdkmanager 安装 NDK r25c..."
    echo "执行: $SDKMANAGER \"ndk;25.2.9519653\""
    echo ""
    
    if "$SDKMANAGER" "ndk;25.2.9519653" --sdk_root="$SDK_DIR"; then
        echo ""
        echo "✓ NDK r25c 安装成功！"
        echo "安装位置: $SDK_DIR/ndk/25.2.9519653"
        exit 0
    else
        echo ""
        echo "✗ sdkmanager 安装失败，尝试直接下载方式..."
        echo ""
    fi
else
    echo "sdkmanager 未找到，使用直接下载方式..."
    echo ""
fi

# 方法 2: 直接下载 ZIP 文件
echo "直接下载 NDK r25c..."
echo ""

NDK_URL="https://dl.google.com/android/repository/android-ndk-r25c-darwin.zip"
NDK_ZIP="$SDK_DIR/android-ndk-r25c-darwin.zip"
NDK_TARGET_DIR="$SDK_DIR/ndk/25.2.9519653"

# 检查 curl
if ! command -v curl &> /dev/null; then
    echo "错误: curl 未找到"
    exit 1
fi

# 下载
echo "下载地址: $NDK_URL"
echo "保存位置: $NDK_ZIP"
echo ""
echo "开始下载（这可能需要几分钟）..."

if curl -L --fail --progress-bar "$NDK_URL" -o "$NDK_ZIP"; then
    echo "✓ 下载完成"
else
    echo ""
    echo "错误: 下载失败"
    echo "请检查网络连接或手动下载: $NDK_URL"
    exit 1
fi

echo ""

# 解压
echo "解压 NDK..."
mkdir -p "$SDK_DIR/ndk"

if unzip -q "$NDK_ZIP" -d "$SDK_DIR/ndk"; then
    echo "✓ 解压完成"
else
    echo "错误: 解压失败"
    exit 1
fi

# 重命名目录以匹配版本号
if [ -d "$SDK_DIR/ndk/android-ndk-r25c" ]; then
    mv "$SDK_DIR/ndk/android-ndk-r25c" "$NDK_TARGET_DIR"
    echo "✓ 已重命名为: $NDK_TARGET_DIR"
fi

# 删除 ZIP 文件
rm -f "$NDK_ZIP"
echo "✓ 已删除 ZIP 文件"

echo ""
echo "=========================================="
echo "✓ NDK r25c 安装完成！"
echo "=========================================="
echo ""
echo "安装位置: $NDK_TARGET_DIR"
echo ""
echo "注意: 如果您的 Mac 是 ARM64 架构（M1/M2），"
echo "      r25c 可能存在兼容性问题。"
echo "      如果遇到问题，建议使用 NDK r26c 或更高版本。"
echo ""

