package com.datn.backend.service.Email;

import com.datn.backend.dto.request.DonHang_Request;
import com.datn.backend.dto.response.ChiTietSanPhamProjection;
import com.datn.backend.entity.HoaDon;
import com.datn.backend.repository.ChiTietSanPhamRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;


@Service
public class EmailService {

    private final JavaMailSender mailSender;
    @Autowired
    ChiTietSanPhamRepository chiTietSanPhamRepository;

    // Lấy URL cơ sở của backend từ application.properties
    @Value("${app.backend.base-url}")
    private String backendBaseUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }


    public void sendOrderConfirmationEmailWithDetails(String to, String token, String customerName, HoaDon savedInvoice, List<DonHang_Request.SanPhamRequest> products) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        // --- XÂY DỰNG CÁC THÀNH PHẦN ĐỘNG ---

        // 1. Link xác nhận
        String confirmationUrl = backendBaseUrl + "/api/v1/khach-hang/online/confirm?token=" + token;
        // 2. Định dạng tiền tệ
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        // 3. Xây dựng bảng danh sách sản phẩm bằng HTML
        StringBuilder productsHtml = new StringBuilder();
        for (DonHang_Request.SanPhamRequest product : products) {
            ChiTietSanPhamProjection ctsp = chiTietSanPhamRepository.findMaTenById(product.getId());
            String ma = ctsp.getMactsp();
            String ten = ctsp.getTenctsp();

            productsHtml.append("<tr>")
                    .append("<td style='padding: 10px; border-bottom: 1px solid #ddd;'>").append(ma).append("</td>")
                    .append("<td style='padding: 10px; border-bottom: 1px solid #ddd;'>").append(ten).append("</td>")
                    .append("<td style='padding: 10px; border-bottom: 1px solid #ddd;' align='center'>").append(product.getSoLuong()).append("</td>")
                    .append("<td style='padding: 10px; border-bottom: 1px solid #ddd;' align='right'>").append(currencyFormatter.format(product.getGiaSauKhiGiam().doubleValue())).append("</td>")
                    .append("</tr>");
        }


        String htmlMsg = """
        <!DOCTYPE html>
        <html lang="vi">
        <head>
            <meta charset="UTF-8">
            <style>
                .email-container { font-family: Arial, sans-serif; max-width: 680px; margin: auto; border: 1px solid #eaeaea; padding: 30px; border-radius: 8px; }
                .header { text-align: center; margin-bottom: 20px; }
                .header h1 { color: #333; }
                .body { line-height: 1.6; }
                .order-details, .shipping-details { margin-top: 30px; }
                .details-table { width: 100%%; border-collapse: collapse; }
                .details-table th, .details-table td { padding: 12px; text-align: left; }
                .details-table th { background-color: #f2f2f2; }
                .total-summary { margin-top: 20px; padding-top: 10px; border-top: 2px solid #eaeaea; text-align: right; }
                .total-summary p { margin: 5px 0; }
                .total-summary .grand-total { font-size: 18px; font-weight: bold; color: #c0392b; }
                .button-container { text-align: center; margin: 40px 0; }
                .button { background-color: #28a745; color: white !important; padding: 15px 30px; text-decoration: none; border-radius: 5px; font-weight: bold; }
                .footer { margin-top: 30px; font-size: 12px; color: #777; text-align: center; }
            </style>
        </head>
        <body>
            <div class="email-container">
                <div class="header">
                    <h1>Cảm ơn bạn đã đặt hàng!</h1>
                </div>
                <div class="body">
                    <p>Chào <strong>%s</strong>,</p>
                    <p>Đơn hàng <strong>#%s</strong> của bạn tại Polo Haven đã được tạo thành công. Vui lòng nhấp vào nút bên dưới để <strong>xác nhận đơn hàng</strong>. Liên kết này sẽ hết hạn sau 24 giờ.</p>

                    <div class="button-container">
                        <a href="%s" class="button">XÁC NHẬN ĐƠN HÀNG</a>
                    </div>

                    <div class="order-details">
                        <h2>Chi tiết đơn hàng</h2>
                        <table class="details-table">
                            <thead>
                                <tr>
                                    <th>Mã sản phẩm</th>
                                    <th>Sản phẩm</th>
                                    <th style="text-align:center;">Số lượng</th>
                                    <th style="text-align:right;">Giá</th>
                                </tr>
                            </thead>
                            <tbody>
                                %s
                            </tbody>
                        </table>
                    </div>

                    <div class="total-summary">
                        <p>Tạm tính: <strong>%s</strong></p>
                        <p>Phí vận chuyển: <strong>%s</strong></p>
                        <p>Giảm giá: <strong>-%s</strong></p>
                        <p class="grand-total">Tổng cộng: %s</p>
                    </div>

                    <div class="shipping-details">
                        <h2>Thông tin giao hàng</h2>
                        <p>
                            <strong>Người nhận:</strong> %s<br>
                            <strong>Số điện thoại:</strong> %s<br>
                            <strong>Địa chỉ:</strong> %s
                        </p>
                    </div>
                </div>
                <div class="footer">
                    <p>Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email.</p>
                    <p>&copy; 2025 Polo Haven. All rights reserved.</p>
                </div>
            </div>
        </body>
        </html>
    """;

        // Thay thế các giá trị động vào template
        String finalHtml = String.format(
                htmlMsg,
                customerName,                                           // %s
                savedInvoice.getMaHoaDon(),                             // %s
                confirmationUrl,                                        // %s
                productsHtml.toString(),                                // %s
                currencyFormatter.format(savedInvoice.getTongTien()),   // %s - Tạm tính
                currencyFormatter.format(savedInvoice.getPhiVanChuyen()),// %s - Phí ship
                currencyFormatter.format(savedInvoice.getSoTienGiam()), // %s - Giảm giá
                currencyFormatter.format(savedInvoice.getTongTienSauKhiGiam()), // %s - Tổng cộng
                savedInvoice.getTenKhachHang(),                         // %s - Tên người nhận
                savedInvoice.getSdt(),                                  // %s - SĐT
                savedInvoice.getDiaChi()                                // %s - Địa chỉ
        );

        helper.setTo(to);
        helper.setSubject("Xác nhận đơn hàng #" + savedInvoice.getMaHoaDon() + " tại Polo Haven");
        helper.setText(finalHtml, true); // true = nội dung là HTML

        mailSender.send(message);
    }
}