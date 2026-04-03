package com.datn.backend.dto.request;

import com.datn.backend.entity.ChucVu;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Data
public class NhanVienRequestDTO {
    private String maNhanVien;
    private String tenNhanVien;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate ngaySinh;
    private String cccd;
    private Byte gioiTinh;
    private String email;
    private String sdt;
    private MultipartFile hinhAnh;
    private String diaChi;
    private String matKhau;
    private Byte trangThai;
    private String loginProvider; // Google hoặc Local
    private String chucVu; // ADMIN hoặc NHANVIEN
}
