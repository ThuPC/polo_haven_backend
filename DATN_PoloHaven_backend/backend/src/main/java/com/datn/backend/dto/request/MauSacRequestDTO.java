package com.datn.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MauSacRequestDTO {

//    private String maThuongHieu;

    @NotBlank(message = "Tên màu sắc không được để trống")
    private String tenMau;
    private String maMauSac;
    private Byte trangThai;
}
