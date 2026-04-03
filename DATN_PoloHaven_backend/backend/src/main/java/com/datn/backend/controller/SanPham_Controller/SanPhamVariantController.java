package com.datn.backend.controller.SanPham_Controller;

import com.datn.backend.dto.request.SanPhamVariantRequestDTO;
import com.datn.backend.dto.response.SanPhamVariantResponseDTO;
import com.datn.backend.service.san_pham.SanPhamVariantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/san-pham-variant")
@RequiredArgsConstructor
public class SanPhamVariantController {

    private final SanPhamVariantService sanPhamVariantService;

    @PostMapping("/create")
    public ResponseEntity<?> createVariant(@RequestBody SanPhamVariantRequestDTO dto) {
        try {
            var result = sanPhamVariantService.create(dto);
            return ResponseEntity.ok(Map.of("success", true, "data", result));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/by-san-pham/{sanPhamId}")
    public ResponseEntity<List<SanPhamVariantResponseDTO>> getVariantsBySanPham(@PathVariable String sanPhamId) {
        return ResponseEntity.ok(sanPhamVariantService.getBySanPhamId(sanPhamId));
    }
}

