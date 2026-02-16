import { NextRequest, NextResponse } from 'next/server'
import { db } from '@/lib/db'

// AI Analysis API using z-ai-web-dev-sdk for threat detection
export async function POST(request: NextRequest) {
  try {
    const body = await request.json()
    const {
      packageName,
      behaviorData,
      networkData,
      permissions,
      fileHash,
      fileContent
    } = body

    // Build analysis prompt for AI
    const analysisPrompt = `You are a cybersecurity expert specializing in mobile threat detection. Analyze the following data to detect potential zerodayrat, spyware, or malware threats.

Package/App Information:
- Package Name: ${packageName || 'Unknown'}
- File Hash: ${fileHash || 'Unknown'}

Behavior Analysis:
${JSON.stringify(behaviorData, null, 2)}

Network Activity:
${JSON.stringify(networkData, null, 2)}

Requested Permissions:
${Array.isArray(permissions) ? permissions.join(', ') : 'None'}

Please analyze and provide:
1. Threat Type: (zerodayrat, spyware, malware, trojan, adware, or safe)
2. Severity: (critical, high, medium, low)
3. Confidence: (0.0 to 1.0)
4. Description: Detailed explanation of the threat
5. Indicators: Specific behaviors or patterns that indicate the threat
6. Recommendations: Steps to remediate the threat

Format your response as JSON:
{
  "threatType": "type",
  "severity": "severity",
  "confidence": 0.0-1.0,
  "description": "detailed description",
  "indicators": ["indicator1", "indicator2"],
  "recommendations": ["rec1", "rec2"]
}`

    // Call z-ai-web-dev-sdk for AI analysis
    const aiResponse = await fetch('http://localhost:3001/api/ai/chat', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        messages: [
          {
            role: 'system',
            content: 'You are a cybersecurity expert specializing in mobile threat detection. Always respond with valid JSON.'
          },
          {
            role: 'user',
            content: analysisPrompt
          }
        ],
        temperature: 0.3,
        maxTokens: 1000
      })
    })

    if (!aiResponse.ok) {
      throw new Error('AI service unavailable')
    }

    const aiData = await aiResponse.json()
    const aiContent = aiData.choices?.[0]?.message?.content || '{}'

    // Parse AI response
    let analysisResult
    try {
      // Extract JSON from AI response
      const jsonMatch = aiContent.match(/\{[\s\S]*\}/)
      analysisResult = jsonMatch ? JSON.parse(jsonMatch[0]) : JSON.parse(aiContent)
    } catch (parseError) {
      // If AI response is not valid JSON, create default response
      analysisResult = {
        threatType: 'unknown',
        severity: 'medium',
        confidence: 0.5,
        description: aiContent || 'Unable to analyze threat',
        indicators: [],
        recommendations: ['Manual review required']
      }
    }

    // Store analysis pattern for ML learning
    if (packageName && analysisResult.confidence > 0.7) {
      await db.threatPattern.create({
        data: {
          patternType: 'behavior',
          patternData: JSON.stringify({
            packageName,
            behaviorData,
            networkData,
            permissions,
            analysis: analysisResult
          }),
          matchRate: analysisResult.confidence,
          falsePositiveRate: 0.0,
          accuracy: analysisResult.confidence
        }
      })
    }

    return NextResponse.json({
      success: true,
      analysis: analysisResult,
      timestamp: new Date().toISOString()
    })
  } catch (error) {
    console.error('Error in AI threat analysis:', error)
    return NextResponse.json(
      {
        success: false,
        error: 'Failed to analyze threat',
        analysis: {
          threatType: 'unknown',
          severity: 'medium',
          confidence: 0.0,
          description: 'AI analysis service unavailable',
          indicators: [],
          recommendations: ['Try again later']
        }
      },
      { status: 500 }
    )
  }
}
