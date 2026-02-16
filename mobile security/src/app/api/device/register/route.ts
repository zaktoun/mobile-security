import { NextRequest, NextResponse } from 'next/server'
import { db } from '@/lib/db'

export async function POST(request: NextRequest) {
  try {
    const body = await request.json()
    const { deviceId, deviceName, platform, osVersion, appVersion } = body

    // Validate required fields
    if (!deviceId || !deviceName || !platform || !osVersion) {
      return NextResponse.json(
        { error: 'Missing required fields' },
        { status: 400 }
      )
    }

    // Check if device already exists
    const existingDevice = await db.device.findUnique({
      where: { deviceId }
    })

    let device
    if (existingDevice) {
      // Update existing device
      device = await db.device.update({
        where: { deviceId },
        data: {
          deviceName,
          osVersion,
          appVersion: appVersion || existingDevice.appVersion,
          lastSeen: new Date()
        }
      })
    } else {
      // Create new device
      device = await db.device.create({
        data: {
          deviceId,
          deviceName,
          platform: platform.toLowerCase(),
          osVersion,
          appVersion: appVersion || '1.0.0',
          status: 'active'
        }
      })

      // Create welcome alert
      await db.alert.create({
        data: {
          type: 'device_compromised',
          title: 'New Device Registered',
          message: `Device ${deviceName} has been successfully registered and is being monitored.`,
          severity: 'low',
          status: 'read',
          deviceId: device.id,
          actionRequired: false
        }
      })
    }

    return NextResponse.json({
      success: true,
      device,
      message: existingDevice ? 'Device updated successfully' : 'Device registered successfully'
    })
  } catch (error) {
    console.error('Error registering device:', error)
    return NextResponse.json(
      { error: 'Failed to register device' },
      { status: 500 }
    )
  }
}
