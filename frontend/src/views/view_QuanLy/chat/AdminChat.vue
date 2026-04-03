<template>
  <div class="chat-container">
    <div class="chat-layout">
      <!-- Sidebar với danh sách chat rooms -->
      <div class="chat-sidebar">
        <div class="sidebar-header">
          <h5 class="mb-0">
            <i class="fas fa-comments me-2"></i>
            Hỗ trợ khách hàng
          </h5>
        </div>
        
        <div class="rooms-list">
          <div 
            v-for="room in chatRooms" 
            :key="room.id"
            :class="['room-item', { active: selectedRoom?.id === room.id }]"
            @click="selectRoom(room)"
          >
            <div class="room-avatar">
              <img 
                v-if="room.customerAvatar" 
                :src="getAvatarUrl(room.customerAvatar)" 
                :alt="room.customerName"
                class="customer-avatar"
              >
              <i v-else class="fas fa-user"></i>
            </div>
            <div class="room-info">
              <div class="room-name">{{ room.customerName || room.roomName }}</div>
              <div class="room-last-message" v-if="room.lastMessage">
                {{ room.lastMessage }}
              </div>
              <div class="room-time" v-if="room.lastMessageTime">
                {{ formatTime(room.lastMessageTime) }}
              </div>
            </div>
            <div class="room-badge" v-if="room.unreadCount > 0">
              {{ room.unreadCount }}
            </div>
          </div>
        </div>
      </div>

      <!-- Chat area -->
      <div class="chat-main">
        <div v-if="!selectedRoom" class="no-room-selected">
          <div class="text-center">
            <i class="fas fa-comments fa-3x text-muted mb-3"></i>
            <h5>Chọn một cuộc trò chuyện để bắt đầu</h5>
            <p class="text-muted">Chọn khách hàng từ danh sách bên trái để hỗ trợ</p>
          </div>
        </div>

        <div v-else class="chat-area">
          <!-- Chat Header -->
          <div class="chat-header">
            <div class="chat-user-info">
              <div class="user-avatar">
                <img 
                  v-if="selectedRoom.customerAvatar" 
                  :src="getAvatarUrl(selectedRoom.customerAvatar)" 
                  :alt="selectedRoom.customerName"
                  class="customer-avatar"
                >
                <i v-else class="fas fa-user"></i>
              </div>
              <div class="user-details">
                <h6 class="mb-0">{{ selectedRoom.customerName || selectedRoom.roomName }}</h6>
                <small class="text-muted">
                  <span v-if="selectedRoom.isActive" class="text-success">
                    <i class="fas fa-circle"></i> Online
                  </span>
                  <span v-else class="text-muted">
                    <i class="fas fa-circle"></i> Offline
                  </span>
                </small>
              </div>
            </div>
            <div class="chat-actions">
              <button class="btn btn-sm btn-outline-secondary me-2">
                <i class="fas fa-phone"></i>
              </button>
              <button class="btn btn-sm btn-outline-secondary">
                <i class="fas fa-video"></i>
              </button>
            </div>
          </div>

          <!-- Messages -->
          <div class="messages-container" ref="messagesContainer">
            <div 
              v-for="message in messages" 
              :key="message.id"
              :class="['message', message.senderType === 'ADMIN' ? 'message-admin' : 'message-customer']"
            >
              <div class="message-avatar" v-if="message.senderType !== 'ADMIN'">
                <img v-if="message.senderAvatar" :src="getAvatarUrl(message.senderAvatar)" :alt="message.senderName" class="sender-avatar" />
                <i v-else class="fas fa-user"></i>
              </div>
              <div class="message-content">
                <div class="message-sender" v-if="message.senderType !== 'ADMIN'">
                  {{ message.senderName }}
                </div>
                <div v-if="message.type === 'image'">
                  <img :src="getFileUrl(message.fileUrl)" class="chat-image" />
                </div>
                <div v-else-if="message.type === 'video'">
                  <video controls :src="getFileUrl(message.fileUrl)" class="chat-video"></video>
                </div>
                <div v-else class="message-text">{{ message.message }}</div>
                <div class="message-time">{{ formatTime(message.createdAt) }}</div>
              </div>
              <div class="message-avatar" v-if="message.senderType === 'ADMIN'">
                <img v-if="message.senderAvatar" :src="getAvatarUrl(message.senderAvatar)" :alt="message.senderName" class="sender-avatar" />
                <i v-else class="fas fa-user"></i>
              </div>
            </div>
          </div>

          <!-- Message input -->
          <div class="message-input-container">
            <div class="input-group">
              <input 
                v-model="newMessage" 
                @keyup.enter="sendMessage"
                type="text" 
                class="form-control" 
                placeholder="Nhập tin nhắn..."
                :disabled="!selectedRoom"
              >
              <input
                type="file"
                ref="fileInput"
                style="display: none"
                accept="image/*,video/*"
                @change="handleFileChange"
              />
              <button class="btn btn-secondary" @click="triggerFileInput">
                <i class="fas fa-paperclip"></i>
              </button>
              <button 
                @click="sendMessage"
                class="btn btn-primary" 
                :disabled="!newMessage.trim() || !selectedRoom"
              >
                <i class="fas fa-paper-plane"></i>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, nextTick, watch } from 'vue'
