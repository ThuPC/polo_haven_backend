package com.datn.backend.service.san_pham;

import com.datn.backend.dto.request.ChiTietSanPhamRequestDTO;
import com.datn.backend.dto.response.ChiTietSanPhamResponseDTO;
import com.datn.backend.dto.response.ChiTietSanPham_BanHang_ResponseDTO;
import com.datn.backend.entity.ChiTietSanPham;
import com.datn.backend.entity.HinhAnh;
import com.datn.backend.mapper.ChiTietSanPhamMapper;
import com.datn.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChiTietSanPhamServiceImpl implements ChiTietSanPhamService {

    private final ChiTietSanPhamRepository chiTietSanPhamRepository;
    private final SanPhamRepo sanPhamRepository;
    private final MauSacRepository mauSacRepository;
    private final KichThuocRepository kichThuocRepository;
    private final HinhAnhRepo hinhAnhRepository;

    private final ChiTietSanPhamMapper chiTietSanPhamMapper;

    @Override
    public ChiTietSanPhamResponseDTO create(ChiTietSanPhamRequestDTO dto) {
        ChiTietSanPham entity = chiTietSanPhamMapper.toEntity(dto);

        // --- Liên kết FK ---
        entity.setSanPham(
                sanPhamRepository.findById(dto.getIdSanPham().toString())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"))
        );
        entity.setMauSac(
                mauSacRepository.findById(dto.getIdMauSac().toString())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy màu sắc"))
        );
        entity.setKichThuoc(
                kichThuocRepository.findById(dto.getIdKichThuoc().toString())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy kích thước"))
        );

        if (dto.getIdHinhAnh() != null) {
            entity.setHinhAnh(
                    hinhAnhRepository.findById(dto.getIdHinhAnh().toString()).orElse(null)
            );
        }

        // --- Sinh mã tự động ---
        entity.setMaCTSP(generateMaCTSP());

        return chiTietSanPhamMapper.toResponseDTO(
                chiTietSanPhamRepository.save(entity)
        );
    }

    @Override
    public List<ChiTietSanPhamResponseDTO> createBatch(List<ChiTietSanPhamRequestDTO> dtos) {
        AtomicLong count = new AtomicLong(chiTietSanPhamRepository.count()); // số CTSP hiện có

        List<ChiTietSanPham> entities = dtos.stream().map(dto -> {
            ChiTietSanPham entity = chiTietSanPhamMapper.toEntity(dto);

            entity.setSanPham(
                    sanPhamRepository.findById(dto.getIdSanPham().toString())
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"))
            );
            entity.setMauSac(
                    mauSacRepository.findById(dto.getIdMauSac().toString())
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy màu sắc"))
            );
            entity.setKichThuoc(
                    kichThuocRepository.findById(dto.getIdKichThuoc().toString())
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy kích thước"))
            );

            if (dto.getIdHinhAnh() != null && !dto.getIdHinhAnh().isBlank()) {
                HinhAnh hinhAnh = hinhAnhRepository.findById(dto.getIdHinhAnh())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy hình ảnh với ID: " + dto.getIdHinhAnh()));
                entity.setHinhAnh(hinhAnh);
            } else if (dto.getTenAnh() != null && !dto.getTenAnh().isBlank()) {
                Optional<HinhAnh> optional = hinhAnhRepository.findByTenAnh(dto.getTenAnh());
                HinhAnh hinhAnh = optional.orElseGet(() -> {
                    HinhAnh newImg = new HinhAnh();
                    newImg.setTenAnh(dto.getTenAnh());
                    newImg.setTrangThai((byte) 1);
                    return hinhAnhRepository.save(newImg);
                });
                entity.setHinhAnh(hinhAnh);
            } else {
                entity.setHinhAnh(null);
            }

            // --- Sinh mã cho mỗi CTSP, tăng dần ---
            entity.setMaCTSP("CTSP" + String.format("%05d", count.incrementAndGet()));

            return entity;
        }).collect(Collectors.toList());

        return chiTietSanPhamRepository.saveAll(entities)
                .stream()
                .map(chiTietSanPhamMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    // --- Hàm sinh mã CTSP cho create đơn lẻ ---
    @Override
    public String generateMaCTSP() {
        long count = chiTietSanPhamRepository.count() + 1;
        return "CTSP" + String.format("%05d", count);
    }

    @Override
    public List<ChiTietSanPhamResponseDTO> getAll() {
        return chiTietSanPhamRepository.findAll()
                .stream()
                .map(chiTietSanPhamMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ChiTietSanPhamResponseDTO getById(String id) {
        return chiTietSanPhamRepository.findById(id)
                .map(chiTietSanPhamMapper::toResponseDTO)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy CTSP với ID: " + id));
    }

    @Override
    public void delete(String id) {
        chiTietSanPhamRepository.deleteById(id);
    }

    private String generateMaCTSP(UUID sanPhamId, UUID mauSacId, UUID kichThuocId) {
        return "CTSP-" + sanPhamId.toString().substring(0, 8)
                + "-" + mauSacId.toString().substring(0, 4)
                + "-" + kichThuocId.toString().substring(0, 4);
    }

    @Override
    public List<ChiTietSanPhamResponseDTO> getBySanPhamId(String idSanPham) {
        return chiTietSanPhamRepository.findBySanPhamId(idSanPham)
                .stream()
                .map(chiTietSanPhamMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ChiTietSanPhamResponseDTO update(String id, ChiTietSanPhamRequestDTO dto) {
        ChiTietSanPham existing = chiTietSanPhamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy CTSP với ID: " + id));

        // Gán lại các thuộc tính cơ bản
        existing.setMaCTSP(dto.getMaCTSP());
        existing.setTenCTSP(dto.getTenCTSP());
        existing.setSoLuong(dto.getSoLuong());
        existing.setDonGia(dto.getDonGia());
        existing.setGhiChu(dto.getGhiChu());
        existing.setTrangThai(dto.getTrangThai());

        // Gán lại quan hệ Sản phẩm
        existing.setSanPham(
                sanPhamRepository.findById(dto.getIdSanPham())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"))
        );

        // Gán lại quan hệ Màu sắc
        existing.setMauSac(
                mauSacRepository.findById(dto.getIdMauSac())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy màu sắc"))
        );

        // Gán lại quan hệ Kích thước
        existing.setKichThuoc(
                kichThuocRepository.findById(dto.getIdKichThuoc())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy kích thước"))
        );

        // ======= Gán Hình Ảnh =======
        if (dto.getIdHinhAnh() != null && !dto.getIdHinhAnh().isBlank()) {
            HinhAnh hinhAnh = hinhAnhRepository.findById(dto.getIdHinhAnh())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hình ảnh với ID: " + dto.getIdHinhAnh()));
            existing.setHinhAnh(hinhAnh);

        } else if (dto.getTenAnh() != null && !dto.getTenAnh().isBlank()) {
            Optional<HinhAnh> optional = hinhAnhRepository.findByTenAnh(dto.getTenAnh());

            HinhAnh hinhAnh = optional.orElseGet(() -> {
                HinhAnh newImg = new HinhAnh();
                newImg.setTenAnh(dto.getTenAnh());
                newImg.setTrangThai((byte) 1);
                return hinhAnhRepository.save(newImg);
            });

            existing.setHinhAnh(hinhAnh);

        } else {
            existing.setHinhAnh(null);
        }

        return chiTietSanPhamMapper.toResponseDTO(chiTietSanPhamRepository.save(existing));
    }

    @Override
    public List<ChiTietSanPham_BanHang_ResponseDTO> getAllEntity() {
        List<ChiTietSanPham_BanHang_ResponseDTO> ct = chiTietSanPhamRepository.getAllChiTietSanPhamForBanHang();
        for (ChiTietSanPham_BanHang_ResponseDTO dto : ct) {
            if (dto.getUrl() != null) {
                dto.setUrl("http://localhost:8080/uploads/san_pham/" + dto.getUrl());
            }
        }
        return ct;
    }


}
