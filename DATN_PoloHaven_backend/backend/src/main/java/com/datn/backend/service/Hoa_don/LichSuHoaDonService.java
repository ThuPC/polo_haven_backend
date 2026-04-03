package com.datn.backend.service.Hoa_don;




import com.datn.backend.dto.response.LichSuHoaDonResponseDto;
import com.datn.backend.entity.LichSuHoaDon;

import java.util.List;

public interface LichSuHoaDonService {

    List<LichSuHoaDon> getListLichSuHoaDon();
    List<LichSuHoaDonResponseDto> getListLichSuHoaDonDto(String id); // Thay đổi từ getLichSuHoaDonById
    LichSuHoaDonResponseDto getLichSuHoaDonId(String hoaDonId);

}
