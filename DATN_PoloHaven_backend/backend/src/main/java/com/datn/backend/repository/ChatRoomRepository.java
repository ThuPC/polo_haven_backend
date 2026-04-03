package com.datn.backend.repository;

import com.datn.backend.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    
    @Query("SELECT cr FROM ChatRoom cr WHERE cr.customerId = :customerId AND cr.isActive = true")
    Optional<ChatRoom> findByCustomerId(@Param("customerId") String customerId);
    
    @Query("SELECT cr FROM ChatRoom cr WHERE cr.adminId = :adminId AND cr.isActive = true ORDER BY cr.lastMessageTime DESC")
    List<ChatRoom> findByAdminId(@Param("adminId") String adminId);
    
    @Query("SELECT cr FROM ChatRoom cr WHERE cr.isActive = true ORDER BY cr.lastMessageTime DESC")
    List<ChatRoom> findAllActiveRooms();
    
    @Query("SELECT cr FROM ChatRoom cr WHERE (cr.customerId = :customerId OR cr.adminId = :adminId) AND cr.isActive = true")
    List<ChatRoom> findByCustomerIdOrAdminId(@Param("customerId") String customerId, @Param("adminId") String adminId);
} 