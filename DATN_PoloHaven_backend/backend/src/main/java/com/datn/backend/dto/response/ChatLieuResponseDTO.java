package com.datn.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatLieuResponseDTO {
    private String id;
    private String maChatLieu;
    private String tenChatLieu;
    private Byte trangThai;
}
