package com.datn.backend.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SanPhamResponseDTO {
    String id;
    String maSanPham;
    String tenSanPham;
    String tenChatLieu;
    String tenXuatXu;
    String tenThuongHieu;
    String moTa;
    Byte trangThai;
    Long soLuongChiTiet;
    BigDecimal giaThapNhat;
    BigDecimal giaCaoNhat;

    public SanPhamResponseDTO(String id, String maSanPham, String tenSanPham,
                              String tenChatLieu, String tenXuatXu, String tenThuongHieu,
                              String moTa, Byte trangThai, Long soLuongChiTiet,
                              BigDecimal giaThapNhat, BigDecimal giaCaoNhat) {
        this.id = id;
        this.maSanPham = maSanPham;
        this.tenSanPham = tenSanPham;
        this.tenChatLieu = tenChatLieu;
        this.tenXuatXu = tenXuatXu;
        this.tenThuongHieu = tenThuongHieu;
        this.moTa = moTa;
        this.trangThai = trangThai;
        this.soLuongChiTiet = soLuongChiTiet;
        this.giaThapNhat = giaThapNhat;
        this.giaCaoNhat = giaCaoNhat;
    }
}

