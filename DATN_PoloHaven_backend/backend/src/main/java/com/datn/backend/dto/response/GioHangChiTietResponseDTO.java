package com.datn.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GioHangChiTietResponseDTO {
    private String id;
    private String idChiTietSanPham;
    private String tenSanPham;
    private String image;
    private BigDecimal price; // giá sau giảm
    private BigDecimal originalPrice; // giá gốc
    private Integer phanTramGiam; // phần trăm giảm giá
    private String color;
    private String size;
    private int quantity;
    private Integer soLuongTon; // thông tin tồn kho
}
