package com.datn.backend.service.Ban_Hang;

import com.datn.backend.dto.request.DonHang_Request;
import com.datn.backend.dto.request.FilterSPBanHangRequestDTO;
import com.datn.backend.dto.response.ChiTietSanPham_BanHang_ResponseDTO;
import com.datn.backend.dto.response.DonHang_Response;
import com.datn.backend.dto.response.FilterSPBanHangResponseDTO;

import java.util.List;
import java.util.Map;

public interface BanHang_Service {
    DonHang_Response createInvoice(DonHang_Request request);
    DonHang_Response createPendingInvoice(DonHang_Request request);
    void confirmVnpayPayment(String orderId);
    Map<String, Object> getOrderStatus(String orderId);
    List<ChiTietSanPham_BanHang_ResponseDTO> getFilterSpBanHang(FilterSPBanHangRequestDTO dto);
    FilterSPBanHangResponseDTO getFilterOptions();

    String generateNewHoaDonCode();

    void confirmGuestOrder(String token);
}
