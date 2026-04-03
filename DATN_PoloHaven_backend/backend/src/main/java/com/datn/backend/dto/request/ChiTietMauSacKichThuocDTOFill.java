package com.datn.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChiTietMauSacKichThuocDTOFill {
    private String idSanPham;
    private String idMauSac;
    private String tenMauSac;
    private String maMauSac;
    private String idKichThuoc;
    private String tenKichThuoc;
    private String idChatLieu;
    private String tenChatLieu;
    private BigDecimal donGia;
    private BigDecimal giaSauGiam;
    private String tenAnh;
}