package com.datn.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatLieuRequestDTO {

//    private String maThuongHieu;

    @NotBlank(message = "Tên chất liệu không được để trống")
    private String tenChatLieu;

    private Byte trangThai;
}
