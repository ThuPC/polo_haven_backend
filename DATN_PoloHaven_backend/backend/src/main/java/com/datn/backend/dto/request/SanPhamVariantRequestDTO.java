package com.datn.backend.dto.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SanPhamVariantRequestDTO {
    private String sanPhamId;       // ID sản phẩm cha (số ít)
    private String mauSacId;        // ID màu sắc (số ít)
    private String kichCoId;        // ID kích cỡ (số ít)
    private String hinhAnhId;
    private Integer soLuong;
    private Double giaBan;
    private Byte trangThai;
}



