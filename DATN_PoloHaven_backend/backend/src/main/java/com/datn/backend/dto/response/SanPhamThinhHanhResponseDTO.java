package com.datn.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SanPhamThinhHanhResponseDTO {
    private String idSanPham;
    private String tenSanPham;
    private String tenAnh;
    private Long tongSoLuong;
}

