package com.datn.backend.controller.DiaChi_Controller;

import com.datn.backend.dto.request.DiaChiRequestDTO;
import com.datn.backend.dto.response.DiaChiResponseDTO;
import com.datn.backend.service.dia_chi.DiaChiService;
import com.datn.backend.service.ghn.GHNService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customers/{customerId}/addresses")
@RequiredArgsConstructor
public class DiaChiController {
    private final DiaChiService diaChiService;
    private final GHNService ghnService;

    @GetMapping
    public ResponseEntity<List<DiaChiResponseDTO>> getAddresses(@PathVariable String customerId) {
        return ResponseEntity.ok(diaChiService.getAddressesByCustomerId(customerId));
    }

    @PostMapping
    public ResponseEntity<DiaChiResponseDTO> addAddress(@PathVariable String customerId, @RequestBody DiaChiRequestDTO request) {
        return new ResponseEntity<>(diaChiService.addAddress(customerId, request), HttpStatus.CREATED);
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<DiaChiResponseDTO> updateAddress(@PathVariable String addressId, @RequestBody DiaChiRequestDTO request) {
        return ResponseEntity.ok(diaChiService.updateAddress(addressId, request));
    }

    @PatchMapping("/{addressId}/set-default")
    public ResponseEntity<String> setDefaultAddress(@PathVariable String customerId, @PathVariable String addressId) {
        diaChiService.setDefaultAddress(customerId, addressId);
        return ResponseEntity.ok("Đặt địa chỉ mặc định thành công");
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<String> deleteAddress(@PathVariable String customerId, @PathVariable String addressId) {
        diaChiService.deleteAddress(customerId, addressId);
        return ResponseEntity.ok("Xóa địa chỉ thành công");
    }

    @GetMapping("/provinces")
    public ResponseEntity<String> getProvinces() {
        return ResponseEntity.ok(ghnService.getProvinces());
    }

    @GetMapping("/districts/{provinceId}")
    public ResponseEntity<String> getDistricts(@PathVariable int provinceId) {
        return ResponseEntity.ok(ghnService.getDistricts(provinceId));
    }

    @GetMapping("/wards/{districtId}")
    public ResponseEntity<String> getWards(@PathVariable int districtId) {
        return ResponseEntity.ok(ghnService.getWards(districtId));
    }

    @PostMapping("/shipping-fee")
    public ResponseEntity<?> calculateShippingFee(@PathVariable String customerId, @RequestBody Object requestBody) {
        try {
            // Kiểm tra payload
            if (!(requestBody instanceof Map)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("{\"message\": \"Payload phải là một Map\"}");
            }
            Map<String, Object> requestMap = (Map<String, Object>) requestBody;
            if (!requestMap.containsKey("from_district_id") || requestMap.get("from_district_id") == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("{\"message\": \"Thiếu hoặc không hợp lệ: from_district_id\"}");
            }
            if (!requestMap.containsKey("to_district_id") || requestMap.get("to_district_id") == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("{\"message\": \"Thiếu hoặc không hợp lệ: to_district_id\"}");
            }
            if (!requestMap.containsKey("service_id") || requestMap.get("service_id") == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("{\"message\": \"Thiếu hoặc không hợp lệ: service_id\"}");
            }
            if (!requestMap.containsKey("to_ward_code") || requestMap.get("to_ward_code") == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("{\"message\": \"Thiếu hoặc không hợp lệ: to_ward_code\"}");
            }
            String response = ghnService.calculateShippingFee(requestBody);
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body("{\"message\": \"" + e.getReason() + "\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"message\": \"Lỗi khi tính phí vận chuyển: " + e.getMessage() + "\"}");
        }
    }
}