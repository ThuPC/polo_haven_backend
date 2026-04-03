package com.datn.backend.dto.response.BanHangOnline;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class SanPhamSummaryDTO {
    private String id;
    private String tenSanPham;
    private String thuongHieu;
    private String chatLieu;
    private String xuatXu;
    private String khoangGia;
    private Long soLuongTon;
    private BigDecimal phanTramGiam;
}