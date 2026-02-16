package com.mobilesecurity.security

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import android.provider.Settings
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Security Utilities - OWASP Compliant Security Functions
 * Implements high-level security measures above OWASP standards
 */
class SecurityUtils(private val context: Context) {

    companion object {
        private const val AES_GCM_TAG_LENGTH = 128
        private const val AES_KEY_SIZE = 256
        private const val GCM_IV_LENGTH = 12
        private const val SHARED_PREFS_NAME = "secure_prefs"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"

        // Critical packages for ZerodayRat detection
        private val KNOWN_RAT_PACKAGES = setOf(
            "com.android.systemui.rat",
            "com.sys.rat.client",
            "com.remote.admin",
            "com.mobile.rat"
        )

        // Suspicious permissions for spyware detection
        private val SUSPICIOUS_PERMISSIONS = setOf(
            "android.permission.READ_SMS",
            "android.permission.SEND_SMS",
            "android.permission.READ_CALL_LOG",
            "android.permission.CALL_PHONE",
            "android.permission.RECORD_AUDIO",
            "android.permission.CAMERA",
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.READ_CONTACTS"
        )
    }

    // ==================== SECURE STORAGE ====================

    /**
     * Get Encrypted SharedPreferences - OWASP Secure Storage
     */
    fun getEncryptedSharedPreferences(): EncryptedSharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .setKeyGenParameterSpec(
                android.security.keystore.KeyGenParameterSpec.Builder(
                    ANDROID_KEYSTORE,
                    android.security.keystore.KeyProperties.PURPOSE_ENCRYPT or android.security.keystore.KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(android.security.keystore.KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(android.security.keystore.KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(AES_KEY_SIZE)
                    .build()
            )
            .build()

        return EncryptedSharedPreferences.create(
            context,
            SHARED_PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        ) as EncryptedSharedPreferences
    }

    // ==================== ENCRYPTION ====================

    /**
     * Generate secure AES-256 key
     */
    fun generateAesKey(): String {
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(AES_KEY_SIZE, SecureRandom())
        val key = keyGenerator.generateKey()
        return Base64.getEncoder().encodeToString(key.encoded)
    }

    /**
     * Encrypt data using AES-256-GCM
     */
    fun encrypt(plaintext: String, key: String): String {
        try {
            val keyBytes = Base64.getDecoder().decode(key)
            val secretKey = SecretKeySpec(keyBytes, "AES")

            val iv = ByteArray(GCM_IV_LENGTH)
            SecureRandom().nextBytes(iv)

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(AES_GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec)

            val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))

            // Combine IV and ciphertext
            val combined = iv + ciphertext
            return Base64.getEncoder().encodeToString(combined)
        } catch (e: Exception) {
            throw SecurityException("Encryption failed", e)
        }
    }

    /**
     * Decrypt data using AES-256-GCM
     */
    fun decrypt(encryptedData: String, key: String): String {
        try {
            val keyBytes = Base64.getDecoder().decode(key)
            val secretKey = SecretKeySpec(keyBytes, "AES")

            val combined = Base64.getDecoder().decode(encryptedData)
            val iv = combined.copyOfRange(0, GCM_IV_LENGTH)
            val ciphertext = combined.copyOfRange(GCM_IV_LENGTH, combined.size)

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(AES_GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

            val plaintext = cipher.doFinal(ciphertext)
            return String(plaintext, Charsets.UTF_8)
        } catch (e: Exception) {
            throw SecurityException("Decryption failed", e)
        }
    }

    // ==================== DEVICE INTEGRITY ====================

    /**
     * Check if device is rooted - OWASP Anti-Tampering
     */
    fun isDeviceRooted(): Boolean {
        return checkRootMethod1() || checkRootMethod2() || checkRootMethod3()
    }

    private fun checkRootMethod1(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/su/bin/su"
        )
        for (path in paths) {
            if (java.io.File(path).exists()) return true
        }
        return false
    }

