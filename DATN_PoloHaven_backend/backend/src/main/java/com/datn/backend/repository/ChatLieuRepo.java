package com.datn.backend.repository;

import com.datn.backend.dto.response.ChatLieuResponseDTO;
import com.datn.backend.entity.ChatLieu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatLieuRepo extends JpaRepository<ChatLieu, String> {
    Page<ChatLieu> findAll(Pageable pageable);

    Page<ChatLieu> findByTenChatLieuContainingIgnoreCase(String keyword, Pageable pageable);

    @Query("SELECT c.tenChatLieu FROM ChatLieu c WHERE c.trangThai = 1")
    List<String> findAllTenChatLieu();

    @Query("SELECT new com.datn.backend.dto.response.ChatLieuResponseDTO(c.id, c.maChatLieu, c.tenChatLieu, c.trangThai) FROM ChatLieu c WHERE c.trangThai = 1")
    List<ChatLieuResponseDTO> findAllActiveChatLieu();
}
