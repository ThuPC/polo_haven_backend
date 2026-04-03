package com.datn.backend.dto.request;

import lombok.Data;

import java.math.BigDecimal;
@Data
public class ChiTietKhuyenMaiRequest {
    private String idChiTietSanPham;
    private String idKhuyenMai;
    Byte trangThai;
}
