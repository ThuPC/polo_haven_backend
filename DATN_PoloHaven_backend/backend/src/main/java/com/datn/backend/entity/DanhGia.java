package com.datn.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "danh_gia")
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DanhGia {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    @ManyToOne
    @JoinColumn(name = "id_khachHang")
    KhachHang khachHang;
    @ManyToOne
    @JoinColumn(name = "id_sanPham")
    SanPham sanPham;
    Byte soSao;
    @Nationalized
    String binhLuan;
    Long NgayDanhGia;
    Byte trangThai;

}
