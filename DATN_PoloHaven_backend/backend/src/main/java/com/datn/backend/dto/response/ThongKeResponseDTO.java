package com.datn.backend.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThongKeResponseDTO {

    private UUID id;
    private String tenSanPham;
    private String tenAnh;
    private Long soLuongBan;

    private BigDecimal doanhSoHomNay;
    private BigDecimal doanhSoHomQua;
    private BigDecimal tiLeHomNay;

    private BigDecimal doanhSoTuanNay;
    private BigDecimal doanhSoTuanTruoc;
    private BigDecimal tiLeTuan;

    private BigDecimal doanhSoThangNay;
    private BigDecimal doanhSoThangTruoc;
    private BigDecimal tiLeThang;

    private LocalDate startDate;
    private LocalDate endDate;
}
