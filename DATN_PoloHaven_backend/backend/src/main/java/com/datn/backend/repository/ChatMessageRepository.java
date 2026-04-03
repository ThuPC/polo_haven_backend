package com.datn.backend.repository;

import com.datn.backend.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatRoom.id = :chatRoomId ORDER BY cm.createdAt ASC")
    List<ChatMessage> findByChatRoomIdOrderByCreatedAtAsc(@Param("chatRoomId") Long chatRoomId);
    
    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE cm.chatRoom.id = :chatRoomId AND cm.isRead = false AND cm.senderType != :senderType")
    Long countUnreadMessagesByChatRoomIdAndSenderType(@Param("chatRoomId") Long chatRoomId, @Param("senderType") ChatMessage.SenderType senderType);
    
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatRoom.id = :chatRoomId AND cm.isRead = false AND cm.senderType != :senderType")
    List<ChatMessage> findUnreadMessagesByChatRoomIdAndSenderType(@Param("chatRoomId") Long chatRoomId, @Param("senderType") ChatMessage.SenderType senderType);
    
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatRoom.id = :chatRoomId AND cm.senderId = :senderId ORDER BY cm.createdAt ASC")
    List<ChatMessage> findByChatRoomIdAndSenderIdOrderByCreatedAtAsc(@Param("chatRoomId") Long chatRoomId, @Param("senderId") String senderId);
}