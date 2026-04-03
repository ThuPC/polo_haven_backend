package com.datn.backend.service.dia_chi;

import com.datn.backend.dto.request.DiaChiRequestDTO;
import com.datn.backend.dto.request.DiaChi_BanHangRequestDTO;
import com.datn.backend.dto.response.DiaChiResponseDTO;
import com.datn.backend.entity.DiaChi;
import com.datn.backend.mapper.DiaChiMapper;
import com.datn.backend.repository.DiaChiRepository;
import com.datn.backend.repository.KhachHangRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiaChiServiceImpl implements DiaChiService {
    private final DiaChiRepository diaChiRepository;
    private final KhachHangRepository khachHangRepository;
    private final DiaChiMapper diaChiMapper;

    @Override
    public List<DiaChiResponseDTO> getAddressesByCustomerId(String customerId) {
        if (!khachHangRepository.existsById(customerId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Khách hàng không tồn tại");
        }
        List<DiaChi> diaChis = diaChiRepository.findByKhachHangId(customerId);
        
        return diaChis.stream()
                .map(diaChiMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DiaChiResponseDTO addAddress(String customerId, DiaChiRequestDTO request) {
        if (!khachHangRepository.existsById(customerId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Khách hàng không tồn tại");
        }
        if (request.getIsDefault() != null && request.getIsDefault()) {
            diaChiRepository.resetDefaultAddress(customerId);
        }
        DiaChi diaChi = diaChiMapper.toEntity(request, customerId);
        DiaChi savedDiaChi = diaChiRepository.save(diaChi);
        return diaChiMapper.toResponse(savedDiaChi);
    }

    @Override
    @Transactional
    public DiaChiResponseDTO updateAddress(String addressId, DiaChiRequestDTO request) {
        DiaChi diaChi = diaChiRepository.findById(addressId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Địa chỉ không tồn tại"));
        if (request.getIsDefault() != null && request.getIsDefault()) {
            diaChiRepository.resetDefaultAddress(diaChi.getKhachHang().getId());
            diaChi.setIsDefault(true);
        }
        diaChiMapper.updateEntityFromDTO(request, diaChi);
        DiaChi savedDiaChi = diaChiRepository.save(diaChi);
        return diaChiMapper.toResponse(savedDiaChi);
    }

    @Override
    @Transactional
    public void setDefaultAddress(String customerId, String addressId) {
        if (!khachHangRepository.existsById(customerId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Khách hàng không tồn tại");
        }
        DiaChi diaChi = diaChiRepository.findById(addressId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Địa chỉ không tồn tại"));
        if (!diaChi.getKhachHang().getId().equals(customerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Địa chỉ không thuộc khách hàng này");
        }
        diaChiRepository.resetDefaultAddress(customerId);
        diaChi.setIsDefault(true);
        diaChiRepository.save(diaChi);
    }

    @Override
    @Transactional
    public void deleteAddress(String customerId, String addressId) {
        if (!khachHangRepository.existsById(customerId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Khách hàng không tồn tại");
        }
        DiaChi diaChi = diaChiRepository.findById(addressId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Địa chỉ không tồn tại"));
        if (!diaChi.getKhachHang().getId().equals(customerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Địa chỉ không thuộc khách hàng này");
        }
        diaChiRepository.deleteById(addressId);
    }

    @Override
    public List<DiaChi_BanHangRequestDTO> getListAddressByCustomer(String idKhachHang) {
        List<DiaChi> diaChiList = diaChiRepository.findByKhachHangId(idKhachHang);
        List<DiaChi_BanHangRequestDTO> diaChiDTOList = diaChiList.stream()
                .map(diaChiEntity -> {
                    DiaChi_BanHangRequestDTO dto = new DiaChi_BanHangRequestDTO();
                    dto.setId(diaChiEntity.getId());
                    dto.setTenNguoiNhan(diaChiEntity.getTenNguoiNhan());
                    dto.setSdt(diaChiEntity.getSdt());
                    dto.setProvinceId(diaChiEntity.getProvinceId());
                    dto.setThanhPho(diaChiEntity.getThanhPho());
                    dto.setDistrictId(diaChiEntity.getDistrictId());
                    dto.setQuanHuyen(diaChiEntity.getQuanHuyen());
                    dto.setWardCode(diaChiEntity.getWardCode());
                    dto.setXaPhuong(diaChiEntity.getXaPhuong());
                    dto.setSoNha(diaChiEntity.getSoNha());


                    return dto;
                })
                .collect(Collectors.toList()); // 3. Thu thập tất cả các DTO đã tạo vào một danh sách mới

        // 4. Trả về danh sách DTO cho Controller
        return diaChiDTOList;
    }
}