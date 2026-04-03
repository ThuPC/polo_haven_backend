package com.datn.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DonTraHangResponse {
    private String id;
    private String maDonTraHang;
    private String maHoaDon;
    private String tenKhachHang;
    private String email;
    private String sdt;
    private LocalDateTime ngayTao;
    private String lyDo;
    private String ghiChu;
    private String ghiChuAdmin;
    private Byte trangThai;
    private List<ChiTietTraHangResponse> chiTietTraHangList;
    private List<MediaTraHangResponse> mediaTraHangList;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChiTietTraHangResponse {
        private String id;
        private String tenSanPham;
        private String mauSac;
        private String kichThuoc;
        private Integer soLuong;
        private String lyDo;
        private String anhSanPham;
        private java.math.BigDecimal donGia;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MediaTraHangResponse {
        private String id;
        private String duongDan;
        private Byte loaiMedia;
    }
}