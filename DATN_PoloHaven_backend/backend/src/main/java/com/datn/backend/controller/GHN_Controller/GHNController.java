package com.datn.backend.controller.GHN_Controller;

import com.datn.backend.service.ghn.GHNService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ghn")
@CrossOrigin(origins = "*")
public class GHNController {

    @Autowired
    private GHNService ghnService;

    // Lấy danh sách tỉnh/thành phố (cần cho UI)
    @GetMapping("/provinces")
    public ResponseEntity<String> getProvinces() {
        try {
            String response = ghnService.getProvinces();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"message\": \"Lỗi khi lấy danh sách tỉnh: " + e.getMessage() + "\"}");
        }
    }

    // Lấy danh sách quận/huyện (cần cho UI)
    @GetMapping("/districts/{provinceId}")
    public ResponseEntity<String> getDistricts(@PathVariable int provinceId) {
        try {
            String response = ghnService.getDistricts(provinceId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"message\": \"Lỗi khi lấy danh sách quận/huyện: " + e.getMessage() + "\"}");
        }
    }

    // Lấy danh sách phường/xã (cần cho UI)
    @GetMapping("/wards/{districtId}")
    public ResponseEntity<String> getWards(@PathVariable int districtId) {
        try {
            String response = ghnService.getWards(districtId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"message\": \"Lỗi khi lấy danh sách phường/xã: " + e.getMessage() + "\"}");
        }
    }

    // Tính phí vận chuyển - endpoint chính
    @PostMapping("/shipping-fee")
    public ResponseEntity<?> calculateShippingFee(@RequestBody Object requestBody) {
        try {
            // Kiểm tra payload
            if (!(requestBody instanceof Map)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("{\"message\": \"Payload phải là một Map\"}");
            }
            Map<String, Object> requestMap = (Map<String, Object>) requestBody;
            
            // Validate các trường bắt buộc
            String[] requiredFields = {"from_district_id", "to_district_id", "to_ward_code", "weight"};
            for (String field : requiredFields) {
                if (!requestMap.containsKey(field) || requestMap.get(field) == null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("{\"message\": \"Thiếu hoặc không hợp lệ: " + field + "\"}");
                }
            }
            
            String response = ghnService.calculateShippingFee(requestBody);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"message\": \"Lỗi khi tính phí vận chuyển: " + e.getMessage() + "\"}");
        }
    }

    // Tính phí vận chuyển theo vùng khi không có API GHN
    @PostMapping("/regional-shipping-fee")
    public ResponseEntity<?> calculateRegionalShippingFee(@RequestBody Map<String, Object> request) {
        try {
            if (!request.containsKey("to_district_id")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("{\"message\": \"Thiếu to_district_id\"}");
            }
            
            int toDistrictId = Integer.parseInt(request.get("to_district_id").toString());
            int regionalFee = ghnService.calculateRegionalFee(toDistrictId);
            
            Map<String, Object> response = new HashMap<>();
            Map<String, Object> data = new HashMap<>();
            data.put("total", regionalFee);
            data.put("service_fee", regionalFee);
            data.put("insurance_fee", 0);
            data.put("is_regional_fee", true);
            response.put("data", data);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"message\": \"Lỗi khi tính phí vận chuyển theo vùng: " + e.getMessage() + "\"}");
        }
    }
} 