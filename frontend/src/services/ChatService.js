import api from '@/utils/api'

class ChatService {
  constructor() {
    this.stompClient = null
    this.isConnected = false
  }

  // Kết nối WebSocket
  connect() {
    const SockJS = require('sockjs-client')
    const Stomp = require('webstomp-client')
    
    const socket = new SockJS('http://localhost:8080/ws')
    this.stompClient = Stomp.over(socket)
    
    return new Promise((resolve, reject) => {
      this.stompClient.connect({}, () => {
        this.isConnected = true
        console.log('Connected to WebSocket')
        resolve()
      }, (error) => {
        console.error('WebSocket connection error:', error)
        reject(error)
      })
    })
  }

  // Ngắt kết nối
  disconnect() {
    if (this.stompClient) {
      this.stompClient.disconnect()
      this.isConnected = false
    }
  }

  // Subscribe to chat room
  subscribeToRoom(roomId, callback) {
    if (this.stompClient && this.isConnected) {
      return this.stompClient.subscribe(`/topic/chat/${roomId}`, (message) => {
        const receivedMessage = JSON.parse(message.body)
        callback(receivedMessage)
      })
    }
  }

  // Gửi tin nhắn qua WebSocket
  sendMessage(messageData) {
    if (this.stompClient && this.isConnected) {
      this.stompClient.send('/app/send', JSON.stringify(messageData))
    } else {
      // Fallback to REST API
      return api.post('/api/chat/send-message', messageData)
    }
  }

  // Lấy danh sách chat rooms cho admin
  async getRoomsForAdmin(adminId) {
    try {
      const response = await api.get(`/api/chat/room/admin/${adminId}`)
      return response.data
    } catch (error) {
      console.error('Error getting chat rooms:', error)
      throw error
    }
  }

  // Lấy tin nhắn của room
  async getMessagesByRoomId(roomId) {
    try {
      const response = await api.get(`/api/chat/room/${roomId}/messages`)
      return response.data
    } catch (error) {
      console.error('Error getting messages:', error)
      throw error
    }
  }

  // Đánh dấu tin nhắn đã đọc
  async markMessagesAsRead(roomId, senderType) {
    try {
      await api.post(`/api/chat/room/${roomId}/mark-read`, { senderType })
    } catch (error) {
      console.error('Error marking messages as read:', error)
      throw error
    }
  }

  // Gán admin cho room
  async assignAdminToRoom(roomId, adminId, adminName) {
    try {
      const response = await api.post(`/api/chat/room/${roomId}/assign-admin`, {
        adminId,
        adminName
      })
      return response.data
    } catch (error) {
      console.error('Error assigning admin to room:', error)
      throw error
    }
  }
}

export default new ChatService() 