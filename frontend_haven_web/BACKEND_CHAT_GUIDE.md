# Hướng dẫn implement Chat Backend

## 1. Cài đặt Dependencies

Thêm vào `pom.xml` (nếu dùng Spring Boot):

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
</dependency>
```

## 2. WebSocket Configuration

```java
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWebSocketHandler(), "/socket.io/")
                .setAllowedOrigins("http://localhost:5173") // Frontend URL
                .withSockJS();
    }

    @Bean
    public WebSocketHandler chatWebSocketHandler() {
        return new ChatWebSocketHandler();
    }
}
```

## 3. Chat WebSocket Handler

```java
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {
    
    private static final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private static final Map<String, String> userRooms = new ConcurrentHashMap<>();
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userId = extractUserId(session);
        String userRole = extractUserRole(session);
        
        sessions.put(userId, session);
        
        // Gửi thông báo online
        broadcastUserStatus(userId, true);
    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        ChatMessage chatMessage = objectMapper.readValue(payload, ChatMessage.class);
        
        // Xử lý tin nhắn
        handleChatMessage(chatMessage);
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String userId = extractUserId(session);
        sessions.remove(userId);
        userRooms.remove(userId);
        
        // Gửi thông báo offline
        broadcastUserStatus(userId, false);
    }
    
    private void handleChatMessage(ChatMessage message) {
        // Lưu tin nhắn vào database
        saveMessageToDatabase(message);
        
        // Gửi tin nhắn đến người nhận
        sendMessageToUser(message.getReceiverId(), message);
        
        // Broadcast tin nhắn đến admin nếu là khách hàng
        if ("customer".equals(message.getSenderRole())) {
            broadcastToAdmins(message);
        }
    }
    
    private void sendMessageToUser(String userId, ChatMessage message) {
        WebSocketSession session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
            } catch (Exception e) {
                log.error("Lỗi khi gửi tin nhắn", e);
            }
        }
    }
    
    private void broadcastToAdmins(ChatMessage message) {
        sessions.forEach((userId, session) -> {
            if (isAdmin(userId) && session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
                } catch (Exception e) {
                    log.error("Lỗi khi broadcast tin nhắn", e);
                }
            }
        });
    }
}
```

## 4. Chat Message Model

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage {
    private String id;
    private String content;
    private String senderId;
    private String receiverId;
    private String senderRole; // "customer" hoặc "admin"
    private String roomId;
    private Date timestamp;
    private String type; // "text", "image", etc.
}
```

## 5. Chat Room Entity

```java
@Entity
@Table(name = "chat_rooms")
@Data
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "customer_id")
    private String customerId;
    
    @Column(name = "customer_name")
    private String customerName;
    
    @Column(name = "created_at")
    private Date createdAt;
    
    @Column(name = "last_message")
    private String lastMessage;
    
    @Column(name = "last_message_time")
    private Date lastMessageTime;
    
    @Column(name = "is_active")
    private boolean isActive = true;
}
```

## 6. Chat Message Entity

```java
@Entity
@Table(name = "chat_messages")
@Data
public class ChatMessageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "room_id")
    private Long roomId;
    
    @Column(name = "sender_id")
    private String senderId;
    
    @Column(name = "content")
    private String content;
    
    @Column(name = "message_type")
    private String messageType;
    
    @Column(name = "timestamp")
    private Date timestamp;
    
    @Column(name = "is_read")
    private boolean isRead = false;
}
```

## 7. REST API Endpoints

```java
@RestController
@RequestMapping("/api/chat")
public class ChatController {
    
    @Autowired
    private ChatService chatService;
    
    // Lấy lịch sử chat
    @GetMapping("/history/{roomId}")
    public ResponseEntity<List<ChatMessage>> getChatHistory(@PathVariable Long roomId) {
        List<ChatMessage> messages = chatService.getChatHistory(roomId);
        return ResponseEntity.ok(messages);
    }
    
    // Tạo chat room mới
    @PostMapping("/room")
    public ResponseEntity<ChatRoom> createChatRoom(@RequestBody CreateRoomRequest request) {
        ChatRoom room = chatService.createChatRoom(request.getCustomerId());
        return ResponseEntity.ok(room);
    }
    
    // Lấy danh sách chat rooms (cho admin)
    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoom>> getChatRooms() {
        List<ChatRoom> rooms = chatService.getChatRooms();
        return ResponseEntity.ok(rooms);
    }
    
    // Đánh dấu tin nhắn đã đọc
    @PutMapping("/messages/{messageId}/read")
    public ResponseEntity<Void> markMessageAsRead(@PathVariable Long messageId) {
        chatService.markMessageAsRead(messageId);
        return ResponseEntity.ok().build();
    }
}
```

## 8. Database Schema

```sql
-- Bảng chat rooms
CREATE TABLE chat_rooms (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id VARCHAR(255) NOT NULL,
    customer_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_message TEXT,
    last_message_time TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);

-- Bảng chat messages
CREATE TABLE chat_messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_id BIGINT NOT NULL,
    sender_id VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    message_type VARCHAR(50) DEFAULT 'text',
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_read BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (room_id) REFERENCES chat_rooms(id)
);

-- Index để tối ưu performance
CREATE INDEX idx_room_id ON chat_messages(room_id);
CREATE INDEX idx_sender_id ON chat_messages(sender_id);
CREATE INDEX idx_timestamp ON chat_messages(timestamp);
```

## 9. Security Configuration

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/socket.io/**").permitAll()
                .requestMatchers("/api/chat/**").authenticated()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
```

## 10. JWT Authentication cho WebSocket

```java
@Component
public class WebSocketJwtAuthentication {
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    public String extractUserId(WebSocketSession session) {
        String token = extractToken(session);
        if (token != null) {
            return jwtTokenProvider.getUserIdFromToken(token);
        }
        return null;
    }
    
    public String extractUserRole(WebSocketSession session) {
        String token = extractToken(session);
        if (token != null) {
            return jwtTokenProvider.getUserRoleFromToken(token);
        }
        return null;
    }
    
    private String extractToken(WebSocketSession session) {
        String query = session.getUri().getQuery();
        if (query != null) {
            String[] params = query.split("&");
            for (String param : params) {
                if (param.startsWith("token=")) {
                    return param.substring(6);
                }
            }
        }
        return null;
    }
}
```

## 11. Cách sử dụng

### Frontend (Vue.js):
1. Cài đặt socket.io-client: `npm install socket.io-client`
2. Import và sử dụng ChatService đã tạo
3. Component CustomerChat sẽ tự động kết nối khi user đăng nhập

### Backend (Spring Boot):
1. Thêm dependencies vào pom.xml
2. Tạo các entity và repository
3. Implement WebSocket handler
4. Tạo REST API endpoints
5. Cấu hình security

### Database:
1. Tạo các bảng theo schema đã cung cấp
2. Thêm indexes để tối ưu performance

## 12. Tính năng nâng cao có thể thêm:

- **File sharing**: Upload và gửi file
- **Typing indicator**: Hiển thị "đang nhập..."
- **Read receipts**: Đánh dấu tin nhắn đã đọc
- **Push notifications**: Thông báo khi có tin nhắn mới
- **Chat history**: Lưu trữ và tìm kiếm tin nhắn cũ
- **User status**: Online/offline status
- **Message reactions**: Emoji reactions
- **Voice messages**: Ghi âm và gửi voice messages 