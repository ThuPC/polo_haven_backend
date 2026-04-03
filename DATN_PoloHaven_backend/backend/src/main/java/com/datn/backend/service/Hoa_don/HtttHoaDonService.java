package com.datn.backend.service.Hoa_don;



import com.datn.backend.dto.response.LichSuThanhToanDto;
import com.datn.backend.entity.HTTT_Hoa_Don;

import java.util.List;

public interface HtttHoaDonService {

    List<HTTT_Hoa_Don> getListHtttHoaDon();
    List<LichSuThanhToanDto> getLichSuThanhToan (String id);
}
