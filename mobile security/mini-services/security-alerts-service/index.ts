import { Server } from 'socket.io'

const io = new Server(3005, {
  cors: {
    origin: '*',
    methods: ['GET', 'POST']
  }
})

console.log('🔒 Security Alerts WebSocket Service running on port 3005')

interface ConnectedDevice {
  deviceId: string
  socketId: string
  connectedAt: Date
}

const connectedDevices = new Map<string, ConnectedDevice>()

io.on('connection', (socket) => {
  console.log('Client connected:', socket.id)

  // Device registration
  socket.on('register-device', (data: { deviceId: string; deviceName: string }) => {
    const { deviceId, deviceName } = data
    console.log('Device registered:', deviceId, deviceName)

    connectedDevices.set(socket.id, {
      deviceId,
      socketId: socket.id,
      connectedAt: new Date()
    })

    socket.join(`device:${deviceId}`)

    io.emit('device-connected', {
      deviceId,
      deviceName,
      timestamp: new Date().toISOString()
    })
  })

  // New threat alert from device
  socket.on('threat-detected', (data: {
    deviceId: string
    threat: any
  }) => {
    const { deviceId, threat } = data
    console.log('Threat detected from device:', deviceId, threat)

    // Broadcast to all dashboard clients
    io.emit('new-threat', {
      deviceId,
      threat,
      timestamp: new Date().toISOString()
    })

    // Also send to specific device room
    io.to(`device:${deviceId}`).emit('threat-alert', {
      threat,
      timestamp: new Date().toISOString()
    })
  })

  // Scan completed
  socket.on('scan-completed', (data: {
    deviceId: string
    scanResult: any
  }) => {
    const { deviceId, scanResult } = data
    console.log('Scan completed from device:', deviceId)

    io.emit('scan-update', {
      deviceId,
      scanResult,
      timestamp: new Date().toISOString()
    })
  })

  // Security status update
  socket.on('status-update', (data: {
    deviceId: string
    status: string
  }) => {
    const { deviceId, status } = data
    console.log('Status update from device:', deviceId, status)

    io.emit('device-status-changed', {
      deviceId,
      status,
      timestamp: new Date().toISOString()
    })
  })

  // Disconnect handler
  socket.on('disconnect', () => {
    const device = connectedDevices.get(socket.id)
    if (device) {
      console.log('Device disconnected:', device.deviceId)
      io.emit('device-disconnected', {
        deviceId: device.deviceId,
        timestamp: new Date().toISOString()
      })
      connectedDevices.delete(socket.id)
    }
  })

  // Error handling
  socket.on('error', (error) => {
    console.error('Socket error:', error)
  })
})

// Broadcast heartbeat
setInterval(() => {
  io.emit('heartbeat', {
    timestamp: new Date().toISOString(),
    connectedDevices: connectedDevices.size
  })
}, 30000) // Every 30 seconds

console.log('✅ WebSocket server is ready to accept connections')
