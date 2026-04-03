<template>
  <div class="admin-chat-container">
    <div class="chat-sidebar">
      <!-- Header -->
      <div class="sidebar-header">
        <h5 class="mb-0">
          <i class="fas fa-comments me-2"></i>
          Hỗ trợ khách hàng
        </h5>
        <div class="status-indicator">
          <span class="text-success">
            <i class="fas fa-circle"></i> Online
          </span>
        </div>
      </div>

      <!-- Chat Rooms List -->
      <div class="chat-rooms">
        <div class="rooms-header">
          <h6 class="mb-2">Cuộc hội thoại</h6>
          <span class="badge bg-primary">{{ chatRooms.length }}</span>
        </div>
        
        <div class="rooms-list">
          <div 
            v-for="room in chatRooms" 
            :key="room.id"
            :class="['room-item', { active: selectedRoom?.id === room.id }]"
            @click="selectRoom(room)"
          >
            <div class="room-avatar">
              <i class="fas fa-user"></i>
            </div>
            <div class="room-info">
              <div class="room-name">{{ room.roomName || 'Khách hàng' }}</div>
              <div class="room-last-message">{{ room.lastMessage || 'Chưa có tin nhắn' }}</div>
            </div>
            <div class="room-meta">
              <div class="room-time">{{ formatTime(room.lastMessageTime) }}</div>
              <div v-if="room.unreadCount > 0" class="unread-count">
                {{ room.unreadCount }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Chat Area -->
    <div class="chat-area">
      <div v-if="selectedRoom" class="chat-content">
        <!-- Chat Header -->
        <div class="chat-header">
          <div class="chat-user-info">
            <div class="user-avatar">
              <i class="fas fa-user"></i>
            </div>
            <div class="user-details">
              <h6 class="mb-0">{{ selectedRoom.roomName || 'Khách hàng' }}</h6>
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
            v-for="message in currentMessages" 
            :key="message.id"
            :class="['message', message.senderType === 'ADMIN' ? 'message-own' : 'message-other']"
          >
            <div class="message-content">
              <div class="message-text">{{ message.message }}</div>
              <div class="message-time">
                {{ formatTime(message.createdAt) }}
              </div>
            </div>
          </div>
        </div>

        <!-- Input -->
        <div class="chat-input">
          <div class="input-group">
            <input
              v-model="newMessage"
              @keyup.enter="sendMessage"
              type="text"
              class="form-control"
              placeholder="Nhập tin nhắn..."
            />
            <button 
              @click="sendMessage"
              class="btn btn-primary"
              :disabled="!newMessage.trim()"
            >
              <i class="fas fa-paper-plane"></i>
            </button>
          </div>
        </div>
      </div>

      <!-- No Room Selected -->
      <div v-else class="no-room-selected">
        <div class="text-center">
          <i class="fas fa-comments fa-3x text-muted mb-3"></i>
          <h5 class="text-muted">Chọn cuộc hội thoại</h5>
          <p class="text-muted">Chọn một cuộc hội thoại từ danh sách bên trái để bắt đầu chat</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue';
import { jwtDecode } from 'jwt-decode';
import chatService from '@/services/chatService';

const chatRooms = ref([]);
const selectedRoom = ref(null);
const currentMessages = ref([]);
const newMessage = ref('');
const currentUserId = ref(null);
const messagesContainer = ref(null);

// Lấy thông tin user từ token
const getCurrentUser = () => {
  const token = localStorage.getItem('authToken');
  if (token) {
    try {
      const decoded = jwtDecode(token);
      currentUserId.value = decoded.sub;
      return decoded;
    } catch (error) {
      console.error('Lỗi khi decode token:', error);
    }
  }
  return null;
};

// Kết nối chat
const connectChat = () => {
  const user = getCurrentUser();
  if (user) {
    chatService.connect(user.sub, 'admin');
    
    // Lắng nghe tin nhắn mới
    chatService.onMessage((message) => {
      // Cập nhật tin nhắn trong room hiện tại
      if (selectedRoom.value && message.roomId === selectedRoom.value.id) {
        currentMessages.value.push(message);
        scrollToBottom();
      }
      
      // Cập nhật last message trong danh sách rooms
      updateRoomLastMessage(message);
    });
    
    // Load danh sách chat rooms
    loadChatRooms();
  }
};

// Load danh sách chat rooms
const loadChatRooms = async () => {
  try {
    const rooms = await chatService.getChatRooms();
    chatRooms.value = rooms.map(room => ({
      ...room,
      unreadCount: 0
    }));
  } catch (error) {
    console.error('Lỗi khi load chat rooms:', error);
  }
};

// Chọn room
const selectRoom = async (room) => {
  selectedRoom.value = room;
  
  // Gán admin cho room nếu chưa có admin
  if (!room.adminId) {
    try {
      await chatService.assignAdminToRoom(room.id);
    } catch (error) {
      console.error('Lỗi khi gán admin cho room:', error);
    }
  }
  
  try {
    const messages = await chatService.getChatHistory(room.id);
    currentMessages.value = messages;
    scrollToBottom();
    
    // Reset unread count
    room.unreadCount = 0;
    
    // Subscribe to room messages
    chatService.subscribeToRoom(room.id, (message) => {
      if (message.senderType === 'CUSTOMER') {
        currentMessages.value.push(message);
        scrollToBottom();
      }
    });
  } catch (error) {
    console.error('Lỗi khi load lịch sử chat:', error);
  }
};

// Gửi tin nhắn
const sendMessage = () => {
  if (!newMessage.value.trim() || !selectedRoom.value) return;
  
  const message = {
    content: newMessage.value,
    senderId: currentUserId.value,
    roomId: selectedRoom.value.id,
    timestamp: new Date(),
    type: 'text'
  };
  
  chatService.sendMessage(message);
  
  // Thêm tin nhắn vào danh sách ngay lập tức
  const localMessage = {
    id: Date.now(), // Temporary ID
    message: newMessage.value,
    senderId: currentUserId.value,
    senderType: 'ADMIN',
    senderName: 'Admin',
    createdAt: new Date().toISOString(),
    isRead: false
  };
  
  currentMessages.value.push(localMessage);
  newMessage.value = '';
  scrollToBottom();
};

// Cập nhật last message trong room
const updateRoomLastMessage = (message) => {
  const room = chatRooms.value.find(r => r.id === message.roomId);
  if (room) {
    room.lastMessage = message.content;
    room.lastMessageTime = message.timestamp;
    if (selectedRoom.value?.id !== message.roomId) {
      room.unreadCount++;
    }
  }
};

// Scroll xuống tin nhắn mới nhất
const scrollToBottom = async () => {
  await nextTick();
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
  }
};

