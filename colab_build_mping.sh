#!/bin/bash
# ==============================================================================
# MPING — iOS 27 LIQUID GLASS AUTOMATED CLOUD APK BUILDER
# Single-cell Google Colab / Cloud Bash Script
# Extracts headless libv2ray.aar from 2dust/v2rayNG & builds Mping-iOS27-LiquidGlass.apk
# ==============================================================================
set -euo pipefail

echo "======================================================================"
echo "  🚀 STARTING ZERO-TOUCH BUILD: Mping (iOS 27 Liquid Glass Edition)   "
echo "======================================================================"

WORK_DIR="/tmp/mping_cloud_builder"
OUTPUT_APK_NAME="Mping-iOS27-LiquidGlass.apk"
FINAL_DIR="/content" # Standard Google Colab root, fallback to current dir
if [ ! -d "/content" ]; then
    FINAL_DIR="$(pwd)"
fi

rm -rf "$WORK_DIR"
mkdir -p "$WORK_DIR"
cd "$WORK_DIR"

# 1. Environment & Java / Android SDK Verification
echo "[1/6] Verifying JDK and Android SDK..."
if ! command -v java &> /dev/null; then
    echo "Installing OpenJDK 17..."
    apt-get update -qq && apt-get install -y -qq openjdk-17-jdk curl wget git unzip zip > /dev/null
    export JAVA_HOME="/usr/lib/jvm/java-17-openjdk-amd64"
    export PATH="$JAVA_HOME/bin:$PATH"
fi

if [ -z "${ANDROID_HOME:-}" ] && [ -z "${ANDROID_SDK_ROOT:-}" ]; then
    if [ -d "/usr/local/lib/android/sdk" ]; then
        export ANDROID_HOME="/usr/local/lib/android/sdk"
        export ANDROID_SDK_ROOT="/usr/local/lib/android/sdk"
    else
        echo "Setting up standalone Android Commandline Tools..."
        CMD_TOOLS_DIR="$WORK_DIR/cmdline-tools"
        mkdir -p "$CMD_TOOLS_DIR"
        wget -q "https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip" -O "$WORK_DIR/cmdline.zip"
        unzip -q "$WORK_DIR/cmdline.zip" -d "$CMD_TOOLS_DIR"
        export ANDROID_HOME="$WORK_DIR/android-sdk"
        mkdir -p "$ANDROID_HOME/cmdline-tools"
        mv "$CMD_TOOLS_DIR/cmdline-tools" "$ANDROID_HOME/cmdline-tools/latest"
        export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH"
        yes | sdkmanager --licenses > /dev/null 2>&1 || true
        sdkmanager "platforms;android-34" "build-tools;34.0.0" > /dev/null 2>&1 || true
    fi
fi

# 2. Extract Headless libv2ray from 2dust/v2rayNG
echo "[2/6] Extracting headless libv2ray core from 2dust/v2rayNG..."
mkdir -p "$WORK_DIR/v2ray_repo"
git clone --depth 1 https://github.com/2dust/v2rayNG.git "$WORK_DIR/v2ray_repo"

MPING_PROJECT="$WORK_DIR/MpingProject"
mkdir -p "$MPING_PROJECT/app/libs"
mkdir -p "$MPING_PROJECT/app/src/main/java/com/mping/vpn"
mkdir -p "$MPING_PROJECT/app/src/main/res/values"
mkdir -p "$MPING_PROJECT/app/src/main/res/drawable"
mkdir -p "$MPING_PROJECT/app/src/main/res/mipmap-xxxhdpi"

# Copy pre-compiled libv2ray.aar or tun2socks if present in repo
find "$WORK_DIR/v2ray_repo" -name "*.aar" -exec cp {} "$MPING_PROJECT/app/libs/" \;
if [ ! -f "$MPING_PROJECT/app/libs/libv2ray.aar" ]; then
    echo "Notice: Fetching latest verified libv2ray release core..."
    wget -q "https://github.com/2dust/AndroidLibV2rayLite/releases/latest/download/libv2ray.aar" -O "$MPING_PROJECT/app/libs/libv2ray.aar" || true
fi

# 3. Create Root Project & Gradle Configuration
echo "[3/6] Generating Mping Project Gradle Structure..."
cat << 'EOF' > "$MPING_PROJECT/settings.gradle"
rootProject.name = "Mping"
include ':app'
EOF

cat << 'EOF' > "$MPING_PROJECT/build.gradle"
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:8.4.1'
        classpath 'org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.24'
    }
}
allprojects {
    repositories {
        google()
        mavenCentral()
        flatDir { dirs 'libs' }
    }
}
EOF

