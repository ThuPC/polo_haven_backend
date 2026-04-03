package com.datn.backend.service.san_pham;

import com.datn.backend.dto.request.ChatLieuRequestDTO;
import com.datn.backend.dto.response.ChatLieuResponseDTO;
import com.datn.backend.entity.ChatLieu;
import com.datn.backend.mapper.ChatLieuMapper;
import com.datn.backend.repository.ChatLieuRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatLieuServiceImpl implements ChatLieuService {

    private final ChatLieuRepo chatLieuRepo;
    private final ChatLieuMapper chatLieuMapper;

    @Override
    public Page<ChatLieuResponseDTO> getAll(Pageable pageable) {
        return chatLieuRepo.findAll(pageable)
                .map(chatLieuMapper::toResponseDTO);
    }

    @Override
    public ChatLieuResponseDTO create(ChatLieuRequestDTO dto) {
        ChatLieu entity = new ChatLieu();
        entity.setMaChatLieu(generateMaChatLieu()); // Tự động sinh mã
        entity.setTenChatLieu(dto.getTenChatLieu());
        entity.setTrangThai((byte) 1); // Mặc định trạng thái là 1

        return chatLieuMapper.toResponseDTO(chatLieuRepo.save(entity));
    }

    @Override
    public ChatLieuResponseDTO update(String id, ChatLieuRequestDTO dto) {
        ChatLieu existing = chatLieuRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Chất Liệu"));
        chatLieuMapper.updateEntityFromDTO(dto, existing);
        return chatLieuMapper.toResponseDTO(chatLieuRepo.save(existing));
    }

    @Override
    public Page<ChatLieuResponseDTO> search(String keyword, Pageable pageable) {
        return chatLieuRepo.findByTenChatLieuContainingIgnoreCase(keyword, pageable)
                .map(chatLieuMapper::toResponseDTO);
    }

    @Override
    public void delete(String id) {
        ChatLieu existing = chatLieuRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Chất Liệu"));
        chatLieuRepo.delete(existing); // hoặc xóa mềm bằng setTrangThai = 0
    }

    @Override
    public ChatLieuResponseDTO updateStatus(String id, Byte trangThai) {
        ChatLieu entity = chatLieuRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chất liệu"));

        entity.setTrangThai(trangThai);
        return chatLieuMapper.toResponseDTO(chatLieuRepo.save(entity));
    }

    @Override
    public ChatLieuResponseDTO findById(String id) {
        ChatLieu existing = chatLieuRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Chất Liệu"));
        return chatLieuMapper.toResponseDTO(existing);
    }

    @Override
    public List<String> getAllTenChatLieu() {
        return chatLieuRepo.findAllTenChatLieu();
    }

    // Hàm tự động sinh mã chất liệu
    private String generateMaChatLieu() {
        List<ChatLieu> all = chatLieuRepo.findAll();

        int max = all.stream()
                .mapToInt(cl -> {
                    try {
                        return Integer.parseInt(cl.getMaChatLieu().replace("CL", ""));
                    } catch (Exception e) {
                        return 0;
                    }
                })
                .max()
                .orElse(0);

        return String.format("CL%03d", max + 1);
    }

    @Override
    public List<ChatLieuResponseDTO> getAllActiveChatLieu() {
        return chatLieuRepo.findAllActiveChatLieu();
    }
}
