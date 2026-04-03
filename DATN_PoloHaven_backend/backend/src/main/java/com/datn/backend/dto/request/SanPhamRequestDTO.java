package com.datn.backend.dto.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SanPhamRequestDTO {
    private String maSanPham;
    private String tenSanPham;
    private String chatLieuId;
    private String xuatXuId;
    private String thuongHieuId;
    private String moTa;
    private Byte trangThai;
}
