package com.datn.backend.dto.response;

import com.datn.backend.contants.LoaiGiamGia;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PhieuGiamGiaResponseDTO {
    String id;
    String maPhieuGiamGia;
    String tenPhieuGiamGia;
    Long soLuong;
    Long daSuDung;
    Long ngayBatDau;
    Long ngayKetThuc;
    Byte yeuCauKhachHang;
    LoaiGiamGia loaiGiamGia;// giảm theo phần trăm hay số tieenf
    Double giaTriGiam;
    Double giaTriToiThieu;
    Double giaTriToiDa;
    Byte congKhai;
    Byte trangThai;
    Long diemCanDoi;

}
