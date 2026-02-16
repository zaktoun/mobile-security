package com.mobilesecurity.scanner

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.mobilesecurity.model.*
import com.mobilesecurity.security.SecurityUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Device Scanner
 * Scans installed applications, system files, and configuration for security issues
 */
class DeviceScanner(private val context: Context) {

    private val securityUtils = SecurityUtils(context)
    private val threatDetector = ThreatDetector(context)

    // Scan statistics
    private var appsScanned = 0
    private var threatsFound = 0
    private var startTime = 0L

    /**
     * Perform full device scan
     */
    suspend fun fullScan(): ScanResult = withContext(Dispatchers.IO) {
        startTime = System.currentTimeMillis()
        val detectedThreats = mutableListOf<Threat>()

        try {
            // 1. Scan installed applications
            val appThreats = scanApplications()
            detectedThreats.addAll(appThreats)

            // 2. Scan system configuration
            val configThreats = scanConfiguration()
            detectedThreats.addAll(configThreats)

            // 3. Scan for root access
            val rootThreats = scanForRoot()
            detectedThreats.addAll(rootThreats)

            // 4. Scan for modified system files
            val fileThreats = scanSystemFiles()
            detectedThreats.addAll(fileThreats)

            // 5. Scan for suspicious permissions
            val permissionThreats = scanPermissions()
            detectedThreats.addAll(permissionThreats)

        } catch (e: Exception) {
            detectedThreats.add(
                Threat(
                    id = generateId(),
                    type = ThreatType.MALWARE,
                    name = "Scan Error",
                    severity = ThreatSeverity.LOW,
                    description = "Error during scan: ${e.message}",
                    packageName = null,
                    filePath = null,
                    signature = "scan_error",
                    confidence = 0.5
                )
            )
        }

        val duration = ((System.currentTimeMillis() - startTime) / 1000).toInt()

        ScanResult(
            id = generateId(),
            scanType = "full",
            status = "completed",
            duration = duration,
            appsScanned = appsScanned,
            threatsFound = detectedThreats.size,
            threatsQuarantined = 0,
            threatsRemoved = 0,
            threats = detectedThreats,
            details = mapOf(
                "scanTime" to System.currentTimeMillis(),
                "deviceModel" to android.os.Build.MODEL,
                "androidVersion" to android.os.Build.VERSION.RELEASE
            )
        )
    }

    /**
     * Perform quick scan
     */
    suspend fun quickScan(): ScanResult = withContext(Dispatchers.IO) {
        startTime = System.currentTimeMillis()
        val detectedThreats = mutableListOf<Threat>()

        try {
            // 1. Quick app scan
            val appThreats = quickScanApplications()
            detectedThreats.addAll(appThreats)

            // 2. Check for known RAT packages
            val ratPackages = securityUtils.detectZerodayRat()
            for (pkg in ratPackages) {
                detectedThreats.add(
                    Threat(
                        id = generateId(),
                        type = ThreatType.ZERODAYRAT,
                        name = "ZerodayRat Detected",
                        severity = ThreatSeverity.CRITICAL,
                        description = "Remote Access Trojan detected",
                        packageName = pkg,
                        filePath = null,
                        signature = "rat_package",
                        confidence = 1.0
                    )
                )
                threatsFound++
            }

            // 3. Quick spyware detection
            val spyware = securityUtils.detectSpyware()
            for (sw in spyware.take(5)) { // Limit to top 5
                if (sw["isSpyware"] as Boolean) {
                    detectedThreats.add(
                        Threat(
                            id = generateId(),
                            type = ThreatType.SPYWARE,
                            name = "Potential Spyware",
                            severity = ThreatSeverity.HIGH,
                            description = "App exhibits spyware behavior",
                            packageName = sw["packageName"] as String,
                            filePath = null,
                            signature = "spyware_quick",
                            confidence = 0.8
                        )
                    )
                    threatsFound++
                }
            }

        } catch (e: Exception) {
            // Log error
        }

        val duration = ((System.currentTimeMillis() - startTime) / 1000).toInt()

        ScanResult(
            id = generateId(),
            scanType = "quick",
            status = "completed",
            duration = duration,
            appsScanned = appsScanned,
            threatsFound = detectedThreats.size,
            threatsQuarantined = 0,
            threatsRemoved = 0,
            threats = detectedThreats,
            details = mapOf(
                "scanTime" to System.currentTimeMillis()
            )
        )
    }

