package com.datn.backend.service.Hoa_don;




import com.datn.backend.dto.response.ChiTietSanPhamHoaDonResponseDto;
import com.datn.backend.entity.ChiTietSanPham;

import java.util.List;

public interface ChiTietSanPhamHoaDonService {
    List<ChiTietSanPham> getListChiTietSanPham();
    List<ChiTietSanPhamHoaDonResponseDto> getDanhSachSanPham(String id);

}
