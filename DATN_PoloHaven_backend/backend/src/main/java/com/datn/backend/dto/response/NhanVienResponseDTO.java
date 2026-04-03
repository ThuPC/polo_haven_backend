package com.datn.backend.dto.response;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Data
public class NhanVienResponseDTO {
    private String id;
    private String cccd;
    private String maNhanVien;
    private String tenNhanVien;
    private LocalDate ngaySinh;
    private Byte gioiTinh;
    private String email;
    private String sdt;
    private String hinhAnh;
    private String diaChi;
    private Long ngayTao;
    private Byte trangThai;
    private String loginProvider;
    private String chucVu;
}
