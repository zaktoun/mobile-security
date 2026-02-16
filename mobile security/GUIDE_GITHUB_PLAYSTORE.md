# 🚀 Deployment Guide - GitHub & Google Play Store

Panduan lengkap untuk meng-upload Mobile Security Dashboard (Web) dan Android App ke GitHub serta Google Play Store.

---

## 📋 Daftar Isi

1. [Web Dashboard Deployment](#web-dashboard-deployment)
   - Upload ke GitHub
   - Deploy ke Vercel/Netlify
   - Setup Production Database

2. [Android App Deployment](#android-app-deployment)
   - Upload ke GitHub
   - Sign & Build APK
   - Upload ke Google Play Store

3. [Environment Configuration](#environment-configuration)
4. [Troubleshooting](#troubleshooting)

---

## 🌐 Web Dashboard Deployment

### Step 1: Upload ke GitHub

#### Cara 1: Menggunakan Deployment Script (Rekomendasi)

```bash
# Dari root project
cd /home/z/my-project

# Jalankan deployment script
chmod +x DEPLOYMENT_SCRIPT.sh
./DEPLOYMENT_SCRIPT.sh
```

#### Cara 2: Manual Upload

```bash
# 1. Inisialisasi Git repository
cd /home/z/my-project
git init

# 2. Buat .gitignore
cat > .gitignore << 'EOF'
# Dependencies
node_modules/
.pnp
.pnp.js

# Testing
coverage/

# Next.js
.next/
out/
build

# Production
dist/

# Misc
.DS_Store
*.pem

# Debug
npm-debug.log*
yarn-debug.log*
yarn-error.log*

# Local env files
.env*.local
.env.production

# Database
*.db
*.db-journal

# IDE
.vscode/
.idea/

# OS
Thumbs.db
EOF

# 3. Add dan commit semua file
git add .
git commit -m "Initial commit: Mobile Security Dashboard"

# 4. Buat repository baru di GitHub
# Buka: https://github.com/new

# 5. Connect ke remote repository
# Ganti YOUR_USERNAME dan YOUR_REPO dengan detail Anda
git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPO.git
git branch -M main
git push -u origin main
```

### Step 2: Deploy ke Vercel (Recommended)

#### Option A: Deploy via Vercel CLI

```bash
# 1. Install Vercel CLI
npm i -g vercel

# 2. Login ke Vercel
vercel login

# 3. Deploy project
vercel

# 4. Setup production deployment
vercel --prod
```

#### Option B: Deploy via Vercel Dashboard

1. **Buat akun Vercel**
   - Kunjungi: https://vercel.com/signup

2. **Import Repository**
   - Klik "Add New Project"
   - Pilih repository GitHub Anda
   - Vercel akan otomatis mendeteksi Next.js

3. **Configure Project**
   ```
   Framework Preset: Next.js
   Root Directory: ./
   Build Command: npm run build
   Output Directory: .next
   Install Command: npm install
   ```

4. **Environment Variables** (Wajib!)
   ```
   DATABASE_URL=file:./db/custom.db
   ```

5. **Deploy**
   - Klik "Deploy"
   - Tunggu beberapa menit
   - Aplikasi akan live di: `https://your-project.vercel.app`

### Step 3: Setup Production Database

#### Option A: Supabase (Recommended)

1. **Buat Project Supabase**
   - Kunjungi: https://supabase.com
   - Sign up/Login
   - Create new project

2. **Get Database URL**
   - Buka Project Settings > Database
   - Copy Connection String
   - Format: `postgresql://postgres:[YOUR-PASSWORD]@db.[PROJECT-REF].supabase.co:5432/postgres`

3. **Update Prisma Schema**
   ```prisma
   datasource db {
     provider = "postgresql"
     url      = env("DATABASE_URL")
   }
   ```

4. **Push Schema ke Production**
   ```bash
   # Update DATABASE_URL di .env.production
   DATABASE_URL="postgresql://postgres:[PASSWORD]@db.[REF].supabase.co:5432/postgres"

   # Push schema
   npx prisma db push
   ```

#### Option B: Neon (Alternative)

1. **Buat Project Neon**
   - Kunjungi: https://neon.tech
   - Sign up/Login
   - Create new project

2. **Get Connection String**
   - Copy Connection String dari dashboard

3. **Update dan Push**
   ```bash
   npx prisma db push
   ```

#### Option C: SQLite (Untuk Testing)

Untuk development/testing, Anda bisa tetap menggunakan SQLite:

```bash
# File akan otomatis dibuat di: db/custom.db
# Tidak perlu konfigurasi tambahan
```

### Step 4: Setup Domain Custom (Opsional)

#### Via Vercel

1. Buka project di Vercel dashboard
2. Settings > Domains
3. Add domain
4. Update DNS records sesuai instruksi Vercel

---

## 📱 Android App Deployment

### Step 1: Persiapan

#### 1. Buat GitHub Repository untuk Android App

```bash
# 1. Buat folder baru untuk Android app
cd ~
mkdir MobileSecurityApp
cd MobileSecurityApp

# 2. Copy semua file dari dokumentasi
cp -r /home/z/my-project/android-app-docs/* .

# 3. Inisialisasi Git
git init
git add .
git commit -m "Initial commit: Mobile Security Android App"

# 4. Buat repository di GitHub
# Buka: https://github.com/new

# 5. Connect ke remote
git remote add origin https://github.com/YOUR_USERNAME/mobile-security-app.git
git branch -M main
git push -u origin main
```

#### 2. Setup Project di Android Studio

1. Buka Android Studio
2. New Project > Empty Activity
3. Configure:
   ```
   Name: MobileSecurityApp
   Package: com.mobilesecurity
   Language: Kotlin
   Minimum SDK: API 24 (Android 7.0)
   ```
4. Copy semua file dari `/home/z/my-project/android-app-docs/` ke project:
   ```
   app/src/main/java/com/mobilesecurity/
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

### Step 2: Generate Keystore (Wajib untuk Play Store)

```bash
# 1. Generate keystore
keytool -genkeypair -v \
  -keystore release-key.keystore \
  -alias mobile-security-key \
  -keyalg RSA \
  -keysize 4096 \
  -validity 10000

# 2. Masukkan informasi:
# Keystore password: [PILIH PASSWORD KUAT]
# Key password: [PILIH PASSWORD KUAT]
# First & Last Name: [NAMA ANDA]
# Organizational Unit: [UNIT ORGANISASI]
# Organization: [NAMA PERUSAHAAN]
# City: [KOTA]
# State/Province: [PROVINSI]
# Country Code: ID
```

**⚠️ PENTING:**
- Simpan keystore dengan aman!
- Jangan upload ke GitHub!
- Backup keystore di lokasi aman!

### Step 3: Configure Signing di build.gradle

Buka `app/build.gradle.kts` dan tambahkan:

```kotlin
android {
    ...

    signingConfigs {
        create("release") {
            storeFile = file("../release-key.keystore")
            storePassword = "YOUR_STORE_PASSWORD"
            keyAlias = "mobile-security-key"
            keyPassword = "YOUR_KEY_PASSWORD"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")

            buildConfigField("boolean", "DEBUG_MODE", "false")
            buildConfigField("String", "API_BASE_URL", "\"https://your-production-domain.com/api\"")
        }

        debug {
            isDebuggable = true
            buildConfigField("boolean", "DEBUG_MODE", "true")
            buildConfigField("String", "API_BASE_URL", "\"https://your-dev-domain.com/api\"")
        }
    }
}
```

**⚠️ SECURITY WARNING:**
Jangan hardcode password! Gunakan environment variables atau `local.properties`:

```kotlin
signingConfigs {
    create("release") {
        val keystorePropertiesFile = rootProject.file("keystore.properties")
        val keystoreProperties = Properties()
        keystoreProperties.load(FileInputStream(keystorePropertiesFile))

        storeFile = file(keystoreProperties["storeFile"] as String)
        storePassword = keystoreProperties["storePassword"] as String
        keyAlias = keystoreProperties["keyAlias"] as String
        keyPassword = keystoreProperties["keyPassword"] as String
    }
}
```

Buat file `keystore.properties`:
```properties
storeFile=/path/to/release-key.keystore
storePassword=YOUR_STORE_PASSWORD
keyAlias=mobile-security-key
keyPassword=YOUR_KEY_PASSWORD
```

Jangan lupa tambahkan ke `.gitignore`:
```
*.keystore
keystore.properties
```

### Step 4: Build Release APK/AAB

#### Build APK (Untuk Testing)

```bash
# Dalam project root
./gradlew assembleRelease

# Output: app/build/outputs/apk/release/app-release.apk
```

#### Build AAB (Untuk Play Store - Required!)

```bash
# Build App Bundle
./gradlew bundleRelease

# Output: app/build/outputs/bundle/release/app-release.aab
```

### Step 5: Upload ke Google Play Console

#### 1. Buat Developer Account

1. **Buka Google Play Console**
   - Kunjungi: https://play.google.com/console

2. **Register sebagai Developer**
   - Klik "Create account"
   - Bayar fee: $25 (sekali bayar)

3. **Setup Account**
   - Masukkan informasi developer
   - Verifikasi identity
   - Setup contact information

#### 2. Buat Application

1. **Create New App**
   - Klik "Create app"
   - Masukkan informasi:
     - App name: Mobile Security - Anti Malware & Spyware
     - App type: Full app
     - Free or Paid: Free

2. **Setup Store Listing**
   - App name: Mobile Security
   - Short description (80 chars): Protect your device from zerodayrat, spyware, and malware with AI-powered security
   - Full description (4000 chars):
     ```
     🛡️ Mobile Security - Advanced Protection Against ZerodayRat & Spyware

     Mobile Security is a comprehensive security application designed to protect your Android device from advanced threats including zerodayrat, spyware, malware, trojans, and adware.

     🎯 KEY FEATURES:

     🤖 AI-POWERED THREAT DETECTION
     - Advanced machine learning algorithms detect zero-day threats
     - Behavioral analysis identifies suspicious app activities
     - Real-time threat intelligence from cloud

     🛡️ COMPREHENSIVE MALWARE PROTECTION
     - Detect and remove zerodayrat & spyware
     - Scan for trojans, adware, and other malware
     - Real-time app monitoring

     🔒 ENTERPRISE-GRADE SECURITY
     - OWASP certified security standards
     - AES-256 encryption for all data
     - Certificate pinning for secure communications
     - Root detection & anti-tampering

     📊 REAL-TIME MONITORING
     - Continuous background scanning
     - Real-time threat alerts
     - Detailed scan reports
     - Dashboard for security overview

     🌐 CLOUD ANALYSIS
     - Upload scan results to secure dashboard
     - Get threat analysis from AI/ML backend
     - Community-based threat intelligence

     📱 DEVICE PROTECTION
     - Monitor installed applications
     - Analyze app permissions
     - Detect suspicious network activity
     - System integrity checks

     ✨ WHY CHOOSE MOBILE SECURITY?

     ✅ Developed by security experts
     ✅ AI/ML powered threat detection
     ✅ OWASP compliance
     ✅ Privacy-focused (no unnecessary data collection)
     ✅ Regular threat database updates
     ✅ 24/7 monitoring

     🚀 PROTECT YOUR DEVICE NOW!
     Download Mobile Security and keep your Android device safe from advanced threats.

     📧 Contact: support@mobilesecurity.com
     🌐 Website: https://mobilesecurity.com

     Privacy Policy: [URL]
     Terms of Service: [URL]
     ```

   - Screenshots (Upload min. 2, max. 8 screenshots)
     - Phone screenshots: 320-3840px (min 2)
     - Tablet screenshots: 320-3840px (optional)

3. **Privacy Policy URL** (Required!)
   - Buat privacy policy page di website Anda
   - Atau gunakan: https://www.freeprivacypolicy.com/

4. **Content Rating**
   - Pilih rating questionnaire
   - Pilih appropriate rating

#### 3. Upload Release

1. **Go to Production > Create new release**

2. **Upload App Bundle**
   - Drag & drop `app-release.aab`
   - Tunggu upload selesai

3. **Testing** (Recommended)
   - Internal testing: Test dengan tim Anda
   - Closed testing: Test dengan grup terbatas
   - Open testing: Public beta

4. **Review & Publish**
   - Review semua detail
   - Klik "Start rollout to production"

5. **Review Process**
   - Google akan review app Anda
   - Biasanya 1-7 hari kerja
   - Akan menerima email jika ada issue

#### 4. App Maintenance

1. **Regular Updates**
   - Update threat signatures
   - Fix bugs
   - Add new features

2. **Monitor Feedback**
   - Review user feedback
   - Respond to reviews
   - Fix reported issues

3. **Analytics**
   - Monitor app performance
   - Track crash reports
   - Analyze user behavior

---

## 🔧 Environment Configuration

### Web Dashboard Environment Variables

Buat file `.env.production`:

```env
# Database
DATABASE_URL=postgresql://postgres:[PASSWORD]@db.[PROJECT-REF].supabase.co:5432/postgres

# API Base URL (untuk Android app)
NEXT_PUBLIC_API_URL=https://your-domain.com/api

# WebSocket URL
NEXT_PUBLIC_WS_URL=wss://your-domain.com

# AI/ML Service (jika menggunakan external service)
AI_SERVICE_URL=https://ai-service.example.com
AI_API_KEY=your_api_key

# App Info
NEXT_PUBLIC_APP_NAME=Mobile Security Dashboard
NEXT_PUBLIC_APP_VERSION=1.0.0
```

### Android App Configuration

Update `app/build.gradle.kts`:

```kotlin
buildConfigField("String", "API_BASE_URL", "\"https://your-production-domain.com/api\"")
buildConfigField("String", "WS_BASE_URL", "\"wss://your-production-domain.com/?XTransformPort=3005\"")
```

Update `DashboardClient.kt`:

```kotlin
companion object {
    private const val BASE_URL = "https://your-production-domain.com/api"
}
```

---

## 🛠️ Troubleshooting

### Web Dashboard Issues

#### Build Error
```bash
# Clean dan rebuild
rm -rf .next node_modules
npm install
npm run build
```

#### Database Connection Error
- Cek DATABASE_URL di environment variables
- Pastikan database server running
- Cek network/firewall settings

#### API Not Working
- Pastikan semua API routes ada di `/src/app/api/`
- Cek error logs di deployment platform
- Verify environment variables

### Android App Issues

#### Build Error
```bash
# Clean project
./gradlew clean
./gradlew build
```

#### Signing Error
- Verify keystore file exists
- Check password in signing config
- Ensure keystore not expired

#### Play Store Rejection
Common reasons:
- ❌ Missing permissions documentation
- ❌ Privacy policy not accessible
- ❌ Inappropriate content
- ❌ Violation of Play Store policies
- ❌ Missing screenshots or icons

**Solution:**
- Read Google Play Developer Policy carefully
- Provide complete app information
- Include proper privacy policy
- Test thoroughly before submission

---

## 📋 Checklist Before Production

### Web Dashboard Checklist
- [ ] Code pushed to GitHub
- [ ] Production database configured
- [ ] Environment variables set
- [ ] Test all API endpoints
- [ ] Test database operations
- [ ] Security audit completed
- [ ] SSL/TLS configured
- [ ] Domain pointed correctly
- [ ] Monitoring/logging setup
- [ ] Backup strategy configured

### Android App Checklist
- [ ] Code pushed to GitHub
- [ ] Release keystore created safely
- [ ] App tested on multiple devices
- [ ] All features working correctly
- [ ] No memory leaks
- [ ] No crash bugs
- [ ] Permissions documented
- [ ] Privacy policy created
- [ ] Store listing complete
- [ ] Screenshots ready
- [ ] App icon optimized
- [ ] ProGuard/R8 configured

---

## 📞 Support & Resources

### Useful Links
- **Vercel Documentation:** https://vercel.com/docs
- **Google Play Console:** https://play.google.com/console
- **Android Developers:** https://developer.android.com
- **Prisma Documentation:** https://www.prisma.io/docs
- **Next.js Documentation:** https://nextjs.org/docs

### Deployment Tools
- **GitHub:** https://github.com
- **Vercel:** https://vercel.com
- **Supabase:** https://supabase.com
- **Neon:** https://neon.tech

---

## ✅ Quick Start Summary

### Web Dashboard
```bash
# 1. Push to GitHub
git add .
git commit -m "Ready for deployment"
git push origin main

# 2. Deploy to Vercel
vercel
vercel --prod
```

### Android App
```bash
# 1. Build release
./gradlew bundleRelease

# 2. Upload to Play Console
# Login ke: https://play.google.com/console
# Create new app
# Upload app-release.aab
```

---

**🎉 SELAMAT! Aplikasi Anda siap untuk didistribusikan ke publik!**

**Need Help?**
- Check the troubleshooting section above
- Review documentation links
- Contact support for assistance

Good luck with your deployment! 🚀
