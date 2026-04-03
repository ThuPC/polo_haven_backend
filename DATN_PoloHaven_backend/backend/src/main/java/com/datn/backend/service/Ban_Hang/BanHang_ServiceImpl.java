package com.datn.backend.service.Ban_Hang;

import com.datn.backend.dto.request.DiaChi_BanHangRequestDTO;
import com.datn.backend.dto.request.DonHang_Request;
import com.datn.backend.dto.request.FilterSPBanHangRequestDTO;
import com.datn.backend.dto.response.ChiTietSanPham_BanHang_ResponseDTO;
import com.datn.backend.dto.response.DonHang_Response;
import com.datn.backend.dto.response.FilterSPBanHangResponseDTO;
import com.datn.backend.entity.ChiTietSanPham;
import com.datn.backend.entity.HoaDon;
import com.datn.backend.entity.HoaDonChiTiet;
import com.datn.backend.entity.KhachHang;
import com.datn.backend.entity.KhachHang_GiamGia;
import com.datn.backend.entity.NhanVien;
import com.datn.backend.entity.PhieuGiamGia;
import com.datn.backend.exception.AppException;
import com.datn.backend.exception.ErrorCode;
import com.datn.backend.repository.ChiTietSanPhamRepository;
import com.datn.backend.repository.KhachHangGiamGiaRepository;
import com.datn.backend.repository.KhachHangRepository;
import com.datn.backend.repository.NhanVienRepository;
import com.datn.backend.repository.PhieuGiamGiaRepository;
import com.datn.backend.repository.hoadon_repo.HoaDonChiTietRepo;
import com.datn.backend.repository.hoadon_repo.HoaDonRepo;
import com.datn.backend.service.Email.EmailService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class BanHang_ServiceImpl implements BanHang_Service {

    @Autowired
    HoaDonRepo hoaDonRepo;
    @Autowired
    HoaDonChiTietRepo hoaDonChiTietRepo;
    @Autowired
    KhachHangRepository khachHangRepository;
    @Autowired
    NhanVienRepository nhanVienRepository;
    @Autowired
    PhieuGiamGiaRepository phieuGiamGiaRepository;
    @Autowired
    ChiTietSanPhamRepository chiTietSanPhamRepository;
    @Autowired
    KhachHangGiamGiaRepository khachHangGiamGiaRepository;
    @Autowired
    EmailService emailService;


    @Override
    @Transactional
    public DonHang_Response createInvoice(DonHang_Request request) {
        if (request.getLoaiHoaDon() == 0) {
            return processTaiQuayInvoice(request);
        } else if (request.getLoaiHoaDon() == 1) {
            return processOnlineInvoice(request);
        } else {
            throw new AppException(ErrorCode.INVALID_INVOICE_TYPE);
        }
    }

    @Transactional
    public void confirmGuestOrder(String token) {
        // 1. Tìm hóa đơn bằng token
        HoaDon hoaDon = hoaDonRepo.findByConfirmationToken(token)
                .orElseThrow(() -> new RuntimeException("Link xác nhận không hợp lệ hoặc đã được sử dụng."));
        // 2. Kiểm tra trạng thái và thời gian hết hạn
        if (hoaDon.getTrangThai() != 11) {
            throw new RuntimeException("Đơn hàng này đã được xác nhận trước đó.");
        }
        if (hoaDon.getTokenExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Link xác nhận đã hết hạn. Vui lòng liên hệ cửa hàng để được hỗ trợ.");
        }

        // 3. Cập nhật trạng thái và vô hiệu hóa token
        hoaDon.setTrangThai((byte) 0); // Chuyển sang "Chờ xác nhận" (từ người bán)
        hoaDon.setConfirmationToken(null); // Xóa token để không thể dùng lại
        hoaDon.setTokenExpiryDate(null);

        hoaDonRepo.save(hoaDon);
    }


    private DonHang_Response processTaiQuayInvoice(DonHang_Request request) {
        // --- Các bước chung ---
        KhachHang khachHangEntity = findKhachHang(request.getIdKhachHang());

        // Tại quầy bắt buộc phải có nhân viên thực hiện giao dịch
        NhanVien nhanVien = nhanVienRepository.findByEmail(request.getEmailNhanVien());
        if (nhanVien == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        KhachHang_GiamGia voucherDetail = applyVoucher(request, khachHangEntity);

        // --- Logic riêng cho hóa đơn TẠI QUẦY ---
        LocalDateTime ngayThanhToan = LocalDateTime.now();
        LocalDateTime ngayGiaoHang = (request.getHinhThucNhanHang() == 0) ? LocalDateTime.now() : null; // Nếu nhận tại quầy thì giao ngay
        byte trangThai = (request.getHinhThucNhanHang() == 0) ? (byte) 5 : (byte) 1; // 5: Hoàn thành, 1: Chờ xác nhận (nếu giao hàng)

        String tenNguoiNhan = (khachHangEntity != null) ? khachHangEntity.getTenKhachHang() : "Khách lẻ";
        String sdt = (khachHangEntity != null) ? khachHangEntity.getSdt() : null;
        String diaChi = null;

        // Nếu là đơn tại quầy nhưng khách muốn giao hàng
        if (request.getHinhThucNhanHang() == 1) {
            DiaChi_BanHangRequestDTO diaChiNhan = request.getDiaChiNhanHang();
            if (diaChiNhan == null ) {
                throw new AppException(ErrorCode.ADDRESS_INVALID);
            }
            tenNguoiNhan = diaChiNhan.getTenNguoiNhan();
            sdt = diaChiNhan.getSdt();
            diaChi = String.join(", ", diaChiNhan.getSoNha(), diaChiNhan.getXaPhuong(), diaChiNhan.getQuanHuyen(), diaChiNhan.getThanhPho());
        }

        HoaDon hoaDon = HoaDon.builder()
                .khachHang(khachHangEntity)
                .nhanVien(nhanVien)
                .khachHangGiamGia(voucherDetail)
                .maHoaDon(generateMaHoaDon())
                .soTienKhachHangThanhToan(request.getSoTienKhachHangThanhToan()) // Dành cho tại quầy
                .soTienHoan(request.getSoTienHoan()) // Dành cho tại quầy
                .thanhToanTruoc(true) // Tại quầy luôn thanh toán trước
                .ngayTao(LocalDateTime.now())
                .ngayThanhToan(ngayThanhToan)
                .ngayGiaoHang(ngayGiaoHang)
                .tenKhachHang(tenNguoiNhan)
                .sdt(sdt)
                .diaChi(diaChi)
                .tongTien(request.getGiaGoc())
                .phiVanChuyen(request.getPhiShip())
                .soTienGiam(request.getGiaGiam())
                .tongTienSauKhiGiam(request.getTongTienSauKhiGiam())
                .loaiHoaDon( 0) // Tại quầy
                .hinhThucNhanHang(request.getHinhThucNhanHang())
                .trangThai(trangThai)
                .ghiChu(request.getGhiChuHoaDon())
                .build();

        HoaDon savedInvoice = hoaDonRepo.save(hoaDon);
        createHoaDonChiTietAndUpdateQuantity(savedInvoice, request.getDanhSachSanPham());
        return buildDonHangResponse(savedInvoice);
    }


    private DonHang_Response processOnlineInvoice(DonHang_Request request) {

        Byte trangThai = 0;
        KhachHang khachHangEntity = null;
        String confirmationToken = null;
        LocalDateTime tokenExpiryDate = null;
        String finalEmail = null;
        if (request.getIdKhachHang() != null) {
            // TRƯỜNG HỢP 1: KHÁCH HÀNG ĐÃ ĐĂNG NHẬP
            khachHangEntity = findKhachHang(request.getIdKhachHang());
            if (khachHangEntity == null) {
                throw new AppException(ErrorCode.USER_NOT_FOUND);
            }
            trangThai = 0; // 1: Chờ xác nhận (từ người bán)
        } else if (request.getEmailNguoiNhan() != null && !request.getEmailNguoiNhan().isEmpty()) {
            // TRƯỜNG HỢP 2: KHÁCH VÃNG LAI CÓ NHẬP EMAIL
            trangThai = 11; // 9: Chờ khách xác nhận qua email
            confirmationToken = UUID.randomUUID().toString();
            tokenExpiryDate = LocalDateTime.now().plusHours(24);
            finalEmail = request.getEmailNguoiNhan();
        } else {
            // TRƯỜNG HỢP 3: KHÁCH VÃNG LAI KHÔNG NHẬP EMAIL (nếu bạn cho phép)
            // chờ khách xác nhận (ko có email thì liên hệ khác qua sdt ko được thì hủy)
            trangThai = 11;
        }


        KhachHang_GiamGia voucherDetail = null;
        if (khachHangEntity != null) {
            voucherDetail = applyVoucher(request, khachHangEntity);
        }
        if (request.getHinhThucNhanHang() != 1) {
            throw new RuntimeException("Lỗi hình thức nhận hàng");
        }
        DiaChi_BanHangRequestDTO diaChiNhan = request.getDiaChiNhanHang();
        if (diaChiNhan == null || diaChiNhan.getSoNha() == null) {
            throw new AppException(ErrorCode.ADDRESS_INVALID);
        }
        String tenNguoiNhan = diaChiNhan.getTenNguoiNhan();
        String sdt = diaChiNhan.getSdt();
        String diaChi = String.join(", ", diaChiNhan.getSoNha(), diaChiNhan.getXaPhuong(), diaChiNhan.getQuanHuyen(), diaChiNhan.getThanhPho());

        // Giả sử: 0 = COD (chưa thanh toán), 1 = VNPAY (chờ thanh toán hoặc đã thanh toán)
        boolean daThanhToanTruoc = false; // Mặc định là COD
        LocalDateTime ngayThanhToan = daThanhToanTruoc ? LocalDateTime.now() : null;

        HoaDon hoaDon = HoaDon.builder()
                // khachHangEntity có thể là null nếu là khách vãng lai
                .khachHang(khachHangEntity)
                // voucherDetail sẽ là null nếu là khách vãng lai
                .khachHangGiamGia(voucherDetail)
                .maHoaDon(generateMaHoaDon())
                .thanhToanTruoc(daThanhToanTruoc)
                .ngayTao(LocalDateTime.now())
                .ngayThanhToan(ngayThanhToan)
                .ngayGiaoHang(null)
                // Luôn lấy tên, sđt, địa chỉ từ thông tin giao hàng để đảm bảo tính nhất quán
                .tenKhachHang(tenNguoiNhan)
                .sdt(sdt)
                .diaChi(diaChi)
                .tongTien(request.getGiaGoc())
                .phiVanChuyen(request.getPhiShip())
                .soTienGiam(request.getGiaGiam())
                .tongTienSauKhiGiam(request.getTongTienSauKhiGiam())
                .loaiHoaDon(1) // Online
                .hinhThucNhanHang(1) // Luôn là giao hàng
                .trangThai(trangThai) // nếu mua ko đăng nhập thì là 0
                .ghiChu(request.getGhiChuHoaDon())
                .emailNguoiNhan(finalEmail)
                .confirmationToken(confirmationToken)
                .tokenExpiryDate(tokenExpiryDate)
                .build();

        HoaDon savedInvoice = hoaDonRepo.save(hoaDon);
        createHoaDonChiTietAndUpdateQuantity(savedInvoice, request.getDanhSachSanPham());
        // --- GỬI EMAIL NẾU CẦN ---
        String responseMessage = "Đặt hàng thành công, chờ xác nhận.";
        if (confirmationToken != null) {
            try {
                // Giả sử bạn đã có EmailService
                emailService.sendOrderConfirmationEmailWithDetails(finalEmail, confirmationToken, tenNguoiNhan,savedInvoice,request.getDanhSachSanPham());
                responseMessage = "Vui lòng kiểm tra email để xác nhận đơn hàng của bạn.";
            } catch (MessagingException e) {
                System.err.println("Lỗi gửi email xác nhận: " + e.getMessage());
                responseMessage = "Đặt hàng thành công nhưng gửi email xác nhận thất bại.";
            }
        }

        return buildDonHangResponse(savedInvoice, "Chờ xác nhận");
    }

    private KhachHang findKhachHang(String idKhachHang) {
        if (idKhachHang != null) {
            return khachHangRepository.findById(idKhachHang).orElse(null);
        }
        return null;
    }

    private KhachHang_GiamGia applyVoucher(DonHang_Request request, KhachHang khachHang) {
        if (request.getIdPhieuGiamGia() != null && !request.getIdPhieuGiamGia().isBlank()) {
            KhachHang_GiamGia voucherDetail = khachHangGiamGiaRepository.findById(request.getIdPhieuGiamGia())
                    .orElseThrow(() -> new AppException(ErrorCode.VOUCHER_NOT_FOUND));

            voucherDetail.setTrangThaiSuDung(true);
            voucherDetail.setSoTienGiam(request.getGiaGiam());
            voucherDetail.setKhachHang(khachHang);
            return khachHangGiamGiaRepository.save(voucherDetail);
        }
        return null;
    }

    private void createHoaDonChiTietAndUpdateQuantity(HoaDon savedInvoice, List<DonHang_Request.SanPhamRequest> sanPhamRequests) {
        List<HoaDonChiTiet> listHDCT = new ArrayList<>();
        for (DonHang_Request.SanPhamRequest product : sanPhamRequests) {
            ChiTietSanPham chiTietSanPham = chiTietSanPhamRepository.findById(product.getId())
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUNT));
            if (chiTietSanPham.getSoLuong() < product.getSoLuong()) {
                throw new AppException(ErrorCode.PRODUCT_NOT_ENOUGH_QUANTITY);
            }

            if(savedInvoice.getLoaiHoaDon()==0){
                // Cập nhật số lượng luôn cho đơn tại quầy
                chiTietSanPham.setSoLuong(chiTietSanPham.getSoLuong() - product.getSoLuong());
                chiTietSanPhamRepository.save(chiTietSanPham);
            }



            // Tạo chi tiết hóa đơn
            HoaDonChiTiet hoaDonChiTiet = HoaDonChiTiet.builder()
                    .hoaDon(savedInvoice)
                    .chiTietSanPham(chiTietSanPham)
                    .giaGoc(chiTietSanPham.getDonGia())
                    .giaBan(product.getGiaSauKhiGiam())
                    .soLuong(product.getSoLuong())
                    .build();
            listHDCT.add(hoaDonChiTiet);
        }
        hoaDonChiTietRepo.saveAll(listHDCT);
    }

    private DonHang_Response buildDonHangResponse(HoaDon savedInvoice) {
        return buildDonHangResponse(savedInvoice, "Hoàn thành");
    }

    private DonHang_Response buildDonHangResponse(HoaDon savedInvoice, String trangThai) {
        String tenKhachHangResponse = (savedInvoice.getKhachHang() != null)
                ? savedInvoice.getKhachHang().getTenKhachHang()
                : "Khách lẻ";

        return DonHang_Response.builder()
                .idHoaDon(savedInvoice.getId())
                .maHoaDon(savedInvoice.getMaHoaDon())
                .ngayTao(savedInvoice.getNgayTao())
                .tenKhachHang(tenKhachHangResponse)
                .tongTienSauKhiGiam(savedInvoice.getTongTienSauKhiGiam())
                .trangThai(trangThai)
                .build();
    }

    private String generateMaHoaDon() {
        String maxMa = hoaDonRepo.findMaxMaHoaDon();
        int nextNumber = 1;
        if (maxMa != null && !maxMa.isEmpty()) {
            nextNumber = Integer.parseInt(maxMa.replace("HD", "")) + 1;
        }
        return String.format("HD%03d", nextNumber);
    }

    @Override
    @Transactional
    public DonHang_Response createPendingInvoice(DonHang_Request request) {
        NhanVien nhanVien = null;
        if(request.getLoaiHoaDon() ==0){
            nhanVien = nhanVienRepository.findByEmail(request.getEmailNhanVien());
            if (nhanVien == null) {
                throw new AppException(ErrorCode.USER_NOT_FOUND);
            }
        }


        KhachHang khachHangEntity = null;
        if (request.getIdKhachHang() != null && !request.getIdKhachHang().isBlank()) {
            khachHangEntity = khachHangRepository.findById(request.getIdKhachHang())
                    .orElse(null);
        }


        KhachHang_GiamGia voucherDetail = null;
        if (request.getIdPhieuGiamGia() != null && !request.getIdPhieuGiamGia().isBlank()) {
            voucherDetail = khachHangGiamGiaRepository.findById(request.getIdPhieuGiamGia())
                    .orElseThrow(() -> new AppException(ErrorCode.VOUCHER_NOT_FOUND));
        }
        String tenNguoiNhan = "Khách lẻ";
        String sdtNguoiNhan = null;
        String diaChiGiaoHang = null;
        if (khachHangEntity != null) {
            tenNguoiNhan = khachHangEntity.getTenKhachHang();
            sdtNguoiNhan = khachHangEntity.getSdt();
        }

        if (request.getHinhThucNhanHang() == 1) {
            DiaChi_BanHangRequestDTO diaChiNhan = request.getDiaChiNhanHang();
            if (diaChiNhan == null ) {
                throw new AppException(ErrorCode.ADDRESS_INVALID);
            }
            tenNguoiNhan = diaChiNhan.getTenNguoiNhan();
            sdtNguoiNhan = diaChiNhan.getSdt();
            diaChiGiaoHang = String.join(", ",
                    diaChiNhan.getSoNha(), diaChiNhan.getXaPhuong(),
                    diaChiNhan.getQuanHuyen(), diaChiNhan.getThanhPho());
        }

        // --- BƯỚC 3: TẠO HÓA ĐƠN VỚI TRẠNG THÁI "CHỜ" ---

        HoaDon hoaDon = HoaDon.builder()
                .khachHang(khachHangEntity)
                .nhanVien(nhanVien)
                .khachHangGiamGia(voucherDetail) // Lưu lại voucher đã áp dụng
                .maHoaDon(generateMaHoaDon()) // Hàm tạo mã hóa đơn của bạn
                .ngayTao(LocalDateTime.now())
                .ngayThanhToan(null) // QUAN TRỌNG: null vì chưa thanh toán
                .ngayGiaoHang(null) // Sẽ được cập nhật sau
                .tenKhachHang(tenNguoiNhan) // Tên người nhận cuối cùng
                .sdt(sdtNguoiNhan) // SĐT người nhận cuối cùng
                .diaChi(diaChiGiaoHang)
                .tongTien(request.getGiaGoc())
                .phiVanChuyen(request.getPhiShip())
                .soTienGiam(request.getGiaGiam())
                .tongTienSauKhiGiam(request.getTongTienSauKhiGiam())
                .loaiHoaDon(request.getLoaiHoaDon())
                .hinhThucNhanHang(request.getHinhThucNhanHang())
                .trangThai((byte) 0)
//                .hinhThucThanhToan(2) // QUAN TRỌNG: Giả sử 2 là "VNPay"
                .ghiChu(request.getGhiChuHoaDon())
                .build();

        HoaDon savedInvoice = hoaDonRepo.save(hoaDon);
        System.out.println("hoa don thanh toan vn pay,"+savedInvoice );

        // --- BƯỚC 4: TẠO HÓA ĐƠN CHI TIẾT NHƯNG KHÔNG TRỪ TỒN KHO ---

        for (DonHang_Request.SanPhamRequest productRequest : request.getDanhSachSanPham()) {
            ChiTietSanPham chiTietSanPham = chiTietSanPhamRepository.findById(productRequest.getId())
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUNT));

            // Chỉ kiểm tra số lượng tồn kho, không trừ
            if (chiTietSanPham.getSoLuong() < productRequest.getSoLuong()) {
                throw new AppException(ErrorCode.PRODUCT_NOT_ENOUGH_QUANTITY);
            }

            HoaDonChiTiet hoaDonChiTiet = HoaDonChiTiet.builder()
                    .hoaDon(savedInvoice)
                    .chiTietSanPham(chiTietSanPham)
                    .giaGoc(chiTietSanPham.getDonGia())
                    .giaBan(productRequest.getGiaSauKhiGiam())
                    .soLuong(productRequest.getSoLuong())
                    .build();

            hoaDonChiTietRepo.save(hoaDonChiTiet);


        }

        // --- BƯỚC 5: TRẢ VỀ KẾT QUẢ CHO FRONTEND ---
        // Frontend cần những thông tin này để tạo mã QR và hiển thị trên modal
        return DonHang_Response.builder()
                .idHoaDon(savedInvoice.getId()) // ID của hóa đơn, dùng làm vnp_TxnRef
                .maHoaDon(savedInvoice.getMaHoaDon())
                .tongTienSauKhiGiam(savedInvoice.getTongTienSauKhiGiam())
                .trangThai("Chờ thanh toán") // Trạng thái để hiển thị
                .build();
    }

    @Override
    @Transactional
    public void confirmVnpayPayment(String orderId) {
        HoaDon hoaDon = hoaDonRepo.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND));
        if (hoaDon.getTrangThai() != 0) {
//            System.out.println("Hóa đơn " + hoaDon.getMaHoaDon() + " không ở trạng thái 'Chờ xác nhận'. Bỏ qua xử lý.");
            return;
        }
        System.out.println("hình thức nhận hàng:"+hoaDon.getHinhThucNhanHang());
        hoaDon.setNgayThanhToan(LocalDateTime.now());
        hoaDon.setSoTienKhachHangThanhToan(hoaDon.getTongTien());

        if (hoaDon.getLoaiHoaDon() == 0) { // Đơn tại quầy
            hoaDon.setTrangThai((byte) 5);
            if(hoaDon.getHinhThucNhanHang()==1){
                System.out.println("vao đay");
                hoaDon.setTrangThai((byte)2);
            }
            System.out.println("hình thức nhận hàng:"+hoaDon.getTrangThai());
           // Chuyển sang "Hoàn thành"
        } else { // Đơn giao hàng
            hoaDon.setTrangThai((byte) 2); // Chuyển sang "Chờ vận chuyển"
        }

        // 4. Trừ số lượng tồn kho của các sản phẩm trong đơn
        List<HoaDonChiTiet> chiTietList = hoaDonChiTietRepo.findByHoaDon(hoaDon);
        for (HoaDonChiTiet ct : chiTietList) {
            ChiTietSanPham sanPham = ct.getChiTietSanPham();
            int soLuongMoi = sanPham.getSoLuong() - ct.getSoLuong();
            if (soLuongMoi < 0) {
                throw new AppException(ErrorCode.PRODUCT_NOT_ENOUGH_QUANTITY);
            }
            sanPham.setSoLuong(soLuongMoi);
            chiTietSanPhamRepository.save(sanPham);
        }

        // 5. Đánh dấu voucher đã sử dụng (nếu có)
        if (hoaDon.getKhachHangGiamGia() != null) {
            KhachHang_GiamGia voucherDetail = hoaDon.getKhachHangGiamGia();
            voucherDetail.setTrangThaiSuDung(true);
            voucherDetail.setKhachHang(hoaDon.getKhachHang());
            voucherDetail.setSoTienGiam(hoaDon.getSoTienGiam());
            khachHangGiamGiaRepository.save(voucherDetail);
        }

        // Lưu lại hóa đơn đã cập nhật
        hoaDonRepo.save(hoaDon);
        System.out.println("Đã hoàn tất thanh toán cho hóa đơn: " + hoaDon.getMaHoaDon());
    }

    // Trong class BanHang_ServiceImpl.java
    @Override
    public Map<String, Object> getOrderStatus(String orderId) {
        HoaDon hoaDon = hoaDonRepo.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND));

        Map<String, Object> response = new HashMap<>();
        response.put("status", hoaDon.getTrangThai());

        return response;
    }

    @Override
    public List<ChiTietSanPham_BanHang_ResponseDTO> getFilterSpBanHang(FilterSPBanHangRequestDTO dto) {
        List<ChiTietSanPham_BanHang_ResponseDTO> results = chiTietSanPhamRepository.findFilteredProducts(
                dto.getKichThuoc(), dto.getMauSac(), dto.getChatLieu(), dto.getThuongHieu(), dto.getXuatXu(), dto.getSearchQuery()
        );
        System.out.println("Found " + results.size() + " products");
        return results;
    }
    @Override
    public FilterSPBanHangResponseDTO getFilterOptions() {
        FilterSPBanHangResponseDTO filterOptions = new FilterSPBanHangResponseDTO();
        filterOptions.setKichThuoc(chiTietSanPhamRepository.findDistinctSizes());
        filterOptions.setMauSac(chiTietSanPhamRepository.findDistinctColors());
        filterOptions.setChatLieu(chiTietSanPhamRepository.findDistinctMaterials());
        filterOptions.setThuongHieu(chiTietSanPhamRepository.findDistinctBrands());
        filterOptions.setXuatXu(chiTietSanPhamRepository.findDistinctOrigins());
        return filterOptions;
    }

    @Override
    public String generateNewHoaDonCode() {
        // 1. Lấy tổng số hóa đơn hiện có trong DB
        long count = hoaDonRepo.count();

        // 2. Số thứ tự tiếp theo sẽ là count + 1
        long nextNumber = count + 1;

        // 3. Định dạng chuỗi: "HD" + 5 chữ số (thêm số 0 vào trước nếu cần)
        // Ví dụ: nextNumber = 1 -> "HD00001"
        //       nextNumber = 123 -> "HD00123"
        String newCode = String.format("HD%02d", nextNumber);

        return newCode;
    }

}

