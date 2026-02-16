import { NextRequest, NextResponse } from 'next/server'
import { db } from '@/lib/db'

export async function GET(request: NextRequest) {
  try {
    const searchParams = request.nextUrl.searchParams
    const limit = parseInt(searchParams.get('limit') || '5')

    const threats = await db.threat.findMany({
      orderBy: { createdAt: 'desc' },
      take: limit
    })

    return NextResponse.json(threats)
  } catch (error) {
    console.error('Error fetching threats:', error)
    return NextResponse.json(
      { error: 'Failed to fetch threats' },
      { status: 500 }
    )
  }
}
