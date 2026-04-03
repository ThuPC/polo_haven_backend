package com.datn.backend.dto.request;

import com.datn.backend.contants.LoaiGiamGia;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PhieuGiamGia_UpdateRequest {
    String id;
    String tenPhieuGiamGia;
    Long soLuong;
    Long ngayBatDau;
    Long ngayKetThuc;
    Boolean dieuKien;
    Byte yeuCauKhachHang;
    LoaiGiamGia loaiGiamGia;
    Double giaTriGiam;
    Double giaTriToiThieu;
    Double giaTriToiDa;
    Byte trangThai;
}
