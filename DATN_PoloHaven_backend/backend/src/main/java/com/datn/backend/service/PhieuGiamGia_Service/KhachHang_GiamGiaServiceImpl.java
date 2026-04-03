package com.datn.backend.service.PhieuGiamGia_Service;
import com.datn.backend.dto.request.KhachHangGiamGiaReuquestDTO;
import com.datn.backend.dto.request.PhieuGiamGia_CreationRequest;
import com.datn.backend.dto.response.KhachHangGiamGia_CongKhaiResponseDTO;

import com.datn.backend.dto.response.LichSuSuDung_VoucherProjection;
import com.datn.backend.entity.KhachHang;
import com.datn.backend.entity.KhachHang_GiamGia;
import com.datn.backend.entity.PhieuGiamGia;
import com.datn.backend.exception.AppException;
import com.datn.backend.exception.ErrorCode;
import com.datn.backend.repository.KhachHangGiamGiaRepository;
import com.datn.backend.repository.KhachHangRepository;
import com.datn.backend.repository.PhieuGiamGiaRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import java.text.NumberFormat;
import java.util.Locale;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Service
public class KhachHang_GiamGiaServiceImpl implements KhachHang_GiamGiaService {

    @Autowired
    KhachHangGiamGiaRepository khachHangGiamGiaRepository;
    @Autowired
    KhachHangRepository khachHangRepository;
    @Autowired
    PhieuGiamGiaRepository phieuGiamGiaRepository;
    @Autowired
    JavaMailSender mailSender;


    // lấy ra danh sách chi tiết mã giảm giá
    @Override
    public List<KhachHang_GiamGia> getListDetailVoucher(String idPhieuGiamGia) {
        PhieuGiamGia pggSearch = phieuGiamGiaRepository.findById(idPhieuGiamGia)
                .orElseThrow(() -> new AppException(ErrorCode.VOUCHER_NOT_FOUND));
        return khachHangGiamGiaRepository.findByPhieuGiamGia(pggSearch);
    }

    // lấy ra danh sách chi tiết mã giảm giá chưa sử dụng
    @Override
    public List<KhachHang_GiamGia> getListDetailVoucherChuaSuDung(String idPhieuGiamGia) {
        return khachHangGiamGiaRepository.findChuaSuDungByPhieuGiamGia(idPhieuGiamGia);
    }

//    @Override
//    public List<KhachHang_GiamGia> creationDetailVoucher(PhieuGiamGia request) {
//        List<KhachHang_GiamGia> vouchersToSave = new ArrayList<>();
//        int customerRequirement = request.getYeuCauKhachHang();
//        if (customerRequirement == 0) {
//            for (int i = 0; i < request.getSoLuong(); i++) {
//                KhachHang_GiamGia khachHangGiamGia = KhachHang_GiamGia.builder()
//                        .maPhieu(generateRandomVoucherCode())
//                        .phieuGiamGia(request)
//                        .khachHang(null)
//                        .trangThai((byte)1)
//                        .trangThaiSuDung(false)
//                        .build();
//                vouchersToSave.add(khachHangGiamGia);
//            }
//        } else {
//            List<KhachHang> targetCustomers = khachHangRepository.findByCapDo(customerRequirement);
//            if (targetCustomers == null || targetCustomers.isEmpty()) {
//                System.err.println("Không tìm thấy khách hàng nào với cấp độ: " + customerRequirement);
//                return Collections.emptyList(); // Hoặc throw một exception tùy theo logic nghiệp vụ
//            }
//            long numberOfVouchersToCreate = Math.min(request.getSoLuong(), targetCustomers.size());
//            for (int i = 0; i < numberOfVouchersToCreate; i++) {
//                KhachHang currentCustomer = targetCustomers.get(i);
//                KhachHang_GiamGia khachHangGiamGia = KhachHang_GiamGia.builder()
//                        .maPhieu(generateRandomVoucherCode()) // Gọi hàm tạo mã
//                        .phieuGiamGia(request)
//                        .khachHang(currentCustomer)
//                        .trangThaiSuDung(false)
//                        .trangThai((byte)1)
//                        .build();
//                vouchersToSave.add(khachHangGiamGia);
//            }
//        }
//        if (!vouchersToSave.isEmpty()) {
//            return khachHangGiamGiaRepository.saveAll(vouchersToSave);
//        }
//        return Collections.emptyList();
//    }

