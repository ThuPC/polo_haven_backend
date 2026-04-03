package com.datn.backend.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HoaDonResponseDTO {
    String id;
    String maHoaDon;
    String tenKhachHang;
    String sdt;
    String diaChi;
    String tenNhanVien; // khi bảng hóa đơn không có mà ta thêm để có thể hiển thị (thì phải map sang mapper)
    BigDecimal tongTien;
    BigDecimal phiVanChuyen;
    BigDecimal soTienGiam;
    BigDecimal soTienHoan;
    BigDecimal tongTienSauKhiGiam;
    BigDecimal soTienKhachHangThanhToan;
    Boolean thanhToanTruoc;
    Integer loaiHoaDon;
    Integer hinhThucNhanHang; // mới thêm
    LocalDateTime ngayTao;
    LocalDateTime ngayThanhToan;
    LocalDateTime ngayGiaoHang;
    LocalDateTime ngayMongMuonNhanHang;
    LocalDateTime ngayDuKienNhanHang;
    String ghiChu;
    Byte trangThai;
    String tenHTTT; // thêm dòng này vào DTO

    List<HoaDonChiTietResponseDTO> chiTietHoaDonList;
}
