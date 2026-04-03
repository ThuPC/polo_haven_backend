package com.datn.backend.controller.HoaDon_Controller;


import com.datn.backend.dto.response.ChiTietSanPhamHoaDonResponseDto;
import com.datn.backend.dto.response.HoaDonGanDayResponseDTO;
import com.datn.backend.dto.response.HoaDonResponseDTO;
import com.datn.backend.dto.response.LichSuHoaDonResponseDto;
import com.datn.backend.entity.HoaDon;
import com.datn.backend.service.Hoa_don.*;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/hoa-don")
public class HoaDonController {

    @Autowired
    private HoaDonService hoaDonService;
    
    @Autowired
    private LichSuHoaDonService lichSuHoaDonService;

    @Autowired
    private ChiTietSanPhamHoaDonService chiTietSanPhamService;


    @GetMapping("/hien-thi")
    public Page<HoaDonResponseDTO> hienThi(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String trangThai,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(required = false) Integer loaiHoaDon,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return hoaDonService.searchHoaDon(keyword, trangThai, fromDate, toDate, loaiHoaDon, pageable);
    }

    @GetMapping("/thong-ke-trang-thai")
    public ResponseEntity<Map<Integer, Long>> thongKeTrangThaiHoaDon() {
        return ResponseEntity.ok(hoaDonService.thongKeSoLuongHoaDonTheoTrangThai());
    }


// upidate

    @GetMapping("/detail/{id}")
    public ResponseEntity<Map<String, Object>> detail(@PathVariable("id") String id) {
        HoaDon hoaDon = hoaDonService.detailHoaDon(id);
        if (hoaDon == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Không tìm thấy hóa đơn với ID: " + id));
        }
        List<LichSuHoaDonResponseDto> listLichSuHoaDonDto = lichSuHoaDonService.getListLichSuHoaDonDto(id);
        List<ChiTietSanPhamHoaDonResponseDto> listChiTietSanPham = chiTietSanPhamService.getDanhSachSanPham(id);
        System.out.println("cmmmm"+hoaDon);
        Map<String, Object> response = new HashMap<>();
        response.put("hoaDon", hoaDon);
        response.put("listLichSuHoaDonDto", listLichSuHoaDonDto);
        response.put("listChiTietSanPham", listChiTietSanPham);


        return ResponseEntity.ok(response);
    }

    @GetMapping("/print/{id}")
    public ResponseEntity<byte[]> printHoaDon(@PathVariable String id) throws Exception {
        HoaDon hoaDon = hoaDonService.findHoaDonById(id);
        byte[] pdfBytes = hoaDonService.generateInvoicePdf(hoaDon);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=hoa-don-" + hoaDon.getMaHoaDon() + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/export-pdf/{id}")
    public ResponseEntity<byte[]> exportHoaDonPdf(@PathVariable String id) throws Exception {
        HoaDon hoaDon = hoaDonService.findHoaDonById(id);
        byte[] pdfBytes = hoaDonService.generateInvoicePdf(hoaDon);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=hoa-don-" + hoaDon.getMaHoaDon() + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @PostMapping("/export-excel")
    public ResponseEntity<ByteArrayResource> exportExcel(@RequestBody List<String> hoaDonIds) {
        List<HoaDon> hoaDons = hoaDonService.findAllByIds(hoaDonIds);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("HoaDon");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("STT");
            header.createCell(1).setCellValue("Mã HĐ");
            header.createCell(2).setCellValue("Khách hàng");
            header.createCell(3).setCellValue("SĐT");
            header.createCell(4).setCellValue("Tổng tiền");
            header.createCell(5).setCellValue("Ngày tạo");
            header.createCell(6).setCellValue("Trạng thái");
            header.createCell(7).setCellValue("Nhân Viên");

            int rowIdx = 1;
            int stt = 1;
            for (HoaDon hd : hoaDons) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(stt++);
                row.createCell(1).setCellValue(hd.getMaHoaDon());
                row.createCell(2).setCellValue(hd.getKhachHang().getTenKhachHang());
                row.createCell(3).setCellValue(hd.getKhachHang().getSdt());
                row.createCell(4).setCellValue(hd.getTongTien().doubleValue());
                row.createCell(5).setCellValue(hd.getNgayTao().toString());
                row.createCell(6).setCellValue(getTrangThaiText(hd.getTrangThai()));
                row.createCell(7).setCellValue(hd.getNhanVien().getTenNhanVien());
            }

            workbook.write(out);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }

        ByteArrayResource resource = new ByteArrayResource(out.toByteArray());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=danh_sach_hoa_don.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .contentLength(resource.contentLength())
                .body(resource);
    }


    private String getTrangThaiText(Byte status) {
        if (status == null) return "Không xác định";

        return switch (status) {
            case 0 -> "Chờ xác nhận";
            case 1 -> "Đã xác nhận";
            case 2 -> "Chờ vận chuyển";
            case 3 -> "Đang vận chuyển";
            case 4 -> "Đã giao hàng";
            case 5 -> "Hoàn thành";
            case 6 -> "Đã hủy";
            case 7 -> "Đã trả hàng";
            default -> "Không xác định";
        };
    }

    @PutMapping("/cap-nhat-trang-thai/{id}")
    public ResponseEntity<HoaDon> capNhatTrangThai(
            @PathVariable String id,
            @RequestParam(required = false) Byte trangThai,
            @RequestParam(required = false) String ghiChu,
            @RequestParam(required = false) String tenNhanVien) {

        if (trangThai == null) {
            throw new RuntimeException("Trạng thái không được để trống");
        }

        HoaDon updated = hoaDonService.capNhatTrangThai(id, trangThai, ghiChu, tenNhanVien);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/thanh-toan/{id}")
    public ResponseEntity<HoaDon> thanhToan(
            @PathVariable String id,
            @RequestParam(required = false) BigDecimal tongTien,
            @RequestParam(required = false) BigDecimal daThanhToan,
            @RequestParam String hinhThucId,
            @RequestParam(required = false) String maGiaoDich,
            @RequestParam(required = false) String ghiChu,
            @RequestParam String tenNhanVien) {

        if (tongTien == null || daThanhToan == null) {
            throw new RuntimeException("Số tiền không được để trống!");
        }

        HoaDon updated = hoaDonService.xuLyThanhToan(id, tongTien, daThanhToan, hinhThucId, maGiaoDich, ghiChu, tenNhanVien);
        return ResponseEntity.ok(updated);
    }


    @PostMapping("/hoan-tien/{id}")
    public ResponseEntity<HoaDon> hoanTien(
            @PathVariable String id,
            @RequestParam(required = false) BigDecimal soTienHoan,
            @RequestParam(required = false) String ghiChu,
            @RequestParam String tenNhanVien) {
        HoaDon updated = hoaDonService.xuLyHoanTien(id, soTienHoan, ghiChu, tenNhanVien);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/lich-su/{hoaDonId}")
    public ResponseEntity<List<LichSuHoaDonResponseDto>> getLichSuHoaDon(@PathVariable String hoaDonId) {
        List<LichSuHoaDonResponseDto> lichSu = hoaDonService.getLichSuHoaDon(hoaDonId);
        return ResponseEntity.ok(lichSu);
    }

    @GetMapping("/don-hang-gan-day")
    public ResponseEntity<List<HoaDonGanDayResponseDTO>> getDonHangGanDay(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<HoaDonGanDayResponseDTO> list = hoaDonService.getDonHangGanDay(page, size);
        return ResponseEntity.ok(list);
    }
}