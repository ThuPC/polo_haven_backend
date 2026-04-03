package com.datn.backend.controller.BanHang_Controller;

import com.datn.backend.configuration.VnpayConfig;
import com.datn.backend.entity.HoaDon;
import com.datn.backend.repository.hoadon_repo.HoaDonRepo;
import com.datn.backend.service.Ban_Hang.BanHang_Service;
import com.datn.backend.service.Vnpay.VnpayUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal; // Import BigDecimal nếu cần
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/payment")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    private VnpayConfig vnpayConfig;

    @Autowired
    private BanHang_Service banHangService;

    @Autowired
    private HoaDonRepo hoaDonRepository;

    record CreatePaymentRequest(long amount, String orderInfo, String orderId) {}

    @PostMapping("/create-payment")
    public ResponseEntity<?> createPayment(
            HttpServletRequest request,
            @RequestBody CreatePaymentRequest paymentRequest
    ) {
        String orderId = paymentRequest.orderId();
        log.info("Bắt đầu tạo yêu cầu thanh toán cho OrderId: {}", orderId);

        // ... validation ...
        log.info("Validation OrderId '{}' thành công.", orderId);

        try {
            String vnp_IpAddr = VnpayUtil.getIpAddress(request);
            String vnp_TmnCode = vnpayConfig.getTmnCode();
            long amount = paymentRequest.amount() * 100;
            Map<String, String> vnp_Params = new HashMap<>();
            vnp_Params.put("vnp_Version", "2.1.0");
            vnp_Params.put("vnp_Command", "pay");
            vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
            vnp_Params.put("vnp_Amount", String.valueOf(amount));
            vnp_Params.put("vnp_CurrCode", "VND");
            vnp_Params.put("vnp_TxnRef", orderId);
            vnp_Params.put("vnp_OrderInfo", paymentRequest.orderInfo());
            vnp_Params.put("vnp_OrderType", "other");
            vnp_Params.put("vnp_Locale", "vn");
//            vnp_Params.put("vnp_BankCode", "NCB");
            vnp_Params.put("vnp_ReturnUrl", vnpayConfig.getReturnUrl());
            vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
//            vnp_Params.put("vnp_IpnURL", vnpayConfig.getIpnUrl());
            LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            vnp_Params.put("vnp_CreateDate", now.format(formatter));
            LocalDateTime expireTime = now.plusMinutes(15);
            vnp_Params.put("vnp_ExpireDate", expireTime.format(formatter));
            String paymentUrl = VnpayUtil.buildPaymentUrl(vnp_Params, vnpayConfig.getUrl(), vnpayConfig.getHashSecret());
            log.info("Tạo URL thanh toán VNPay thành công cho đơn hàng {}", orderId);
            return ResponseEntity.ok(paymentUrl);
        } catch (Exception e) {
            log.error("Lỗi nghiêm trọng khi tạo URL thanh toán", e);
            return ResponseEntity.internalServerError().body("Lỗi khi tạo URL thanh toán.");
        }
    }

    // Hàm tiện ích để lấy thông báo lỗi VNPAY (có thể đặt trong Utils hoặc Service)
    private String getVnpayErrorMessage(String responseCode) {
        switch (responseCode) {
            case "00": return "Giao dịch thành công.";
            case "07": return "Giao dịch bị nghi ngờ (liên quan tới lừa đảo).";
            case "09": return "Thẻ/Tài khoản chưa đăng ký Internet Banking.";
            case "10": return "Xác thực thông tin thẻ/tài khoản không đúng quá 3 lần.";
            case "11": return "Đã hết hạn chờ thanh toán.";
            case "12": return "Thẻ/Tài khoản của khách hàng bị khóa.";
            case "13": return "Sai mật khẩu xác thực giao dịch (OTP).";
            case "24": return "Khách hàng hủy giao dịch.";
            case "51": return "Không đủ số dư để thực hiện giao dịch.";
            case "65": return "Vượt quá hạn mức giao dịch trong ngày.";
            case "75": return "Ngân hàng đang bảo trì.";
            default: return "Giao dịch không thành công. Mã lỗi: " + responseCode;
        }
    }

    @GetMapping("/vnpay-payment-return")
    public ResponseEntity<String> vnpayReturn(@RequestParam Map<String, String> params) {
        log.info("Nhận callback trực tiếp từ VNPAY với các tham số: {}", params);

        String vnp_SecureHash = params.get("vnp_SecureHash");

        // Tạo bản copy params để loại bỏ chữ ký
        Map<String, String> hashParams = new HashMap<>(params);
        hashParams.remove("vnp_SecureHash");
        hashParams.remove("vnp_SecureHashType");

        String hashData = VnpayUtil.buildQueryStringForHashing(hashParams);
        String calculatedHash = VnpayUtil.hmacSHA512(vnpayConfig.getHashSecret(), hashData);

        // 1. Kiểm tra chữ ký
        if (vnp_SecureHash == null || !vnp_SecureHash.equals(calculatedHash)) {
            log.warn("Chữ ký không hợp lệ. Nguyên bản: {}, Tính toán: {}", vnp_SecureHash, calculatedHash);
            return ResponseEntity.ok("INVALID_SIGNATURE");
        }

        try {
            String orderId = params.get("vnp_TxnRef");
            long amountFromVnpay = Long.parseLong(params.get("vnp_Amount")) / 100;
            String responseCode = params.get("vnp_ResponseCode");

            HoaDon hoaDon = hoaDonRepository.findById(orderId).orElse(null);
            if (hoaDon == null) {
                log.warn("Không tìm thấy hóa đơn với OrderId: {}", orderId);
                return ResponseEntity.ok("ORDER_NOT_FOUND");
            }

            // 2. Kiểm tra số tiền
            if (hoaDon.getTongTienSauKhiGiam().longValue() != amountFromVnpay) {
                log.warn("Số tiền không khớp. Đơn hàng: {}, VNPAY: {}", hoaDon.getTongTienSauKhiGiam(), amountFromVnpay);
                return ResponseEntity.ok("AMOUNT_MISMATCH");
            }

            // 3. Xử lý theo mã kết quả
            if ("00".equals(responseCode)) {
                if (hoaDon.getTrangThai() == 0 || hoaDon.getTrangThai() == 1) {
                    banHangService.confirmVnpayPayment(orderId);
                    log.info("Thanh toán thành công. Đã cập nhật trạng thái hóa đơn {}", orderId);
                } else {
                    log.info("Hóa đơn {} đã được xác nhận trước đó.", orderId);
                }
                // VNPAY yêu cầu trả về "OK" nếu xử lý thành công
                return ResponseEntity.ok("OK");
            } else {
                log.warn("Thanh toán thất bại. ResponseCode: {}", responseCode);
                // Có thể cập nhật trạng thái thất bại cho đơn hàng
                // banHangService.cancelVnpayPayment(orderId, getVnpayErrorMessage(responseCode));
                return ResponseEntity.ok("FAIL");
            }
        } catch (Exception e) {
            log.error("Lỗi xử lý callback từ VNPAY", e);
            return ResponseEntity.ok("ERROR");
        }
    }

}