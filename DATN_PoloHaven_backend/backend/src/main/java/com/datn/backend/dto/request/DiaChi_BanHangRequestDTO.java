package com.datn.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class DiaChi_BanHangRequestDTO {
    private String id;
    private String tenNguoiNhan;
    private String sdt;
    private Integer provinceId;
    private String thanhPho;
    private Integer districtId;
    private String quanHuyen;
    private String wardCode;
    private String xaPhuong;
    private String soNha;
}