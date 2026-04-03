package com.datn.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "chuc_vu")
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChucVu {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String maChucVu;
    @Nationalized
    String tenChucVu;
    Byte trangThai;
}