cat << 'EOF' > "$MPING_PROJECT/app/build.gradle"
plugins {
    id 'com.android.application'
    id 'org.jetbrains.kotlin.android'
}

android {
    namespace 'com.mping.vpn'
    compileSdk 34

    defaultConfig {
        applicationId "com.mping.vpn"
        minSdk 24
        targetSdk 34
        versionCode 1
        versionName "1.0-iOS27"
        ndk { abiFilters 'arm64-v8a', 'armeabi-v7a', 'x86_64' }
    }

    signingConfigs {
        release {
            storeFile file("keystore.jks")
            storePassword "mpingpass"
            keyAlias "mping"
            keyPassword "mpingpass"
        }
    }

    buildTypes {
        release {
            minifyEnabled false
            signingConfig signingConfigs.release
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = '17' }

    buildFeatures { compose true }
    composeOptions { kotlinCompilerExtensionVersion '1.5.14' }
}

dependencies {
    implementation fileTree(dir: 'libs', include: ['*.jar', '*.aar'])
    implementation 'androidx.core:core-ktx:1.13.1'
    implementation 'androidx.activity:activity-compose:1.9.0'
    implementation platform('androidx.compose:compose-bom:2024.05.00')
    implementation 'androidx.compose.ui:ui'
    implementation 'androidx.compose.ui:ui-graphics'
    implementation 'androidx.compose.material3:material3'
    implementation 'androidx.compose.material:material-icons-extended'
    implementation 'androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0'
    implementation 'com.squareup.okhttp3:okhttp:4.12.0'
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0'
}
EOF

# 4. Generate Self-Signed Release Keystore
echo "[4/6] Creating deployment keystore..."
keytool -genkeypair -v -keystore "$MPING_PROJECT/app/keystore.jks" \
  -alias "mping" -keyalg RSA -keysize 2048 -validity 10000 \
  -storepass "mpingpass" -keypass "mpingpass" \
  -dname "CN=Mping, OU=Core, O=LiquidGlass, L=Cupertino, ST=CA, C=US" > /dev/null 2>&1

# 5. Populate Manifest & Compose UI
cat << 'EOF' > "$MPING_PROJECT/app/src/main/AndroidManifest.xml"
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

    <application
        android:label="Mping"
        android:theme="@android:style/Theme.Material.NoActionBar">
        <activity
            android:name=".MpingMainActivity"
            android:exported="true"
            android:windowSoftInputMode="adjustResize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <service
            android:name=".MpingVpnService"
            android:permission="android.permission.BIND_VPN_SERVICE"
            android:exported="false">
            <intent-filter>
                <action android:name="android.net.VpnService" />
            </intent-filter>
        </service>
    </application>
</manifest>
EOF

# Copy app code from current repo
CURRENT_REPO_SRC="$(pwd)/app/src/main/java/com/example"
if [ -d "$CURRENT_REPO_SRC" ]; then
    cp -r "$CURRENT_REPO_SRC/"* "$MPING_PROJECT/app/src/main/java/com/mping/vpn/"
    # Adjust package name in copied files
    find "$MPING_PROJECT/app/src/main/java/com/mping/vpn" -type f -name "*.kt" -exec sed -i 's/package com.example/package com.mping.vpn/g' {} +
    find "$MPING_PROJECT/app/src/main/java/com/mping/vpn" -type f -name "*.kt" -exec sed -i 's/import com.example\./import com.mping.vpn\./g' {} +
fi

# 6. Execute Gradle Build
echo "[5/6] Building signed release APK with Gradle..."
cd "$MPING_PROJECT"
curl -s "https://services.gradle.org/distributions/gradle-8.7-bin.zip" -o gradle.zip
unzip -q gradle.zip
export PATH="$MPING_PROJECT/gradle-8.7/bin:$PATH"

gradle assembleRelease

RELEASE_APK=$(find "$MPING_PROJECT/app/build/outputs/apk/release" -name "*.apk" | head -n 1)
cp "$RELEASE_APK" "$FINAL_DIR/$OUTPUT_APK_NAME"

echo "======================================================================"
echo "  🎉 SUCCESS! READY-TO-INSTALL APK GENERATED:                       "
echo "  --> $FINAL_DIR/$OUTPUT_APK_NAME                                    "
echo "======================================================================"

# If running in Google Colab, trigger browser download
if [ -f "/content/$OUTPUT_APK_NAME" ]; then
    python3 -c "
try:
    from google.colab import files
    files.download('/content/$OUTPUT_APK_NAME')
    print('Download started in your browser!')
except Exception:
    pass
" || true
fi
