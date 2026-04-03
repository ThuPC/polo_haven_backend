package com.datn.backend.dto.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class KhuyenMaiRequest {
    String maKMTCT;
    String tenKhuyenMai;
    String moTa;
    BigDecimal phanTramGiam;
    LocalDate ngayBatDau;
    LocalDate ngayKetThuc;
    Byte trangThai;
}
