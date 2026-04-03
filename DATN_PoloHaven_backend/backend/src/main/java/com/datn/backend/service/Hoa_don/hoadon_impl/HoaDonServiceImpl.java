package com.datn.backend.service.Hoa_don.hoadon_impl;


import com.datn.backend.dto.response.HoaDonChiTietResponseDTO;
import com.datn.backend.dto.response.HoaDonGanDayResponseDTO;
import com.datn.backend.dto.response.HoaDonResponseDTO;
import com.datn.backend.dto.response.LichSuHoaDonResponseDto;
import com.datn.backend.entity.*;
import com.datn.backend.mapper.HoaDonMapper;
import com.datn.backend.repository.hoadon_repo.*;
import com.datn.backend.service.Hoa_don.HoaDonService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.font.PdfFontFactory.EmbeddingStrategy;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class HoaDonServiceImpl implements HoaDonService {
    @Autowired
    HoaDonRepo hoaDonRepo;
    @Autowired
    private HtttRepo htttRepo;
    @Autowired
    private HtttHoaDonRepo htttHoaDonRepo;
    @Autowired
    private LichSuHoaDonRepo lichSuHoaDonRepo;

    @Autowired
    private HoaDonMapper hoaDonMapper;
    @Autowired
    private ChiTietSanPhamHoaDonRepo chiTietSanPhamHoaDonRepo;
    @Autowired
    private com.datn.backend.repository.hoadon_repo.TraHangRepo traHangRepo;

    @Override
    public Page<HoaDonResponseDTO> searchHoaDon(String keyword, String trangThai,
                                                LocalDateTime fromDate, LocalDateTime toDate,
                                                Integer loaiHoaDon, Pageable pageable) {
        Page<HoaDon> page = hoaDonRepo.searchHoaDon(keyword,
                trangThai != null ? Integer.parseInt(trangThai) : null,
                fromDate, toDate, loaiHoaDon, pageable);
        return page.map(hoaDonMapper::toResponseDTO);
    }

    @Override
    public Map<Integer, Long> thongKeSoLuongHoaDonTheoTrangThai() {
        Map<Integer, Long> result = new HashMap<>();
        for (int i = 0; i <= 11; i++) {
            Long count = hoaDonRepo.countByTrangThai((byte) i);
            result.put(i, count);
        }
        result.put(-1, hoaDonRepo.count());

        return result;
    }


    @Override
    public HoaDon detailHoaDon(String id) {
        return hoaDonRepo.findById(id).orElse(null);
    }

    @Override
    public List<HoaDon> findAllByIds(List<String> ids) {
        return hoaDonRepo.findAllByIdIn(ids);
    }

    @Override
    public HoaDon findHoaDonById(String id) {
        return hoaDonRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn với id: " + id));
    }

    @Override
    public byte[] generateInvoicePdf(HoaDon hoaDon) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);

        String fontPath = getClass().getClassLoader()
                .getResource("fonts/static/Roboto-Regular.ttf")
                .getPath();

        PdfFont font = PdfFontFactory.createFont(
                fontPath,
                PdfEncodings.IDENTITY_H,
                EmbeddingStrategy.PREFER_EMBEDDED
        );
        document.setFont(font);

        document.add(new Paragraph("HAVEN POLO")
                .setFontSize(18)
                .setTextAlignment(TextAlignment.CENTER)
                .setBold()
                .setMarginBottom(5f));

        document.add(new Paragraph("Địa chỉ cửa hàng: 123 Đường ABC, Quận XYZ, Hà Nội")
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER)
                .setBold()
                .setMarginBottom(3f));

        document.add(new Paragraph("SĐT: 0366 200 005")
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER)
                .setBold()
                .setMarginBottom(20f));

        document.add(new Paragraph("HÓA ĐƠN BÁN HÀNG")
                .setFontSize(16)
                .setTextAlignment(TextAlignment.CENTER)
                .setBold()
                .setMarginBottom(20f));

        document.add(new Paragraph()
                .add("Mã hóa đơn: " + hoaDon.getMaHoaDon() + "\n")
                .add("Ngày tạo: " + hoaDon.getNgayTao().toString() + "\n")
                .add("Khách hàng: " + hoaDon.getTenKhachHang() + "\n")
                .add("SĐT: " + hoaDon.getSdt() + "\n")
                .add("Địa chỉ: " + (hoaDon.getDiaChi() != null ? hoaDon.getDiaChi() : "N/A") + "\n")
                .setMarginBottom(15f));

        float[] columnWidths = {50f, 200f, 80f, 80f, 80f, 80f, 80f};
        Table table = new Table(columnWidths).setWidth(UnitValue.createPercentValue(100));

        table.addHeaderCell(new Cell().add(new Paragraph("STT").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Tên sản phẩm").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Màu sắc").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Kích thước").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Đơn giá").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Số lượng").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Thành tiền").setBold()));

        int stt = 1;
        for (HoaDonChiTiet ct : hoaDon.getChiTietHoaDonList()) {
            table.addCell(String.valueOf(stt++));
            table.addCell(ct.getChiTietSanPham().getSanPham().getTenSanPham());
            table.addCell(String.valueOf(ct.getChiTietSanPham().getMauSac().getTenMau()));
            table.addCell(ct.getChiTietSanPham().getKichThuoc().getSize());
            table.addCell(formatCurrency(ct.getGiaBan()));            table.addCell(String.valueOf(ct.getSoLuong()));
            table.addCell(formatCurrency(
                    ct.getGiaBan().multiply(BigDecimal.valueOf(ct.getSoLuong()))
            ));
        }
        document.add(table);