    private fun checkRootMethod2(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "su"))
            val `in` = process.inputStream
            if (`in`.read() == -1) false else true
        } catch (e: Exception) {
            false
        }
    }

    private fun checkRootMethod3(): Boolean {
        return try {
            val buildTags = Build.TAGS
            buildTags != null && buildTags.contains("test-keys")
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Check if device is in emulator - Anti-emulator
     */
    fun isEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk" == Build.PRODUCT)
    }

    /**
     * Check if debugger is attached - Anti-debugging
     */
    fun isDebuggerAttached(): Boolean {
        return android.os.Debug.isDebuggerConnected() || android.os.Debug.waitingForDebugger()
    }

    /**
     * Verify app signature - Integrity check
     */
    fun verifyAppSignature(expectedSignature: String): Boolean {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNATURES
                )
            }

            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.signingInfo?.apkContentsSigners ?: emptyArray()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.signatures ?: emptyArray()
            }

            val currentSignature = signatures.firstOrNull()?.let { getSignatureHash(it) }
            currentSignature == expectedSignature
        } catch (e: Exception) {
            false
        }
    }

    private fun getSignatureHash(signature: Signature): String {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(signature.toByteArray())
        return Base64.getEncoder().encodeToString(md.digest())
    }

    // ==================== ZERODAYRAT DETECTION ====================

    /**
     * Check for known ZerodayRat packages
     */
    fun detectZerodayRat(): List<String> {
        val foundRats = mutableListOf<String>()
        val packages = context.packageManager.getInstalledPackages(0)

        for (pkg in packages) {
            if (KNOWN_RAT_PACKAGES.any { pkg.packageName.contains(it) }) {
                foundRats.add(pkg.packageName)
            }
        }

        return foundRats
    }

    /**
     * Analyze app for suspicious RAT behavior
     */
    fun analyzeRatBehavior(packageName: String): Map<String, Any> {
        val suspiciousIndicators = mutableListOf<String>()
        val riskScore = calculateRiskScore(packageName)

        try {
            val packageInfo = context.packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
            val permissions = packageInfo.requestedPermissions ?: emptyArray()

            // Check for suspicious permission combinations
            val suspiciousCombos = listOf(
                listOf("android.permission.INTERNET", "android.permission.RECORD_AUDIO"),
                listOf("android.permission.INTERNET", "android.permission.CAMERA"),
                listOf("android.permission.READ_SMS", "android.permission.INTERNET"),
                listOf("android.permission.READ_CALL_LOG", "android.permission.INTERNET"),
                listOf("android.permission.ACCESS_FINE_LOCATION", "android.permission.INTERNET")
            )

            for (combo in suspiciousCombos) {
                if (combo.all { it in permissions }) {
                    suspiciousIndicators.add("Suspicious permission combo: ${combo.joinToString(", ")}")
                }
            }

        } catch (e: Exception) {
            suspiciousIndicators.add("Failed to analyze package")
        }

        return mapOf(
            "packageName" to packageName,
            "riskScore" to riskScore,
            "suspiciousIndicators" to suspiciousIndicators,
            "isSuspicious" to suspiciousIndicators.isNotEmpty() || riskScore > 70
        )
    }

    private fun calculateRiskScore(packageName: String): Int {
        var score = 0

        try {
            val packageInfo = context.packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
            val permissions = packageInfo.requestedPermissions ?: emptyArray()

            // Score based on suspicious permissions
            for (perm in permissions) {
                if (perm in SUSPICIOUS_PERMISSIONS) {
                    score += 10
                }
            }

            // Check if system app
            if ((packageInfo.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0) {
                score += 5
            }

            // Check package name patterns
            if (packageName.contains("admin") || packageName.contains("remote") || packageName.contains("rat")) {
                score += 20
            }

        } catch (e: Exception) {
            // Analysis failed
        }

        return minOf(score, 100)
    }

    // ==================== SPYWARE DETECTION ====================

    /**
     * Detect potential spyware apps
     */
    fun detectSpyware(): List<Map<String, Any>> {
        val potentialSpyware = mutableListOf<Map<String, Any>>()
        val packages = context.packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS)

        for (pkg in packages) {
            val analysis = analyzeForSpyware(pkg.packageName)
            if (analysis["isSpyware"] as Boolean) {
                potentialSpyware.add(analysis)
            }
        }

        return potentialSpyware
    }

    private fun analyzeForSpyware(packageName: String): Map<String, Any> {
        val spywareIndicators = mutableListOf<String>()
        var riskScore = 0

        try {
            val packageInfo = context.packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
            val permissions = packageInfo.requestedPermissions ?: emptyArray()

            // Count suspicious permissions
            val suspiciousCount = permissions.count { it in SUSPICIOUS_PERMISSIONS }
            riskScore += suspiciousCount * 15

            if (suspiciousCount >= 4) {
                spywareIndicators.add("Has $suspiciousCount suspicious permissions")
            }

            // Check for SMS and call log access
            if ("android.permission.READ_SMS" in permissions && "android.permission.INTERNET" in permissions) {
                spywareIndicators.add("SMS snooping capability detected")
                riskScore += 30
            }

            if ("android.permission.READ_CALL_LOG" in permissions && "android.permission.INTERNET" in permissions) {
                spywareIndicators.add("Call log snooping capability detected")
                riskScore += 30
            }

            // Check for recording capabilities
            if ("android.permission.RECORD_AUDIO" in permissions && "android.permission.INTERNET" in permissions) {
                spywareIndicators.add("Audio recording and transmission capability")
                riskScore += 35
            }

        } catch (e: Exception) {
            // Package not found or cannot be analyzed
        }

        return mapOf(
            "packageName" to packageName,
            "riskScore" to minOf(riskScore, 100),
            "spywareIndicators" to spywareIndicators,
            "isSpyware" to riskScore >= 70
        )
    }

    // ==================== NETWORK SECURITY ====================

    /**
     * Generate secure random token
     */
    fun generateSecureToken(length: Int = 32): String {
        val random = SecureRandom()
        val bytes = ByteArray(length)
        random.nextBytes(bytes)
        return Base64.getEncoder().encodeToString(bytes)
    }

    /**
     * Hash data using SHA-256
     */
    fun hashSHA256(data: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(data.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(hash)
    }

    /**
     * Get unique device identifier (hashed for privacy)
     */
    fun getDeviceId(): String {
        val androidId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: "unknown"
        return hashSHA256(androidId)
    }
}
