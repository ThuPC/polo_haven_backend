package com.datn.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Entity
@Table(name = "khach_hang_giam_gia")
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class KhachHang_GiamGia {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    @ManyToOne
    @JoinColumn(name = "id_khachHang")
    KhachHang khachHang;
    @ManyToOne
    @JoinColumn(name = "id_phieuGiamGia")
    PhieuGiamGia phieuGiamGia;
    String maPhieu;
    Boolean trangThaiSuDung;
    BigDecimal soTienGiam;
    Byte trangThai;
}
