package com.datn.backend.entity;

import com.datn.backend.contants.LoaiGiamGia;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "phieu_giam_gia")
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PhieuGiamGia {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String maPhieuGiamGia;
    @Nationalized
    String tenPhieuGiamGia;
    Long ngayBatDau;
    Long ngayKetThuc;
    Byte yeuCauKhachHang;
    LoaiGiamGia loaiGiamGia;
    Double giaTriGiam;
    Double giaTriToiThieu;
    Double giaTriToiDa;
    Byte trangThai;
    Long ngaySua;
    Long ngayTao;
    Byte congKhai;
    // áp dụng cho phiếu đổi điểm
    Long diemCanDoi;
}