package com.mobilesecurity.detection

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import com.mobilesecurity.security.SecurityUtils
import com.mobilesecurity.model.Threat
import com.mobilesecurity.model.ThreatSeverity
import com.mobilesecurity.model.ThreatType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Advanced Threat Detector
 * Detects zerodayrat, spyware, malware, and other security threats
 * Using behavioral analysis and signature matching
 */
class ThreatDetector(private val context: Context) {

    private val securityUtils = SecurityUtils(context)

    // Known threat signatures (can be updated from server)
    private val threatSignatures = mutableMapOf<String, String>()

    // Suspicious network patterns
    private val suspiciousNetworkPatterns = listOf(
        Regex(".*\\.onion.*"), // Tor network
        Regex(".*:4433.*"),    // Non-standard HTTPS
        Regex(".*:[0-9]{5}.*") // High port numbers
    )

    // Suspicious behavior patterns
    private val suspiciousBehaviors = listOf(
        "frequent_network_connections",
        "background_audio_recording",
        "unusual_file_access",
        "clipboard_monitoring"
    )

    /**
     * Perform full device scan
     */
    suspend fun fullScan(): List<Threat> = withContext(Dispatchers.IO) {
        val threats = mutableListOf<Threat>()

        // 1. Check for root
        if (securityUtils.isDeviceRooted()) {
            threats.add(
                Threat(
                    id = generateId(),
                    type = ThreatType.TROJAN,
                    name = "Root Access Detected",
                    severity = ThreatSeverity.HIGH,
                    description = "Device is rooted, which exposes it to security risks",
                    packageName = null,
                    filePath = null,
                    signature = "root_detection",
                    confidence = 1.0
                )
            )
        }

        // 2. Check for emulator
        if (securityUtils.isEmulator()) {
            threats.add(
                Threat(
                    id = generateId(),
                    type = ThreatType.MALWARE,
                    name = "Emulator Environment Detected",
                    severity = ThreatSeverity.MEDIUM,
                    description = "App is running in emulator, possible analysis environment",
                    packageName = null,
                    filePath = null,
                    signature = "emulator_detection",
                    confidence = 0.8
                )
            )
        }

        // 3. Scan for ZerodayRat
        val ratPackages = securityUtils.detectZerodayRat()
        for (pkg in ratPackages) {
            threats.add(
                Threat(
                    id = generateId(),
                    type = ThreatType.ZERODAYRAT,
                    name = "ZerodayRat Detected",
                    severity = ThreatSeverity.CRITICAL,
                    description = "Remote Access Trojan (RAT) detected on device",
                    packageName = pkg,
                    filePath = null,
                    signature = "rat_signature",
                    confidence = 0.95
                )
            )
        }

        // 4. Scan for Spyware
        val spyware = securityUtils.detectSpyware()
        for (sw in spyware) {
            if (sw["isSpyware"] as Boolean) {
                threats.add(
                    Threat(
                        id = generateId(),
                        type = ThreatType.SPYWARE,
                        name = "Potential Spyware",
                        severity = when (sw["riskScore"] as Int) {
                            in 90..100 -> ThreatSeverity.CRITICAL
                            in 70..89 -> ThreatSeverity.HIGH
                            else -> ThreatSeverity.MEDIUM
                        },
                        description = "App exhibits spyware-like behavior: ${sw["spywareIndicators"]}",
                        packageName = sw["packageName"] as String,
                        filePath = null,
                        signature = "spyware_pattern",
                        confidence = (sw["riskScore"] as Int) / 100.0
                    )
                )
            }
        }

        // 5. Analyze installed apps for threats
        val appThreats = scanInstalledApps()
        threats.addAll(appThreats)

        // 6. Scan system files for modifications
        val fileThreats = scanSystemFiles()
        threats.addAll(fileThreats)

        threats
    }

    /**
     * Scan installed applications for threats
     */
    private fun scanInstalledApps(): List<Threat> {
        val threats = mutableListOf<Threat>()
        val packages = context.packageManager.getInstalledPackages(
            PackageManager.GET_PERMISSIONS or PackageManager.GET_ACTIVITIES
        )

        for (pkg in packages) {
            val appInfo = pkg.applicationInfo ?: continue

            // Skip system apps (unless they're suspicious)
            if (appInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0) {
                continue
            }

            val analysis = securityUtils.analyzeRatBehavior(pkg.packageName)
            if (analysis["isSuspicious"] as Boolean) {
                val riskScore = analysis["riskScore"] as Int
                threats.add(
                    Threat(
                        id = generateId(),
                        type = ThreatType.MALWARE,
                        name = "Suspicious App Detected",
                        severity = when (riskScore) {
                            in 80..100 -> ThreatSeverity.CRITICAL
                            in 60..79 -> ThreatSeverity.HIGH
                            in 40..59 -> ThreatSeverity.MEDIUM
                            else -> ThreatSeverity.LOW
                        },
                        description = "App ${pkg.applicationInfo?.loadLabel(context.packageManager)} shows suspicious behavior",
                        packageName = pkg.packageName,
                        filePath = appInfo.sourceDir,
                        signature = "behavioral_analysis",
                        confidence = riskScore / 100.0
                    )
                )
            }
        }

        return threats
    }

