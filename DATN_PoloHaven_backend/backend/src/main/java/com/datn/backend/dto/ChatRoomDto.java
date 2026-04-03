package com.datn.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomDto {
    private Long id;
    private String roomName;
    private String customerId;
    private String adminId;
    private Boolean isActive;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String customerName; // Tên khách hàng
    private String customerAvatar; // Avatar khách hàng
    private String adminName; // Tên admin
    private Integer unreadCount; // Số tin nhắn chưa đọc
    private List<ChatMessageDto> messages;
} 