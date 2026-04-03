package com.datn.backend.dto.response.BanHangOnline;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SanPhamVariantDetailDTO {
    private String idMauSac;
    private String tenMauSac;
    private String maMauSac;
    private String idKichThuoc;
    private String size;
    private String imageUrl;
}