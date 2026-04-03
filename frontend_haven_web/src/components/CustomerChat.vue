<template>
  <div class="customer-chat">
    <!-- Chat button -->
    <div v-if="!isOpen" class="chat-button" @click="openChat">
      <i class="fas fa-comments"></i>
      <span class="notification-badge" v-if="unreadCount > 0">{{ unreadCount }}</span>
    </div>

    <!-- Chat window -->
    <div v-if="isOpen" class="chat-window">
      <div class="chat-header">
        <div class="chat-title">
          <i class="fas fa-headset me-2"></i>
          Hỗ trợ khách hàng
        </div>
        <button class="close-btn" @click="closeChat">
          <i class="fas fa-times"></i>
        </button>
      </div>

      <div class="chat-messages" ref="messagesContainer">
        <div v-for="message in messages" :key="message.id" :class="[
          'message',
          message.senderId === currentUserId ? 'message-own' : 'message-other'
        ]">
          <div class="message-avatar" v-if="message.senderType === 'ADMIN'">
            <i class="fas fa-headset"></i>
          </div>
          <div class="message-content">
            <div class="message-sender" v-if="message.senderType === 'ADMIN'">
              {{ message.senderName || 'Hỗ trợ viên' }}
            </div>
            <div v-if="message.type === 'image'">
              <img :src="getFileUrl(message.fileUrl)" class="chat-image" />
            </div>
            <div v-else-if="message.type === 'video'">
              <video controls :src="getFileUrl(message.fileUrl)" class="chat-video"></video>
            </div>
            <div v-else>
              {{ message.message }}
            </div>
            <div class="message-time">{{ formatTime(message.createdAt) }}</div>
          </div>
          <div class="message-avatar" v-if="message.senderType === 'CUSTOMER'">
            <img v-if="message.senderAvatar" :src="getAvatarUrl(message.senderAvatar)" :alt="message.senderName"
              class="sender-avatar" />
            <i v-else class="fas fa-user"></i>
          </div>
        </div>
      </div>

      <div class="chat-input">
        <div class="input-group">
          <input v-model="newMessage" @keyup.enter="sendMessage" type="text" class="form-control"
            placeholder="Nhập tin nhắn..." :disabled="!chatRoom">
          <input type="file" ref="fileInput" style="display: none" accept="image/*,video/*"
            @change="handleFileChange" />
          <button class="btn btn-secondary" @click="triggerFileInput">
            <i class="fas fa-paperclip"></i>
          </button>
          <button @click="sendMessage" class="btn btn-primary" :disabled="!newMessage.trim() || !chatRoom">
            <i class="fas fa-paper-plane"></i>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, nextTick, watch } from 'vue'
import api from '@/utils/api'


