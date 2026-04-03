package com.datn.backend.service.thong_ke;

import com.datn.backend.dto.response.SanPhamThinhHanhResponseDTO;
import com.datn.backend.dto.response.ThongKeResponseDTO;
import com.datn.backend.entity.ThongKe;
import com.datn.backend.mapper.ThongKeMapper;
import com.datn.backend.repository.ThongKeRepo;
import com.datn.backend.repository.hoadon_repo.HoaDonChiTietRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ThongKeServiceImpl implements ThongKeService {

    private final ThongKeRepo thongKeRepository;
    private final ThongKeMapper thongKeMapper;
    private final HoaDonChiTietRepo hoaDonChiTietRepository;

    @Override
    public ThongKeResponseDTO getThongKeHienTai() {
        BigDecimal doanhSoHomNay = thongKeRepository.getDoanhSoHomNay();
        BigDecimal doanhSoHomQua = thongKeRepository.getDoanhSoHomQua();

        BigDecimal doanhSoTuanNay = thongKeRepository.getDoanhSoTuanNay();
        BigDecimal doanhSoTuanTruoc = thongKeRepository.getDoanhSoTuanTruoc();

        BigDecimal doanhSoThangNay = thongKeRepository.getDoanhSoThangNay();
        BigDecimal doanhSoThangTruoc = thongKeRepository.getDoanhSoThangTruoc();

        BigDecimal tiLeHomNay = calculatePercentageChange(doanhSoHomNay, doanhSoHomQua);
        BigDecimal tiLeTuan = calculatePercentageChange(doanhSoTuanNay, doanhSoTuanTruoc);
        BigDecimal tiLeThang = calculatePercentageChange(doanhSoThangNay, doanhSoThangTruoc);

        System.out.println("Doanh số hôm nay: " + doanhSoHomNay);

        ThongKe thongKe = ThongKe.builder()
                .doanhSoHomNay(doanhSoHomNay)
                .doanhSoHomQua(doanhSoHomQua)
                .tiLeHomNay(tiLeHomNay)

                .doanhSoTuanNay(doanhSoTuanNay)
                .doanhSoTuanTruoc(doanhSoTuanTruoc)
                .tiLeTuan(tiLeTuan)

                .doanhSoThangNay(doanhSoThangNay)
                .doanhSoThangTruoc(doanhSoThangTruoc)
                .tiLeThang(tiLeThang)

                .startDate(LocalDate.now().withDayOfMonth(1))
                .endDate(LocalDate.now())
                .build();

        return thongKeMapper.toResponseDTO(thongKe);
    }

    private BigDecimal calculatePercentageChange(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.valueOf(100);
        }
        return current.subtract(previous)
                .multiply(BigDecimal.valueOf(100))
                .divide(previous, 2, RoundingMode.HALF_UP);
    }

    @Override
    public List<SanPhamThinhHanhResponseDTO> getTopSanPhamBanChay(Pageable pageable) {
        return hoaDonChiTietRepository.getTopSanPhamBanChay(pageable); // dùng pageable từ controller truyền vào
    }


    private static final Map<Byte, String> STATUS_LABELS = new HashMap<>();
    static {
        STATUS_LABELS.put((byte) 0, "Chờ xác nhận");
        STATUS_LABELS.put((byte) 1, "Đã xác nhận");
        STATUS_LABELS.put((byte) 2, "Chờ vận chuyển");
        STATUS_LABELS.put((byte) 3, "Đang vận chuyển");
        STATUS_LABELS.put((byte) 4, "Đã giao hàng");
        STATUS_LABELS.put((byte) 5, "Hoàn thành");
        STATUS_LABELS.put((byte) 6, "Hủy");
        STATUS_LABELS.put((byte) 7, "Trả hàng");
        STATUS_LABELS.put((byte) 8, "Đang trả hàng");
        STATUS_LABELS.put((byte) 9, "Trả hàng thành công");
        STATUS_LABELS.put((byte) 10, "Từ chối trả hàng");
    }

    @Override
    public Map<String, Object> getBarChartData(String timeframe, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        List<Map<String, Object>> data;
        switch (timeframe.toUpperCase()) {
            case "DAY":
                data = thongKeRepository.findBarChartDataByDay(startDateTime, endDateTime);
                break;
            case "WEEK":
                data = thongKeRepository.findBarChartDataByWeek(startDateTime, endDateTime);
                break;
            case "MONTH":
                data = thongKeRepository.findBarChartDataByMonth(startDateTime, endDateTime);
                break;
            default:
                throw new IllegalArgumentException("Invalid timeframe: " + timeframe);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("labels", data.stream().map(row -> row.get("label")).toList());
        result.put("quantities", data.stream().map(row -> row.get("quantity")).toList());
        result.put("revenues", data.stream().map(row -> row.get("revenue")).toList());
        return result;
    }

    @Override
    public Map<String, Object> getPieChartData(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        List<Map<String, Object>> data = thongKeRepository.findPieChartData(startDateTime, endDateTime);
        Map<String, Object> result = new HashMap<>();
        result.put("statuses", data.stream().map(row -> row.get("status")).toList());
        result.put("counts", data.stream().map(row -> row.get("count")).toList());
        return result;
    }
}


