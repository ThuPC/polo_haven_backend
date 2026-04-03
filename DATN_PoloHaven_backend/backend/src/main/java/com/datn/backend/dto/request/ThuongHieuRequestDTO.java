package com.datn.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ThuongHieuRequestDTO {

//    private String maThuongHieu;

    @NotBlank(message = "Tên thương hiệu không được để trống")
    private String tenThuongHieu;

    private Byte trangThai;
}
