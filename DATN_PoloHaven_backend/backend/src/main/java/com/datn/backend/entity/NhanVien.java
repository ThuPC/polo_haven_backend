package com.datn.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDate;

@Entity
@Table(name = "nhan_vien")
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NhanVien {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String maNhanVien;
    @Nationalized
    String tenNhanVien;
    Byte gioiTinh;
    LocalDate ngaySinh;
    String email;
    String cccd;
    String sdt;
    String matKhau;
    String hinhAnh;
    Double luong;
    String diaChi;
    Byte trangThai;


    @ManyToOne
    @JoinColumn(name = "id_chuc_vu")
    ChucVu chucVu;
    Long ngayTao;
    Long ngaySua;
    String loginProvider;
}
