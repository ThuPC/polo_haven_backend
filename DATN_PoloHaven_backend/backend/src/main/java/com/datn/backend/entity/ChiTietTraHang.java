package com.datn.backend.entity;

import com.datn.backend.enums.TinhTrangHangTraEnum;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Entity
@Table(name = "chi_tiet_tra_hang")
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChiTietTraHang {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    
    @ManyToOne
    @JoinColumn(name = "id_don_tra_hang")
    @JsonBackReference
    DonTraHang donTraHang;
    
    @ManyToOne
    @JoinColumn(name = "id_chi_tiet_san_pham")
    ChiTietSanPham chiTietSanPham;
    
    @ManyToOne
    @JoinColumn(name = "id_hoa_don_chi_tiet")
    HoaDonChiTiet hoaDonChiTiet;
    
    Integer soLuong;
    
    BigDecimal giaTien;
    
    String lyDo;
    
    // Tình trạng hàng trả: 0 - Nguyên vẹn, 1 - Bị lỗi
    @Column(name = "tinh_trang_hang")
    Byte tinhTrangHang;
    
    /**
     * Kiểm tra có cộng lại số lượng không dựa trên tình trạng hàng
     */
    public boolean isCongLaiSoLuong() {
        if (tinhTrangHang == null) {
            return false; // Mặc định không cộng nếu chưa xác định
        }
        TinhTrangHangTraEnum tinhTrang = TinhTrangHangTraEnum.fromValue(tinhTrangHang);
        return tinhTrang.isCongLaiSoLuong();
    }
}