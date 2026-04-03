package com.datn.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "thuong_hieu")
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ThuongHieu {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String maThuongHieu;
    @Nationalized
    String tenThuongHieu;
    Byte trangThai;

}
