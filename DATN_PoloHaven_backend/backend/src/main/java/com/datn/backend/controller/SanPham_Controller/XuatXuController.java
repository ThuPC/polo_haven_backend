package com.datn.backend.controller.SanPham_Controller;

import com.datn.backend.dto.request.XuatXuRequestDTO;
import com.datn.backend.dto.response.XuatXuResponseDTO;
import com.datn.backend.service.san_pham.XuatXuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/xuat-xu")
@RequiredArgsConstructor
public class XuatXuController {

    private final XuatXuService xuatXuService;

    @GetMapping("/getAll")
    public ResponseEntity<Page<XuatXuResponseDTO>> getAllXuatXu(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String keyword
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<XuatXuResponseDTO> result = xuatXuService.getAll(pageable, keyword);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@Valid @RequestBody XuatXuRequestDTO dto) {
        try {
            XuatXuResponseDTO created = xuatXuService.create(dto);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Tạo xuất xứ thành công",
                    "data", created
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Tạo xuất xứ thất bại: " + e.getMessage()
            ));
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable String id,
                                    @Valid @RequestBody XuatXuRequestDTO dto) {
        try {
            XuatXuResponseDTO updated = xuatXuService.update(id, dto);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Cập nhật xuất xứ thành công",
                    "data", updated
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Cập nhật thất bại: " + e.getMessage()
            ));
        }
    }

    @PutMapping("/change-status/{id}")
    public ResponseEntity<?> changeStatus(@PathVariable String id, @RequestBody Map<String, Byte> request) {
        try {
            Byte newStatus = request.get("trangThai");
            XuatXuResponseDTO result = xuatXuService.updateStatus(id, newStatus);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Cập nhật trạng thái thành công",
                    "data", result
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Cập nhật trạng thái thất bại: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<XuatXuResponseDTO> getById(@PathVariable String id) {
        try {
            return ResponseEntity.ok(xuatXuService.findById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/get-all-ten")
    public ResponseEntity<List<XuatXuResponseDTO>> getAllActiveXuatXu() {
        return ResponseEntity.ok(xuatXuService.getAllActiveXuatXu());
    }
}
