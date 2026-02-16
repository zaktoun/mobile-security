import { NextRequest, NextResponse } from 'next/server'
import { db } from '@/lib/db'

export async function POST(request: NextRequest) {
  try {
    const body = await request.json()
    const {
      deviceId,
      type,
      name,
      severity,
      description,
      packageName,
      filePath,
      signature,
      confidence
    } = body

    // Validate required fields
    if (!deviceId || !type || !name) {
      return NextResponse.json(
        { error: 'Missing required fields' },
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

    // Create threat
    const threat = await db.threat.create({
      data: {
        type: type.toLowerCase(),
        name,
        severity: severity?.toLowerCase() || 'medium',
        description: description || '',
        packageName: packageName || null,
        filePath: filePath || null,
        signature: signature || '',
        confidence: confidence || 0.5,
        deviceId: device.id,
        status: 'detected'
      }
    })

    // Create alert
    await db.alert.create({
      data: {
        type: 'threat_detected',
        title: `${severity?.toUpperCase() || 'MEDIUM'}: ${name}`,
        message: description || `Threat detected: ${name}`,
        severity: severity?.toLowerCase() || 'medium',
        status: 'unread',
        deviceId: device.id,
        threatId: threat.id,
        actionRequired: true
      }
    })

    // Update device status if critical or high threat
    if (severity?.toLowerCase() === 'critical' || severity?.toLowerCase() === 'high') {
      await db.device.update({
        where: { id: device.id },
        data: { status: 'compromised' }
      })
    }

    return NextResponse.json({
      success: true,
      threat,
      message: 'Threat reported successfully'
    })
  } catch (error) {
    console.error('Error reporting threat:', error)
    return NextResponse.json(
      { error: 'Failed to report threat' },
      { status: 500 }
    )
  }
}
