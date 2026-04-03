package com.datn.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Data
@Builder
public class DonHang_Response {
    private String idHoaDon;
    private String maHoaDon;
    private LocalDateTime ngayTao;
    private String tenKhachHang;
    private BigDecimal tongTienSauKhiGiam;
    private String trangThai;
}
