package com.datn.backend.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.datn.backend.entity.ChiTietSanPham;
import lombok.Data;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor      // Tạo constructor rỗng (cần cho các thư viện như Jackson)
@AllArgsConstructor
public class ChiTietSanPham_BanHang_ResponseDTO {
    private String id;
    private String maCTSP;
    private String tenCTSP;
    private String maMauSac;
    private String mauSac;
    private String kichThuoc;
    private Integer soLuong;
    private BigDecimal donGia;
    private BigDecimal giaSauGiam;
    private BigDecimal phanTramGiam;
    private String ghiChu;
    private String chatLieu;
    private String thuongHieu;
    private String xuatXu;
    private String url;
}