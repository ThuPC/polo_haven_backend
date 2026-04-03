package com.datn.backend.service.san_pham;

import com.datn.backend.dto.request.SanPhamVariantRequestDTO;
import com.datn.backend.dto.response.SanPhamVariantResponseDTO;
import com.datn.backend.entity.KichThuoc;
import com.datn.backend.entity.MauSac;
import com.datn.backend.entity.SanPham;
import com.datn.backend.entity.SanPhamVariant;
import com.datn.backend.mapper.SanPhamVariantMapper;
import com.datn.backend.repository.KichThuocRepository;
import com.datn.backend.repository.MauSacRepository;
import com.datn.backend.repository.SanPhamRepo;
import com.datn.backend.repository.SanPhamVariantRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SanPhamVariantServiceImpl implements SanPhamVariantService {

    private final SanPhamVariantRepo variantRepo;
    private final SanPhamRepo sanPhamRepo;
    private final MauSacRepository mauSacRepo;
    private final KichThuocRepository kichThuocRepo;
    private final SanPhamVariantMapper mapper;

    @Override
    public SanPhamVariantResponseDTO create(SanPhamVariantRequestDTO dto) {
        SanPham sanPham = sanPhamRepo.findById(dto.getSanPhamId())
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

        MauSac mauSac = mauSacRepo.findById(dto.getMauSacId())
                .orElseThrow(() -> new RuntimeException("Màu sắc không tồn tại"));

        KichThuoc kichThuoc = kichThuocRepo.findById(dto.getKichCoId())
                .orElseThrow(() -> new RuntimeException("Kích cỡ không tồn tại"));

        SanPhamVariant variant = SanPhamVariant.builder()
                .sanPham(sanPham)
                .mauSac(mauSac)
                .kichCo(kichThuoc)
                .soLuong(dto.getSoLuong())
                .giaBan(dto.getGiaBan())
                .trangThai(dto.getTrangThai())
                .build();

        return mapper.toResponseDTO(variantRepo.save(variant));
    }

    @Override
    public SanPhamVariantResponseDTO update(String id, SanPhamVariantRequestDTO dto) {
        SanPhamVariant variant = variantRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Biến thể không tồn tại"));

        // update fields
        if (dto.getSanPhamId() != null) {
            SanPham sanPham = sanPhamRepo.findById(dto.getSanPhamId())
                    .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));
            variant.setSanPham(sanPham);
        }
        if (dto.getMauSacId() != null) {
            MauSac mauSac = mauSacRepo.findById(dto.getMauSacId())
                    .orElseThrow(() -> new RuntimeException("Màu sắc không tồn tại"));
            variant.setMauSac(mauSac);
        }
        if (dto.getKichCoId() != null) {
            KichThuoc kichThuoc = kichThuocRepo.findById(dto.getKichCoId())
                    .orElseThrow(() -> new RuntimeException("Kích cỡ không tồn tại"));
            variant.setKichCo(kichThuoc);
        }
        if (dto.getSoLuong() != null) variant.setSoLuong(dto.getSoLuong());
        if (dto.getGiaBan() != null) variant.setGiaBan(dto.getGiaBan());
        if (dto.getTrangThai() != null) variant.setTrangThai(dto.getTrangThai());

        return mapper.toResponseDTO(variantRepo.save(variant));
    }

    @Override
    public void delete(String id) {
        SanPhamVariant variant = variantRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Biến thể không tồn tại"));
        variantRepo.delete(variant);
    }

    @Override
    public SanPhamVariantResponseDTO findById(String id) {
        SanPhamVariant variant = variantRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Biến thể không tồn tại"));
        return mapper.toResponseDTO(variant);
    }

    @Override
    public List<SanPhamVariantResponseDTO> getBySanPhamId(String sanPhamId) {
        List<SanPhamVariant> list = variantRepo.findBySanPhamId(sanPhamId);
        return list.stream().map(mapper::toResponseDTO).toList();
    }
}
