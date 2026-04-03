package com.datn.backend.service.san_pham;

import com.datn.backend.dto.response.SanPhamTenResponseDTO;
import com.datn.backend.entity.SanPham;
import com.datn.backend.mapper.SanPhamMapper;
import com.datn.backend.mapper.SanPhamTenMapper;
import com.datn.backend.repository.SanPhamRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SanPhamTenServiceImpl implements SanPhamTenService {

    private final SanPhamRepo sanPhamRepo;
    private final SanPhamTenMapper sanPhamTenMapper;

    @Override
    public List<SanPhamTenResponseDTO> getAllTenSanPham() {
        return sanPhamRepo.findAll()
                .stream()
                .map(sanPhamTenMapper::toSanPhamTenResponseDTO)
                .collect(Collectors.toList());
    }
}


