package com.datn.backend.dto.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.List;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KhachHangRequestDTO {
    private String tenKhachHang;
    private Byte gioiTinh;
    private String sdt;
    private String email;
    private String matKhau;
    private String loginProvider;
    private Byte trangThai;
    private MultipartFile hinhAnh;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate ngaySinh;
    private List<DiaChiRequestDTO> diaChis;
    private String diaChi; // Địa chỉ đơn giản từ Excel import

    public void setDiaChis(List<DiaChiRequestDTO> diaChis) {
        if (diaChis != null && diaChis.size() > 1) {
            throw new IllegalArgumentException("Chỉ được gửi một địa chỉ");
        }
        this.diaChis = diaChis;
    }
}

