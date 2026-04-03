package com.datn.backend.dto.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SanPhamVariantsRequestDTO {
    private String sanPhamId;
    private String tenSanPham;
    private String chatLieuIds;
    private String xuatXuIds;
    private String thuongHieuIds;
//    private List<String> mauSacList;
//    private List<String> kichCoList;

    private List<SanPhamVariantRequestDTO> variants;

    // Thêm các trường này nếu muốn dùng chung cho tất cả biến thể
    private Double giaBan;
    private Integer soLuong;
    private Byte trangThai;
}


