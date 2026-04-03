package com.datn.backend.service.san_pham;

import com.datn.backend.dto.request.XuatXuRequestDTO;
import com.datn.backend.dto.response.XuatXuResponseDTO;
import com.datn.backend.entity.XuatXu;
import com.datn.backend.mapper.XuatXuMapper;
import com.datn.backend.repository.XuatXuRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class XuatXuServiceImpl implements XuatXuService {

    private final XuatXuRepo xuatXuRepo;
    private final XuatXuMapper xuatXuMapper;

    @Override
    public Page<XuatXuResponseDTO> getAll(Pageable pageable) {
        return xuatXuRepo.findAll(pageable)
                .map(xuatXuMapper::toResponseDTO);
    }

    @Override
    public Page<XuatXuResponseDTO> getAll(Pageable pageable, String keyword) {
        Page<XuatXu> page;

        if (keyword != null && !keyword.trim().isEmpty()) {
            page = xuatXuRepo.findByNoiXuatXuContainingIgnoreCase(keyword.trim(), pageable);
        } else {
            page = xuatXuRepo.findAll(pageable);
        }

        return page.map(xuatXuMapper::toResponseDTO);
    }

    @Override
    public XuatXuResponseDTO create(XuatXuRequestDTO dto) {
        XuatXu entity = new XuatXu();
        entity.setMaXuatXu(generateMaXuatXu()); // Tự động sinh mã
        entity.setNoiXuatXu(dto.getNoiXuatXu());
        entity.setTrangThai((byte) 1); // Mặc định trạng thái là 1

        return xuatXuMapper.toResponseDTO(xuatXuRepo.save(entity));
    }

    @Override
    public XuatXuResponseDTO update(String id, XuatXuRequestDTO dto) {
        XuatXu existing = xuatXuRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Xuất Xứ"));
        xuatXuMapper.updateEntityFromDTO(dto, existing);
        return xuatXuMapper.toResponseDTO(xuatXuRepo.save(existing));
    }

    @Override
    public void delete(String id) {
        XuatXu existing = xuatXuRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Xuất Xứ"));
        xuatXuRepo.delete(existing); // hoặc xóa mềm bằng setTrangThai = 0
    }

    @Override
    public XuatXuResponseDTO updateStatus(String id, Byte trangThai) {
        XuatXu entity = xuatXuRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Xuất xứ"));

        entity.setTrangThai(trangThai);
        return xuatXuMapper.toResponseDTO(xuatXuRepo.save(entity));
    }

    @Override
    public XuatXuResponseDTO findById(String id) {
        XuatXu existing = xuatXuRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Xuất Xứ"));
        return xuatXuMapper.toResponseDTO(existing);
    }

    @Override
    public List<String> getAllTenXuatXu() {
        return xuatXuRepo.findAllTenXuatXu();
    }

    // Hàm tự động sinh mã xuất xứ
    private String generateMaXuatXu() {
        List<XuatXu> all = xuatXuRepo.findAll();

        int max = all.stream()
                .mapToInt(xx -> {
                    try {
                        return Integer.parseInt(xx.getMaXuatXu().replace("XX", ""));
                    } catch (Exception e) {
                        return 0;
                    }
                })
                .max()
                .orElse(0);

        return String.format("XX%03d", max + 1);
    }

    @Override
    public List<XuatXuResponseDTO> getAllActiveXuatXu() {
        return xuatXuRepo.findAllActiveXuatXu();
    }
}