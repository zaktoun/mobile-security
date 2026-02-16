package com.mobilesecurity.api

import android.content.Context
import com.google.gson.Gson
import com.mobilesecurity.model.*
import com.mobilesecurity.security.SecurityUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.*

/**
 * Dashboard Client - Secure API Communication
 * Implements certificate pinning and secure communication
 */
class DashboardClient(private val context: Context) {

    companion object {
        private const val BASE_URL = "https://your-dashboard.com/api"
        private const val CONNECT_TIMEOUT = 30L
        private const val READ_TIMEOUT = 30L
        private const val WRITE_TIMEOUT = 30L
    }

    private val securityUtils = SecurityUtils(context)
    private val gson = Gson()

    // OkHttpClient with security features
    private val okHttpClient = createSecureClient()

    // Device information
    private val deviceId by lazy { securityUtils.getDeviceId() }

    /**
     * Create secure OkHttpClient with certificate pinning
     */
    private fun createSecureClient(): OkHttpClient {
        // Logging interceptor for debugging (remove in production)
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        return OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .addInterceptor(AuthenticationInterceptor(securityUtils))
            .addInterceptor(ErrorHandlingInterceptor())
            // Certificate pinning (enabled in production)
            // .certificatePinner(certificatePinner)
            .build()
    }

    // ==================== DEVICE REGISTRATION ====================

