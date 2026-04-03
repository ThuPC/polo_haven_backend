package com.datn.backend.dto.response.BanHangOnline;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;


@Data
public class SanPhamResponse {
    private String id;
    private String tenSanPham;
    private String thuongHieu;
    private String chatLieu;
    private String xuatXu;
    private String khoangGia;
    private List<String> imageList;
    private List<MauSacResponse> mauSacList;
    private List<KichThuocResponse> kichThuocList;
    private List<ChatLieuResponse> chatLieuList;
    private Integer soLuongTon;
    private BigDecimal phanTramGiam;
}
