package com.datn.backend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "hoa_don")
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class HoaDon {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    @ManyToOne
    @JoinColumn(name = "id_khach_hang")
    KhachHang khachHang;
    @ManyToOne
    @JoinColumn(name = "id_nhan_vien")
    NhanVien nhanVien;
    @ManyToOne
    @JoinColumn(name = "id_khach_hang_giam_gia")
    KhachHang_GiamGia khachHangGiamGia;
    String maHoaDon;
//    String npm;
@JsonFormat(pattern = "dd/MM/yyyy HH:mm", timezone = "Asia/Ho_Chi_Minh")
    LocalDateTime ngayTao;
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm", timezone = "Asia/Ho_Chi_Minh")
    LocalDateTime ngayThanhToan;
    @Nationalized
    String tenKhachHang;
//    String tenNguoiNhan;
    String sdt;
    @Nationalized
    String diaChi;
    BigDecimal tongTien;
    Integer hinhThucNhanHang;
    BigDecimal phiVanChuyen;
    BigDecimal soTienGiam;
    BigDecimal soTienHoan;
    BigDecimal tongTienSauKhiGiam;
    BigDecimal soTienKhachHangThanhToan;
    Boolean thanhToanTruoc;
    Integer loaiHoaDon;
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm", timezone = "Asia/Ho_Chi_Minh")
    LocalDateTime ngayGiaoHang;
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm", timezone = "Asia/Ho_Chi_Minh")
    LocalDateTime ngayMongMuonNhanHang;
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm", timezone = "Asia/Ho_Chi_Minh")
    LocalDateTime ngayDuKienNhanHang;
    String ghiChu;
    Byte trangThai;

    @OneToMany(mappedBy = "hoaDon", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference // tránh đệ quy lặp lại vô hạn cho thg cha
    private List<HoaDonChiTiet> chiTietHoaDonList;





    @Column(name = "email_nguoi_nhan")
    private String emailNguoiNhan; // Lưu email của khách vãng lai

    @Column(name = "confirmation_token")
    private String confirmationToken; // Lưu token xác nhận

    @Column(name = "token_expiry_date")
    private LocalDateTime tokenExpiryDate; // Thời gian token hết hạn
}