//        BigDecimal tienTruGiamGia= hoaDon.getTongTien().subtract(hoaDon.getSoTienGiam() != null
//                ? hoaDon.getSoTienGiam() : BigDecimal.ZERO);
//
//        BigDecimal tienCongVanChuyen = tienTruGiamGia.add(hoaDon.getPhiVanChuyen() != null
//                ? hoaDon.getPhiVanChuyen() : BigDecimal.ZERO);

        document.add(new Paragraph()
//                .add("\nTổng tiền: " + formatCurrency(hoaDon.getTongTien()) + "\n")
                .add("Giảm giá: " + (hoaDon.getSoTienGiam() != null ? formatCurrency(hoaDon.getSoTienGiam()) : "0") + "\n")
                .add("Phí vận chuyển: " + (hoaDon.getPhiVanChuyen() != null ? formatCurrency(hoaDon.getPhiVanChuyen()) : "0") + "\n")
//                .add("Tổng thanh toán: " + formatCurrency(
//                        tienCongVanChuyen) + "\n")
//                .add("Tổng thanh toán: " + formatCurrency(
//                        hoaDon.getTongTienSauKhiGiam() != null ? hoaDon.getTongTienSauKhiGiam() : hoaDon.getTongTien()) + "\n")
                .add("Tổng thanh toán: " + formatCurrency(
                        hoaDon.getSoTienKhachHangThanhToan()) + "\n")
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginTop(15f)
                .setBold());

        document.add(new Paragraph()
                .add("\n\nTrạng thái: " + getTrangThaiText(hoaDon.getTrangThai()) + "\n")
                .add("Ngày in: " + LocalDateTime.now().toString() + "\n\n")
                .add("Người lập hóa đơn\n(Ký và ghi rõ họ tên)")
                .setTextAlignment(TextAlignment.RIGHT));

        BitMatrix bitMatrix = new QRCodeWriter().encode(
                hoaDon.getMaHoaDon(), BarcodeFormat.QR_CODE, 100, 100);
        BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(qrImage, "png", baos);
        ImageData imageData = ImageDataFactory.create(baos.toByteArray());
        Image qrCodeImage = new Image(imageData).setWidth(100).setHeight(100);
        document.add(qrCodeImage);

        document.close();
        return out.toByteArray();
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
            case 6 -> "Hủy";
            case 7 -> "Trả hàng";
            case 8 -> "Đang trả hàng";
            case 9 -> "Trả hàng thành công";
            case 10 -> "Từ chối trả hàng";
            case 11 -> "Khách chưa xác nhận";
            default -> "Không xác định";
        };
    }

    private String formatCurrency(BigDecimal amount) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return nf.format(amount);
    }


    @Override
    @Transactional
    public HoaDon capNhatTrangThai(String id, Byte trangThai, String ghiChu, String tenNhanVien) {
        HoaDon hoaDon = hoaDonRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));

        // Kiểm tra số lượng sản phẩm trước khi chuyển trạng thái (áp dụng cho tất cả trạng thái)
        // Chỉ kiểm tra khi chuyển từ trạng thái chưa trừ kho sang trạng thái đã trừ kho
        if (shouldCheckInventory(hoaDon.getTrangThai(), trangThai)) {
            for (HoaDonChiTiet chiTietHoaDon : hoaDon.getChiTietHoaDonList()) {
                ChiTietSanPham ctsp = chiTietHoaDon.getChiTietSanPham();
                
                // Kiểm tra số lượng hiện tại có đủ để trừ không
                if (ctsp.getSoLuong() < chiTietHoaDon.getSoLuong()) {
                    throw new RuntimeException("Sản phẩm " + ctsp.getSanPham().getTenSanPham() +
                            " (Màu: " + ctsp.getMauSac().getTenMau() +
                            ", Size: " + ctsp.getKichThuoc().getSize() + ")" +
                            " chỉ còn " + ctsp.getSoLuong() + " sản phẩm trong kho, " +
                            "không đủ " + chiTietHoaDon.getSoLuong() + " sản phẩm!");
                }
            }
        }

        if ((trangThai == 5 || trangThai == 1) && hoaDon.getLoaiHoaDon() == 0) {
            BigDecimal tongTienSauGiam = hoaDon.getTongTienSauKhiGiam() != null
                    ? hoaDon.getTongTienSauKhiGiam() : BigDecimal.ZERO;

            BigDecimal daThanhToan = hoaDon.getSoTienKhachHangThanhToan() != null
                    ? hoaDon.getSoTienKhachHangThanhToan() : BigDecimal.ZERO;

            BigDecimal daHoanTien = hoaDon.getSoTienHoan() != null
                    ? hoaDon.getSoTienHoan() : BigDecimal.ZERO;

            BigDecimal tienThua = daThanhToan.subtract(tongTienSauGiam);

            if (tienThua.compareTo(BigDecimal.ZERO) > 0 && daHoanTien.compareTo(tienThua) < 0) {
                hoaDon.setSoTienHoan(tienThua);

                String maLichSuHoan = "LSHD-" + System.currentTimeMillis();
                LichSuHoaDon lichSuHoan = LichSuHoaDon.builder()
                        .hoaDon(hoaDon)
                        .maLichSuHoaDon(maLichSuHoan)
                        .hanhDongNguoiThaoTac("Hệ thống tự động hoàn tiền")
                        .tenNhanVien(tenNhanVien)
                        .ngayCapNhat(LocalDateTime.now())
                        .ghiChu("Tự động hoàn " + tienThua + " trước khi chuyển trạng thái")
                        .trangThai(hoaDon.getTrangThai())
                        .build();
                lichSuHoaDonRepo.save(lichSuHoan);
            }
        }

