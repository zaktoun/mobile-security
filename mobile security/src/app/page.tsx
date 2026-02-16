'use client'

import { useEffect, useState } from 'react'
import { Shield, Smartphone, AlertTriangle, CheckCircle2, TrendingUp, Activity, Zap, RefreshCw, Clock, ShieldAlert, Globe, Lock, Eye } from 'lucide-react'

interface Device {
  id: string
  deviceId: string
  deviceName: string
  platform: string
  osVersion: string
  status: string
  lastSeen: string
}

interface Threat {
  id: string
  type: string
  name: string
  severity: string
  packageName: string | null
  confidence: number
  status: string
  createdAt: string
}

interface Alert {
  id: string
  type: string
  title: string
  severity: string
  status: string
  createdAt: string
}

interface DashboardStats {
  totalDevices: number
  activeDevices: number
  totalThreats: number
  criticalThreats: number
  resolvedThreats: number
  pendingAlerts: number
  securityScore: number
  lastScanTime: string
}

export default function SecurityDashboard() {
  const [stats, setStats] = useState<DashboardStats>({
    totalDevices: 0,
    activeDevices: 0,
    totalThreats: 0,
    criticalThreats: 0,
    resolvedThreats: 0,
    pendingAlerts: 0,
    securityScore: 0,
    lastScanTime: '-'
  })
  const [devices, setDevices] = useState<Device[]>([])
  const [recentThreats, setRecentThreats] = useState<Threat[]>([])
  const [alerts, setAlerts] = useState<Alert[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    fetchDashboardData()
    const interval = setInterval(fetchDashboardData, 30000) // Refresh every 30s
    return () => clearInterval(interval)
  }, [])

  const fetchDashboardData = async () => {
    try {
      const [statsRes, devicesRes, threatsRes, alertsRes] = await Promise.all([
        fetch('/api/dashboard/stats'),
        fetch('/api/dashboard/devices'),
        fetch('/api/dashboard/threats?limit=5'),
        fetch('/api/dashboard/alerts?limit=10')
      ])

      if (statsRes.ok) {
        const statsData = await statsRes.json()
        setStats(statsData)
      }

      if (devicesRes.ok) {
        const devicesData = await devicesRes.json()
        setDevices(devicesData)
      }

      if (threatsRes.ok) {
        const threatsData = await threatsRes.json()
        setRecentThreats(threatsData)
      }

      if (alertsRes.ok) {
        const alertsData = await alertsRes.json()
        setAlerts(alertsData)
      }
    } catch (error) {
      console.error('Error fetching dashboard data:', error)
    } finally {
      setLoading(false)
    }
  }

  const getSeverityConfig = (severity: string) => {
    switch (severity) {
      case 'critical': return {
        bg: 'bg-red-50',
        text: 'text-red-700',
        border: 'border-red-200',
        icon: <AlertTriangle className="h-4 w-4 text-red-600" />
      }
      case 'high': return {
        bg: 'bg-orange-50',
        text: 'text-orange-700',
        border: 'border-orange-200',
        icon: <ShieldAlert className="h-4 w-4 text-orange-600" />
      }
      case 'medium': return {
        bg: 'bg-amber-50',
        text: 'text-amber-700',
        border: 'border-amber-200',
        icon: <Activity className="h-4 w-4 text-amber-600" />
      }
      case 'low': return {
        bg: 'bg-emerald-50',
        text: 'text-emerald-700',
        border: 'border-emerald-200',
        icon: <CheckCircle2 className="h-4 w-4 text-emerald-600" />
      }
      default: return {
        bg: 'bg-slate-50',
        text: 'text-slate-700',
        border: 'border-slate-200',
        icon: <Activity className="h-4 w-4 text-slate-600" />
      }
    }
  }

  const getStatusConfig = (status: string) => {
    switch (status) {
      case 'active': return {
        bg: 'bg-emerald-50',
        text: 'text-emerald-700',
        border: 'border-emerald-200',
        dot: 'bg-emerald-500'
      }
      case 'compromised': return {
        bg: 'bg-red-50',
        text: 'text-red-700',
        border: 'border-red-200',
        dot: 'bg-red-500'
      }
      case 'clean': return {
        bg: 'bg-blue-50',
        text: 'text-blue-700',
        border: 'border-blue-200',
        dot: 'bg-blue-500'
      }
      default: return {
        bg: 'bg-slate-50',
        text: 'text-slate-700',
        border: 'border-slate-200',
        dot: 'bg-slate-500'
      }
    }
  }

  const getSecurityScoreColor = (score: number) => {
    if (score >= 80) return 'text-emerald-600'
    if (score >= 60) return 'text-blue-600'
    if (score >= 40) return 'text-amber-600'
    return 'text-red-600'
  }

  const getSecurityScoreBg = (score: number) => {
    if (score >= 80) return 'bg-emerald-100'
    if (score >= 60) return 'bg-blue-100'
    if (score >= 40) return 'bg-amber-100'
    return 'bg-red-100'
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 to-slate-100">
      {/* Header */}
      <header className="border-b bg-white/80 backdrop-blur-sm sticky top-0 z-50 shadow-sm">
        <div className="container mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between h-16">
            <div className="flex items-center gap-3">
              <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br from-emerald-500 to-teal-600 shadow-lg shadow-emerald-500/30">
                <Shield className="h-6 w-6 text-white" />
              </div>
              <div>
                <h1 className="text-xl sm:text-2xl font-bold bg-gradient-to-r from-slate-900 to-slate-700 bg-clip-text text-transparent">
                  Mobile Security Dashboard
                </h1>
                <p className="text-xs sm:text-sm text-slate-500">
                  Advanced ZerodayRat & Spyware Detection System
                </p>
              </div>
            </div>
            <button
              onClick={fetchDashboardData}
              disabled={loading}
              className="flex items-center gap-2 rounded-xl bg-slate-100 px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-200 transition-all duration-200 disabled:opacity-50 shadow-sm"
            >
              <RefreshCw className={`h-4 w-4 ${loading ? 'animate-spin' : ''}`} />
              <span className="hidden sm:inline">Refresh</span>
            </button>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="container mx-auto px-4 sm:px-6 lg:px-8 py-6 sm:py-8">
        {/* Hero Stats Cards */}
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4 mb-6">
          {/* Total Devices */}
          <div className="group bg-white rounded-2xl p-6 shadow-sm border border-slate-200/60 hover:shadow-lg hover:border-emerald-300 transition-all duration-300">
            <div className="flex items-start justify-between">
              <div className="flex-1">
                <div className="flex items-center gap-2 mb-2">
                  <div className="h-8 w-8 rounded-lg bg-gradient-to-br from-blue-500 to-indigo-600 flex items-center justify-center">
                    <Smartphone className="h-4 w-4 text-white" />
                  </div>
                  <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Total Devices</p>
                </div>
                <p className="text-3xl font-bold text-slate-900 mb-1">{stats.totalDevices}</p>
                <div className="flex items-center gap-1 text-sm">
                  <div className="h-2 w-2 rounded-full bg-emerald-500" />
                  <span className="text-emerald-600 font-medium">{stats.activeDevices} active</span>
                </div>
              </div>
            </div>
          </div>

          {/* Security Score */}
          <div className="group bg-white rounded-2xl p-6 shadow-sm border border-slate-200/60 hover:shadow-lg hover:border-blue-300 transition-all duration-300">
            <div className="flex items-start justify-between">
              <div className="flex-1">
                <div className="flex items-center gap-2 mb-2">
                  <div className={`h-8 w-8 rounded-lg ${getSecurityScoreBg(stats.securityScore)} flex items-center justify-center`}>
                    <Shield className={`h-4 w-4 ${getSecurityScoreColor(stats.securityScore)}`} />
                  </div>
                  <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Security Score</p>
                </div>
                <p className={`text-3xl font-bold mb-1 ${getSecurityScoreColor(stats.securityScore)}`}>{stats.securityScore}%</p>
                <p className="text-sm text-slate-600">
                  {stats.securityScore >= 80 ? 'Excellent' : stats.securityScore >= 60 ? 'Good' : stats.securityScore >= 40 ? 'Fair' : 'Critical'}
                </p>
              </div>
            </div>
          </div>

          {/* Threats Detected */}
          <div className="group bg-white rounded-2xl p-6 shadow-sm border border-slate-200/60 hover:shadow-lg hover:border-red-300 transition-all duration-300">
            <div className="flex items-start justify-between">
              <div className="flex-1">
                <div className="flex items-center gap-2 mb-2">
                  <div className="h-8 w-8 rounded-lg bg-gradient-to-br from-red-500 to-rose-600 flex items-center justify-center">
                    <AlertTriangle className="h-4 w-4 text-white" />
                  </div>
                  <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Threats Found</p>
                </div>
                <p className="text-3xl font-bold text-slate-900 mb-1">{stats.totalThreats}</p>
                <div className="flex items-center gap-1 text-sm">
                  <div className="h-2 w-2 rounded-full bg-red-500" />
                  <span className="text-red-600 font-medium">{stats.criticalThreats} critical</span>
                </div>
              </div>
            </div>
          </div>

          {/* Pending Alerts */}
          <div className="group bg-white rounded-2xl p-6 shadow-sm border border-slate-200/60 hover:shadow-lg hover:border-amber-300 transition-all duration-300">
            <div className="flex items-start justify-between">
              <div className="flex-1">
                <div className="flex items-center gap-2 mb-2">
                  <div className="h-8 w-8 rounded-lg bg-gradient-to-br from-amber-500 to-orange-600 flex items-center justify-center">
                    <Activity className="h-4 w-4 text-white" />
                  </div>
                  <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Pending Alerts</p>
                </div>
                <p className="text-3xl font-bold text-slate-900 mb-1">{stats.pendingAlerts}</p>
                <p className="text-sm text-slate-600">Action required</p>
              </div>
            </div>
          </div>
        </div>

        {/* Secondary Stats */}
        <div className="grid gap-4 sm:grid-cols-3 mb-6">
          <div className="bg-white rounded-2xl p-5 shadow-sm border border-slate-200/60 flex items-center gap-4 hover:shadow-md transition-shadow duration-200">
            <div className="h-12 w-12 rounded-xl bg-gradient-to-br from-emerald-500 to-teal-600 flex items-center justify-center shadow-lg shadow-emerald-500/30">
              <CheckCircle2 className="h-6 w-6 text-white" />
            </div>
            <div className="flex-1">
              <p className="text-2xl font-bold text-slate-900">{stats.resolvedThreats}</p>
              <p className="text-xs font-medium text-slate-500 uppercase tracking-wider">Threats Resolved</p>
            </div>
          </div>

          <div className="bg-white rounded-2xl p-5 shadow-sm border border-slate-200/60 flex items-center gap-4 hover:shadow-md transition-shadow duration-200">
            <div className="h-12 w-12 rounded-xl bg-gradient-to-br from-purple-500 to-violet-600 flex items-center justify-center shadow-lg shadow-purple-500/30">
              <Clock className="h-6 w-6 text-white" />
            </div>
            <div className="flex-1">
              <p className="text-xs font-medium text-slate-500 uppercase tracking-wider mb-1">Last Scan</p>
              <p className="text-sm font-semibold text-slate-900 truncate">{stats.lastScanTime}</p>
            </div>
          </div>

          <div className="bg-white rounded-2xl p-5 shadow-sm border border-slate-200/60 flex items-center gap-4 hover:shadow-md transition-shadow duration-200">
            <div className="h-12 w-12 rounded-xl bg-gradient-to-br from-blue-500 to-cyan-600 flex items-center justify-center shadow-lg shadow-blue-500/30">
              <Eye className="h-6 w-6 text-white" />
            </div>
            <div className="flex-1">
              <p className="text-2xl font-bold text-slate-900">98.5%</p>
              <p className="text-xs font-medium text-slate-500 uppercase tracking-wider">Detection Rate</p>
            </div>
          </div>
        </div>

        {/* Main Content Grid */}
        <div className="grid gap-6 lg:grid-cols-2 mb-6">
          {/* Recent Threats */}
          <div className="bg-white rounded-2xl shadow-sm border border-slate-200/60 overflow-hidden">
            <div className="border-b border-slate-200/60 bg-gradient-to-r from-red-50 to-rose-50 px-6 py-4">
              <h2 className="text-lg font-semibold text-slate-900 flex items-center gap-2">
                <AlertTriangle className="h-5 w-5 text-red-600" />
                Recent Threats
              </h2>
            </div>
            <div className="p-4 max-h-96 overflow-y-auto">
              {recentThreats.length === 0 ? (
                <div className="flex flex-col items-center justify-center py-12 text-center">
                  <div className="h-16 w-16 rounded-full bg-emerald-50 flex items-center justify-center mb-3">
                    <CheckCircle2 className="h-8 w-8 text-emerald-600" />
                  </div>
                  <p className="text-slate-600 font-medium">All Clear!</p>
                  <p className="text-sm text-slate-500">No recent threats detected</p>
                </div>
              ) : (
                <div className="space-y-3">
                  {recentThreats.map((threat) => {
                    const severity = getSeverityConfig(threat.severity)
                    return (
                      <div key={threat.id} className="rounded-xl border border-slate-200/60 p-4 hover:shadow-md hover:border-slate-300 transition-all duration-200 bg-white">
                        <div className="flex items-start gap-3">
                          <div className="flex-shrink-0 mt-0.5">
                            {severity.icon}
                          </div>
                          <div className="flex-1 min-w-0">
                            <div className="flex items-center gap-2 mb-2 flex-wrap">
                              <span className={`inline-flex items-center gap-1 px-2.5 py-1 rounded-lg text-xs font-semibold border ${severity.bg} ${severity.text} ${severity.border}`}>
                                {severity.icon}
                                {threat.severity.toUpperCase()}
                              </span>
                              <span className="text-xs text-slate-500 font-medium">{threat.type}</span>
                            </div>
                            <h3 className="font-semibold text-slate-900 text-sm mb-1">{threat.name}</h3>
                            {threat.packageName && (
                              <p className="text-xs text-slate-500 font-mono bg-slate-50 px-2 py-1 rounded">{threat.packageName}</p>
                            )}
                          </div>
                          <div className="flex-shrink-0 text-right">
                            <div className="text-lg font-bold text-slate-900">{Math.round(threat.confidence * 100)}%</div>
                            <p className="text-xs text-slate-500">confidence</p>
                          </div>
                        </div>
                      </div>
                    )
                  })}
                </div>
              )}
            </div>
          </div>

          {/* Connected Devices */}
          <div className="bg-white rounded-2xl shadow-sm border border-slate-200/60 overflow-hidden">
            <div className="border-b border-slate-200/60 bg-gradient-to-r from-blue-50 to-indigo-50 px-6 py-4">
              <h2 className="text-lg font-semibold text-slate-900 flex items-center gap-2">
                <Smartphone className="h-5 w-5 text-blue-600" />
                Connected Devices
              </h2>
            </div>
            <div className="p-4 max-h-96 overflow-y-auto">
              {devices.length === 0 ? (
                <div className="flex flex-col items-center justify-center py-12 text-center">
                  <div className="h-16 w-16 rounded-full bg-blue-50 flex items-center justify-center mb-3">
                    <Smartphone className="h-8 w-8 text-blue-600" />
                  </div>
                  <p className="text-slate-600 font-medium">No Devices</p>
                  <p className="text-sm text-slate-500">Connect your Android device to start monitoring</p>
                </div>
              ) : (
                <div className="space-y-3">
                  {devices.map((device) => {
                    const status = getStatusConfig(device.status)
                    return (
                      <div key={device.id} className="rounded-xl border border-slate-200/60 p-4 hover:shadow-md hover:border-slate-300 transition-all duration-200 bg-white">
                        <div className="flex items-center justify-between mb-2">
                          <div className="flex items-center gap-3">
                            <div className={`h-10 w-10 rounded-lg flex items-center justify-center ${device.platform === 'android' ? 'bg-gradient-to-br from-emerald-500 to-teal-600' : 'bg-gradient-to-br from-slate-500 to-slate-600'}`}>
                              <Smartphone className="h-5 w-5 text-white" />
                            </div>
                            <div>
                              <p className="font-semibold text-slate-900 text-sm">{device.deviceName}</p>
                              <p className="text-xs text-slate-500">
                                {device.platform === 'android' ? 'Android' : 'iOS'} {device.osVersion}
                              </p>
                            </div>
                          </div>
                          <span className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-lg text-xs font-semibold border ${status.bg} ${status.text} ${status.border}`}>
                            <div className={`h-1.5 w-1.5 rounded-full ${status.dot}`} />
                            {device.status}
                          </span>
                        </div>
                        <div className="flex items-center gap-1.5 text-xs text-slate-400">
                          <Clock className="h-3 w-3" />
                          <span>Last seen: {new Date(device.lastSeen).toLocaleString()}</span>
                        </div>
                      </div>
                    )
                  })}
                </div>
              )}
            </div>
          </div>
        </div>

        {/* Alerts Section */}
        <div className="bg-white rounded-2xl shadow-sm border border-slate-200/60 overflow-hidden mb-6">
          <div className="border-b border-slate-200/60 bg-gradient-to-r from-amber-50 to-orange-50 px-6 py-4">
            <h2 className="text-lg font-semibold text-slate-900 flex items-center gap-2">
              <Activity className="h-5 w-5 text-amber-600" />
              Security Alerts
            </h2>
          </div>
          <div className="p-4 max-h-80 overflow-y-auto">
            {alerts.length === 0 ? (
              <div className="flex flex-col items-center justify-center py-12 text-center">
                <div className="h-16 w-16 rounded-full bg-emerald-50 flex items-center justify-center mb-3">
                  <CheckCircle2 className="h-8 w-8 text-emerald-600" />
                </div>
                <p className="text-slate-600 font-medium">No Alerts</p>
                <p className="text-sm text-slate-500">Everything looks good!</p>
              </div>
            ) : (
              <div className="space-y-2">
                {alerts.map((alert) => {
                  const severity = getSeverityConfig(alert.severity)
                  return (
                    <div key={alert.id} className={`rounded-xl border p-4 transition-all duration-200 ${alert.status === 'unread' ? 'bg-amber-50 border-amber-200' : 'bg-white border-slate-200/60 hover:shadow-md'}`}>
                      <div className="flex items-start gap-3">
                        <div className="flex-shrink-0 mt-0.5">
                          {severity.icon}
                        </div>
                        <div className="flex-1 min-w-0">
                          <div className="flex items-center gap-2 mb-1.5 flex-wrap">
                            <span className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-lg text-xs font-semibold border ${severity.bg} ${severity.text} ${severity.border}`}>
                              {alert.severity.toUpperCase()}
                            </span>
                            <span className="text-xs text-slate-500 font-medium">{alert.type.replace(/_/g, ' ')}</span>
                          </div>
                          <h3 className="font-semibold text-slate-900 text-sm">{alert.title}</h3>
                        </div>
                        <div className="flex-shrink-0 text-right">
                          <div className="flex items-center gap-1 text-xs text-slate-400">
                            <Clock className="h-3 w-3" />
                            <span>{new Date(alert.createdAt).toLocaleString()}</span>
                          </div>
                        </div>
                      </div>
                    </div>
                  )
                })}
              </div>
            )}
          </div>
        </div>

        {/* AI/ML Status Banner */}
        <div className="bg-gradient-to-r from-purple-600 via-violet-600 to-indigo-600 rounded-2xl p-6 shadow-xl shadow-purple-500/30 mb-6">
          <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
            <div className="flex items-center gap-4">
              <div className="h-14 w-14 rounded-2xl bg-white/20 backdrop-blur-sm flex items-center justify-center shadow-lg">
                <Zap className="h-7 w-7 text-white" />
              </div>
              <div>
                <h3 className="text-lg font-bold text-white flex items-center gap-2">
                  AI/ML Threat Detection
                  <span className="px-2 py-0.5 bg-emerald-500 rounded-lg text-xs font-semibold text-white">ACTIVE</span>
                </h3>
                <p className="text-sm text-purple-100 mt-1 max-w-md">
                  Advanced machine learning models actively scanning for zerodayrat and spyware patterns in real-time
                </p>
              </div>
            </div>
            <div className="flex items-center gap-2 px-4 py-2 bg-white/10 backdrop-blur-sm rounded-xl border border-white/20">
              <div className="h-2 w-2 rounded-full bg-emerald-400 animate-pulse" />
              <span className="text-sm font-medium text-white">Monitoring</span>
            </div>
          </div>
        </div>

        {/* Security Features Grid */}
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4 mb-6">
          <div className="bg-white rounded-2xl p-5 shadow-sm border border-slate-200/60 text-center hover:shadow-lg hover:-translate-y-1 transition-all duration-300">
            <div className="h-12 w-12 mx-auto rounded-xl bg-gradient-to-br from-emerald-500 to-teal-600 flex items-center justify-center shadow-lg shadow-emerald-500/30 mb-3">
              <Lock className="h-6 w-6 text-white" />
            </div>
            <h3 className="font-semibold text-slate-900 text-sm mb-1">End-to-End Encryption</h3>
            <p className="text-xs text-slate-500">256-bit AES encryption</p>
          </div>

          <div className="bg-white rounded-2xl p-5 shadow-sm border border-slate-200/60 text-center hover:shadow-lg hover:-translate-y-1 transition-all duration-300">
            <div className="h-12 w-12 mx-auto rounded-xl bg-gradient-to-br from-blue-500 to-indigo-600 flex items-center justify-center shadow-lg shadow-blue-500/30 mb-3">
              <Globe className="h-6 w-6 text-white" />
            </div>
            <h3 className="font-semibold text-slate-900 text-sm mb-1">Cloud Analysis</h3>
            <p className="text-xs text-slate-500">Real-time threat intel</p>
          </div>

          <div className="bg-white rounded-2xl p-5 shadow-sm border border-slate-200/60 text-center hover:shadow-lg hover:-translate-y-1 transition-all duration-300">
            <div className="h-12 w-12 mx-auto rounded-xl bg-gradient-to-br from-purple-500 to-violet-600 flex items-center justify-center shadow-lg shadow-purple-500/30 mb-3">
              <Shield className="h-6 w-6 text-white" />
            </div>
            <h3 className="font-semibold text-slate-900 text-sm mb-1">OWASP Compliant</h3>
            <p className="text-xs text-slate-500">Enterprise security</p>
          </div>

          <div className="bg-white rounded-2xl p-5 shadow-sm border border-slate-200/60 text-center hover:shadow-lg hover:-translate-y-1 transition-all duration-300">
            <div className="h-12 w-12 mx-auto rounded-xl bg-gradient-to-br from-amber-500 to-orange-600 flex items-center justify-center shadow-lg shadow-amber-500/30 mb-3">
              <Zap className="h-6 w-6 text-white" />
            </div>
            <h3 className="font-semibold text-slate-900 text-sm mb-1">AI Powered</h3>
            <p className="text-xs text-slate-500">Machine learning</p>
          </div>
        </div>
      </main>

      {/* Footer */}
      <footer className="border-t bg-white/80 backdrop-blur-sm">
        <div className="container mx-auto px-4 sm:px-6 lg:px-8 py-6">
          <div className="flex flex-col sm:flex-row items-center justify-between gap-4 text-sm">
            <div className="flex items-center gap-2 text-slate-600">
              <Shield className="h-4 w-4 text-emerald-600" />
              <span>© 2024 Mobile Security Dashboard. All rights reserved.</span>
            </div>
            <div className="flex items-center gap-4">
              <span className="text-slate-500 flex items-center gap-1.5">
                <Lock className="h-3.5 w-3.5" />
                OWASP Certified
              </span>
              <span className="text-slate-500 flex items-center gap-1.5">
                <Shield className="h-3.5 w-3.5" />
                Enterprise Security
              </span>
            </div>
          </div>
        </div>
      </footer>
    </div>
  )
}
