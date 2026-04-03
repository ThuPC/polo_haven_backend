package com.datn.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "san_pham")
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SanPham {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String maSanPham;
    @Nationalized
    String tenSanPham;
    @ManyToOne
    @JoinColumn(name = "id_chat_lieu")
    ChatLieu chatLieu;
    @ManyToOne
    @JoinColumn(name = "id_xuat_xu")
    XuatXu xuatXu;
    @ManyToOne
    @JoinColumn(name = "id_thuong_hieu")
    ThuongHieu thuongHieu;
    @Nationalized
    String moTa;
    Byte trangThai;
    Long ngayTao;
}
