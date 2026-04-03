package com.datn.backend.dto.response;

import lombok.*;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Data
public class ChiTietSanPhamResponseDTO {
    private String id;
    private String tenSanPham;
    private String mauSac;
    private String kichThuoc;
    private String maCTSP;
    private String tenCTSP;
    private Integer soLuong;
    private BigDecimal donGia;
    private String ghiChu;
    private Byte trangThai;

    private String chatLieu;
    private String thuongHieu;
    private String xuatXu;

    private String tenAnh;
    private String url;


}