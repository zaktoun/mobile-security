# 🚀 Quick Start Deployment

Panduan cepat untuk upload ke GitHub dan Play Store.

---

## 📦 Web Dashboard (Next.js)

### Step 1: Upload ke GitHub

```bash
# Masuk ke project directory
cd /home/z/my-project

# Inisialisasi git (jika belum)
git init

# Buat .gitignore
cat > .gitignore << 'EOF'
# Dependencies
node_modules/

# Next.js
.next/
out/

# Database
*.db

# Environment
.env.local
.env.production.local

# IDE
.vscode/
.idea/
EOF

# Commit changes
git add .
git commit -m "Initial commit: Mobile Security Dashboard"

# Buat repository di GitHub: https://github.com/new
# Lalu connect dengan:
git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPO.git
git branch -M main
git push -u origin main
```

### Step 2: Deploy ke Vercel (3 Menit)

#### Cara Paling Mudah:

1. **Buka Vercel**
   - Kunjungi: https://vercel.com/signup

2. **Import Project**
   - Klik "Add New Project"
   - Pilih repository GitHub yang baru dibuat
   - Klik "Import"

3. **Configure** (Otomatis terdeteksi Next.js)
   ```
   Framework: Next.js
   Build Command: npm run build
   Output Directory: .next
   Install Command: npm install
   ```

4. **Environment Variables**
   ```
   DATABASE_URL=file:./db/custom.db
   ```

5. **Deploy**
   - Klik "Deploy"
   - Tunggu 2-3 menit
   - Selesai! Aplikasi live di `https://your-project.vercel.app`

---

## 📱 Android App

### Step 1: Persiapan

1. **Buka Android Studio**
2. **New Project > Empty Activity**
3. **Configure:**
   ```
   Name: MobileSecurityApp
   Package: com.mobilesecurity
   Language: Kotlin
   Minimum SDK: API 24
   ```

4. **Copy file dari dokumentasi:**
   - Buka `/home/z/my-project/android-app-docs/`
   - Copy semua file ke project Android Studio

### Step 2: Generate Keystore

```bash
# Generate release keystore
keytool -genkeypair -v \
  -keystore release-key.keystore \
  -alias mobile-security-key \
  -keyalg RSA \
  -keysize 4096 \
  -validity 10000
```

⚠️ **SIMPAN KEYSOTRE DENGAN AMAN!**
- Jangan upload ke GitHub
- Backup di lokasi aman
- Jangan lupa password!

### Step 3: Build AAB (Untuk Play Store)

```bash
# Build App Bundle (Format yang dibutuhkan Play Store)
./gradlew bundleRelease

# Output: app/build/outputs/bundle/release/app-release.aab
```

### Step 4: Upload ke Google Play Store

1. **Buat Developer Account**
   - Buka: https://play.google.com/console
   - Register: $25 (sekali bayar)

2. **Create New App**
   - App name: Mobile Security
   - Free app
   - Setup store listing (name, description, screenshots)

3. **Upload Release**
   - Upload `app-release.aab`
   - Review & publish

4. **Tunggu Review**
   - Google akan review (1-7 hari)
   - Setelah disetujui, app akan live!

---

## 🎯 Summary

| Project | GitHub | Deployment |
|---------|---------|------------|
| **Web Dashboard** | ✅ Easy | ✅ Vercel (3 min) |
| **Android App** | ✅ Easy | ✅ Play Store (1-7 days review) |

---

## 📖 Dokumentasi Lengkap

Untuk panduan lengkap, baca:
- `GUIDE_GITHUB_PLAYSTORE.md` - Panduan detail deployment
- `android-app-docs/README.md` - Dokumentasi Android App
- `android-app-docs/SETUP.md` - Setup Android Studio

---

## ⚡ Quick Commands

### Web Dashboard
```bash
# Deploy ke Vercel
vercel

# Production deploy
vercel --prod

# Local development
bun run dev
```

### Android App
```bash
# Build release APK
./gradlew assembleRelease

# Build AAB (Play Store)
./gradlew bundleRelease

# Install di device
./gradlew installDebug
```

---

## 🛡️ Security Tips

### Production Deployment Checklist

**Web Dashboard:**
- [ ] Environment variables configured
- [ ] Database connection working
- [ ] SSL/TLS enabled
- [ ] API endpoints tested
- [ ] Monitoring setup

**Android App:**
- [ ] Release keystore created & backed up
- [ ] Code obfuscation enabled
- [ ] All permissions documented
- [ ] Privacy policy published
- [ ] Testing completed

---

## 🆘 Need Help?

**Common Issues:**

1. **Git Push Error**
   ```bash
   # Login ke GitHub di terminal
   git config --global credential.helper store
   git push
   ```

2. **Vercel Build Error**
   ```bash
   # Clean rebuild
   rm -rf .next node_modules
   npm install
   ```

3. **Android Build Error**
   ```bash
   # Clean build
   ./gradlew clean
   ./gradlew build
   ```

---

## 🎉 You're Ready to Go!

**Next Steps:**
1. ✅ Upload Web Dashboard ke GitHub
2. ✅ Deploy ke Vercel
3. ✅ Setup production database
4. ✅ Build Android App
5. ✅ Upload ke Google Play Store

**For detailed guidance:** See `GUIDE_GITHUB_PLAYSTORE.md`

Good luck! 🚀🛡️
