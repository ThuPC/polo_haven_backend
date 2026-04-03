package com.datn.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class DiaChiRequestDTO {
    private String maDiaChi;
    private String thanhPho;
    private String quanHuyen;
    private String xaPhuong;
    private String soNha;
    private String diaChiChiTiet;
    private String tenNguoiNhan;
    private String sdt;
    private Byte trangThai;
    private Boolean isDefault;
    private String khachHangId;
    // Các trường mới cho API GHN
    private Integer provinceId; // ID tỉnh/thành phố từ GHN
    private Integer districtId; // ID quận/huyện từ GHN
    private String wardCode; // Mã phường/xã từ GHN
}