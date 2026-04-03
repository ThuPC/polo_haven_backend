package com.datn.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "dia_chi")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DiaChi {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String maDiaChi;
    @Nationalized
    String thanhPho;
    @Nationalized
    String quanHuyen;
    @Nationalized
    String xaPhuong;
    @Nationalized
    String soNha;
    @Nationalized
    String diaChiChiTiet;
    @Nationalized
    String tenNguoiNhan;
    String sdt;
    Byte trangThai;
    @Column(name = "is_default", columnDefinition = "TINYINT")
    private Boolean isDefault;
    @ManyToOne
    @JsonIgnore(value = true)
    @JoinColumn(name = "id_khach_hang")
    KhachHang khachHang;
    // Các trường mới cho API GHN
    @Column(name = "province_id")
    Integer provinceId;
    @Column(name = "district_id")
    Integer districtId;
    @Column(name = "ward_code")
    String wardCode;
}