# Hệ thống Chat Khách hàng - Admin

## Tổng quan

Hệ thống chat real-time giữa khách hàng và admin được xây dựng bằng:
- **Frontend**: Vue.js 3 + Socket.io-client
- **Backend**: Spring Boot + WebSocket
- **Database**: MySQL/PostgreSQL

## Tính năng chính

### Cho Khách hàng:
- ✅ Chat widget floating ở góc màn hình
- ✅ Gửi và nhận tin nhắn real-time
- ✅ Hiển thị trạng thái online/offline
- ✅ Đếm tin nhắn chưa đọc
- ✅ Lịch sử chat
- ✅ Giao diện responsive

### Cho Admin:
- ✅ Dashboard quản lý nhiều cuộc hội thoại
- ✅ Danh sách khách hàng đang chat
- ✅ Chuyển đổi giữa các cuộc hội thoại
- ✅ Hiển thị tin nhắn chưa đọc
- ✅ Giao diện chuyên nghiệp

## Cài đặt và Chạy

### 1. Frontend (Vue.js)

```bash
# Cài đặt dependencies
npm install

# Chạy development server
npm run dev
```

### 2. Backend (Spring Boot)

Xem file `BACKEND_CHAT_GUIDE.md` để biết chi tiết implement backend.

### 3. Database

Chạy script SQL trong `BACKEND_CHAT_GUIDE.md` để tạo các bảng cần thiết.

## Cách sử dụng

### Cho Khách hàng:

1. **Đăng nhập** vào hệ thống
2. **Chat widget** sẽ xuất hiện ở góc dưới bên phải
3. **Click vào widget** để mở chat window
4. **Gửi tin nhắn** và nhận phản hồi từ admin

### Cho Admin:

1. **Truy cập** trang `/admin-chat`
2. **Chọn cuộc hội thoại** từ danh sách bên trái
3. **Gửi tin nhắn** cho khách hàng
4. **Quản lý** nhiều cuộc hội thoại cùng lúc

## Cấu trúc Files

```
src/
├── components/
│   ├── CustomerChat.vue      # Chat widget cho khách hàng
│   └── AdminChat.vue         # Dashboard chat cho admin
├── services/
│   └── chatService.js        # Service xử lý WebSocket
├── views/
│   └── AdminChat.vue         # Trang chat admin
└── App.vue                   # Component chính (đã thêm CustomerChat)
```

## API Endpoints

### Chat API:
- `GET /api/chat/history/{roomId}` - Lấy lịch sử chat
- `POST /api/chat/room` - Tạo chat room mới
- `GET /api/chat/rooms` - Lấy danh sách chat rooms
- `PUT /api/chat/messages/{messageId}/read` - Đánh dấu đã đọc

### WebSocket Events:
- `connect` - Kết nối WebSocket
- `disconnect` - Ngắt kết nối
- `sendMessage` - Gửi tin nhắn
- `newMessage` - Nhận tin nhắn mới
- `userStatus` - Trạng thái online/offline

## Cấu hình

### Frontend Configuration:

```javascript
// src/services/chatService.js
const socket = io('http://localhost:8080', {
  auth: {
    token: localStorage.getItem('authToken')
  }
});
```

### Backend Configuration:

```java
// WebSocket endpoint
registry.addHandler(chatWebSocketHandler(), "/socket.io/")
        .setAllowedOrigins("http://localhost:5173")
        .withSockJS();
```

## Tính năng nâng cao

### Đã implement:
- ✅ Real-time messaging
- ✅ User authentication
- ✅ Message history
- ✅ Unread message count
- ✅ Online/offline status
- ✅ Responsive design

### Có thể thêm:
- 📝 File sharing
- 📝 Typing indicator
- 📝 Read receipts
- 📝 Push notifications
- 📝 Message reactions
- 📝 Voice messages
- 📝 Video call integration

## Troubleshooting

### Lỗi thường gặp:

1. **WebSocket không kết nối được**
   - Kiểm tra backend server có chạy không
   - Kiểm tra CORS configuration
   - Kiểm tra JWT token có hợp lệ không

2. **Tin nhắn không gửi được**
   - Kiểm tra user đã đăng nhập chưa
   - Kiểm tra WebSocket connection
   - Kiểm tra backend logs

3. **Chat widget không hiển thị**
   - Kiểm tra user đã đăng nhập chưa
   - Kiểm tra component đã import đúng chưa
   - Kiểm tra console errors

### Debug:

```javascript
// Thêm vào chatService.js để debug
socket.on('connect', () => {
  console.log('Connected to chat server');
});

socket.on('error', (error) => {
  console.error('Socket error:', error);
});
```

## Performance

### Frontend:
- Sử dụng Vue 3 Composition API
- Lazy loading cho components
- Debounce cho typing events
- Virtual scrolling cho message list (có thể thêm)

### Backend:
- Connection pooling
- Message caching
- Database indexing
- Load balancing (có thể thêm)

## Security

### Implemented:
- ✅ JWT authentication
- ✅ WebSocket authentication
- ✅ Input validation
- ✅ SQL injection prevention

### Recommendations:
- 🔒 Rate limiting
- 🔒 Message encryption
- 🔒 File upload validation
- 🔒 User role validation

## Deployment

### Frontend:
```bash
npm run build
# Deploy dist/ folder
```

### Backend:
```bash
./mvnw clean package
# Deploy JAR file
```

### Environment Variables:
```bash
# Frontend
VITE_API_URL=http://localhost:8080

# Backend
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/chat_db
JWT_SECRET=your-secret-key
```

## Contributing

1. Fork repository
2. Tạo feature branch
3. Commit changes
4. Push to branch
5. Tạo Pull Request

## License

MIT License - xem file LICENSE để biết chi tiết. 