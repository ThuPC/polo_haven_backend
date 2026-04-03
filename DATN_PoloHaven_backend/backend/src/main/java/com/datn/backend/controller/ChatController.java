package com.datn.backend.controller;

import com.datn.backend.dto.ChatMessageDto;
import com.datn.backend.dto.ChatRoomDto;
import com.datn.backend.entity.ChatMessage;
import com.datn.backend.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChatController {
    
    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    
    // WebSocket endpoints
    @MessageMapping("/send")
    public void sendMessage(@Payload Map<String, Object> payload) {
        Long roomId = Long.valueOf(payload.get("roomId").toString());
        String senderId = payload.get("senderId").toString();
        ChatMessage.SenderType senderType = ChatMessage.SenderType.valueOf(payload.get("senderType").toString());
        String message = payload.get("message") != null ? payload.get("message").toString() : "";
        String senderName = payload.get("senderName") != null ? payload.get("senderName").toString() : "";
        String type = payload.get("type") != null ? payload.get("type").toString() : "text";
        String fileUrl = payload.get("fileUrl") != null ? payload.get("fileUrl").toString() : null;

        ChatMessageDto sentMessage = chatService.sendMessage(roomId, senderId, senderType, message, senderName, type, fileUrl);

        // Gửi tin nhắn đến topic của room
        if (sentMessage != null) {
            messagingTemplate.convertAndSend("/topic/chat/" + roomId, sentMessage);
        }
    }

    @MessageMapping("/join")
    public void addUser(@Payload Map<String, Object> payload, SimpMessageHeaderAccessor headerAccessor) {
        String senderName = payload.get("senderName").toString();
        String roomId = payload.get("roomId").toString();

        headerAccessor.getSessionAttributes().put("username", senderName);
        headerAccessor.getSessionAttributes().put("roomId", roomId);

        ChatMessageDto message = new ChatMessageDto();
        message.setSenderName(senderName);
        message.setMessage("User joined");

        // Gửi đến topic tương ứng với roomId
        messagingTemplate.convertAndSend("/topic/chat/" + roomId, message);
    }
    
    // REST API endpoints
    @PostMapping("/room/create")
    public ResponseEntity<ChatRoomDto> createChatRoom(@RequestBody Map<String, Object> request) {
        try {
            System.out.println("Request data: " + request); // Log đầu vào

            // Kiểm tra dữ liệu đầu vào
            if (request.get("customerId") == null) {
                System.err.println("customerId is null in request");
                return ResponseEntity.badRequest().body(null);
            }

            if (request.get("customerName") == null) {
                System.err.println("customerName is null in request");
                return ResponseEntity.badRequest().body(null);
            }

            // Lấy customerId dưới dạng String (không chuyển đổi)
            String customerId = request.get("customerId").toString();
            String customerName = request.get("customerName").toString();

            System.out.println("Parsed data - customerId: " + customerId + ", customerName: " + customerName);

            ChatRoomDto room = chatService.createOrGetChatRoom(customerId, customerName);
            return ResponseEntity.ok(room);

        } catch (Exception e) {
            System.err.println("Error in createChatRoom: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    @PostMapping("/room/{roomId}/assign-admin")
    public ResponseEntity<ChatRoomDto> assignAdminToRoom(
            @PathVariable Long roomId,
            @RequestBody Map<String, Object> request) {
        String adminId = request.get("adminId").toString();
        String adminName = request.get("adminName").toString();
        
        ChatRoomDto room = chatService.assignAdminToRoom(roomId, adminId, adminName);
        return ResponseEntity.ok(room);
    }
    
    @GetMapping("/room/customer/{customerId}")
    public ResponseEntity<ChatRoomDto> getRoomForCustomer(@PathVariable String customerId) {
        ChatRoomDto room = chatService.getRoomForCustomer(customerId);
        if (room != null) {
            return ResponseEntity.ok(room);
        }
        return ResponseEntity.notFound().build();
    }
    
    @GetMapping("/room/admin/{adminId}")
    public ResponseEntity<List<ChatRoomDto>> getRoomsForAdmin(@PathVariable String adminId) {
        System.out.println("Controller: Getting rooms for admin ID: " + adminId);
        List<ChatRoomDto> rooms = chatService.getRoomsForAdmin(adminId);
        System.out.println("Controller: Returning " + rooms.size() + " rooms");
        return ResponseEntity.ok(rooms);
    }
    
    // Test endpoint để lấy tất cả chat rooms
    @GetMapping("/room/all")
    public ResponseEntity<List<ChatRoomDto>> getAllChatRooms() {
        System.out.println("Controller: Getting all chat rooms");
        List<ChatRoomDto> rooms = chatService.getAllChatRooms();
        System.out.println("Controller: Returning " + rooms.size() + " rooms");
        return ResponseEntity.ok(rooms);
    }
    
    @GetMapping("/room/{roomId}/messages")
    public ResponseEntity<List<ChatMessageDto>> getMessagesByRoomId(@PathVariable Long roomId) {
        List<ChatMessageDto> messages = chatService.getMessagesByRoomId(roomId);
        return ResponseEntity.ok(messages);
    }
    
    @PostMapping("/room/{roomId}/mark-read")
    public ResponseEntity<Void> markMessagesAsRead(
            @PathVariable Long roomId,
            @RequestBody Map<String, Object> request) {
        ChatMessage.SenderType senderType = ChatMessage.SenderType.valueOf(request.get("senderType").toString());
        chatService.markMessagesAsRead(roomId, senderType);
        return ResponseEntity.ok().build();
    }
    
//    @PostMapping("/send-message")
//    public ResponseEntity<ChatMessageDto> sendMessageViaRest(@RequestBody Map<String, Object> request) {
//        Long roomId = Long.valueOf(request.get("roomId").toString());
//        String senderId = request.get("senderId").toString();
//        ChatMessage.SenderType senderType = ChatMessage.SenderType.valueOf(request.get("senderType").toString());
//        String message = request.get("message") != null ? request.get("message").toString() : "";
//        String senderName = request.get("senderName") != null ? request.get("senderName").toString() : "";
//        String type = request.get("type") != null ? request.get("type").toString() : "text";
//        String fileUrl = request.get("fileUrl") != null ? request.get("fileUrl").toString() : null;
//
//        ChatMessageDto sentMessage = chatService.sendMessage(roomId, senderId, senderType, message, senderName, type, fileUrl);
//        return ResponseEntity.ok(sentMessage);
//    }
@PostMapping("/send-message")
public ResponseEntity<ChatMessageDto> sendMessageViaRest(@RequestBody Map<String, Object> request) {
    Long roomId = Long.valueOf(request.get("roomId").toString());
    String senderId = request.get("senderId").toString();
    ChatMessage.SenderType senderType = ChatMessage.SenderType.valueOf(request.get("senderType").toString());
    String message = request.get("message") != null ? request.get("message").toString() : "";
    String senderName = request.get("senderName") != null ? request.get("senderName").toString() : "";

    // ✅ Fix fallback for type and fileUrl
    String type = (request.get("type") != null && !request.get("type").toString().isBlank())
            ? request.get("type").toString()
            : "text";

    String fileUrl = (request.get("fileUrl") != null && !request.get("fileUrl").toString().isBlank())
            ? request.get("fileUrl").toString()
            : null;

    ChatMessageDto sentMessage = chatService.sendMessage(roomId, senderId, senderType, message, senderName, type, fileUrl);
    return ResponseEntity.ok(sentMessage);
}


    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            Path uploadDirPath = Paths.get(System.getProperty("user.dir"), "uploads", "chat");
            File uploadDir = uploadDirPath.toFile();
            if (!uploadDir.exists()) uploadDir.mkdirs();

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path destPath = uploadDirPath.resolve(fileName);
            File dest = destPath.toFile();
            file.transferTo(dest);

            // Log absolute path for debugging static resource issues
            System.out.println("Saved upload to: " + dest.getAbsolutePath());

            // Return a relative URL under /uploads so the StaticResourceConfig can serve it
            String url = "/uploads/chat/" + fileName;
            return ResponseEntity.ok(Map.of("url", url));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Upload failed"));
        }
    }

}