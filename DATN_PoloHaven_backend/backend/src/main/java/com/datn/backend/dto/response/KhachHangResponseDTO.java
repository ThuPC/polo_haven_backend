package com.datn.backend.dto.response;
import com.datn.backend.dto.request.DiaChiRequestDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.List;
import java.time.LocalDate;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KhachHangResponseDTO {
    private String id;
    private String maKhachHang;
    private String tenKhachHang;
    private Byte gioiTinh;
    private Byte capDo;
    private String sdt;
    private String email;
    private String loginProvider;
    private Byte trangThai;
    private String hinhAnh;
    private Long ngayTao;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate ngaySinh;
    private List<DiaChiResponseDTO> diaChis;
}

