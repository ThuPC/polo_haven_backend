package com.datn.backend.mapper;

import com.datn.backend.dto.response.HoaDonGanDayResponseDTO;
import com.datn.backend.dto.response.HoaDonResponseDTO;
import com.datn.backend.entity.HoaDon;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface HoaDonMapper {
    @Mapping(source = "nhanVien.tenNhanVien", target = "tenNhanVien")
        // thêm dòng này để có thể lấy dữu liệu ở bảng khác khi k có trong hóa đơn
    HoaDonResponseDTO toResponseDTO(HoaDon hoaDon);

    // Mapping HoaDon sang HoaDonGanDayResponseDTO
    @Mapping(source = "id", target = "id")
    @Mapping(source = "maHoaDon", target = "maHoaDon")
    @Mapping(source = "trangThai", target = "trangThai")
    @Mapping(source = "loaiHoaDon", target = "loaiHoaDon")
    @Mapping(source = "khachHang.tenKhachHang", target = "tenKhachHang")
    @Mapping(source = "tongTien", target = "tongTienTruocGiam")
    @Mapping(source = "tongTienSauKhiGiam", target = "tongTienSauGiam")
    HoaDonGanDayResponseDTO toHoaDonGanDayResponseDTO(HoaDon hoaDon);

}
