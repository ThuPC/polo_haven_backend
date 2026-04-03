package com.datn.backend.dto.request;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@ToString
public class DonHang_Request {
    private String idKhachHang;
    private String emailNhanVien;
    private String emailNguoiNhan;
    private String ghiChuHoaDon;
    private Integer hinhThucNhanHang;
    private byte hinhThucThanhToan;
    private Integer loaiHoaDon;
    private String addressId;    // tam thoi ko dung
    private DiaChi_BanHangRequestDTO diaChiNhanHang;
    private BigDecimal phiShip;
    private String idPhieuGiamGia;
    private BigDecimal giaGoc;
    private BigDecimal giaGiam;
    BigDecimal soTienHoan;
    BigDecimal soTienKhachHangThanhToan;
    Boolean thanhToanTruoc;
    private BigDecimal tongTienSauKhiGiam;
    private List<SanPhamRequest> danhSachSanPham;

    @Data
    @Builder
    public static class SanPhamRequest {
        private String id;
        private Integer soLuong;
        private BigDecimal giaGoc;
        private BigDecimal giaSauKhiGiam;
    }
}
