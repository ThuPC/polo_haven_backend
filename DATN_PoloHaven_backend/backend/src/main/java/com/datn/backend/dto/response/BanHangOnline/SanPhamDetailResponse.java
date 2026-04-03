package com.datn.backend.dto.response.BanHangOnline;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;


@Data
public class SanPhamDetailResponse {
    private String id;
    private String tenSanPham;
    private String maSanPham;
    private String moTa;
    private String thuongHieu;
    private String chatLieu;
    private String xuatXu;
    private String khoangGia;
    private List<String> imageList;
    private List<MauSacResponse> mauSacList;
    private List<KichThuocResponse> kichThuocList;
    private Long soLuongTon;
    private BigDecimal phanTramGiam;
    private Long soLuongDaBan;
}
