package com.datn.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "mau_sac")
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MauSac {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String maMau;
    @Nationalized
    String tenMau;
    String maMauSac;
    Byte trangThai;

}
