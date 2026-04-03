package com.datn.backend.dto.response;

import com.datn.backend.dto.request.DiaChiRequestDTO;
import com.datn.backend.dto.request.DiaChi_BanHangRequestDTO;
import lombok.Data;

import java.util.List;

@Data
public class KhachHang_BanHang_ResponseDTO {
    private String id;
    private String maKhachHang;
    private String tenKhachHang;
    private Byte gioiTinh;
    private String sdt;
    private String email;
    private Byte trangThai;
    private String hinhAnh;
    private List<DiaChi_BanHangRequestDTO> diaChis;

}

