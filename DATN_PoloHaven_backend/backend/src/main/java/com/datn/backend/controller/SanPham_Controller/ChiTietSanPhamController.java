package com.datn.backend.controller.SanPham_Controller;

import com.datn.backend.dto.request.ChiTietSanPhamRequestDTO;
import com.datn.backend.dto.response.ChiTietSanPhamResponseDTO;
import com.datn.backend.service.san_pham.ChiTietSanPhamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/san-pham-chi-tiet")
@RequiredArgsConstructor
public class ChiTietSanPhamController {

    private final ChiTietSanPhamService chiTietSanPhamService;

    @PostMapping
    public ResponseEntity<ChiTietSanPhamResponseDTO> create(@RequestBody ChiTietSanPhamRequestDTO dto) {
        return ResponseEntity.ok(chiTietSanPhamService.create(dto));
    }

    @PostMapping("/batch")
    public ResponseEntity<List<ChiTietSanPhamResponseDTO>> createBatch(@RequestBody List<ChiTietSanPhamRequestDTO> dtos) {
        return ResponseEntity.ok(chiTietSanPhamService.createBatch(dtos));
    }

    @GetMapping
    public ResponseEntity<List<ChiTietSanPhamResponseDTO>> getAll() {
        return ResponseEntity.ok(chiTietSanPhamService.getAll());
    }

    @GetMapping("/by-san-pham/{idSanPham}")
    public ResponseEntity<List<ChiTietSanPhamResponseDTO>> getBySanPhamId(@PathVariable String idSanPham) {
        return ResponseEntity.ok(chiTietSanPhamService.getBySanPhamId(idSanPham));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChiTietSanPhamResponseDTO> getById(@PathVariable String id) {
        return ResponseEntity.ok(chiTietSanPhamService.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        chiTietSanPhamService.delete(id);
        return ResponseEntity.noContent().build();
    }

    //    hafm suwar owr ddaya
    @PutMapping("/{id}")
    public ResponseEntity<ChiTietSanPhamResponseDTO> updateCTSP(
            @PathVariable("id") String id,
            @RequestBody ChiTietSanPhamRequestDTO dto
    ) {
        System.out.println("[UPDATE] ID Hình Ảnh nhận được: " + dto.getIdHinhAnh());
        ChiTietSanPhamResponseDTO response = chiTietSanPhamService.update(id, dto);
        return ResponseEntity.ok(response);
    }

}

