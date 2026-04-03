package com.datn.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChiTietSanPhamHoaDonResponseDto {
    private String id;
    private String tenAnh;
    private String tenMau;
    private String size;
    private String tenCTSP;
    private String maCTSP;
    private Integer soLuong;
    private BigDecimal donGia;
}