    @Override
    public List<KhachHang_GiamGia> creationDetailVoucher(PhieuGiamGia request) {
        return List.of();
    }

    @Override
    public List<KhachHang_GiamGia> creationDetailVoucherV2(PhieuGiamGia_CreationRequest request, PhieuGiamGia savedVoucher) {
        List<KhachHang_GiamGia> vouchersToSave = new ArrayList<>();
        String doiTuongApDung = request.getDoiTuongApDung();


        if ("ALL".equalsIgnoreCase(doiTuongApDung)) {
            Integer soLuong = request.getSoLuong();
            if (soLuong == null || soLuong <= 0) {
                throw new IllegalArgumentException("Số lượng voucher công khai phải lớn hơn 0");
            }

            for (int i = 0; i < soLuong; i++) {
                KhachHang_GiamGia voucherDetail = KhachHang_GiamGia.builder()
                        .maPhieu(generateRandomVoucherCode())
                        .phieuGiamGia(savedVoucher)
                        .khachHang(null)
                        .trangThai((byte) 1)
                        .trangThaiSuDung(false)
                        .build();
                vouchersToSave.add(voucherDetail);
            }
        }
        else if ("SPECIFIC".equalsIgnoreCase(doiTuongApDung)) {
            List<String> customerIds = request.getDanhSachKhachHangId();
            if (customerIds == null || customerIds.isEmpty()) {
                throw new IllegalArgumentException("Danh sách khách hàng không được rỗng cho voucher cá nhân.");
            }
            List<KhachHang> targetCustomers = khachHangRepository.findByIdIn(customerIds);
            if (targetCustomers.size() != customerIds.size()){
                System.err.println("Cảnh báo: Một vài ID khách hàng không tồn tại trong hệ thống.");
            }
            for (KhachHang customer : targetCustomers) {
                KhachHang_GiamGia voucherDetail = KhachHang_GiamGia.builder()
                        .maPhieu(generateRandomVoucherCode())
                        .phieuGiamGia(savedVoucher)
                        .khachHang(customer)
                        .trangThai((byte) 1)
                        .trangThaiSuDung(false)
                        .build();
                vouchersToSave.add(voucherDetail);
            }
        }else if("DOI_DIEM".equalsIgnoreCase(doiTuongApDung)){
            Integer soLuong = request.getSoLuong();
            if (soLuong == null || soLuong <= 0) {
                throw new IllegalArgumentException("Số lượng voucher công khai phải lớn hơn 0");
            }
            for (int i = 0; i < soLuong; i++) {
                KhachHang_GiamGia voucherDetail = KhachHang_GiamGia.builder()
                        .maPhieu(generateRandomVoucherCode())
                        .phieuGiamGia(savedVoucher)
                        .khachHang(null)
                        .trangThai((byte) 1)
                        .trangThaiSuDung(false)
                        .build();
                vouchersToSave.add(voucherDetail);
            }
        }
        else {
            throw new IllegalArgumentException("Giá trị của 'doiTuongApDung' không hợp lệ: " + doiTuongApDung);
        }

        if (!vouchersToSave.isEmpty()) {
            // 1. Lưu tất cả vào DB trước để đảm bảo dữ liệu tồn tại
            List<KhachHang_GiamGia> savedVoucherDetails = khachHangGiamGiaRepository.saveAll(vouchersToSave);

            // 2. Nếu là voucher cá nhân, lặp qua danh sách đã lưu và gửi email
            if ("SPECIFIC".equalsIgnoreCase(doiTuongApDung)) {
                System.out.println("Bắt đầu gửi email thông báo voucher...");
                for (KhachHang_GiamGia savedDetail : savedVoucherDetails) {
                    // Lấy thông tin khách hàng từ bản ghi đã lưu
                    KhachHang customer = savedDetail.getKhachHang();
                    if (customer != null && customer.getEmail() != null && !customer.getEmail().isEmpty()) {
                        // Gọi hàm gửi email
                        sendVoucherNotificationEmail(customer, savedVoucher, savedDetail);
                    }
                }
                System.out.println("Hoàn tất gửi email.");
            }
            return savedVoucherDetails;
        }
        return Collections.emptyList();
    }
    @Override
    public List<KhachHang> getListKhachHang() {
        return khachHangRepository.findAll();
    }

