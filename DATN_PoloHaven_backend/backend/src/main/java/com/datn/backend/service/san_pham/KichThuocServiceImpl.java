package com.datn.backend.service.san_pham;

import com.datn.backend.dto.request.KichThuocRequestDTO;
import com.datn.backend.dto.response.KichThuocResponseDTO;
import com.datn.backend.entity.KichThuoc;
import com.datn.backend.mapper.KichThuocMapper;
import com.datn.backend.repository.KichThuocRepository;
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
public class KichThuocServiceImpl implements KichThuocService {

    private final KichThuocRepository kichThuocRepository;
    private final KichThuocMapper kichThuocMapper;

    @Override
    public Page<KichThuocResponseDTO> getAll(Pageable pageable) {
        Page<KichThuoc> kichThuocPage = kichThuocRepository.findAll(pageable);
        return kichThuocPage.map(kichThuocMapper::toResponseDTO);
    }

    @Override
    public List<KichThuocResponseDTO> getAllSize() {
        return kichThuocRepository.findAllActiveKichThuoc();
    }

    @Override
    public List<KichThuocResponseDTO> getAllActiveKichThuoc() {
        return kichThuocRepository.findAllActiveKichThuoc();
    }

    @Override
    public List<KichThuocResponseDTO> getAllDistinctTenKichThuoc() {
        List<KichThuocResponseDTO> all = kichThuocRepository.findAllActiveKichThuoc();

        return all.stream()
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(
                                KichThuocResponseDTO::getSize, // key: kích thước (string)
                                Function.identity(),
                                (existing, replacement) -> existing
                        ),
                        map -> new ArrayList<>(map.values())
                ));
    }

    @Override
    public KichThuocResponseDTO create(KichThuocRequestDTO dto) {
        KichThuoc entity = new KichThuoc();
        entity.setMaKichThuoc(generateMaKichThuoc());
        entity.setSize(dto.getSize());
        entity.setTrangThai((byte) 1); // mặc định là đang hoạt động
        return kichThuocMapper.toResponseDTO(kichThuocRepository.save(entity));
    }

    @Override
    public KichThuocResponseDTO update(String id, KichThuocRequestDTO dto) {
        KichThuoc existing = kichThuocRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy kích thước"));
        kichThuocMapper.updateEntityFromDTO(dto, existing);
        return kichThuocMapper.toResponseDTO(kichThuocRepository.save(existing));
    }

    @Override
    public KichThuocResponseDTO updateStatus(String id, Byte trangThai) {
        KichThuoc entity = kichThuocRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy kích thước"));
        entity.setTrangThai(trangThai);
        return kichThuocMapper.toResponseDTO(kichThuocRepository.save(entity));
    }

    @Override
    public void delete(String id) {
        KichThuoc existing = kichThuocRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy kích thước"));
        kichThuocRepository.delete(existing);
    }

    @Override
    public List<KichThuocResponseDTO> getAllIdSize() {
        List<KichThuocResponseDTO> list = kichThuocRepository.findAllActiveKichThuoc();
        list.forEach(dto -> System.out.println("Size: " + dto.getSize()));
        return list;
    }

    @Override
    public Page<KichThuocResponseDTO> search(String keyword, Pageable pageable) {
        return kichThuocRepository.searchBySize(keyword, pageable)
                .map(kichThuocMapper::toResponseDTO);
    }

    @Override
    public KichThuocResponseDTO findById(String id) {
        KichThuoc existing = kichThuocRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy kích thước"));
        return kichThuocMapper.toResponseDTO(existing);
    }

    private String generateMaKichThuoc() {
        List<KichThuoc> all = kichThuocRepository.findAll();

        int max = all.stream()
                .mapToInt(k -> {
                    try {
                        return Integer.parseInt(k.getMaKichThuoc().replace("KT", ""));
                    } catch (Exception e) {
                        return 0;
                    }
                })
                .max()
                .orElse(0);

        return String.format("KT%03d", max + 1);
    }


}