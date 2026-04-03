package com.datn.backend.controller.KhuyenMai_Controller;

import com.datn.backend.dto.request.KhuyenMaiRequest;
import com.datn.backend.dto.response.KhuyenMaiResponse;
import com.datn.backend.entity.KhuyenMai;
import com.datn.backend.service.khuyen_mai.KhuyenMaiService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/giam-gia/khuyen-mai")
@RequiredArgsConstructor
public class KhuyenMaiController {
    private final KhuyenMaiService khuyenMaiService;

    @GetMapping
    public Page<KhuyenMaiResponse> getAll(@PageableDefault(size = 10, sort = "ngayBatDau") Pageable pageable) {
        return khuyenMaiService.getAllKhuyenMai(pageable);
    }

    @GetMapping("/{id}")
    public KhuyenMaiResponse getById(@PathVariable String id) {
        return khuyenMaiService.getKhuyenMaiById(id);
    }

    @PreAuthorize("hasRole('QUANLY')")
    @PostMapping("/add")
    public ResponseEntity<?> create(@RequestBody KhuyenMaiRequest request) {
        try{
            KhuyenMaiResponse result = khuyenMaiService.createKhuyenMai(request);
            return ResponseEntity.ok(Map.of(
                    "success",true,
                    "message","Tạo khuyến mãi thành công.",
                    "data",result
            ));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(Map.of(
                    "errors",false,
                    "message","Tạo khuyến mãi thất bại." +e.getMessage()
            ));
        }
    }

    @PreAuthorize("hasRole('QUANLY')")
    @PutMapping("/update/{id}")
    public KhuyenMaiResponse update(@PathVariable String id, @RequestBody KhuyenMaiRequest request) {
        return khuyenMaiService.updateKhuyenMai(id, request);
    }

    @PreAuthorize("hasRole('QUANLY')")
    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable String id) {
        khuyenMaiService.deleteKhuyenMai(id);
    }

    //check toggle xem có spct nào trung không
    @PutMapping("/toggle")
    public ResponseEntity<?> toggle(@RequestBody KhuyenMai request) {
        try {
            String msg = khuyenMaiService.toggleKhuyenMai(request);
            return ResponseEntity.ok(msg);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/validate-before-toggle")
    public ResponseEntity<?> validateBeforeToggle(
            @RequestParam String id,
            @RequestParam Integer trangThai) {
        khuyenMaiService.validateBeforeToggle(id, trangThai.byteValue());
        return ResponseEntity.ok("OK");
    }

}
