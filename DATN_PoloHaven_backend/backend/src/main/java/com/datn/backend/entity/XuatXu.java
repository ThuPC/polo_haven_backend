package com.datn.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "xuat_xu")
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class XuatXu {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String maXuatXu;
    @Nationalized
    String noiXuatXu;
    Byte trangThai;

}
