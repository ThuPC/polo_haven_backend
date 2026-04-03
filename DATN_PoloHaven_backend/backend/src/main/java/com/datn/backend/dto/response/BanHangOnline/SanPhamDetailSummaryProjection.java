package com.datn.backend.dto.response.BanHangOnline;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;



public interface SanPhamDetailSummaryProjection {
    String getId();
    String getTenSanPham();
    String getMoTa();
    String getThuongHieu();
    String getChatLieu();
    String getXuatXu();
    String getKhoangGia();
    Long getSoLuongTon();
    BigDecimal getPhanTramGiam();
    Long getSoLuongDaBan();
}
