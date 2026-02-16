import { NextResponse } from 'next/server'
import { db } from '@/lib/db'

export async function GET() {
  try {
    const [
      totalDevices,
      activeDevices,
      totalThreats,
      criticalThreats,
      resolvedThreats,
      pendingAlerts,
      latestScan
    ] = await Promise.all([
      db.device.count(),
      db.device.count({ where: { status: 'active' } }),
      db.threat.count(),
      db.threat.count({ where: { severity: 'critical' } }),
      db.threat.count({ where: { status: { in: ['quarantined', 'removed'] } } }),
      db.alert.count({ where: { status: 'unread' } }),
      db.scanResult.findFirst({
        orderBy: { createdAt: 'desc' },
        select: { createdAt: true }
      })
    ])

    // Calculate security score based on threats and resolved issues
    const securityScore = totalThreats > 0
      ? Math.max(0, Math.min(100, 100 - (criticalThreats * 10) + (resolvedThreats * 2)))
      : 100

    return NextResponse.json({
      totalDevices,
      activeDevices,
      totalThreats,
      criticalThreats,
      resolvedThreats,
      pendingAlerts,
      securityScore,
      lastScanTime: latestScan?.createdAt 
        ? new Date(latestScan.createdAt).toLocaleString() 
        : 'No scans yet'
    })
  } catch (error) {
    console.error('Error fetching dashboard stats:', error)
    return NextResponse.json(
      { error: 'Failed to fetch dashboard stats' },
      { status: 500 }
    )
  }
}