import api from '@/utils/api'

export default {
  name: 'AdminChat',
  setup() {
    const chatRooms = ref([])
    const selectedRoom = ref(null)
    const messages = ref([])
    const newMessage = ref('')
    const messagesContainer = ref(null)
    const stompClient = ref(null)
    const adminId = ref(null)
    const adminName = ref('Admin')
    const fileInput = ref(null);

    // Lấy thông tin admin từ token
    const getAdminInfo = () => {
      const token = localStorage.getItem('authToken')
      console.log('Token found:', !!token)
      
      if (token) {
        try {
          const decoded = JSON.parse(atob(token.split('.')[1]))
          console.log('Decoded token:', decoded)
          
          adminId.value = decoded.sub
          adminName.value = decoded.name || 'Admin'
          
          console.log('Admin ID extracted:', adminId.value)
          console.log('Admin Name extracted:', adminName.value)
          
          return decoded
        } catch (error) {
          console.error('Lỗi khi decode token:', error)
        }
      }
      return null
    }

    // Kết nối WebSocket
    const connectWebSocket = async () => {
      try {
        const SockJS = (await import('sockjs-client')).default
        const Stomp = (await import('webstomp-client')).default
        
        const socket = new SockJS('http://localhost:8080/ws')
        stompClient.value = Stomp.over(socket)
        
        stompClient.value.connect({}, () => {
          console.log('Connected to WebSocket')
          
          // Subscribe to admin notifications
          if (adminId.value) {
            stompClient.value.subscribe(`/user/${adminId.value}/queue/chat/notification`, (message) => {
              const receivedMessage = JSON.parse(message.body)
              console.log('Received notification:', receivedMessage)
              
              // Cập nhật tin nhắn trong room hiện tại nếu đang mở
              if (selectedRoom.value && receivedMessage.chatRoomId === selectedRoom.value.id) {
                messages.value.push(receivedMessage)
                scrollToBottom()
              }
              
              // Cập nhật last message trong danh sách rooms
              updateRoomLastMessage(receivedMessage)
            })
          }
          
          // Subscribe to general admin notifications (for unassigned rooms)
          stompClient.value.subscribe('/topic/admin/chat/notification', (message) => {
            const receivedMessage = JSON.parse(message.body)
            console.log('Received general admin notification:', receivedMessage)
            
            // Cập nhật tin nhắn trong room hiện tại nếu đang mở
            if (selectedRoom.value && receivedMessage.chatRoomId === selectedRoom.value.id) {
              messages.value.push(receivedMessage)
              scrollToBottom()
            }
            
            // Cập nhật last message trong danh sách rooms
            updateRoomLastMessage(receivedMessage)
            
            // Reload chat rooms để cập nhật danh sách
            loadChatRooms()
          })
        })
      } catch (error) {
        console.error('Error connecting to WebSocket:', error)
      }
    }

    // Cập nhật last message trong room
    const updateRoomLastMessage = (message) => {
      const room = chatRooms.value.find(r => r.id === message.chatRoomId)
      if (room) {
        room.lastMessage = message.message
        room.lastMessageTime = message.createdAt
        if (selectedRoom.value?.id !== message.chatRoomId) {
          room.unreadCount = (room.unreadCount || 0) + 1
        }
      }
    }

    // Lấy danh sách chat rooms
    const loadChatRooms = async () => {
      try {
        if (!adminId.value) {
          console.error('Admin ID not found')
          return
        }
        
        console.log('Loading chat rooms for admin ID:', adminId.value)
        const response = await api.get(`/api/chat/room/admin/${adminId.value}`)
        console.log('API response:', response)
        chatRooms.value = response.data
        console.log('Loaded chat rooms:', response.data)
      } catch (error) {
        console.error('Error loading chat rooms:', error)
        if (error.response) {
          console.error('Response status:', error.response.status)
          console.error('Response data:', error.response.data)
        }
      }
    }

    // Lấy tin nhắn của room
    const loadMessages = async (roomId) => {
      try {
        const response = await api.get(`/api/chat/room/${roomId}/messages`)
        messages.value = response.data

        // Wait for DOM render
        await nextTick()

        // First attempt to scroll to bottom immediately
        scrollToBottom()

        // Keep a short delay to allow images/videos to load and change layout, then scroll again
        await new Promise(resolve => setTimeout(resolve, 120))
        scrollToBottom()
      } catch (error) {
        console.error('Error loading messages:', error)
      }
    }

    // Auto-scroll when new messages arrive but only if the user is near the bottom
    watch(messages, (newVal, oldVal) => {
      if (!messagesContainer.value) return
      const el = messagesContainer.value
      const oldLen = oldVal ? oldVal.length : 0
      const newLen = newVal ? newVal.length : 0
      if (newLen <= oldLen) return

      // distance from bottom in pixels
      const distanceFromBottom = el.scrollHeight - el.scrollTop - el.clientHeight
      const AUTO_SCROLL_THRESHOLD_PX = 150

      // If user is near bottom, auto-scroll; otherwise respect user's position
      if (distanceFromBottom < AUTO_SCROLL_THRESHOLD_PX) {
        nextTick().then(() => scrollToBottom())
      }
    }, { deep: true })

    // Gán admin cho room
    const assignAdminToRoom = async (roomId) => {
      try {
        const response = await api.post(`/api/chat/room/${roomId}/assign-admin`, {
          adminId: adminId.value,
          adminName: adminName.value
        })
        console.log('Assigned admin to room:', response.data)
        return response.data
      } catch (error) {
        console.error('Error assigning admin to room:', error)
      }
    }

    // Chọn room
    const selectRoom = async (room) => {
      selectedRoom.value = room
      
      // Gán admin cho room nếu chưa có admin
      if (!room.adminId) {
        await assignAdminToRoom(room.id)
      }
      
      await loadMessages(room.id)
      
      // Mark messages as read
      try {
        await api.post(`/api/chat/room/${room.id}/mark-read`, {
          senderType: 'ADMIN'
        })
        // Reset unread count
        room.unreadCount = 0
      } catch (error) {
        console.error('Error marking messages as read:', error)
      }
      
      // Subscribe to room messages
      if (stompClient.value && stompClient.value.connected) {
        stompClient.value.subscribe(`/topic/chat/${room.id}`, (message) => {
          const receivedMessage = JSON.parse(message.body)
          if (receivedMessage.id && receivedMessage.senderType === 'CUSTOMER') {
            messages.value.push(receivedMessage)
            scrollToBottom()
          }
        })
      }
    }

    // Gửi tin nhắn
    const sendMessage = async () => {
      if (!newMessage.value.trim() || !selectedRoom.value) return

      const messageData = {
        roomId: Number(selectedRoom.value.id),
        senderId: adminId.value.toString(),
        senderType: 'ADMIN',
        message: newMessage.value,
        senderName: adminName.value
      }

      try {
        if (stompClient.value && stompClient.value.connected) {
          stompClient.value.send('/app/send', JSON.stringify(messageData))
        } else {
          // Fallback to REST API
          await api.post('/api/chat/send-message', messageData)
        }
        
        // Thêm tin nhắn vào danh sách ngay lập tức
        const localMessage = {
          id: Date.now(),
          chatRoomId: selectedRoom.value.id,
          senderId: adminId.value,
          senderType: 'ADMIN',
          message: newMessage.value,
          senderName: adminName.value,
          createdAt: new Date().toISOString(),
          isRead: false
        }
        
        // Sau khi push tin nhắn mới:
        messages.value.push(localMessage)
        newMessage.value = ''
        await nextTick()
        scrollToBottom()
      } catch (error) {
        console.error('Error sending message:', error)
      }
    }

    // Refresh messages
    const refreshMessages = async () => {
      if (selectedRoom.value) {
        await loadMessages(selectedRoom.value.id)
      }
    }

    // Scroll to bottom
    const scrollToBottom = () => {
      if (messagesContainer.value) {
        messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
      }
    }

    // Format time
    const formatTime = (timeString) => {
      if (!timeString) return ''
      const date = new Date(timeString)
      return date.toLocaleTimeString('vi-VN', { 
        hour: '2-digit', 
        minute: '2-digit' 
      })
    }

    // Get avatar URL
    const getAvatarUrl = (avatarPath) => {
      if (!avatarPath) return null
      
      // Nếu là URL đầy đủ
      if (avatarPath.startsWith('http')) {
        return avatarPath
      }
      
      // Nếu là đường dẫn tương đối, thêm base URL
      return `http://localhost:8080/uploads/khach_hang/${avatarPath}`
    }

    // Normalize file URLs for images/videos served by backend
    const getFileUrl = (path) => {
      if (!path) return null
      if (path.startsWith('http')) return path
      if (path.startsWith('/uploads')) return `http://localhost:8080${path}`
      if (path.startsWith('uploads')) return `http://localhost:8080/${path}`
      return `http://localhost:8080/uploads/${path}`
    }

    const triggerFileInput = () => {
      fileInput.value && fileInput.value.click();
    };

    const handleFileChange = async (event) => {
      const file = event.target.files[0];
      if (!file || !selectedRoom.value) return;
      const formData = new FormData();
      formData.append('file', file);
      const response = await api.post('/api/chat/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      });
      const fileUrl = response.data.url;
      let type = file.type.startsWith('image') ? 'image' : 'video';
      const messageData = {
        roomId: Number(selectedRoom.value.id),
        senderId: adminId.value.toString(),
        senderType: 'ADMIN',
        type,
        fileUrl,
        message: '', // Có thể thêm chú thích nếu muốn
        senderName: adminName.value
      };
      if (stompClient.value && stompClient.value.connected) {
        stompClient.value.send('/app/send', JSON.stringify(messageData));
        messages.value.push({
          ...messageData,
          createdAt: new Date().toISOString(),
          id: Math.random().toString(36).substr(2, 9)
        });
        scrollToBottom();
      } else {
        await api.post('/api/chat/send-message', messageData);
        await loadMessages(selectedRoom.value.id);
      }
    };

    // Watch selected room changes
    watch(selectedRoom, (newRoom) => {
      if (newRoom) {
        loadMessages(newRoom.id)
      }
    })

    onMounted(async () => {
      getAdminInfo()
      await connectWebSocket()
      await loadChatRooms()
    })

    return {
      chatRooms,
      selectedRoom,
      messages,
      newMessage,
      messagesContainer,
      fileInput,
      selectRoom,
      sendMessage,
      refreshMessages,
      formatTime,
      getAvatarUrl,
      getFileUrl,
      handleFileChange,
      triggerFileInput
    }
  }
}
</script>

<style scoped>
.chat-container {
  height: calc(100vh - 100px);
  background: #f8f9fa;
}

.chat-layout {
  display: flex;
  height: 100%;
  background: white;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 0 20px rgba(0,0,0,0.1);
}

.chat-sidebar {
  width: 300px;
  border-right: 1px solid #e9ecef;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid #e9ecef;
  background: #f8f9fa;
}

.rooms-list {
  flex: 1;
  overflow-y: auto;
}

.room-item {
  padding: 15px 20px;
  border-bottom: 1px solid #f1f3f4;
  cursor: pointer;
  transition: background-color 0.2s;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.room-item:hover {
  background-color: #f8f9fa;
}

.room-item.active {
  background-color: #e3f2fd;
  border-left: 4px solid #2196f3;
}

.room-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #e9ecef;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  overflow: hidden; /* Ensure images don't overflow */
}

.customer-avatar {
  width: 100%;
  height: 100%;
  object-fit: cover; /* Cover the area without distortion */
}

.room-info {
  flex: 1;
}

.room-name {
  font-weight: 600;
  margin-bottom: 4px;
}

.room-last-message {
  font-size: 0.875rem;
  color: #6c757d;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 200px;
}

.room-time {
  font-size: 0.75rem;
  color: #adb5bd;
  margin-top: 2px;
}

.room-badge {
  background: #dc3545;
  color: white;
  border-radius: 50%;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.75rem;
  font-weight: 600;
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.no-room-selected {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #6c757d;
}

.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.chat-header {
  padding: 20px;
  border-bottom: 1px solid #e9ecef;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #f8f9fa;
}

.chat-user-info {
  display: flex;
  align-items: center;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #e9ecef;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  overflow: hidden;
}

.customer-avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.messages-container {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background: #f8f9fa;
  min-height: 0;
  max-height: calc(100vh - 260px); /* Đảm bảo không che input */
  /* Add extra bottom padding so last messages are not hidden behind input */
  padding-bottom: 180px; /* increased to ensure large media won't be hidden */
}

/* Ensure images and videos inside messages are responsive and constrained */
.message-content img,
.message-content video,
.chat-image,
.chat-video {
  max-width: 100%;        /* never wider than the message bubble */
  width: auto;
  height: auto;
  max-height: calc(60vh - 120px); /* limit height relative to viewport to avoid huge media */
  object-fit: contain;    /* preserve aspect ratio and avoid cropping */
  display: block;
  border-radius: 12px;
}

/* In admin (right-side) bubble use white text contrast for images/videos background if needed */
.message-admin .message-content img,
.message-admin .message-content video {
  filter: none;
}

/* For very small screens reduce heights so input stays visible */
@media (max-width: 480px) {
  .messages-container { padding-bottom: 140px; }
  .message-content img,
  .message-content video,
  .chat-image,
  .chat-video { max-height: 40vh; }
}

.message {
  display: flex;
  margin-bottom: 15px;
  align-items: flex-end;
}
.message-customer {
  flex-direction: row;
  justify-content: flex-start;
}
.message-admin {
  flex-direction: row-reverse;
  justify-content: flex-end;
}
.message-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #e9ecef;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 8px;
  overflow: hidden;
  flex-shrink: 0;
}
.sender-avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.message-content {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 18px;
  background: white;
  box-shadow: 0 1px 2px rgba(0,0,0,0.1);
}
.message-admin .message-content {
  background: #2196f3;
  color: white;
}
.message-customer .message-content {
  background: #fff;
  color: #222;
}
.message-sender {
  font-size: 0.75rem;
  font-weight: 600;
  margin-bottom: 4px;
  color: #6c757d;
}
.message-text {
  margin-bottom: 4px;
  word-wrap: break-word;
}
.message-time {
  font-size: 0.75rem;
  opacity: 0.7;
}

.message-input-container {
  padding: 20px;
  border-top: 1px solid #e9ecef;
  background: white;
  position: sticky;
  bottom: 0;
  z-index: 20; /* higher so it stays above messages and devtools overlay */
  box-shadow: 0 -6px 18px rgba(0,0,0,0.08);
}

.input-group {
  display: flex;
  gap: 10px;
}

.form-control {
  border-radius: 25px;
  border: 1px solid #e9ecef;
  padding: 12px 20px;
}

.btn {
  border-radius: 50%;
  width: 45px;
  height: 45px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* Scrollbar styling */
.rooms-list::-webkit-scrollbar,
.messages-container::-webkit-scrollbar {
  width: 6px;
}

.rooms-list::-webkit-scrollbar-track,
.messages-container::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.rooms-list::-webkit-scrollbar-thumb,
.messages-container::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.rooms-list::-webkit-scrollbar-thumb:hover,
.messages-container::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* Responsive adjustments: reduce bottom padding on small screens */
@media (max-width: 480px) {
  .messages-container { padding-bottom: 110px; }
  .message-input-container { padding: 12px; }
}
</style>