export default {
  name: 'CustomerChat',
  props: {
    customerId: {
      type: [Number, String], // Cho phép cả Number và String
      required: true
    },
    customerName: {
      type: String,
      required: true
    }
  },
  setup(props) {
    const isOpen = ref(false)
    const chatRoom = ref(null)
    const messages = ref([])
    const newMessage = ref('')
    const messagesContainer = ref(null)
    const stompClient = ref(null)
    const unreadCount = ref(0)
    const currentUserId = props.customerId.toString();
    const fileInput = ref(null);

    // Kết nối WebSocket
    const connectWebSocket = async () => {
      try {
        const SockJS = (await import('sockjs-client')).default
        const Stomp = (await import('webstomp-client')).default

        const socket = new SockJS('http://localhost:8080/ws')
        stompClient.value = Stomp.over(socket)

        stompClient.value.connect({}, () => {
          console.log('Connected to WebSocket')

          // Subscribe to chat room
          if (chatRoom.value) {
            stompClient.value.subscribe(`/topic/chat/${chatRoom.value.id}`, async (message) => {
              const receivedMessage = JSON.parse(message.body)
              if (receivedMessage.id) {
                // Prevent duplicates: ignore if message with same id already present
                if (!messages.value.some(m => m.id === receivedMessage.id)) {
                  messages.value.push(receivedMessage)
                }
                await nextTick()
                scrollToBottom()
                // Update unread count if message is from admin
                if (receivedMessage.senderType === 'ADMIN') {
                  unreadCount.value++
                }
              }
            })
          }
        })
      } catch (error) {
        console.error('Error connecting to WebSocket:', error)
      }
    }

    // Tạo hoặc lấy chat room
    const createOrGetChatRoom = async () => {
      try {
        // Kiểm tra props trước khi gửi request
        console.log("Props check:", {
          customerId: props.customerId,
          customerName: props.customerName,
          customerIdType: typeof props.customerId
        })

        // Validate dữ liệu trước khi gửi
        if (!props.customerId || !props.customerName) {
          console.error('Missing required props:', { customerId: props.customerId, customerName: props.customerName })
          return
        }

        // Gửi customerId dưới dạng string (không chuyển đổi thành số)
        const requestData = {
          customerId: props.customerId.toString(), // Đảm bảo là string
          customerName: props.customerName,
        }

        console.log("Sending request data:", requestData)

        const response = await api.post('/api/chat/room/create', requestData)
        chatRoom.value = response.data
        await loadMessages()
      } catch (error) {
        console.error('Error creating chat room:', error)
        if (error.response) {
          console.error('Response data:', error.response.data)
          console.error('Response status:', error.response.status)
        }
      }
    }

    // Lấy tin nhắn
    const loadMessages = async () => {
      if (!chatRoom.value) return

      try {
        const response = await api.get(`/api/chat/room/${chatRoom.value.id}/messages`)
        messages.value = response.data
        await nextTick()

        // Scroll to bottom after render
        scrollToBottom()

        // Allow media/layout to settle then scroll again
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

    // Gửi tin nhắn
    const sendMessage = async () => {
      if (!newMessage.value.trim() || !chatRoom.value) return

      const messageData = {
        roomId: Number(chatRoom.value.id), // Gửi dưới dạng number cho WebSocket
        senderId: props.customerId.toString(), // Gửi dưới dạng string
        senderType: 'CUSTOMER',
        message: newMessage.value,
        senderName: props.customerName
      }

      try {
        if (stompClient.value && stompClient.value.connected) {
          stompClient.value.send('/app/send', JSON.stringify(messageData))
        } else {
          // Fallback to REST API
          await api.post('/api/chat/send-message', messageData)
        }

        newMessage.value = ''
      } catch (error) {
        console.error('Error sending message:', error)
      }
    }

    // Mở chat (đợi DOM render rồi cuộn xuống cuối để hiển thị tin nhắn mới nhất)
    const openChat = async () => {
      isOpen.value = true
      unreadCount.value = 0

      // Đợi Vue render phần chat window và tin nhắn
      await nextTick()
      // Cho layout/media settle một chút rồi cuộn lần nữa
      await new Promise(resolve => setTimeout(resolve, 120))
      scrollToBottom()

      // Mark messages as read
      if (chatRoom.value) {
        api.post(`/api/chat/room/${chatRoom.value.id}/mark-read`, {
          senderType: 'CUSTOMER'
        }).catch(error => {
          console.error('Error marking messages as read:', error)
        })
      }
    }

    // Đóng chat
    const closeChat = () => {
      isOpen.value = false
    }

    // Scroll to bottom
    const scrollToBottom = () => {
      if (messagesContainer.value) {
        messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
      }
    }

    // Format time
    const formatTime = (timeString) => {
      const date = new Date(timeString)
      return date.toLocaleTimeString('vi-VN', {
        hour: '2-digit',
        minute: '2-digit'
      })
    }

    // Watch chat room changes
    // watch(chatRoom, (newRoom) => {
    //   if (newRoom) {
    //     loadMessages()
    //   }
    // })
    watch(chatRoom, (newRoom) => {
      if (newRoom && stompClient.value && stompClient.value.connected) {
        stompClient.value.subscribe(`/topic/chat/${newRoom.id}`, async (message) => {
          const receivedMessage = JSON.parse(message.body)
          if (receivedMessage.id) {
            if (!messages.value.some(m => m.id === receivedMessage.id)) {
              messages.value.push(receivedMessage)
            }
            await nextTick()
            scrollToBottom()
            if (receivedMessage.senderType === 'ADMIN') {
              unreadCount.value++
            }
          }
        })
      }
    })


    onMounted(() => {
      // Thêm delay nhỏ để đảm bảo props đã được truyền đúng
      setTimeout(() => {
        createOrGetChatRoom()
        connectWebSocket()
      }, 100)
    })

    const getAvatarUrl = (avatarPath) => {
      if (!avatarPath) return null;
      if (avatarPath.startsWith('http')) return avatarPath;
      return `http://localhost:8080/uploads/khach_hang/${avatarPath}`;
    };

    // Helper to normalize chat file URLs (images/videos)
    const getFileUrl = (path) => {
      if (!path) return null;
      if (path.startsWith('http')) return path;
      // If backend returns paths like '/uploads/chat/...' or 'uploads/chat/...'
      if (path.startsWith('/uploads')) return `http://localhost:8080${path}`;
      if (path.startsWith('uploads')) return `http://localhost:8080/${path}`;
      // If backend returns just 'chat/filename' or 'chat/...'
      return `http://localhost:8080/uploads/${path}`;
    };

    const triggerFileInput = () => {
      fileInput.value && fileInput.value.click();
    };

    const handleFileChange = async (event) => {
      const file = event.target.files[0];
      if (!file) return;
      // Gửi file lên backend
      const formData = new FormData();
      formData.append('file', file);
      const response = await api.post('/api/chat/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      });
      const fileUrl = response.data.url;
      // Normalize URL so frontend always requests the correct static path
      const normalizedFileUrl = getFileUrl(fileUrl);
      let type = file.type.startsWith('image') ? 'image' : 'video';
      // Gửi message qua WebSocket
      const messageData = {
        roomId: Number(chatRoom.value.id),
        senderId: props.customerId.toString(),
        senderType: 'CUSTOMER',
        type,
        fileUrl: normalizedFileUrl,
        message: '', // Có thể để trống hoặc thêm chú thích
        senderName: props.customerName
      };
      // if (stompClient.value && stompClient.value.connected) {
      //   stompClient.value.send('/app/send', JSON.stringify(messageData));
      //   // Do not push optimistic local message here to avoid duplicate when server broadcasts
      //   // The server will broadcast the saved message and the subscription above will add it.
      // } else {
      //   await api.post('/api/chat/send-message', messageData);
      //   await loadMessages();
      // }
      if (stompClient.value && stompClient.value.connected) {
        stompClient.value.send('/app/send', JSON.stringify(messageData))

        // Push local message ngay để hiển thị
        messages.value.push({
          ...messageData,
          id: Date.now(), // tạm id client để Vue render
          createdAt: new Date().toISOString()
        })
        await nextTick()
        scrollToBottom()
      } else {
        await api.post('/api/chat/send-message', messageData)
        await loadMessages()
      }

    };

    return {
      isOpen,
      chatRoom,
      messages,
      newMessage,
      messagesContainer,
      unreadCount,
      sendMessage,
      openChat,
      closeChat,
      formatTime,
      getAvatarUrl,
      getFileUrl,
      currentUserId,
      fileInput,
      triggerFileInput,
      handleFileChange
    }
  }
}
</script>

<style scoped>
.customer-chat {
  position: fixed;
  bottom: 90px;
  right: 25px;
  z-index: 1000;
}

.chat-button {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: #2196f3;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(33, 150, 243, 0.3);
  transition: all 0.3s ease;
  position: relative;
}

.chat-button:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 16px rgba(33, 150, 243, 0.4);
}

.chat-button i {
  font-size: 24px;
}

.notification-badge {
  position: absolute;
  top: -5px;
  right: -5px;
  background: #dc3545;
  color: white;
  border-radius: 50%;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
}

.chat-window {
  width: 350px;
  height: 500px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-header {
  background: #2196f3;
  color: white;
  padding: 15px 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.chat-title {
  font-weight: 600;
  font-size: 16px;
}

.close-btn {
  background: none;
  border: none;
  color: white;
  cursor: pointer;
  font-size: 18px;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: background-color 0.2s;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

.chat-messages {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background: #f8f9fa;
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

.message-own {
  flex-direction: row-reverse;
  justify-content: flex-end;
}

.message-other {
  flex-direction: row;
  justify-content: flex-start;
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

.message-content {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 18px;
  background: white;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.message-admin .message-content {
  background: #2196f3;
  color: white;
}

.message-own .message-content {
  background: #e3f2fd;
  /* Màu xanh nhạt cho khách hàng */
  color: #222;
}

.message-other .message-content {
  background: #ffe0e0;
  /* Màu đỏ nhạt cho admin */
  color: #222;
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
  font-size: 14px;
}

.message-time {
  font-size: 11px;
  opacity: 0.7;
}

/* Ensure images/videos inside chat don't overflow and keep proper aspect ratio */
.chat-image,
.chat-video {
  max-width: 100%;
  /* never exceed bubble width */
  max-height: 300px;
  /* prevent extremely tall media */
  display: block;
  border-radius: 8px;
  object-fit: cover;
  /* crop if needed while keeping aspect */
}

/* For larger screens, slightly larger max height */
@media (min-width: 768px) {

  .chat-image,
  .chat-video {
    max-height: 400px;
  }
}

.chat-input {
  padding: 15px 20px;
  border-top: 1px solid #e9ecef;
  background: white;
}

.input-group {
  display: flex;
  gap: 10px;
}

.form-control {
  border-radius: 25px;
  border: 1px solid #e9ecef;
  padding: 10px 16px;
  font-size: 14px;
}

.btn {
  border-radius: 50%;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
}

/* Scrollbar styling */
.chat-messages::-webkit-scrollbar {
  width: 4px;
}

.chat-messages::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 2px;
}

.chat-messages::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* Responsive */
@media (max-width: 480px) {
  .chat-window {
    width: calc(100vw - 40px);
    height: calc(100vh - 120px);
    position: fixed;
    top: 20px;
    left: 20px;
    right: 20px;
    bottom: 20px;
  }

  .chat-button {
    width: 50px;
    height: 50px;
  }

  .chat-button i {
    font-size: 20px;
  }
}
</style>