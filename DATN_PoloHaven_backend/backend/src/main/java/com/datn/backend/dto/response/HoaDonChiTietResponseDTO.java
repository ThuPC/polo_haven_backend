package com.datn.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HoaDonChiTietResponseDTO {
    String id;
    Integer soLuong;
    BigDecimal giaBan;
    Byte trangThai;
    String tenSanPham; // có thể lấy từ ChiTietSanPham
    String mauSac;       // thêm mới
    String kichThuoc;    // thay đổi từ Integer thành String
    String hinhAnh;      // thêm mới (có thể là URL hoặc tên file ảnh)
    String chiTietSanPhamId; // thêm ID của ChiTietSanPham để dùng cho trả hàng
}
