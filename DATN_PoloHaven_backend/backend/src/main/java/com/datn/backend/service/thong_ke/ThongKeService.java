package com.datn.backend.service.thong_ke;

import com.datn.backend.dto.response.SanPhamThinhHanhResponseDTO;
import com.datn.backend.dto.response.ThongKeResponseDTO;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface ThongKeService {
    ThongKeResponseDTO getThongKeHienTai();

    //    List<SanPhamThinhHanhResponseDTO> getTopSanPhamThinhHanh();
    List<SanPhamThinhHanhResponseDTO> getTopSanPhamBanChay(Pageable pageable);

    Map<String, Object> getBarChartData(String timeframe, LocalDateTime startDateTime, LocalDateTime endDateTime);
    Map<String, Object> getPieChartData(LocalDateTime startDateTime, LocalDateTime endDateTime);
}
