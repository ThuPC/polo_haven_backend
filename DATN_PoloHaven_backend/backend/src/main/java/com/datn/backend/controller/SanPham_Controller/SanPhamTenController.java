package com.datn.backend.controller.SanPham_Controller;

import com.datn.backend.dto.response.SanPhamTenResponseDTO;
import com.datn.backend.service.san_pham.SanPhamTenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/san-pham")
@RequiredArgsConstructor
public class SanPhamTenController {

    private final SanPhamTenService sanPhamTenService;

    @GetMapping("/ten-san-pham")
    public ResponseEntity<List<SanPhamTenResponseDTO>> getAllTenSanPham() {
        List<SanPhamTenResponseDTO> result = sanPhamTenService.getAllTenSanPham();
        return ResponseEntity.ok(result);
    }
}