    /**
     * Scan all installed applications
     */
    private fun scanApplications(): List<Threat> {
        val threats = mutableListOf<Threat>()
        val packages = context.packageManager.getInstalledPackages(
            PackageManager.GET_PERMISSIONS or
            PackageManager.GET_ACTIVITIES or
            PackageManager.GET_SERVICES
        )

        for (pkg in packages) {
            appsScanned++
            val appInfo = pkg.applicationInfo ?: continue

            // Skip system apps (unless they're suspicious)
            if (appInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0) {
                continue
            }

            // Analyze app behavior
            val analysis = securityUtils.analyzeRatBehavior(pkg.packageName)
            if (analysis["isSuspicious"] as Boolean) {
                val riskScore = analysis["riskScore"] as Int
                threats.add(
                    Threat(
                        id = generateId(),
                        type = ThreatType.MALWARE,
                        name = "Suspicious Application",
                        severity = getSeverityFromScore(riskScore),
                        description = "App shows suspicious behavior patterns",
                        packageName = pkg.packageName,
                        filePath = appInfo.sourceDir,
                        signature = "app_behavior_${pkg.packageName}",
                        confidence = riskScore / 100.0
                    )
                )
                threatsFound++
            }

            // Check permissions
            val permissionThreat = threatDetector.analyzePermissions(pkg.packageName)
            if (permissionThreat != null) {
                threats.add(permissionThreat)
                threatsFound++
            }
        }

        return threats
    }

    /**
     * Quick scan applications (limited check)
     */
    private fun quickScanApplications(): List<Threat> {
        val threats = mutableListOf<Threat>()
        val packages = context.packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS)

        for (pkg in packages) {
            appsScanned++
            val appInfo = pkg.applicationInfo ?: continue

            // Skip system apps
            if (appInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0) {
                continue
            }

            // Only check for high-risk apps
            val analysis = securityUtils.analyzeRatBehavior(pkg.packageName)
            if ((analysis["isSuspicious"] as Boolean) && (analysis["riskScore"] as Int) >= 70) {
                threats.add(
                    Threat(
                        id = generateId(),
                        type = ThreatType.MALWARE,
                        name = "High-Risk Application",
                        severity = ThreatSeverity.HIGH,
                        description = "App has high-risk behavior score",
                        packageName = pkg.packageName,
                        filePath = appInfo.sourceDir,
                        signature = "quick_scan_${pkg.packageName}",
                        confidence = (analysis["riskScore"] as Int) / 100.0
                    )
                )
                threatsFound++
            }
        }

