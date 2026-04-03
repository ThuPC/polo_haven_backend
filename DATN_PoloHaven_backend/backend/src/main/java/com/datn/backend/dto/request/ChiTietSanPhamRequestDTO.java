package com.datn.backend.dto.request;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ChiTietSanPhamRequestDTO {
    private String idSanPham;
    private String idMauSac;
    private String idKichThuoc;
    private String idHinhAnh; // optional
    private String maCTSP;
    private String tenCTSP;
    private Integer soLuong;
    private BigDecimal donGia;
    private String ghiChu;
    private Byte trangThai;

    private String tenAnh;
    private String url;
}
