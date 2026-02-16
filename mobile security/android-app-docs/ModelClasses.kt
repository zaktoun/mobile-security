package com.mobilesecurity.model

import com.google.gson.annotations.SerializedName

// ==================== THREAT MODELS ====================

/**
 * Threat data model
 */
data class Threat(
    val id: String,
    val type: ThreatType,
    val name: String,
    val severity: ThreatSeverity,
    val description: String,
    val packageName: String?,
    val filePath: String?,
    val signature: String,
    val confidence: Double
)

/**
 * Threat type enumeration
 */
enum class ThreatType {
    ZERODAYRAT,
    SPYWARE,
    MALWARE,
    TROJAN,
    ADWARE,
    UNKNOWN
}

/**
 * Threat severity enumeration
 */
enum class ThreatSeverity {
    CRITICAL,
    HIGH,
    MEDIUM,
    LOW
}

// ==================== SCAN RESULT MODELS ====================

/**
 * Scan result data model
 */
data class ScanResult(
    val id: String,
    val scanType: String,
    val status: String,
    val duration: Int,
    val appsScanned: Int,
    val threatsFound: Int,
    val threatsQuarantined: Int,
    val threatsRemoved: Int,
    val threats: List<Threat>,
    val details: Map<String, Any>? = null
)

// ==================== DEVICE MODELS ====================

/**
 * Device information model
 */
data class Device(
    val id: String,
    val deviceId: String,
    val deviceName: String,
    val platform: String,
    val osVersion: String,
    val appVersion: String,
    val status: String,
    val lastSeen: String,
    val lastScanAt: String? = null
)

/**
 * Device info for registration
 */
data class DeviceInfo(
    val deviceId: String,
    val deviceName: String,
    val platform: String,
    val osVersion: String,
    val appVersion: String
)

// ==================== ALERT MODELS ====================

/**
 * Security alert model
 */
data class Alert(
    val id: String,
    val type: AlertType,
    val title: String,
    val message: String,
    val severity: ThreatSeverity,
    val status: AlertStatus,
    val deviceId: String,
    val threatId: String? = null,
    val actionRequired: Boolean,
    val timestamp: String
)

/**
 * Alert type enumeration
 */
enum class AlertType {
    THREAT_DETECTED,
    SCAN_COMPLETED,
    DEVICE_COMPROMISED,
    SECURITY_UPDATE,
    NEW_THREAT_INTEL
}

/**
 * Alert status enumeration
 */
enum class AlertStatus {
    UNREAD,
    READ,
    ACKNOWLEDGED,
    RESOLVED
}

// ==================== NETWORK MODELS ====================

/**
 * Network connection data
 */
data class NetworkConnection(
    val host: String,
    val port: Int,
    val protocol: String,
    val packageName: String,
    val timestamp: Long,
    val bytesTransferred: Long
)

/**
 * Network traffic data
 */
data class NetworkTraffic(
    val connections: List<NetworkConnection>,
    val totalBytes: Long,
    val scanDuration: Long
)

// ==================== PERMISSION MODELS ====================

/**
 * App permission data
 */
data class AppPermission(
    val name: String,
    val isGranted: Boolean,
    val isDangerous: Boolean
)

/**
 * App permissions summary
 */
data class AppPermissionsSummary(
    val packageName: String,
    val permissions: List<AppPermission>,
    val dangerousCount: Int,
    val suspiciousCombos: List<List<String>>
)

// ==================== BEHAVIOR MODELS ====================

/**
 * App behavior data
 */
data class AppBehavior(
    val packageName: String,
    val networkActivity: Boolean,
    val fileAccess: Boolean,
    val smsAccess: Boolean,
    val callAccess: Boolean,
    val microphoneAccess: Boolean,
    val cameraAccess: Boolean,
    val locationAccess: Boolean,
    val clipboardAccess: Boolean
)

/**
 * Behavior analysis result
 */
data class BehaviorAnalysis(
    val packageName: String,
    val riskScore: Int,
    val suspiciousIndicators: List<String>,
    val recommendation: String
)

// ==================== QUARANTINE MODELS ====================

/**
 * Quarantine item
 */
data class QuarantineItem(
    val id: String,
    val threat: Threat,
    val originalPath: String?,
    val packageName: String?,
    val quarantinedAt: String,
    val canRestore: Boolean
)

/**
 * Quarantine action result
 */
data class QuarantineResult(
    val success: Boolean,
    val quarantinedItems: List<QuarantineItem>,
    val failedItems: List<String>
)

// ==================== INFECTION STATUS ====================

/**
 * Device infection status
 */
data class InfectionStatus(
    val isInfected: Boolean,
    val infectionLevel: InfectionLevel,
    val threatCount: Int,
    val criticalThreats: Int,
    val lastScanTime: String,
    val recommendations: List<String>
)

/**
 * Infection level enumeration
 */
enum class InfectionLevel {
    CLEAN,
    LOW_RISK,
    MODERATE,
    HIGH_RISK,
    CRITICAL
}

// ==================== INTELLIGENCE MODELS ====================

/**
 * Threat intelligence data
 */
data class ThreatIntelligence(
    val threatType: ThreatType,
    val signature: String,
    val variants: List<String>,
    behaviorPatterns: List<String>,
    val knownSources: List<String>,
    val firstSeen: String,
    val lastSeen: String
)

/**
 * Community threat data
 */
data class CommunityThreatData(
    val threatId: String,
    val detectionCount: Int,
    val affectedDevices: Int,
    val geographicalDistribution: Map<String, Int>
)

// ==================== API RESPONSE MODELS ====================

/**
 * Generic API response
 */
data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String?,
    val error: String?
)

/**
 * Paginated response
 */
data class PaginatedResponse<T>(
    val items: List<T>,
    val total: Int,
    val page: Int,
    val pageSize: Int,
    val hasMore: Boolean
)

// ==================== CONFIGURATION MODELS ====================

/**
 * App configuration
 */
data class SecurityConfig(
    val autoScan: Boolean,
    val scanInterval: Int, // in hours
    val realTimeProtection: Boolean,
    val cloudAnalysis: Boolean,
    val reportToDashboard: Boolean,
    val notifications: NotificationConfig
)

/**
 * Notification configuration
 */
data class NotificationConfig(
    val enabled: Boolean,
    val criticalAlerts: Boolean,
    val highPriorityAlerts: Boolean,
    val scanComplete: Boolean,
    val quietHours: QuietHours?
)

/**
 * Quiet hours configuration
 */
data class QuietHours(
    val enabled: Boolean,
    val startHour: Int,
    val endHour: Int
)

// ==================== SYNC MODELS ====================

/**
 * Sync status
 */
data class SyncStatus(
    val lastSyncTime: String,
    val syncInProgress: Boolean,
    val pendingItems: Int,
    val failedItems: Int
)

/**
 * Dashboard sync data
 */
data class DashboardSyncData(
    val deviceId: String,
    val scanResults: List<ScanResult>,
    val threats: List<Threat>,
    val deviceInfo: DeviceInfo
)
