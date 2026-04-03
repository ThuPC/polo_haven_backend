package com.datn.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LichSuThanhToanDto {
    String maGiaoDich;
    BigDecimal soTienThanhToan;
    LocalDateTime thoiGian;
    String phuongThucThanhToan;
}
