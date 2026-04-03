package com.datn.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class XuatXuRequestDTO {

    @NotBlank(message = "Nơi xuất xứ không được để trống")
    private String noiXuatXu;

    private Byte trangThai;
}
