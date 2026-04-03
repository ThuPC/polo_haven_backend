package com.datn.backend.service.san_pham;

import com.datn.backend.dto.request.ThuongHieuRequestDTO;
import com.datn.backend.dto.response.ThuongHieuResponseDTO;
import com.datn.backend.entity.ThuongHieu;
import com.datn.backend.mapper.ThuongHieuMapper;
import com.datn.backend.repository.ThuongHieuRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ThuongHieuServiceImpl implements ThuongHieuService {

    private final ThuongHieuRepo thuongHieuRepo;
    private final ThuongHieuMapper thuongHieuMapper;

    @Override
    public Page<ThuongHieuResponseDTO> getAll(Pageable pageable) {
        return thuongHieuRepo.findAll(pageable)
                .map(thuongHieuMapper::toResponseDTO);
    }

    @Override
    public ThuongHieuResponseDTO create(ThuongHieuRequestDTO dto) {
        ThuongHieu entity = new ThuongHieu();

//        entity.setId(UUID.randomUUID().toString());
        entity.setMaThuongHieu(generateMaThuongHieu()); // Tự động sinh mã
        entity.setTenThuongHieu(dto.getTenThuongHieu());
        entity.setTrangThai((byte) 1); // Mặc định trạng thái là 1

        return thuongHieuMapper.toResponseDTO(thuongHieuRepo.save(entity));
    }

    @Override
    public Page<ThuongHieuResponseDTO> search(String keyword, Pageable pageable) {
        return thuongHieuRepo.findByTenThuongHieuContainingIgnoreCase(keyword, pageable)
                .map(thuongHieuMapper::toResponseDTO);
    }


    @Override
    public ThuongHieuResponseDTO update(String id, ThuongHieuRequestDTO dto) {
        ThuongHieu existing = thuongHieuRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Thương Hiệu"));
        thuongHieuMapper.updateEntityFromDTO(dto, existing);
        return thuongHieuMapper.toResponseDTO(thuongHieuRepo.save(existing));
    }

    @Override
    public void delete(String id) {
        ThuongHieu existing = thuongHieuRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Thương Hiệu"));
        thuongHieuRepo.delete(existing); // hoặc xóa mềm bằng setTrangThai = 0
    }

    @Override
    public ThuongHieuResponseDTO updateStatus(String id, Byte trangThai) {
        ThuongHieu entity = thuongHieuRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thương hiệu"));

        entity.setTrangThai(trangThai);
        return thuongHieuMapper.toResponseDTO(thuongHieuRepo.save(entity));
    }

    @Override
    public ThuongHieuResponseDTO findById(String id) {
        ThuongHieu existing = thuongHieuRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Thương Hiệu"));
        return thuongHieuMapper.toResponseDTO(existing);
    }

    @Override
    public List<String> getAllTenThuongHieu() {
        return thuongHieuRepo.findAllTenThuongHieu();
    }

    // Hàm tự động sinh mã thương hiệu
    private String generateMaThuongHieu() {
        List<ThuongHieu> all = thuongHieuRepo.findAll();

        int max = all.stream()
                .mapToInt(th -> {
                    try {
                        return Integer.parseInt(th.getMaThuongHieu().replace("TH", ""));
                    } catch (Exception e) {
                        return 0;
                    }
                })
                .max()
                .orElse(0);

        return String.format("TH%03d", max + 1);
    }

    @Override
    public List<ThuongHieuResponseDTO> getAllActiveThuongHieu() {
        return thuongHieuRepo.findAllActiveThuongHieu();
    }
}