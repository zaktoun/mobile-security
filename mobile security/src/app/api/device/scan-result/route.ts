import { NextRequest, NextResponse } from 'next/server'
import { db } from '@/lib/db'

export async function POST(request: NextRequest) {
  try {
    const body = await request.json()
    const {
      deviceId,
      scanType,
      duration,
      appsScanned,
      threatsFound,
      threatsQuarantined,
      threatsRemoved,
      details,
      threats = []
    } = body

    // Validate required fields
    if (!deviceId) {
      return NextResponse.json(
        { error: 'deviceId is required' },
        { status: 400 }
      )
    }

    // Find device by deviceId
    const device = await db.device.findUnique({
      where: { deviceId }
    })

    if (!device) {
      return NextResponse.json(
        { error: 'Device not found' },
        { status: 404 }
      )
    }

    // Create scan result
    const scanResult = await db.scanResult.create({
      data: {
        scanType: scanType || 'quick',
        status: 'completed',
        duration: duration || 0,
        appsScanned: appsScanned || 0,
        threatsFound: threatsFound || 0,
        threatsQuarantined: threatsQuarantined || 0,
        threatsRemoved: threatsRemoved || 0,
        details: details ? JSON.stringify(details) : null,
        deviceId: device.id
      }
    })

    // Update device last scan time
    await db.device.update({
      where: { id: device.id },
      data: {
        lastScanAt: new Date(),
        lastSeen: new Date(),
        status: threatsFound > 0 ? 'compromised' : 'clean'
      }
    })

    // Create threats and alerts if any
    if (threats && threats.length > 0) {
      for (const threatData of threats) {
        const threat = await db.threat.create({
          data: {
            type: threatData.type || 'unknown',
            name: threatData.name || 'Unknown Threat',
            severity: threatData.severity || 'medium',
            description: threatData.description || '',
            packageName: threatData.packageName || null,
            filePath: threatData.filePath || null,
            signature: threatData.signature || '',
            confidence: threatData.confidence || 0.5,
            deviceId: device.id,
            status: threatData.status || 'detected'
          }
        })

        // Create alert for each threat
        await db.alert.create({
          data: {
            type: 'threat_detected',
            title: `${threatData.severity.toUpperCase()}: ${threatData.name}`,
            message: threatData.description || `Threat detected: ${threatData.name}`,
            severity: threatData.severity || 'medium',
            status: 'unread',
            deviceId: device.id,
            threatId: threat.id,
            actionRequired: true
          }
        })
      }
    }

    // Create scan completed alert
    await db.alert.create({
      data: {
        type: 'scan_completed',
        title: 'Scan Completed',
        message: `${scanType || 'Quick'} scan completed. ${threatsFound} threat(s) found, ${threatsRemoved} removed.`,
        severity: threatsFound > 0 ? 'high' : 'low',
        status: 'read',
        deviceId: device.id,
        actionRequired: false
      }
    })

    return NextResponse.json({
      success: true,
      scanResult,
      threatsProcessed: threats?.length || 0,
      message: 'Scan result saved successfully'
    })
  } catch (error) {
    console.error('Error saving scan result:', error)
    return NextResponse.json(
      { error: 'Failed to save scan result' },
      { status: 500 }
    )
  }
}
