package com.datn.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "khach_hang")
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class KhachHang {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String maKhachHang;
    @Nationalized
    String tenKhachHang;
    Byte gioiTinh;
    Byte capDo;
    String sdt;
    String email;
    String matKhau;
    String loginProvider;
    String hinhAnh;
    Byte trangThai;
    Long ngayTao;
    @Column(name = "ngay_sinh")
    private LocalDate ngaySinh;
    
    @Column(name = "otp_change_password")
    private String otpChangePassword;
    
    @Column(name = "otp_expiry")
    private Long otpExpiry;
    @OneToMany(mappedBy = "khachHang", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DiaChi> diaChis = new ArrayList<>();
}
