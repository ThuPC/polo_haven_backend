package com.datn.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
public class HoaDonGanDayResponseDTO {
    private UUID id;
    private String maHoaDon;
    private int trangThai;
    private String loaiHoaDon;
    private String tenKhachHang;
    private BigDecimal tongTienTruocGiam;
    private BigDecimal tongTienSauGiam;
}

