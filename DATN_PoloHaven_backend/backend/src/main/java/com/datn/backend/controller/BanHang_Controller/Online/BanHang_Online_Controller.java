package com.datn.backend.controller.BanHang_Controller.Online;

import com.datn.backend.contants.RoutesConstant;
import com.datn.backend.dto.request.DonHang_Request;
import com.datn.backend.service.Ban_Hang.BanHang_Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping(RoutesConstant.PREFIX_API_CUSTOMER +"/online")
public class BanHang_Online_Controller {
    @Autowired
    BanHang_Service banHangService;


    @PostMapping()
    public ResponseEntity<?> createInvoice (@RequestBody DonHang_Request request){
        var result = banHangService.createInvoice(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/create-for-payment")
    public ResponseEntity<?> createPendingInvoice(@RequestBody DonHang_Request request) {
        var result = banHangService.createPendingInvoice(request);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/status/{orderId}")
    public ResponseEntity<?> getOrderStatus(@PathVariable String orderId) {
        try {
            Map<String, Object> statusResponse = banHangService.getOrderStatus(orderId);
            return ResponseEntity.ok(statusResponse);
        }  catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi không xác định khi lấy trạng thái đơn hàng.");
        }
    }

    @GetMapping("/confirm")
    public ResponseEntity<Void> confirmGuestOrder(@RequestParam("token") String token) {
        try {
            banHangService.confirmGuestOrder(token);
            // Nếu thành công, chuyển hướng về trang thành công trên frontend
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create("http://localhost:5173/order-success")) // <-- Sửa lại URL frontend của bạn
                    .build();
        } catch (Exception e) {
            // Nếu thất bại, chuyển hướng về trang thất bại
            String errorMessage = java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create("http://localhost:5173/order-failed?message=" + errorMessage)) // <-- Sửa lại URL frontend của bạn
                    .build();
        }
    }


}
