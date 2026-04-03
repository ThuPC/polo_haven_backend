package com.datn.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SanPhamTenResponseDTO {
    private String id;
    private String tenSanPham;
    private String moTa;
}

