package com.datn.backend.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;


public interface LichSuSuDung_VoucherProjection {
    String getId();
    String getMaPhieu();
    String getTenKhachHang();
    BigDecimal getSoTienGiam();
    String getMaHoaDon();
    LocalDateTime getNgayTao(); // Spring sẽ tự động chuyển đổi kiểu khi dùng interface
}

