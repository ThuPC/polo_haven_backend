package com.datn.backend.dto.request;

import com.datn.backend.contants.LoaiGiamGia;
import com.datn.backend.entity.PhieuGiamGia;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PhieuGiamGia_CreationRequest {
    private String tenPhieuGiamGia;
    private String loaiGiamGia;
    private Double giaTriGiam;
    private Double giaTriToiDa;
    private Double giaTriToiThieu;
    private Integer trangThai;
    private Long ngayBatDau;
    private Long ngayKetThuc;
    private String doiTuongApDung;
    private Integer soLuong;
    private List<String> danhSachKhachHangId;
    private Byte yeuCauKhachHang;
    private PhieuGiamGia phieuGiamGia;
    private List<String> idKhachHang;
    private Long diemDoiToiThieu;
}
