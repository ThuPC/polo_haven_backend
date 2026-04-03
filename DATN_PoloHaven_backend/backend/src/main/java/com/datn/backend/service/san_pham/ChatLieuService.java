package com.datn.backend.service.san_pham;

import com.datn.backend.dto.request.ChatLieuRequestDTO;
import com.datn.backend.dto.response.ChatLieuResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ChatLieuService {
    List<String> getAllTenChatLieu();
    Page<ChatLieuResponseDTO> search(String keyword, Pageable pageable);
    Page<ChatLieuResponseDTO> getAll(Pageable pageable);
    ChatLieuResponseDTO create(ChatLieuRequestDTO dto);
    ChatLieuResponseDTO update(String id, ChatLieuRequestDTO dto);
    void delete(String id);
    ChatLieuResponseDTO findById(String id);
    ChatLieuResponseDTO updateStatus(String id, Byte trangThai);
    List<ChatLieuResponseDTO> getAllActiveChatLieu();
}