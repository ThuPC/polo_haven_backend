package com.datn.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@NoArgsConstructor
public class SanPhamApDungKhuyenMaiResponse {
    private String id;
    private String maSanPhamKhuyenMai;
    private String tenKhuyenMai;
    private String tenCTSP;
    private BigDecimal donGia;
    private Byte trangThai;
    public SanPhamApDungKhuyenMaiResponse(String id, String maSanPhamKhuyenMai,
                                          String tenKhuyenMai, String tenCTSP,
                                          BigDecimal donGia, Byte trangThai) {
        this.id = id;
        this.maSanPhamKhuyenMai = maSanPhamKhuyenMai;
        this.tenKhuyenMai = tenKhuyenMai;
        this.tenCTSP = tenCTSP;
        this.donGia = donGia;
        this.trangThai = trangThai;
    }
}
