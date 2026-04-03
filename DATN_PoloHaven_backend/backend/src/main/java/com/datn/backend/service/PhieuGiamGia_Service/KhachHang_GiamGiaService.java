package com.datn.backend.service.PhieuGiamGia_Service;

import com.datn.backend.dto.request.KhachHangGiamGiaReuquestDTO;
import com.datn.backend.dto.request.PhieuGiamGia_CreationRequest;
import com.datn.backend.dto.response.KhachHangGiamGia_CongKhaiResponseDTO;
import com.datn.backend.dto.response.LichSuSuDung_VoucherProjection;
import com.datn.backend.entity.KhachHang;
import com.datn.backend.entity.KhachHang_GiamGia;
import com.datn.backend.entity.PhieuGiamGia;

import java.util.List;

public interface KhachHang_GiamGiaService {

    List<KhachHang_GiamGia> getListDetailVoucher(String idPhieuGiamGia);
    List<KhachHang_GiamGia> getListDetailVoucherChuaSuDung(String idPhieuGiamGia);
    List<KhachHang_GiamGia> creationDetailVoucher(PhieuGiamGia request);
    List<KhachHang_GiamGia> creationDetailVoucherV2(PhieuGiamGia_CreationRequest request,PhieuGiamGia phieuGiamGia);
    List<KhachHang> getListKhachHang();

    List<KhachHangGiamGia_CongKhaiResponseDTO> getPublicVoucher();
    List<KhachHangGiamGia_CongKhaiResponseDTO> getPublicPrivateVoucher(String idKhachHang);

    List<LichSuSuDung_VoucherProjection> getListDetailVoucherDaSuDung(String id);

    String deleteMa(String id);
}
