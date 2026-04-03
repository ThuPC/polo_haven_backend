package com.datn.backend.dto;

import com.datn.backend.entity.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDto {
    private Long id;
    private Long chatRoomId;
    private String senderId;
    private ChatMessage.SenderType senderType;
    private String message;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private String senderName; // Tên người gửi
    private String senderAvatar; // Avatar người gửi
    private String type ;
    private String fileUrl;
} 