//        // Xử lý chuyển hình thức nhận hàng về tại quầy
//        if (hoaDon.getTrangThai() == 5 && hoaDon.getLoaiHoaDon() == 1 && hoaDon.getHinhThucNhanHang() == 1) {
//            hoaDon.setHinhThucNhanHang(0);
//        }
//
//        // Xử lý trường hợp đặc biệt: hóa đơn tại quầy chuyển từ giao hàng về nhận tại quầy
//        if (trangThai == 1 && hoaDon.getLoaiHoaDon() == 0 && hoaDon.getHinhThucNhanHang() == 1) {
//            hoaDon.setHinhThucNhanHang(0);
//        }

        if(hoaDon.getTrangThai() == 0 && hoaDon.getLoaiHoaDon() == 1){
            // Hoàn trả số lượng sản phẩm về kho với validation
            for (HoaDonChiTiet chiTietHoaDon : hoaDon.getChiTietHoaDonList()) {
                ChiTietSanPham ctsp = chiTietHoaDon.getChiTietSanPham();
                Integer soLuongHienTai = ctsp.getSoLuong();
                Integer soLuongCanTru = chiTietHoaDon.getSoLuong();

                // Kiểm tra số lượng có đủ để trừ không
                if (soLuongHienTai < soLuongCanTru) {
                    throw new RuntimeException(
                        String.format("Không thể trừ số lượng sản phẩm '%s'. " +
                                     "Số lượng hiện tại: %d, số lượng cần trừ: %d. " +
                                     "Số lượng không đủ để thực hiện thao tác này.",
                                     ctsp.getSanPham().getTenSanPham(),
                                     soLuongHienTai,
                                     soLuongCanTru)
                    );
                }

                // Chỉ trừ khi đã validate đủ số lượng
                ctsp.setSoLuong(soLuongHienTai - soLuongCanTru);
                chiTietSanPhamHoaDonRepo.save(ctsp);
            }
        }

        if (hoaDon.getLoaiHoaDon() == 1 && Boolean.FALSE.equals(hoaDon.getThanhToanTruoc())) {
            BigDecimal tong = hoaDon.getTongTienSauKhiGiam() != null
                    ? hoaDon.getTongTienSauKhiGiam() : hoaDon.getTongTien();
            BigDecimal daThanhToan = hoaDon.getSoTienKhachHangThanhToan() != null
                    ? hoaDon.getSoTienKhachHangThanhToan() : BigDecimal.ZERO;

            if (trangThai == 5 && daThanhToan.compareTo(tong) < 0) {
                throw new RuntimeException("Khách chưa thanh toán đủ. Không thể hoàn thành đơn hàng.");
            }
        }

        if (trangThai == 4) {
            hoaDon.setNgayThanhToan(LocalDateTime.now());
        }

        if (hoaDon.getTrangThai() != 6 && trangThai == 6 || hoaDon.getTrangThai() != 9 && trangThai == 9) {
            for (HoaDonChiTiet chiTietHoaDon : hoaDon.getChiTietHoaDonList()) {
                ChiTietSanPham ctsp = chiTietHoaDon.getChiTietSanPham(); // lấy biến thể sản phẩm

                ctsp.setSoLuong(ctsp.getSoLuong() + chiTietHoaDon.getSoLuong());

                chiTietSanPhamHoaDonRepo.save(ctsp);
            }
        }

        String maLichSu = "LSHD-" + System.currentTimeMillis();
        LichSuHoaDon lichSu = LichSuHoaDon.builder()
                .hoaDon(hoaDon)
                .maLichSuHoaDon(maLichSu)
                .hanhDongNguoiThaoTac("Cập nhật trạng thái")
                .tenNhanVien(tenNhanVien)
                .ngayCapNhat(LocalDateTime.now())
                .ghiChu(ghiChu)
                .trangThai(trangThai)
                .build();
        lichSuHoaDonRepo.save(lichSu);

        hoaDon.setTrangThai(trangThai);
        if (trangThai == 4) {
            hoaDon.setNgayThanhToan(LocalDateTime.now());
        }

        return hoaDonRepo.save(hoaDon);
    }


    @Override
    @Transactional
    public HoaDon xuLyThanhToan(String id, BigDecimal tongTien, BigDecimal daThanhToan,
                                String hinhThucId, String maGiaoDich, String ghiChu, String tenNhanVien) {
        HoaDon hoaDon = hoaDonRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));

        HTTT hinhThucTT = htttRepo.findById(hinhThucId)
                .orElseThrow(() -> new RuntimeException("Hình thức thanh toán không tồn tại"));

        if (daThanhToan.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Số tiền thanh toán phải lớn hơn 0");
        }

        // Xác định trạng thái mới
        Byte trangThaiMoi;
        if (daThanhToan.compareTo(tongTien) >= 0) {
            trangThaiMoi = 4;
        } else {
            trangThaiMoi = 3;
        }

        hoaDon.setSoTienKhachHangThanhToan(daThanhToan);
        hoaDon.setTongTien(tongTien);
        hoaDon.setNgayThanhToan(LocalDateTime.now());
        hoaDon.setTrangThai(trangThaiMoi);
        hoaDonRepo.save(hoaDon);

        HTTT_Hoa_Don htttHoaDon = HTTT_Hoa_Don.builder()
                .hoaDon(hoaDon)
                .httt(hinhThucTT)
                .maGiaoDich(maGiaoDich)
                .trangThai((byte) 1) // 1: Đã thanh toán
                .build();
        htttHoaDonRepo.save(htttHoaDon);

        String maLichSu = "LSHD-" + System.currentTimeMillis();
        LichSuHoaDon lichSu = LichSuHoaDon.builder()
                .hoaDon(hoaDon)
                .maLichSuHoaDon(maLichSu)
                .hanhDongNguoiThaoTac("Thanh toán hóa đơn")
                .tenNhanVien(tenNhanVien)
                .ngayCapNhat(LocalDateTime.now())
                .ghiChu(ghiChu)
                .trangThai(trangThaiMoi)
                .build();
        lichSuHoaDonRepo.save(lichSu);

        return hoaDon;
    }


    @Override
    @Transactional
    public HoaDon xuLyHoanTien(String id, BigDecimal soTienHoan, String ghiChu, String tenNhanVien) {
        HoaDon hoaDon = hoaDonRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));

        if (soTienHoan.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Số tiền hoàn phải lớn hơn 0");
        }

        BigDecimal tienDaThanhToan = hoaDon.getSoTienKhachHangThanhToan();
        if (tienDaThanhToan == null || tienDaThanhToan.compareTo(soTienHoan) < 0) {
            throw new RuntimeException("Số tiền hoàn không hợp lệ");
        }

        hoaDon.setSoTienKhachHangThanhToan(tienDaThanhToan.subtract(soTienHoan));

        hoaDon.setSoTienHoan(hoaDon.getSoTienHoan() == null
                ? soTienHoan
                : hoaDon.getSoTienHoan().add(soTienHoan));
        hoaDonRepo.save(hoaDon);

        String maLichSu = "LSHD-" + System.currentTimeMillis();
        LichSuHoaDon lichSu = LichSuHoaDon.builder()
                .hoaDon(hoaDon)
                .maLichSuHoaDon(maLichSu)
                .hanhDongNguoiThaoTac("Hoàn tiền")
                .tenNhanVien(tenNhanVien)
                .ngayCapNhat(LocalDateTime.now())
                .ghiChu(ghiChu)
                .trangThai(hoaDon.getTrangThai())
                .build();
        lichSuHoaDonRepo.save(lichSu);

        return hoaDon;
    }


    @Override
    public List<LichSuHoaDonResponseDto> getLichSuHoaDon(String hoaDonId) {
        return lichSuHoaDonRepo.getLichSuHoaDon(hoaDonId);
    }

    @Override
    public List<HoaDonGanDayResponseDTO> getDonHangGanDay(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<HoaDon> hoaDons = hoaDonRepo.findDonHangGanDay(pageable);
        return hoaDons.stream()
                .map(hoaDonMapper::toHoaDonGanDayResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<HoaDon> findAllByKhachHang(String khachHangId, Integer trangThai, Pageable pageable) {
        return hoaDonRepo.findByKhachHangIdAndTrangThai(khachHangId, trangThai, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public HoaDonResponseDTO getHoaDonChiTietChoKhachHang(String hoaDonId, String idKhachHang) {
        try {
            // Validate input
            if (hoaDonId == null || hoaDonId.trim().isEmpty()) {
                throw new IllegalArgumentException("ID hóa đơn không hợp lệ");
            }

            if (idKhachHang == null || idKhachHang.trim().isEmpty()) {
                throw new IllegalArgumentException("ID khách hàng không hợp lệ");
            }

            System.out.println("�� Debug: Tìm hóa đơn với hoaDonId = " + hoaDonId + ", khachHangId = " + idKhachHang);

            // Tìm hóa đơn
            HoaDon hoaDon = hoaDonRepo.findByIdAndKhachHang_Id(hoaDonId, idKhachHang)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn với ID: " + hoaDonId));

//            System.out.println(" Debug: Tìm thấy hóa đơn: " + hoaDon.getMaHoaDon());

            // Map sang DTO
            HoaDonResponseDTO dto = hoaDonMapper.toResponseDTO(hoaDon);

            // Map danh sách chi tiết hóa đơn
            if (hoaDon.getChiTietHoaDonList() != null) {
//                System.out.println(" DEBUG: Bắt đầu mapping chi tiết hóa đơn...");
                List<HoaDonChiTietResponseDTO> chiTietDTOs = hoaDon.getChiTietHoaDonList().stream()
                        .map(ct -> {
                            System.out.println(" DEBUG: Processing HoaDonChiTiet ID: " + ct.getId());
                            ChiTietSanPham ctsp = ct.getChiTietSanPham();
                            System.out.println("🔍 DEBUG: ChiTietSanPham: " + (ctsp != null ? "NOT NULL" : "NULL"));
                            
                            if (ctsp != null) {
                                System.out.println("🔍 DEBUG: SanPham: " + (ctsp.getSanPham() != null ? ctsp.getSanPham().getTenSanPham() : "NULL"));
                                System.out.println("🔍 DEBUG: MauSac: " + (ctsp.getMauSac() != null ? ctsp.getMauSac().getTenMau() : "NULL"));
                                System.out.println("🔍 DEBUG: KichThuoc: " + (ctsp.getKichThuoc() != null ? ctsp.getKichThuoc().getSize() : "NULL"));
                                System.out.println("🔍 DEBUG: HinhAnh: " + (ctsp.getHinhAnh() != null ? ctsp.getHinhAnh().getTenAnh() : "NULL"));
                            }
                            
                            String tenSanPham = ctsp != null && ctsp.getSanPham() != null 
                                    ? ctsp.getSanPham().getTenSanPham() : "Không có tên";
                            String mauSac = ctsp != null && ctsp.getMauSac() != null 
                                    ? ctsp.getMauSac().getTenMau() : "Không có màu";
                            String kichThuoc = ctsp != null && ctsp.getKichThuoc() != null 
                                    ? String.valueOf(ctsp.getKichThuoc().getSize()) : "Không có size";
                            String hinhAnh = ctsp != null && ctsp.getHinhAnh() != null 
                                    ? ctsp.getHinhAnh().getTenAnh() : null;
                            
                            System.out.println("🔍 DEBUG: Final values - tenSanPham: " + tenSanPham + ", mauSac: " + mauSac + ", kichThuoc: " + kichThuoc + ", hinhAnh: " + hinhAnh);
                            
                            return HoaDonChiTietResponseDTO.builder()
                                    .id(ct.getId())
                                    .soLuong(ct.getSoLuong())
                                    .giaBan(ct.getGiaBan())
                                    .trangThai(ct.getTrangThai())
                                    .tenSanPham(tenSanPham)
                                    .mauSac(mauSac)
                                    .kichThuoc(kichThuoc)
                                    .hinhAnh(hinhAnh)
                                    .chiTietSanPhamId(ctsp != null ? ctsp.getId() : null) // Thêm ID của ChiTietSanPham
                                    .build();
                        }).collect(Collectors.toList());

                dto.setChiTietHoaDonList(chiTietDTOs);
                System.out.println("🔍 Debug: Số lượng chi tiết: " + chiTietDTOs.size());
            }

            // Lấy tên hình thức thanh toán
            try {
                HTTT_Hoa_Don htttHoaDon = htttHoaDonRepo.findByHoaDonId(hoaDon.getId()).orElse(null);
                if (htttHoaDon != null && htttHoaDon.getHttt() != null) {
                    dto.setTenHTTT(htttHoaDon.getHttt().getTenHinhThuc());
                    System.out.println("🔍 Debug: HTTT = " + dto.getTenHTTT());
                }
            } catch (Exception e) {
                System.err.println("⚠️ Warning: Không thể lấy thông tin HTTT: " + e.getMessage());
            }

            return dto;

        } catch (Exception e) {
            System.err.println("❌ Error in service: " + e.getMessage());
            throw new RuntimeException("Lỗi khi lấy chi tiết hóa đơn: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public HoaDon huyDonHangKhach(String idHoaDon, String khachHangId, String lyDo) {
        // Tìm hóa đơn
        HoaDon hoaDon = hoaDonRepo.findById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));

        // Kiểm tra quyền sở hữu
        if (!hoaDon.getKhachHang().getId().equals(khachHangId)) {
            throw new RuntimeException("Không có quyền hủy đơn hàng này");
        }

        // Kiểm tra trạng thái có thể hủy (chỉ cho phép hủy khi chờ xác nhận hoặc đã xác nhận)
        if (hoaDon.getTrangThai() != 0 && hoaDon.getTrangThai() != 1) {
            throw new RuntimeException("Đơn hàng không thể hủy ở trạng thái hiện tại: " + getStatusText(hoaDon.getTrangThai()));
        }

        // Cập nhật trạng thái thành "Đã hủy" (6)
        hoaDon.setTrangThai((byte) 6);

        // Cập nhật ghi chú với lý do hủy
        String ghiChuHuy = "Khách hàng yêu cầu hủy: " + lyDo;
        hoaDon.setGhiChu(ghiChuHuy);

        // Tạo lịch sử hóa đơn
        String maLichSu = "LSHD-" + System.currentTimeMillis();
        LichSuHoaDon lichSu = LichSuHoaDon.builder()
                .hoaDon(hoaDon)
                .maLichSuHoaDon(maLichSu)
                .hanhDongNguoiThaoTac("Khách hàng yêu cầu hủy đơn hàng")
                .tenNhanVien("Khách hàng")
                .ngayCapNhat(LocalDateTime.now())
                .ghiChu(lyDo)
                .trangThai((byte) 6)
                .build();
        lichSuHoaDonRepo.save(lichSu);

        // Nếu đơn hàng đã thanh toán trước, cần hoàn tiền
        if (hoaDon.getSoTienKhachHangThanhToan() != null &&
                hoaDon.getSoTienKhachHangThanhToan().compareTo(BigDecimal.ZERO) > 0) {

            BigDecimal tienCanHoan = hoaDon.getSoTienKhachHangThanhToan();
            hoaDon.setSoTienHoan(tienCanHoan);

            // Tạo lịch sử hoàn tiền
            String maLichSuHoan = "LSHD-" + System.currentTimeMillis() + "-HOAN";
            LichSuHoaDon lichSuHoan = LichSuHoaDon.builder()
                    .hoaDon(hoaDon)
                    .maLichSuHoaDon(maLichSuHoan)
                    .hanhDongNguoiThaoTac("Hệ thống tự động hoàn tiền")
                    .tenNhanVien("Hệ thống")
                    .ngayCapNhat(LocalDateTime.now())
                    .ghiChu("Tự động hoàn " + tienCanHoan + " do hủy đơn hàng")
                    .trangThai((byte) 6)
                    .build();
            lichSuHoaDonRepo.save(lichSuHoan);
        }

        // Hoàn trả số lượng sản phẩm về kho
        for (HoaDonChiTiet chiTietHoaDon : hoaDon.getChiTietHoaDonList()) {
            ChiTietSanPham ctsp = chiTietHoaDon.getChiTietSanPham();
            ctsp.setSoLuong(ctsp.getSoLuong() + chiTietHoaDon.getSoLuong());
            chiTietSanPhamHoaDonRepo.save(ctsp);
        }

        return hoaDonRepo.save(hoaDon);
    }

    // Helper method để lấy text trạng thái
    private String getStatusText(Byte trangThai) {
        String[] statuses = {
                "Chờ xác nhận", // 0
                "Đã xác nhận", //1
                "Chờ vận chuyển", //2
                "Đang vận chuyển", //3
                "Đã giao hàng", //4
                "Hoàn thành", //5
                "Đã hủy",      //6
                "Trả hàng",    //7
                "Đang trả hàng", //8
                "Trả hàng thành công", //9
                "Từ chối trả hàng", //10
                "Khách chưa xác nhận" //11
        };
        return trangThai < statuses.length ? statuses[trangThai] : "Không xác định";
    }

    @Override
    @Transactional
    public HoaDon traHangKhach(String idHoaDon, String khachHangId, String lyDo) {
        // Tìm hóa đơn
        HoaDon hoaDon = hoaDonRepo.findById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));

        // Kiểm tra quyền sở hữu
        if (!hoaDon.getKhachHang().getId().equals(khachHangId)) {
            throw new RuntimeException("Không có quyền trả hàng đơn hàng này");
        }

        // Kiểm tra trạng thái có thể trả hàng (chỉ cho phép khi đã giao hàng hoặc hoàn thành)
        if (hoaDon.getTrangThai() != 4 && hoaDon.getTrangThai() != 5) {
            throw new RuntimeException("Đơn hàng không thể trả hàng ở trạng thái hiện tại: " + getStatusText(hoaDon.getTrangThai()));
        }

        // Cập nhật trạng thái thành "Trả hàng" (7)
        hoaDon.setTrangThai((byte) 7);

        // Cập nhật ghi chú với lý do trả hàng
        String ghiChuTra = "Khách hàng yêu cầu trả hàng: " + lyDo;
        hoaDon.setGhiChu(ghiChuTra);

        // Tạo lịch sử hóa đơn
        String maLichSu = "LSHD-" + System.currentTimeMillis();
        LichSuHoaDon lichSu = LichSuHoaDon.builder()
                .hoaDon(hoaDon)
                .maLichSuHoaDon(maLichSu)
                .hanhDongNguoiThaoTac("Khách hàng yêu cầu trả hàng")
                .tenNhanVien("Khách hàng")
                .ngayCapNhat(LocalDateTime.now())
                .ghiChu(lyDo)
                .trangThai((byte) 7)
                .build();
        lichSuHoaDonRepo.save(lichSu);

        return hoaDonRepo.save(hoaDon);
    }

    @Override
    @Transactional
    public com.datn.backend.entity.TraHang taoYeuCauTraHang(String idHoaDon, String khachHangId, String lyDo, java.util.List<String> mediaUrls) {
        HoaDon hoaDon = hoaDonRepo.findById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));

        if (!hoaDon.getKhachHang().getId().equals(khachHangId)) {
            throw new RuntimeException("Không có quyền trả hàng đơn hàng này");
        }

        if (hoaDon.getTrangThai() != 4 && hoaDon.getTrangThai() != 5) {
            throw new RuntimeException("Đơn hàng không thể trả hàng ở trạng thái hiện tại: " + getStatusText(hoaDon.getTrangThai()));
        }

        com.datn.backend.entity.TraHang traHang = com.datn.backend.entity.TraHang.builder()
                .hoaDon(hoaDon)
                .khachHangId(khachHangId)
                .lyDo(lyDo)
                .mediaUrlsJson(mediaUrls != null ? toJson(mediaUrls) : null)
                .status((byte)0)
                .requestedAt(LocalDateTime.now())
                .build();

        // update hoaDon status to 'Trả hàng' (7)
        hoaDon.setTrangThai((byte)7);
        hoaDonRepo.save(hoaDon);

        // create history
        String maLichSu = "LSHD-" + System.currentTimeMillis();
        LichSuHoaDon lichSu = LichSuHoaDon.builder()
                .hoaDon(hoaDon)
                .maLichSuHoaDon(maLichSu)
                .hanhDongNguoiThaoTac("Khách hàng yêu cầu trả hàng (có media)")
                .tenNhanVien("Khách hàng")
                .ngayCapNhat(LocalDateTime.now())
                .ghiChu(lyDo)
                .trangThai((byte)7)
                .build();
        lichSuHoaDonRepo.save(lichSu);

        return traHangRepo.save(traHang);
    }

    @Override
    public java.util.List<com.datn.backend.entity.TraHang> getTraHangRequests(Byte status) {
        if (status == null) return traHangRepo.findAll();
        return traHangRepo.findByStatus(status);
    }

    @Override
    @Transactional
    public com.datn.backend.entity.TraHang reviewTraHangRequest(String traHangId, Byte newStatus, String adminNote, String adminName) {
        com.datn.backend.entity.TraHang traHang = traHangRepo.findById(traHangId)
                .orElseThrow(() -> new RuntimeException("Yêu cầu trả hàng không tồn tại"));

        traHang.setStatus(newStatus);
        traHang.setAdminNote(adminNote);
        traHang.setReviewedAt(LocalDateTime.now());

        // update related hoaDon status depending on review
        HoaDon hoaDon = traHang.getHoaDon();
        if (newStatus == 1) { // nguyên vẹn -> Trả hàng thành công (9)
            hoaDon.setTrangThai((byte)9);
        } else if (newStatus == 2) { // lỗi -> Từ chối trả hàng (10)
            hoaDon.setTrangThai((byte)10);
        }
        hoaDonRepo.save(hoaDon);

        // add history
        String maLichSu = "LSHD-" + System.currentTimeMillis();
        LichSuHoaDon lichSu = LichSuHoaDon.builder()
                .hoaDon(hoaDon)
                .maLichSuHoaDon(maLichSu)
                .hanhDongNguoiThaoTac("Duyệt yêu cầu trả hàng")
                .tenNhanVien(adminName)
                .ngayCapNhat(LocalDateTime.now())
                .ghiChu(adminNote)
                .trangThai(hoaDon.getTrangThai())
                .build();
        lichSuHoaDonRepo.save(lichSu);

        return traHangRepo.save(traHang);
    }

    // helper to convert list to JSON string
    private String toJson(java.util.List<String> list) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.writeValueAsString(list);
        } catch (Exception e) {
            return null;
        }
    }

    // Helper method để kiểm tra có cần validate số lượng kho không
    private boolean shouldCheckInventory(Byte currentStatus, Byte newStatus) {
        // Chỉ kiểm tra khi chuyển từ trạng thái chưa trừ kho sang trạng thái đã trừ kho
        // Trạng thái chưa trừ kho: 0 (Chờ xác nhận), 11 (Khách chưa xác nhận)
        // Trạng thái đã trừ kho: 1,2,3,4,5 (Đã xác nhận trở đi)
        
        boolean currentNotDeducted = (currentStatus == 0 || currentStatus == 11);
        boolean newDeducted = (newStatus >= 1 && newStatus <= 5);
        
        return currentNotDeducted && newDeducted;
    }

}
