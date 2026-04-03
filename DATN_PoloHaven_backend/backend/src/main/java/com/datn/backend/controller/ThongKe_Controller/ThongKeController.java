package com.datn.backend.controller.ThongKe_Controller;

import com.datn.backend.dto.response.SanPhamThinhHanhResponseDTO;
import com.datn.backend.dto.response.ThongKeResponseDTO;
import com.datn.backend.entity.ThongKe;
import com.datn.backend.mapper.ThongKeMapper;
import com.datn.backend.service.thong_ke.ThongKeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/thong-ke")
public class ThongKeController {

    private final ThongKeService thongKeService;

    @GetMapping("/hien-tai")
    public ResponseEntity<ThongKeResponseDTO> getThongKeHienTai() {
        return ResponseEntity.ok(thongKeService.getThongKeHienTai());
    }

//    @GetMapping("/san-pham-thinh-hanh")
//    public ResponseEntity<List<SanPhamThinhHanhResponseDTO>> getSanPhamThinhHanh() {
//        return ResponseEntity.ok(thongKeService.getTopSanPhamThinhHanh());
//    }

    @GetMapping("/san-pham-thinh-hanh")
    public ResponseEntity<List<SanPhamThinhHanhResponseDTO>> getTopSanPhamBanChay(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(thongKeService.getTopSanPhamBanChay(pageable));
    }

    @GetMapping("/bar-chart")
    public Map<String, Object> getBarChartData(
            @RequestParam String timeframe,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return thongKeService.getBarChartData(timeframe, startDate.atStartOfDay(), endDate.atTime(23, 59, 59));
    }

    @GetMapping("/pie-chart")
    public Map<String, Object> getPieChartData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return thongKeService.getPieChartData(startDate.atStartOfDay(), endDate.atTime(23, 59, 59));
    }
}

