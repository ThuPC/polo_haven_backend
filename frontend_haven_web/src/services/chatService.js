import api from '@/utils/api'

class ChatService {
  constructor() {
    this.stompClient = null
    this.isConnected = false
    this.messageCallbacks = []
  }

  // Kết nối WebSocket
  async connect(userId, userType) {
    try {
      const SockJS = (await import('sockjs-client')).default
      const Stomp = (await import('webstomp-client')).default
      
      const socket = new SockJS('http://localhost:8080/ws')
      this.stompClient = Stomp.over(socket)
      
      return new Promise((resolve, reject) => {
        this.stompClient.connect({}, () => {
          this.isConnected = true
          console.log('Connected to WebSocket')
          
          // Subscribe to user-specific notifications
          if (userType === 'admin') {
            this.stompClient.subscribe(`/user/${userId}/queue/chat/notification`, (message) => {
              const receivedMessage = JSON.parse(message.body)
              this.messageCallbacks.forEach(callback => callback(receivedMessage))
            })
          }
          
          resolve()
        }, (error) => {
          console.error('WebSocket connection error:', error)
          reject(error)
        })
      })
    } catch (error) {
      console.error('Error importing WebSocket libraries:', error)
      throw error
    }
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

  // Lắng nghe tin nhắn
  onMessage(callback) {
    this.messageCallbacks.push(callback)
  }

  // Gửi tin nhắn qua WebSocket
  sendMessage(messageData) {
    if (this.stompClient && this.isConnected) {
      // Format message data for backend
      const formattedMessage = {
        roomId: Number(messageData.roomId),
        senderId: messageData.senderId.toString(),
        senderType: 'ADMIN',
        message: messageData.content,
        senderName: 'Admin'
      }
      
      this.stompClient.send('/app/send', JSON.stringify(formattedMessage))
    } else {
      // Fallback to REST API
      const formattedMessage = {
        roomId: Number(messageData.roomId),
        senderId: messageData.senderId.toString(),
        senderType: 'ADMIN',
        message: messageData.content,
        senderName: 'Admin'
      }
      return api.post('/api/chat/send-message', formattedMessage)
    }
  }

  // Lấy danh sách chat rooms cho admin
  async getChatRooms() {
    try {
      const userId = this.getCurrentUserId()
      const response = await api.get(`/api/chat/room/admin/${userId}`)
      return response.data
    } catch (error) {
      console.error('Error getting chat rooms:', error)
      throw error
    }
  }

  // Lấy lịch sử chat
  async getChatHistory(roomId) {
    try {
      const response = await api.get(`/api/chat/room/${roomId}/messages`)
      return response.data
    } catch (error) {
      console.error('Error getting chat history:', error)
      throw error
    }
  }

  // Gán admin cho room
  async assignAdminToRoom(roomId) {
    try {
      const userId = this.getCurrentUserId()
      const response = await api.post(`/api/chat/room/${roomId}/assign-admin`, {
        adminId: userId,
        adminName: 'Admin'
      })
      return response.data
    } catch (error) {
      console.error('Error assigning admin to room:', error)
      throw error
    }
  }

  // Tạo hoặc lấy chat room cho khách hàng
  async createOrGetChatRoom(customerId, customerName) {
    try {
      const response = await api.post('/api/chat/room/create', {
        customerId,
        customerName
      })
      return response.data
    } catch (error) {
      console.error('Error creating chat room:', error)
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

  // Lấy user ID từ token
  getCurrentUserId() {
    const token = localStorage.getItem('authToken')
    if (token) {
      try {
        const decoded = JSON.parse(atob(token.split('.')[1]))
        return decoded.sub
      } catch (error) {
        console.error('Error decoding token:', error)
      }
    }
    return null
  }
}

export default new ChatService() 