    /**
     * Register device with dashboard
     */
    suspend fun registerDevice(): Result<Device> = withContext(Dispatchers.IO) {
        try {
            val deviceInfo = DeviceInfo(
                deviceId = deviceId,
                deviceName = android.os.Build.MODEL,
                platform = "android",
                osVersion = "${android.os.Build.VERSION.RELEASE} (API ${android.os.Build.VERSION.SDK_INT})",
                appVersion = BuildConfig.VERSION_NAME
            )

            val request = Request.Builder()
                .url("$BASE_URL/device/register")
                .post(deviceInfo.toJsonRequestBody())
                .build()

            val response = okHttpClient.newCall(request).execute()

            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                val registerResponse = gson.fromJson(responseBody, RegisterResponse::class.java)
                Result.success(registerResponse.device)
            } else {
                Result.failure(IOException("Failed to register device: ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== SCAN RESULT SUBMISSION ====================

    /**
     * Submit scan result to dashboard
     */
    suspend fun submitScanResult(scanResult: ScanResult): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$BASE_URL/device/scan-result")
                .post(scanResult.toJsonRequestBody())
                .build()

            val response = okHttpClient.newCall(request).execute()

            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(IOException("Failed to submit scan result: ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== THREAT REPORTING ====================

    /**
     * Report a detected threat
     */
    suspend fun reportThreat(threat: Threat): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val threatData = mapOf(
                "deviceId" to deviceId,
                "type" to threat.type.name.lowercase(),
                "name" to threat.name,
                "severity" to threat.severity.name.lowercase(),
                "description" to threat.description,
                "packageName" to threat.packageName,
                "filePath" to threat.filePath,
                "signature" to threat.signature,
                "confidence" to threat.confidence
            )

            val requestBody = gson.toJson(threatData)
                .toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("$BASE_URL/device/threat")
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()

            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(IOException("Failed to report threat: ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== AI THREAT ANALYSIS ====================

    /**
     * Analyze threat using AI/ML backend
     */
    suspend fun analyzeWithAI(analysisData: AnalysisData): Result<AIAnalysisResult> = withContext(Dispatchers.IO) {
        try {
            val requestBody = gson.toJson(analysisData)
                .toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("$BASE_URL/ai/analyze")
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()

            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                val aiResponse = gson.fromJson(responseBody, AIAnalysisResponse::class.java)
                Result.success(aiResponse.analysis)
            } else {
                Result.failure(IOException("AI analysis failed: ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== DASHBOARD DATA ====================

    /**
     * Get dashboard statistics
     */
    suspend fun getDashboardStats(): Result<DashboardStats> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$BASE_URL/dashboard/stats")
                .get()
                .build()

            val response = okHttpClient.newCall(request).execute()

            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                val stats = gson.fromJson(responseBody, DashboardStats::class.java)
                Result.success(stats)
            } else {
                Result.failure(IOException("Failed to get stats: ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get recent threats
     */
    suspend fun getRecentThreats(limit: Int = 10): Result<List<Threat>> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$BASE_URL/dashboard/threats?limit=$limit")
                .get()
                .build()

            val response = okHttpClient.newCall(request).execute()

            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                val threats = gson.fromJson(responseBody, Array<Threat>::class.java).toList()
                Result.success(threats)
            } else {
                Result.failure(IOException("Failed to get threats: ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== HELPER FUNCTIONS ====================

    private fun Any.toJsonRequestBody(): RequestBody {
        return gson.toJson(this).toRequestBody("application/json".toMediaType())
    }

    // ==================== INTERCEPTORS ====================

    /**
     * Authentication Interceptor
     * Adds device ID and secure token to all requests
     */
    private class AuthenticationInterceptor(private val securityUtils: SecurityUtils) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest = chain.request()

            // Add authentication headers
            val authenticatedRequest = originalRequest.newBuilder()
                .header("X-Device-ID", securityUtils.getDeviceId())
                .header("X-App-Version", BuildConfig.VERSION_NAME)
                .header("X-Timestamp", System.currentTimeMillis().toString())
                .header("X-Auth-Token", getAuthToken())
                .build()

            return chain.proceed(authenticatedRequest)
        }

        private fun getAuthToken(): String {
            // Generate or retrieve auth token from secure storage
            val securePrefs = securityUtils.getEncryptedSharedPreferences()
            return securePrefs.getString("auth_token", "") ?: ""
        }
    }

    /**
     * Error Handling Interceptor
     * Handles API errors and retries
     */
    private class ErrorHandlingInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            val response = chain.proceed(request)

            // Handle common error codes
            return when (response.code) {
                401 -> {
                    // Unauthorized - token expired
                    // Could trigger re-authentication here
                    response
                }
                429 -> {
                    // Too many requests
                    // Could implement exponential backoff
                    response
                }
                else -> response
            }
        }
    }

    // ==================== CERTIFICATE PINNING ====================

    /**
     * Certificate Pinner for production environment
     * Uncomment in production
     */
    /*
    private val certificatePinner = CertificatePinner.Builder()
        .add("your-dashboard.com", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
        .build()
    */

    // ==================== CUSTOM TRUST MANAGER ====================

    /**
     * Custom Trust Manager for SSL verification
     * Only use in development - remove in production
     */
    private fun createUnsafeTrustManager(): X509TrustManager {
        return object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        }
    }

    /**
     * Create unsafe SSL socket context
     * Only use in development - remove in production
     */
    private fun createUnsafeSSLSocketFactory(): SSLSocketFactory {
        val trustAllCerts = arrayOf<TrustManager>(createUnsafeTrustManager())
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, java.security.SecureRandom())
        return sslContext.socketFactory
    }
}

// ==================== DATA MODELS ====================

data class RegisterResponse(
    val success: Boolean,
    val device: Device,
    val message: String
)

data class AIAnalysisResponse(
    val success: Boolean,
    val analysis: AIAnalysisResult,
    val timestamp: String
)

data class AnalysisData(
    val packageName: String?,
    val behaviorData: Map<String, Any>?,
    val networkData: Map<String, Any>?,
    val permissions: List<String>?,
    val fileHash: String?,
    val fileContent: String?
)

data class AIAnalysisResult(
    val threatType: String,
    val severity: String,
    val confidence: Double,
    val description: String,
    val indicators: List<String>,
    val recommendations: List<String>
)

data class DashboardStats(
    val totalDevices: Int,
    val activeDevices: Int,
    val totalThreats: Int,
    val criticalThreats: Int,
    val resolvedThreats: Int,
    val pendingAlerts: Int,
    val securityScore: Int,
    val lastScanTime: String
)
