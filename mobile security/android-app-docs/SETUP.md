# Android App Setup Guide

## Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- Android SDK 34 (API 34)
- JDK 17 or later
- Gradle 8.2+

## Project Structure Setup

### 1. Create New Project

```bash
# Open Android Studio
# File -> New -> New Project
# Select "Empty Activity"
# Name: MobileSecurityApp
# Language: Kotlin
# Minimum SDK: API 24 (Android 7.0)
```

### 2. Configure build.gradle (Project Level)

```kotlin
// build.gradle.kts (root)

plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
```

### 3. Configure build.gradle (App Level)

```kotlin
// app/build.gradle.kts

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
}

android {
    namespace = "com.mobilesecurity"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.mobilesecurity"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Build config fields
        buildConfigField("String", "API_BASE_URL", "\"https://your-dashboard.com/api\"")
        buildConfigField("String", "WS_BASE_URL", "\"wss://your-dashboard.com/?XTransformPort=3005\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("boolean", "DEBUG_MODE", "false")
        }
        debug {
            isDebuggable = true
            buildConfigField("boolean", "DEBUG_MODE", "true")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.fragment:fragment-ktx:1.6.2")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // Network & API
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("io.socket:socket.io-client:2.1.0") {
        exclude(group = "org.json", module = "json")
    }

    // Security & Crypto
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("androidx.biometric:biometric:1.1.0")

    // Database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // Work Manager
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Preferences
    implementation("androidx.preference:preference-ktx:1.2.1")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
```

### 4. Configure AndroidManifest.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <!-- Permissions -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.QUERY_ALL_PACKAGES" />
    <uses-permission android:name="android.permission.USE_BIOMETRIC" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />

    <application
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.MobileSecurity"
        android:usesCleartextTraffic="false"
        android:networkSecurityConfig="@xml/network_security_config"
        tools:targetApi="31">

        <activity
            android:name=".ui.MainActivity"
            android:exported="true"
            android:theme="@style/Theme.MobileSecurity">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <activity
            android:name=".ui.ScanActivity"
            android:exported="false" />

        <activity
            android:name=".ui.ThreatsActivity"
            android:exported="false" />

        <!-- Security Application -->
        <application
            android:name=".SecurityApplication"
            ... />

        <!-- Foreground Service for background scanning -->
        <service
            android:name=".service.SecurityService"
            android:enabled="true"
            android:exported="false"
            android:foregroundServiceType="dataSync" />

    </application>

</manifest>
```

### 5. Network Security Configuration

Create `res/xml/network_security_config.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="false">
        <domain includeSubdomains="true">your-dashboard.com</domain>
    </domain-config>

    <!-- Certificate pinning for production -->
    <domain-config>
        <domain includeSubdomains="true">your-dashboard.com</domain>
        <pin-set>
            <pin digest="SHA-256">AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=</pin>
            <!-- Backup pin -->
            <pin digest="SHA-256">BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB=</pin>
        </pin-set>
    </domain-config>
</network-security-config>
```

### 6. ProGuard Rules

Create `proguard-rules.pro`:

```proguard
# Keep model classes
-keep class com.mobilesecurity.model.** { *; }
-keepclassmembers class com.mobilesecurity.model.** { *; }

# Keep security classes
-keep class com.mobilesecurity.security.** { *; }
-keepclassmembers class com.mobilesecurity.security.** { *; }

# Keep Gson classes
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Keep Retrofit classes
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepexceptions Exceptions

# Keep OkHttp classes
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }

# Obfuscate security keys
-keepclassmembers class * {
    @com.mobilesecurity.security.Obfuscate <methods>;
}
```

## File Placement

Place the provided Kotlin files in the appropriate directories:

```
app/src/main/java/com/mobilesecurity/
├── SecurityApplication.kt
├── model/
│   └── ModelClasses.kt
├── security/
│   └── SecurityUtils.kt
├── detection/
│   └── ThreatDetector.kt
├── scanner/
│   └── DeviceScanner.kt
├── api/
│   └── DashboardClient.kt
└── ui/
    ├── MainActivity.kt
    ├── ScanActivity.kt
    └── ThreatsActivity.kt
```

## Build Instructions

### Debug Build

```bash
# From project root
./gradlew assembleDebug

# Or from Android Studio
# Build -> Build Bundle(s) / APK(s) -> Build APK(s)
```

### Release Build

```bash
# From project root
./gradlew assembleRelease
```

### Run on Device

```bash
# Install debug APK
adb install app/build/outputs/apk/debug/app-debug.apk

# Or run directly
./gradlew installDebug
```

## Configuration

### Update API Base URL

In `app/build.gradle.kts`, update:

```kotlin
buildConfigField("String", "API_BASE_URL", "\"https://your-actual-dashboard.com/api\"")
```

### Update Dashboard URL

In `DashboardClient.kt`, update:

```kotlin
companion object {
    private const val BASE_URL = "https://your-actual-dashboard.com/api"
}
```

### Generate Release Key

```bash
keytool -genkeypair -v -keystore release.keystore \
  -alias release_key -keyalg RSA -keysize 2048 -validity 10000
```

## Testing

### Run Unit Tests

```bash
./gradlew test
```

### Run Instrumented Tests

```bash
./gradlew connectedAndroidTest
```

## Troubleshooting

### Build Errors

If you encounter build errors:

1. Clean and rebuild:
   ```bash
   ./gradlew clean
   ./gradlew build
   ```

2. Check Gradle wrapper version in `gradle/wrapper/gradle-wrapper.properties`

3. Ensure JDK 17 is installed and configured

### Dependency Conflicts

If you have dependency conflicts:

```bash
./gradlew app:dependencies
```

### Permissions Issues

If permissions are denied, ensure:

1. All required permissions are in `AndroidManifest.xml`
2. Runtime permissions are requested for dangerous permissions
3. App is granted `QUERY_ALL_PACKAGES` permission

## Security Notes

### Development Mode

- Remove `android:usesCleartextTraffic="true"` in production
- Disable logging in release builds
- Implement proper certificate pinning

### Production Deployment

1. Enable ProGuard/R8 obfuscation
2. Sign with release keystore
3. Verify app signature
4. Test on multiple devices
5. Perform security audit

## Next Steps

1. Implement UI activities (MainActivity, ScanActivity, etc.)
2. Add background scanning service
3. Implement real-time protection
4. Add biometric authentication
5. Configure scheduled scans
6. Set up crash reporting
7. Implement analytics
