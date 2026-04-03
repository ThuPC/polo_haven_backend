package com.datn.backend.service.san_pham;

import com.datn.backend.dto.request.MauSacRequestDTO;
import com.datn.backend.dto.response.MauSacResponseDTO;
import com.datn.backend.entity.MauSac;
import com.datn.backend.mapper.MauSacMapper;
import com.datn.backend.repository.MauSacRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MauSacServiceImpl implements MauSacService {

    private final MauSacRepository mauSacRepository;
    private final MauSacMapper mauSacMapper;

    @Override
    public Page<MauSacResponseDTO> getAll(Pageable pageable) {
        return mauSacRepository.findAll(pageable)
                .map(mauSacMapper::toResponseDTO);
    }

    @Override
    public MauSacResponseDTO create(MauSacRequestDTO dto) {
        MauSac entity = new MauSac();
        entity.setMaMau(generateMaMau());
        entity.setTenMau(dto.getTenMau());
        entity.setMaMauSac(dto.getMaMauSac());
        entity.setTrangThai((byte) 1);

        return mauSacMapper.toResponseDTO(mauSacRepository.save(entity));
    }


    @Override
    public MauSacResponseDTO update(String id, MauSacRequestDTO dto) {
        MauSac existing = mauSacRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy màu sắc"));
        mauSacMapper.updateEntityFromDTO(dto, existing);
        return mauSacMapper.toResponseDTO(mauSacRepository.save(existing));
    }

    @Override
    public List<MauSacResponseDTO> getAllDistinctTenMauSac() {
        List<MauSacResponseDTO> all = mauSacRepository.findAllActiveMauSac();

        // Lọc trùng theo tenMau
        return all.stream()
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(
                                MauSacResponseDTO::getTenMau,
                                Function.identity(),
                                (existing, duplicate) -> existing
                        ),
                        map -> new ArrayList<>(map.values())
                ));
    }

    @Override
    public Page<MauSacResponseDTO> search(String keyword, Pageable pageable) {
        return mauSacRepository.findByTenMauContainingIgnoreCase(keyword, pageable)
                .map(mauSacMapper::toResponseDTO);
    }


    @Override
    public void delete(String id) {
        MauSac existing = mauSacRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy màu sắc"));
        mauSacRepository.delete(existing); // hoặc xóa mềm nếu cần
    }

    @Override
    public MauSacResponseDTO updateStatus(String id, Byte trangThai) {
        MauSac entity = mauSacRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy màu sắc"));
        entity.setTrangThai(trangThai);
        return mauSacMapper.toResponseDTO(mauSacRepository.save(entity));
    }

    @Override
    public MauSacResponseDTO findById(String id) {
        MauSac existing = mauSacRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy màu sắc"));
        return mauSacMapper.toResponseDTO(existing);
    }

    @Override
    public List<MauSacResponseDTO> getAllIdTenMauSac() {
        List<MauSacResponseDTO> list = mauSacRepository.findAllActiveMauSac();
        list.forEach(dto -> System.out.println("Màu: " + dto.getTenMau()));
        return list;
    }

    private String generateMaMau() {
        List<MauSac> all = mauSacRepository.findAll();

        int max = all.stream()
                .mapToInt(ms -> {
                    try {
                        return Integer.parseInt(ms.getMaMau().replace("MS", ""));
                    } catch (Exception e) {
                        return 0;
                    }
                })
                .max()
                .orElse(0);

        return String.format("MS%03d", max + 1);
    }
}