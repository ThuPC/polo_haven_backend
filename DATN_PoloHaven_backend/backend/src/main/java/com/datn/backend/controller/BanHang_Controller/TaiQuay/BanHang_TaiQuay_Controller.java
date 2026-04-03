package com.datn.backend.controller.BanHang_Controller.TaiQuay;

import com.datn.backend.contants.RoutesConstant;
import com.datn.backend.dto.request.DonHang_Request;
import com.datn.backend.dto.request.FilterSPBanHangRequestDTO;
import com.datn.backend.dto.response.ChiTietSanPham_BanHang_ResponseDTO;
import com.datn.backend.dto.response.FilterSPBanHangResponseDTO;
import com.datn.backend.service.Ban_Hang.BanHang_Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(RoutesConstant.PREFIX_API_ADMIN +"/ban-hang-tai-quay")
public class BanHang_TaiQuay_Controller {
    @Autowired
    BanHang_Service banHangService;

    @PostMapping("/create-for-payment")
    public ResponseEntity<?> createPendingInvoice(@RequestBody DonHang_Request request) {
        var result = banHangService.createPendingInvoice(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/products/filter")
    public ResponseEntity<?> getFilteredProducts(@RequestBody FilterSPBanHangRequestDTO filter) {
        try {
            List<ChiTietSanPham_BanHang_ResponseDTO> products = banHangService.getFilterSpBanHang(filter);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Không thể lọc sản phẩm: " + e.getMessage());
        }
    }

    @PostMapping()
    public ResponseEntity<?> createInvoice (@RequestBody DonHang_Request request){
        var result = banHangService.createInvoice(request);
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
    @GetMapping("/filter-options")
    public ResponseEntity<FilterSPBanHangResponseDTO> getFilterOptions() {
        FilterSPBanHangResponseDTO filterOptions = banHangService.getFilterOptions();
        return ResponseEntity.ok(filterOptions);
    }

    @GetMapping("/generate-new-code")
    public ResponseEntity<?> generateNewHoaDonCode() {
        String newCode = banHangService.generateNewHoaDonCode();
        // Trả về một đối tượng JSON để frontend dễ dàng xử lý
        return ResponseEntity.ok(Map.of("newCode", newCode));
    }


}