    @Override
    public List<KhachHangGiamGia_CongKhaiResponseDTO> getPublicVoucher() {
        return khachHangGiamGiaRepository.findAvailablePublicVouchers();
    }

    @Override
    public List<KhachHangGiamGia_CongKhaiResponseDTO> getPublicPrivateVoucher(String idKhachHang) {
        return khachHangGiamGiaRepository.findAvailableVouchersByKhachHang(idKhachHang);
    }

    @Override
    public List<LichSuSuDung_VoucherProjection> getListDetailVoucherDaSuDung(String id) {
        return khachHangGiamGiaRepository.findVoucherDaSuDung(id);
    }

    @Override
    public String deleteMa(String id) {
        khachHangGiamGiaRepository.deleteById(id);
        return "Xóa thành công";
    }

    private String generateRandomVoucherCode() {
        String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        int LENGTH = 6;
        StringBuilder result = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < LENGTH; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            result.append(CHARACTERS.charAt(randomIndex));
        }
        return result.toString();
    }

    private void sendVoucherNotificationEmail(KhachHang customer, PhieuGiamGia voucher, KhachHang_GiamGia voucherDetail) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(customer.getEmail());
            helper.setSubject("Bạn đã nhận được một phiếu giảm giá mới từ Polo Haven!");
            String formattedGiaTriGiam = formatGiaTriGiam(voucher);
            String htmlContent = String.format(
                    "<h3>Kính chào %s,</h3>" +
                            "<p>Cảm ơn bạn đã đồng hành cùng Polo Haven. Chúng tôi xin gửi tặng bạn một phiếu giảm giá trong đợt phát hành <strong>%s</strong>.</p>" +
                            "<p>Chi tiết phiếu giảm giá của bạn:</p>" +
                            "<ul style='list-style-type: none; padding: 0;'>" +
                            "  <li style='margin-bottom: 10px;'><strong>Mã sử dụng:</strong> <span style='background-color: #f0f0f0; padding: 3px 8px; border-radius: 4px; font-weight: bold;'>%s</span></li>" +
                            "  <li style='margin-bottom: 10px;'><strong>Mức giảm:</strong> %s</li>" +
                            "</ul>" +
                            "<p>Hãy truy cập website <a href='https://your-website.com'>https://www.polohaven</a> để khám phá các sản phẩm mới và sử dụng ngay phiếu giảm giá của bạn!</p>" +
                            "<p>Trân trọng,<br>Đội ngũ Polo Haven </p>",
                    customer.getTenKhachHang(),
                    voucher.getTenPhieuGiamGia(),
                    voucherDetail.getMaPhieu(),
                    formattedGiaTriGiam
            );
            helper.setText(htmlContent, true);
            mailSender.send(message);

        } catch (MessagingException e) {
            // Ghi log lỗi thay vì throw RuntimeException để không làm dừng toàn bộ quá trình
            System.err.println("Lỗi khi gửi email tới " + customer.getEmail() + ": " + e.getMessage());
        }
    }
    private String formatGiaTriGiam(PhieuGiamGia voucher) {
        if ("PHAN_TRAM".equals(voucher.getLoaiGiamGia())) {
            return String.format("%.0f%%", voucher.getGiaTriGiam()); // Bỏ phần thập phân .0
        } else { // SO_TIEN
            NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            return currencyFormatter.format(voucher.getGiaTriGiam()); // Ví dụ: 50.000 ₫
        }
    }

}
