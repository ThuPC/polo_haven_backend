package com.datn.backend.service.dia_chi;

import com.datn.backend.dto.request.DiaChiRequestDTO;
import com.datn.backend.dto.request.DiaChi_BanHangRequestDTO;
import com.datn.backend.dto.response.DiaChiResponseDTO;

import java.util.List;

public interface DiaChiService {
    List<DiaChiResponseDTO> getAddressesByCustomerId(String customerId);
    DiaChiResponseDTO addAddress(String customerId, DiaChiRequestDTO request);
    DiaChiResponseDTO updateAddress(String addressId, DiaChiRequestDTO request);
    void setDefaultAddress(String customerId, String addressId);
    void deleteAddress(String customerId, String addressId);

    List<DiaChi_BanHangRequestDTO> getListAddressByCustomer(String idKhachHang);
}
