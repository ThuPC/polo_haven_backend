package com.datn.backend.dto.response;

import java.math.BigDecimal;

public class ChiTietKhuyenMaiResponse {
    private String id;
    private String khuyenMaiId;
    private String maKMTCT;
    private String tenKhuyenMai;
    private String chiTietSanPhamId;
    private String maCTSP;
    private String tenCTSP;
    private String maSanPhamKhuyenMai;
    private BigDecimal soTienGiam;
    private Byte trangThai;
    public ChiTietKhuyenMaiResponse(String id, String khuyenMaiId, String maKMTCT, String tenKhuyenMai,
                                    String chiTietSanPhamId, String maCTSP, String tenCTSP,
                                    String maSanPhamKhuyenMai, BigDecimal soTienGiam, Byte trangThai) {
        this.id = id;
        this.khuyenMaiId = khuyenMaiId;
        this.maKMTCT = maKMTCT;
        this.tenKhuyenMai = tenKhuyenMai;
        this.chiTietSanPhamId = chiTietSanPhamId;
        this.maCTSP = maCTSP;
        this.tenCTSP = tenCTSP;
        this.maSanPhamKhuyenMai = maSanPhamKhuyenMai;
        this.soTienGiam = soTienGiam;
        this.trangThai = trangThai;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getKhuyenMaiId() { return khuyenMaiId; }
    public void setKhuyenMaiId(String khuyenMaiId) { this.khuyenMaiId = khuyenMaiId; }
    public String getMaKMTCT() { return maKMTCT; }
    public void setMaKMTCT(String maKMTCT) { this.maKMTCT = maKMTCT; }
    public String getTenKhuyenMai() { return tenKhuyenMai; }
    public void setTenKhuyenMai(String tenKhuyenMai) { this.tenKhuyenMai = tenKhuyenMai; }
    public String getChiTietSanPhamId() { return chiTietSanPhamId; }
    public void setChiTietSanPhamId(String chiTietSanPhamId) { this.chiTietSanPhamId = chiTietSanPhamId; }
    public String getMaCTSP() { return maCTSP; }
    public void setMaCTSP(String maCTSP) { this.maCTSP = maCTSP; }
    public String getTenCTSP() { return tenCTSP; }
    public void setTenCTSP(String tenCTSP) { this.tenCTSP = tenCTSP; }
    public String getMaSanPhamKhuyenMai() { return maSanPhamKhuyenMai; }
    public void setMaSanPhamKhuyenMai(String maSanPhamKhuyenMai) { this.maSanPhamKhuyenMai = maSanPhamKhuyenMai; }
    public BigDecimal getSoTienGiam() { return soTienGiam; }
    public void setSoTienGiam(BigDecimal soTienGiam) { this.soTienGiam = soTienGiam; }
    public Byte getTrangThai() { return trangThai; }
    public void setTrangThai(Byte trangThai) { this.trangThai = trangThai; }
}
