package com.datn.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LichSuHoaDonResponseDto {
    private String id;
    private String maGiaoDich;
    private BigDecimal tongTien;
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm", timezone = "Asia/Ho_Chi_Minh")
    private LocalDateTime ngayThanhToan;
    private String tenHinhThuc;
    private Byte trangThai;
    private String tenNhanVien;
    private String hanhDongNguoiThaoTac;
    private String ghiChu;
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm", timezone = "Asia/Ho_Chi_Minh")
    private LocalDateTime ngayCapNhat;
}