    /**
     * Scan system files for modifications
     */
    private fun scanSystemFiles(): List<Threat> {
        val threats = mutableListOf<Threat>()

        // Check for modified system files
        val systemDirs = listOf("/system", "/system/bin", "/system/app")
        for (dir in systemDirs) {
            val systemDir = File(dir)
            if (systemDir.exists() && systemDir.canRead()) {
                try {
                    val files = systemDir.listFiles()
                    files?.forEach { file ->
                        if (isFileModified(file)) {
                            threats.add(
                                Threat(
                                    id = generateId(),
                                    type = ThreatType.TROJAN,
                                    name = "System File Modified",
                                    severity = ThreatSeverity.HIGH,
                                    description = "System file ${file.name} may have been modified",
                                    packageName = null,
                                    filePath = file.absolutePath,
                                    signature = hashFile(file),
                                    confidence = 0.7
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    // Cannot access system files (normal on non-rooted devices)
                }
            }
        }

        return threats
    }

    /**
     * Check if file has been modified
     */
    private fun isFileModified(file: File): Boolean {
        // On non-rooted devices, we can't check system files
        // This is a placeholder for root device analysis
        if (!file.canRead()) return false

        // Check for unusual file size or modification time
        val currentTime = System.currentTimeMillis()
        val sixMonthsAgo = currentTime - (180L * 24 * 60 * 60 * 1000)

        return file.lastModified() > sixMonthsAgo
    }

    /**
     * Calculate file hash
     */
    private fun hashFile(file: File): String {
        return try {
            val bytes = file.readBytes()
            securityUtils.hashSHA256(bytes.toString())
        } catch (e: Exception) {
            "unknown"
        }
    }

    /**
     * Analyze network behavior for threats
     */
    suspend fun analyzeNetworkBehavior(trafficData: Map<String, Any>): List<Threat> =
        withContext(Dispatchers.IO) {
            val threats = mutableListOf<Threat>()
            val connections = trafficData["connections"] as? List<Map<String, Any>> ?: return@withContext threats

            for (conn in connections) {
                val host = conn["host"] as? String ?: continue
                val port = conn["port"] as? Int ?: continue

                // Check for suspicious network patterns
                for (pattern in suspiciousNetworkPatterns) {
                    if (pattern.containsMatchIn(host) || pattern.containsMatchIn(port.toString())) {
                        threats.add(
                            Threat(
                                id = generateId(),
                                type = ThreatType.SPYWARE,
                                name = "Suspicious Network Activity",
                                severity = ThreatSeverity.HIGH,
                                description = "Connection to suspicious endpoint: $host:$port",
                                packageName = conn["packageName"] as? String,
                                filePath = null,
                                signature = "network_pattern_$host",
                                confidence = 0.8
                            )
                        )
                    }
                }
            }

            threats
        }

    /**
     * Update threat signatures from server
     */
    fun updateThreatSignatures(signatures: Map<String, String>) {
        threatSignatures.clear()
        threatSignatures.putAll(signatures)
    }

    /**
     * Match app against threat signatures
     */
    fun matchThreatSignature(packageName: String, fileHash: String): Threat? {
        for ((signature, threatInfo) in threatSignatures) {
            if (fileHash == signature) {
                // Parse threat info (JSON format)
                return Threat(
                    id = generateId(),
                    type = ThreatType.MALWARE,
                    name = "Signature Match",
                    severity = ThreatSeverity.CRITICAL,
                    description = "Package matches known threat signature",
                    packageName = packageName,
                    filePath = null,
                    signature = fileHash,
                    confidence = 1.0
                )
            }
        }
        return null
    }

    /**
     * Analyze app permissions for spyware indicators
     */
    fun analyzePermissions(packageName: String): Threat? {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(
                packageName,
                PackageManager.GET_PERMISSIONS
            )
            val permissions = packageInfo.requestedPermissions ?: return null

            val dangerousPermissions = listOf(
                "android.permission.READ_SMS",
                "android.permission.SEND_SMS",
                "android.permission.READ_CALL_LOG",
                "android.permission.CALL_PHONE",
                "android.permission.RECORD_AUDIO",
                "android.permission.CAMERA",
                "android.permission.ACCESS_FINE_LOCATION",
                "android.permission.READ_CONTACTS",
                "android.permission.READ_CALENDAR",
                "android.permission.BODY_SENSORS"
            )

            val hasDangerousPerms = permissions.count { it in dangerousPermissions }
            val hasInternet = "android.permission.INTERNET" in permissions

            if (hasDangerousPerms >= 4 && hasInternet) {
                Threat(
                    id = generateId(),
                    type = ThreatType.SPYWARE,
                    name = "Excessive Dangerous Permissions",
                    severity = when {
                        hasDangerousPerms >= 7 -> ThreatSeverity.CRITICAL
                        hasDangerousPerms >= 5 -> ThreatSeverity.HIGH
                        else -> ThreatSeverity.MEDIUM
                    },
                    description = "App requests $hasDangerousPerms dangerous permissions with internet access",
                    packageName = packageName,
                    filePath = null,
                    signature = "permission_analysis",
                    confidence = hasDangerousPerms / 10.0
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private fun generateId(): String {
        return java.util.UUID.randomUUID().toString()
    }
}