        return threats
    }

    /**
     * Scan device configuration
     */
    private fun scanConfiguration(): List<Threat> {
        val threats = mutableListOf<Threat>()

        // Check for ADB debugging enabled
        if (android.provider.Settings.Global.getInt(
                context.contentResolver,
                android.provider.Settings.Global.ADB_ENABLED,
                0
            ) == 1
        ) {
            threats.add(
                Threat(
                    id = generateId(),
                    type = ThreatType.TROJAN,
                    name = "ADB Debugging Enabled",
                    severity = ThreatSeverity.MEDIUM,
                    description = "USB debugging is enabled, which may expose device to security risks",
                    packageName = null,
                    filePath = null,
                    signature = "adb_enabled",
                    confidence = 0.9
                )
            )
            threatsFound++
        }

        // Check for unknown sources (Android 7 and below)
        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.N) {
            try {
                val installUnknownSources = android.provider.Settings.Secure.getInt(
                    context.contentResolver,
                    android.provider.Settings.Secure.INSTALL_NON_MARKET_APPS
                )
                if (installUnknownSources == 1) {
                    threats.add(
                        Threat(
                            id = generateId(),
                            type = ThreatType.MALWARE,
                            name = "Install from Unknown Sources",
                            severity = ThreatSeverity.MEDIUM,
                            description = "Installation from unknown sources is allowed",
                            packageName = null,
                            filePath = null,
                            signature = "unknown_sources",
                            confidence = 0.8
                        )
                    )
                    threatsFound++
                }
            } catch (e: Exception) {
                // Setting may not exist
            }
        }

        // Check for developer mode
        if (android.provider.Settings.Global.getInt(
                context.contentResolver,
                android.provider.Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
                0
            ) == 1
        ) {
            threats.add(
                Threat(
                    id = generateId(),
                    type = ThreatType.TROJAN,
                    name = "Developer Mode Enabled",
                    severity = ThreatSeverity.LOW,
                    description = "Developer options are enabled",
                    packageName = null,
                    filePath = null,
                    signature = "developer_mode",
                    confidence = 0.6
                )
            )
        }

        return threats
    }

    /**
     * Scan for root access
     */
    private fun scanForRoot(): List<Threat> {
        val threats = mutableListOf<Threat>()

        if (securityUtils.isDeviceRooted()) {
            threats.add(
                Threat(
                    id = generateId(),
                    type = ThreatType.TROJAN,
                    name = "Root Access Detected",
                    severity = ThreatSeverity.HIGH,
                    description = "Device has root access, which increases security risk",
                    packageName = null,
                    filePath = null,
                    signature = "root_access",
                    confidence = 1.0
                )
            )
            threatsFound++
        }

        return threats
    }

    /**
     * Scan system files for modifications
     */
    private fun scanSystemFiles(): List<Threat> {
        val threats = mutableListOf<Threat>()

        // Check for common RAT/Trojan files
        val suspiciousPaths = listOf(
            "/data/local/tmp/rat",
            "/data/local/tmp/.rat",
            "/system/bin/.su",
            "/system/xbin/.su",
            "/data/data/com.android.systemui.rat"
        )

        for (path in suspiciousPaths) {
            val file = File(path)
            if (file.exists()) {
                threats.add(
                    Threat(
                        id = generateId(),
                        type = ThreatType.ZERODAYRAT,
                        name = "Suspicious File Detected",
                        severity = ThreatSeverity.CRITICAL,
                        description = "Suspicious file found at: $path",
                        packageName = null,
                        filePath = path,
                        signature = securityUtils.hashSHA256(path),
                        confidence = 0.9
                    )
                )
                threatsFound++
            }
        }

        return threats
    }

    /**
     * Scan for suspicious permissions
     */
    private fun scanPermissions(): List<Threat> {
        val threats = mutableListOf<Threat>()
        val packages = context.packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS)

        for (pkg in packages) {
            val appInfo = pkg.applicationInfo ?: continue

            // Skip system apps
            if (appInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0) {
                continue
            }

            // Check for spyware-like permissions
            val permissionThreat = threatDetector.analyzePermissions(pkg.packageName)
            if (permissionThreat != null) {
                threats.add(permissionThreat)
                threatsFound++
            }
        }

        return threats
    }

    private fun getSeverityFromScore(score: Int): ThreatSeverity {
        return when (score) {
            in 90..100 -> ThreatSeverity.CRITICAL
            in 70..89 -> ThreatSeverity.HIGH
            in 40..69 -> ThreatSeverity.MEDIUM
            else -> ThreatSeverity.LOW
        }
    }

    private fun generateId(): String {
        return java.util.UUID.randomUUID().toString()
    }
}
