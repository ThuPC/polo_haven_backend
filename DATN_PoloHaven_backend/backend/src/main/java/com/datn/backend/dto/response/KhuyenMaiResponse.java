package com.datn.backend.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class KhuyenMaiResponse {
    String id;
    String maKMTCT;
    String tenKhuyenMai;
    String moTa;
    BigDecimal phanTramGiam;
    LocalDate ngayBatDau;
    LocalDate ngayKetThuc;
    LocalDateTime ngayTao;
    Byte trangThai;
}
