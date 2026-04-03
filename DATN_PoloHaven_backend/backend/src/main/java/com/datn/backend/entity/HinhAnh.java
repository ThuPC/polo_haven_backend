package com.datn.backend.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "hinh_anh")
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class HinhAnh {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String maAnh;
    @Nationalized
    String tenAnh;
    Byte trangThai;
    @Column(unique = true)
    private String hash;

}
