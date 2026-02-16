#!/bin/bash

# 🚀 Deployment Script for GitHub Upload
# Script ini akan membantu upload project ke GitHub dengan mudah

echo "🛡️ Mobile Security Dashboard - GitHub Deployment Script"
echo "===================================================="
echo ""

# Cek apakah git sudah terinstall
if ! command -v git &> /dev/null; then
    echo "❌ Git tidak terinstall. Silakan install git terlebih dahulu."
    exit 1
fi

# Cek apakah .git sudah ada
if [ -d ".git" ]; then
    echo "✅ Git repository sudah terinisialisasi"
else
    echo "📦 Inisialisasi Git repository..."
    git init
    git add .
    git commit -m "Initial commit: Mobile Security Dashboard with AI/ML"
fi

# Tanya GitHub repository URL
echo ""
read -p "🔗 Masukkan GitHub repository URL (contoh: https://github.com/username/repo.git): " repo_url

if [ -z "$repo_url" ]; then
    echo "❌ Repository URL tidak boleh kosong!"
    exit 1
fi

# Cek remote sudah ada atau belum
if git remote get-url origin &> /dev/null; then
    echo "🔄 Update remote URL..."
    git remote set-url origin "$repo_url"
else
    echo "📌 Menambahkan remote origin..."
    git remote add origin "$repo_url"
fi

# Branch ke main
echo "🌿 Switching ke branch main..."
git branch -M main

# Push ke GitHub
echo ""
echo "🚀 Push ke GitHub..."
echo "Anda akan diminta untuk login ke GitHub."
echo ""

git push -u origin main

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ SUCCESS! Project berhasil di-upload ke GitHub!"
    echo ""
    echo "📌 Repository URL: $repo_url"
    echo ""
    echo "📖 Langkah selanjutnya:"
    echo "   1. Buka repository di GitHub"
    echo "   2. Setup GitHub Pages atau deploy ke Vercel"
    echo "   3. Setup database production"
    echo "   4. Update environment variables"
    echo ""
else
    echo "❌ Gagal push ke GitHub. Silakan coba manual."
    exit 1
fi
