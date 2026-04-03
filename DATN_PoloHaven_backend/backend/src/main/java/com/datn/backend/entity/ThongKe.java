package com.datn.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.*;

@Entity
@Table(name = "thong_ke")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ThongKe {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(name = "doanh_so_hom_nay", precision = 18, scale = 2)
    BigDecimal doanhSoHomNay;

    @Column(name = "doanh_so_hom_qua", precision = 18, scale = 2)
    BigDecimal doanhSoHomQua;

    @Column(name = "ti_le_hom_nay", precision = 5, scale = 2)
    BigDecimal tiLeHomNay;

    @Column(name = "doanh_so_tuan_nay", precision = 18, scale = 2)
    BigDecimal doanhSoTuanNay;

    @Column(name = "doanh_so_tuan_truoc", precision = 18, scale = 2)
    BigDecimal doanhSoTuanTruoc;

    @Column(name = "ti_le_tuan", precision = 5, scale = 2)
    BigDecimal tiLeTuan;

    @Column(name = "doanh_so_thang_nay", precision = 18, scale = 2)
    BigDecimal doanhSoThangNay;

    @Column(name = "doanh_so_thang_truoc", precision = 18, scale = 2)
    BigDecimal doanhSoThangTruoc;

    @Column(name = "ti_le_thang", precision = 5, scale = 2)
    BigDecimal tiLeThang;

    @Column(name = "start_date")
    LocalDate startDate;

    @Column(name = "end_date")
    LocalDate endDate;
}

