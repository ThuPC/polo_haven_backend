package com.datn.backend.service;

import com.datn.backend.dto.ChatMessageDto;
import com.datn.backend.dto.ChatRoomDto;
import com.datn.backend.entity.ChatMessage;
import com.datn.backend.entity.ChatRoom;
import com.datn.backend.entity.KhachHang;
import com.datn.backend.repository.ChatMessageRepository;
import com.datn.backend.repository.ChatRoomRepository;
import com.datn.backend.repository.KhachHangRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final KhachHangRepository khachHangRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // Trong ChatService.java

    // Hàm gửi tin nhắn chào bot
    private void sendWelcomeBotMessage(ChatRoom room, String customerName) {
        try {
            ChatMessage botMessage = new ChatMessage();
            botMessage.setChatRoom(room);
            botMessage.setSenderId("BOT");
            botMessage.setSenderType(ChatMessage.SenderType.ADMIN);

            String displayName = (customerName != null && !customerName.isBlank()) ? customerName : "quý khách";
            
            String botText = String.format(
                "Xin chào %s 👋, cảm ơn bạn đã liên hệ với PoloHaven!\n" +
                "Chúng tôi luôn sẵn sàng hỗ trợ bạn. Dưới đây là một số mục bạn có thể tham khảo nhanh:\n\n" +
                "1) 🕑 Giờ làm việc: Thứ 2 – Chủ nhật, từ 8:00 đến 22:00\n" +
                "2) 🔄 Chính sách đổi/trả hàng: Trong vòng 7 ngày, sản phẩm còn nguyên tem/mác\n" +
                "3) 📦 Tra cứu trạng thái đơn hàng: Vui lòng cung cấp mã đơn để kiểm tra\n" +
                "4) 💳 Hình thức thanh toán: Tiền mặt, chuyển khoản, hoặc ví điện tử\n\n" +
                "Nếu bạn cần nhân viên hỗ trợ trực tiếp, vui lòng để lại thông tin (tên + số điện thoại).\n" +
                "Đội ngũ PoloHaven sẽ phản hồi sớm nhất có thể ❤️",
                displayName
            );

            botMessage.setMessage(botText);
            botMessage.setType("text");
            botMessage.setFileUrl(null);

            ChatMessage savedBot = chatMessageRepository.save(botMessage);

            // Cập nhật last message cho room
            room.setLastMessage("Trợ lý ảo: Xin chào " + displayName + ", cảm ơn bạn đã liên hệ với PoloHaven!");
            room.setLastMessageTime(LocalDateTime.now());
            chatRoomRepository.save(room);

            ChatMessageDto botDto = convertToMessageDto(savedBot, "Trợ lý ảo");

            // Gửi broadcast tới topic phòng
            messagingTemplate.convertAndSend("/topic/chat/" + room.getId(), botDto);

        } catch (Exception ex) {
            System.err.println("Error while sending bot reply: " + ex.getMessage());
            ex.printStackTrace();
        }
    }


    // Tạo hoặc lấy chat room cho khách hàng
    public ChatRoomDto createOrGetChatRoom(String customerId, String customerName) {
        Optional<ChatRoom> existingRoom = chatRoomRepository.findByCustomerId(customerId);

        if (existingRoom.isPresent()) {
            return convertToDto(existingRoom.get());
        }

        ChatRoom newRoom = new ChatRoom();
        newRoom.setCustomerId(customerId);
        newRoom.setRoomName("Chat với " + customerName);
        newRoom.setIsActive(true);

        ChatRoom savedRoom = chatRoomRepository.save(newRoom);

        // 👉 Gửi tin nhắn chào bot ngay khi tạo phòng
        sendWelcomeBotMessage(savedRoom, customerName);

        return convertToDto(savedRoom);
    }

    // Gán admin cho chat room
    public ChatRoomDto assignAdminToRoom(Long roomId, String adminId, String adminName) {
        Optional<ChatRoom> roomOpt = chatRoomRepository.findById(roomId);
        if (roomOpt.isPresent()) {
            ChatRoom room = roomOpt.get();
            room.setAdminId(adminId);
            room.setRoomName("Chat với " + room.getRoomName());
            ChatRoom savedRoom = chatRoomRepository.save(room);
            return convertToDto(savedRoom);
        }
        return null;
    }

    // Gửi tin nhắn
    public ChatMessageDto sendMessage(Long roomId, String senderId, ChatMessage.SenderType senderType, String message, String senderName, String type, String fileUrl) {
        Optional<ChatRoom> roomOpt = chatRoomRepository.findById(roomId);
        if (roomOpt.isPresent()) {
            ChatRoom room = roomOpt.get();

            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setChatRoom(room);
            chatMessage.setSenderId(senderId);
            chatMessage.setSenderType(senderType);
            chatMessage.setMessage(message);
            chatMessage.setType(type);
            chatMessage.setFileUrl(fileUrl);

            ChatMessage savedMessage = chatMessageRepository.save(chatMessage);

            // Cập nhật last message cho room
            room.setLastMessage(message);
            room.setLastMessageTime(LocalDateTime.now());
            chatRoomRepository.save(room);

            // Tạo DTO để trả về
            ChatMessageDto messageDto = convertToMessageDto(savedMessage, senderName);

            // Gửi thông báo đến admin nếu là tin nhắn từ khách hàng
            if (senderType == ChatMessage.SenderType.CUSTOMER) {
                // Nếu room đã có admin được gán, gửi thông báo cho admin đó
                if (room.getAdminId() != null) {
                    messagingTemplate.convertAndSendToUser(
                            room.getAdminId().toString(),
                            "/queue/chat/notification",
                            messageDto
                    );
                } else {
                    // Nếu chưa có admin, gửi thông báo đến topic chung để tất cả admin có thể nhận
                    messagingTemplate.convertAndSend("/topic/admin/chat/notification", messageDto);
                }
            }

            // Nếu là tin nhắn đầu tiên từ khách hàng và phòng chưa có admin, gửi tin nhắn chào mừng
            if (senderType == ChatMessage.SenderType.CUSTOMER && room.getAdminId() == null) {
                // Kiểm tra xem đã có tin nhắn từ BOT chưa để tránh gửi lặp lại
                List<ChatMessage> existingBotMessages = chatMessageRepository.findByChatRoomIdAndSenderIdOrderByCreatedAtAsc(roomId, "BOT");
                if (existingBotMessages.isEmpty()) {
                    sendWelcomeBotMessage(room, senderName);
                }
            }


            return messageDto;
        }
        return null;
    }

    // Lấy tin nhắn của room
    public List<ChatMessageDto> getMessagesByRoomId(Long roomId) {
        List<ChatMessage> messages = chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(roomId);
        return messages.stream()
                .map(this::convertToMessageDto)
                .collect(Collectors.toList());
    }

    // Lấy danh sách room cho admin
    public List<ChatRoomDto> getRoomsForAdmin(String adminId) {
        System.out.println("Getting rooms for admin ID: " + adminId);

        // Lấy tất cả chat rooms (cả đã gán admin và chưa gán)
        List<ChatRoom> allRooms = chatRoomRepository.findAll();
        System.out.println("Total rooms found: " + allRooms.size());

        // Lọc và sắp xếp: rooms đã gán cho admin này trước, sau đó là rooms chưa gán admin
        List<ChatRoom> sortedRooms = allRooms.stream()
                .sorted((r1, r2) -> {
                    // Rooms đã gán cho admin này có ưu tiên cao nhất
                    if (adminId.equals(r1.getAdminId()) && !adminId.equals(r2.getAdminId())) {
                        return -1;
                    }
                    if (!adminId.equals(r1.getAdminId()) && adminId.equals(r2.getAdminId())) {
                        return 1;
                    }
                    // Sau đó sắp xếp theo thời gian tin nhắn cuối cùng (mới nhất trước)
                    if (r1.getLastMessageTime() != null && r2.getLastMessageTime() != null) {
                        return r2.getLastMessageTime().compareTo(r1.getLastMessageTime());
                    }
                    if (r1.getLastMessageTime() != null) {
                        return -1;
                    }
                    if (r2.getLastMessageTime() != null) {
                        return 1;
                    }
                    return 0;
                })
                .collect(Collectors.toList());

        System.out.println("Rooms after sorting: " + sortedRooms.size());
        sortedRooms.forEach(room -> {
            System.out.println("Room ID: " + room.getId() +
                    ", Customer: " + room.getCustomerId() +
                    ", Admin: " + room.getAdminId() +
                    ", Last Message: " + room.getLastMessage());
        });

        List<ChatRoomDto> result = sortedRooms.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        System.out.println("Returning " + result.size() + " rooms to admin");
        return result;
    }

    // Lấy tất cả chat rooms (cho debug)
    public List<ChatRoomDto> getAllChatRooms() {
        List<ChatRoom> allRooms = chatRoomRepository.findAll();
        return allRooms.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Lấy room cho khách hàng
    public ChatRoomDto getRoomForCustomer(String customerId) {
        Optional<ChatRoom> room = chatRoomRepository.findByCustomerId(customerId);
        return room.map(this::convertToDto).orElse(null);
    }

    // Đánh dấu tin nhắn đã đọc
    public void markMessagesAsRead(Long roomId, ChatMessage.SenderType senderType) {
        List<ChatMessage> unreadMessages = chatMessageRepository.findUnreadMessagesByChatRoomIdAndSenderType(roomId, senderType);
        unreadMessages.forEach(message -> {
            message.setIsRead(true);
            chatMessageRepository.save(message);
        });
    }

    // Convert entity to DTO
    private ChatRoomDto convertToDto(ChatRoom room) {
        ChatRoomDto dto = new ChatRoomDto();
        dto.setId(room.getId());
        dto.setRoomName(room.getRoomName());
        dto.setCustomerId(room.getCustomerId());
        dto.setAdminId(room.getAdminId());
        dto.setIsActive(room.getIsActive());
        dto.setLastMessage(room.getLastMessage());
        dto.setLastMessageTime(room.getLastMessageTime());
        dto.setCreatedAt(room.getCreatedAt());
        dto.setUpdatedAt(room.getUpdatedAt());

        // Lấy thông tin khách hàng
        if (room.getCustomerId() != null) {
            Optional<KhachHang> khachHangOpt = khachHangRepository.findById(room.getCustomerId());
            if (khachHangOpt.isPresent()) {
                KhachHang khachHang = khachHangOpt.get();
                dto.setCustomerName(khachHang.getTenKhachHang());
                dto.setCustomerAvatar(khachHang.getHinhAnh());
            } else {
                // Fallback nếu không tìm thấy khách hàng
                dto.setCustomerName("Khách hàng");
                dto.setCustomerAvatar(null);
            }
        }

        // Tính số tin nhắn chưa đọc
        Long unreadCount = chatMessageRepository.countUnreadMessagesByChatRoomIdAndSenderType(
                room.getId(),
                room.getAdminId() != null ? ChatMessage.SenderType.ADMIN : ChatMessage.SenderType.CUSTOMER
        );
        dto.setUnreadCount(unreadCount.intValue());

        return dto;
    }

    private ChatMessageDto convertToMessageDto(ChatMessage message) {
        return convertToMessageDto(message, null);
    }

    private ChatMessageDto convertToMessageDto(ChatMessage message, String senderName) {
        ChatMessageDto dto = new ChatMessageDto();
        dto.setId(message.getId());
        dto.setChatRoomId(message.getChatRoom().getId());
        dto.setSenderId(message.getSenderId());
        dto.setSenderType(message.getSenderType());
        dto.setMessage(message.getMessage());
        dto.setIsRead(message.getIsRead());
        dto.setCreatedAt(message.getCreatedAt());
        dto.setSenderName(senderName);

        // Propagate file type and file URL so frontend can render images/videos
        dto.setType(message.getType());
        dto.setFileUrl(message.getFileUrl());

        // Lấy avatar của người gửi
        if (message.getSenderType() == ChatMessage.SenderType.CUSTOMER) {
            // Lấy avatar khách hàng
            Optional<KhachHang> khachHangOpt = khachHangRepository.findById(message.getSenderId());
            if (khachHangOpt.isPresent()) {
                dto.setSenderAvatar(khachHangOpt.get().getHinhAnh());
                if (senderName == null) {
                    dto.setSenderName(khachHangOpt.get().getTenKhachHang());
                }
            }
        } else {
            // Lấy avatar admin (có thể thêm AdminRepository sau)
            dto.setSenderAvatar(null); // Tạm thời để null cho admin
        }

        return dto;
    }
}