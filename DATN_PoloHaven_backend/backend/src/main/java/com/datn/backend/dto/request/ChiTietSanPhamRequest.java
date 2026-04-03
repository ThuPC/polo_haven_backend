package com.datn.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChiTietSanPhamRequest {
    private String id;
    private String maCTSP;
    private String tenCTSP;
    private Integer soLuong;
    private BigDecimal donGia;

    private String tensnSanPham;
    private String tenMau;
    private String size;
}
