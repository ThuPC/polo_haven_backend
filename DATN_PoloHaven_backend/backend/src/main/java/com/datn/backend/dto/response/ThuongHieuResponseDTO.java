package com.datn.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ThuongHieuResponseDTO {
    private String id;
    private String maThuongHieu;
    private String tenThuongHieu;
    private Byte trangThai;
}