// Format thời gian
const formatTime = (timestamp) => {
  if (!timestamp) return '';
  const date = new Date(timestamp);
  const now = new Date();
  const diffInHours = (now - date) / (1000 * 60 * 60);
  
  if (diffInHours < 24) {
    return date.toLocaleTimeString('vi-VN', { 
      hour: '2-digit', 
      minute: '2-digit' 
    });
  } else {
    return date.toLocaleDateString('vi-VN');
  }
};

// Watch messages để auto scroll
watch(currentMessages, () => {
  scrollToBottom();
});

onMounted(() => {
  connectChat();
});

onUnmounted(() => {
  chatService.disconnect();
});
</script>

<style scoped>
.admin-chat-container {
  display: flex;
  height: 100vh;
  background: #f8f9fa;
}

.chat-sidebar {
  width: 350px;
  background: white;
  border-right: 1px solid #e9ecef;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid #e9ecef;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.status-indicator {
  margin-top: 10px;
}

.chat-rooms {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.rooms-header {
  padding: 15px 20px;
  border-bottom: 1px solid #e9ecef;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.rooms-list {
  flex: 1;
  overflow-y: auto;
}

.room-item {
  display: flex;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #f8f9fa;
  cursor: pointer;
  transition: background 0.3s ease;
}

.room-item:hover {
  background: #f8f9fa;
}

.room-item.active {
  background: #e3f2fd;
  border-left: 3px solid #667eea;
}

.room-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
}

.room-info {
  flex: 1;
  min-width: 0;
}

.room-name {
  font-weight: 600;
  margin-bottom: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.room-last-message {
  font-size: 12px;
  color: #6c757d;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.room-meta {
  text-align: right;
  min-width: 60px;
}

.room-time {
  font-size: 11px;
  color: #6c757d;
  margin-bottom: 2px;
}

.unread-count {
  background: #ff4757;
  color: white;
  border-radius: 50%;
  width: 20px;
  height: 20px;
  font-size: 11px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
}

.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.chat-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: white;
}

.chat-header {
  padding: 15px 20px;
  border-bottom: 1px solid #e9ecef;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: white;
}

.chat-user-info {
  display: flex;
  align-items: center;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
}

.user-details h6 {
  margin: 0;
  font-weight: 600;
}

.chat-actions {
  display: flex;
  gap: 8px;
}

.messages-container {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background: #f8f9fa;
}

.message {
  margin-bottom: 15px;
  display: flex;
}

.message-own {
  justify-content: flex-end;
}

.message-other {
  justify-content: flex-start;
}

.message-content {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 18px;
  position: relative;
}

.message-own .message-content {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-bottom-right-radius: 5px;
}

.message-other .message-content {
  background: white;
  color: #333;
  border: 1px solid #e9ecef;
  border-bottom-left-radius: 5px;
}

.message-text {
  margin-bottom: 5px;
  word-wrap: break-word;
}

.message-time {
  font-size: 11px;
  opacity: 0.7;
}

.chat-input {
  padding: 15px 20px;
  background: white;
  border-top: 1px solid #e9ecef;
}

.input-group {
  display: flex;
  gap: 10px;
}

.form-control {
  border: 1px solid #e9ecef;
  border-radius: 25px;
  padding: 10px 15px;
  font-size: 14px;
}

.form-control:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 0.2rem rgba(102, 126, 234, 0.25);
}

.btn {
  border-radius: 50%;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  transition: all 0.3s ease;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.btn-primary:hover {
  transform: scale(1.05);
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.no-room-selected {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
}

/* Responsive */
@media (max-width: 768px) {
  .admin-chat-container {
    flex-direction: column;
  }
  
  .chat-sidebar {
    width: 100%;
    height: 200px;
  }
}
</style> 