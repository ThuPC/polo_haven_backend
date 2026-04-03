package com.datn.backend.dto.response;

import lombok.Data;

@Data
public class SanPhamVariantResponseDTO {
    private String id;
    private String sanPhamId;
    private String tenSanPham;
    private String mauSac;
    private String kichCo;
    private Integer soLuong;
    private Double giaBan;
    private Byte trangThai;
}
