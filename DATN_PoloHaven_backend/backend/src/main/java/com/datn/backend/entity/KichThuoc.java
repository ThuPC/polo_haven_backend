package com.datn.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Entity
@Table(name = "kich_thuoc")
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class KichThuoc {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String maKichThuoc;
    @Column(columnDefinition = "VARCHAR(50)")
    String size;
    BigDecimal chieuDaiVai;
    BigDecimal chieuDaiThan;
    BigDecimal vongNguc;
    BigDecimal vongEo;
    BigDecimal doDaiTay;
    Byte trangThai;

}
