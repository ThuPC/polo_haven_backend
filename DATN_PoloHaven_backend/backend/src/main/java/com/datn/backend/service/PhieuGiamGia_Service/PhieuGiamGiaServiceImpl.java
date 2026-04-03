package com.datn.backend.service.PhieuGiamGia_Service;

import com.datn.backend.dto.request.PhieuGiamGia_CreationRequest;
import com.datn.backend.dto.request.PhieuGiamGia_UpdateRequest;
import com.datn.backend.dto.response.ApiResponse;
import com.datn.backend.dto.response.PhieuGiamGiaResponseDTO;
import com.datn.backend.entity.PhieuGiamGia;
import com.datn.backend.exception.AppException;
import com.datn.backend.exception.ErrorCode;
import com.datn.backend.mapper.PhieuGiamGiaMapper;
import com.datn.backend.repository.KhachHangGiamGiaRepository;
import com.datn.backend.repository.PhieuGiamGiaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.sql.Timestamp;
import java.util.Date;
@Service
public class PhieuGiamGiaServiceImpl implements PhieuGiamGiaService {
    @Autowired
    PhieuGiamGiaRepository phieuGiamGiaRepository;
    @Autowired
    KhachHangGiamGiaRepository khachHangGiamGiaRepository;
    @Autowired
    PhieuGiamGiaMapper phieuGiamGiaMapper;
    @Autowired
    KhachHang_GiamGiaService khachHangGiamGiaService;


    @Override
    public PhieuGiamGia taoPhieuGiamGia(PhieuGiamGia_CreationRequest request) {
        PhieuGiamGia phieuGiamGiaNew = phieuGiamGiaMapper.toPhieuGiamGia(request);
        phieuGiamGiaNew.setMaPhieuGiamGia(generateMaPhieuGiamGia());
        phieuGiamGiaNew.setNgayTao(System.currentTimeMillis());
        phieuGiamGiaNew.setNgaySua(System.currentTimeMillis());
        Byte congKhai = 0;
        if ("ALL".equalsIgnoreCase(request.getDoiTuongApDung())) {
            congKhai = 1;
        }
        phieuGiamGiaNew.setCongKhai(congKhai);
        PhieuGiamGia savedVoucher = phieuGiamGiaRepository.save(phieuGiamGiaNew);
        khachHangGiamGiaService.creationDetailVoucherV2(request, savedVoucher);
        return savedVoucher;
    }
    private String generateMaPhieuGiamGia() {
        String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
        Random random = new Random();
        int rand = 100 + random.nextInt(900);
        return "VC" + date + rand;
    }

    @Override
    public List<PhieuGiamGia> fullDanhSachPhieuGiamGia() {
        return phieuGiamGiaRepository.findAll();
    }

    @Override
    public ApiResponse<PhieuGiamGia> xoaPhieuGiamGia(String id) {
        try {
            PhieuGiamGia pgg = phieuGiamGiaRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu giảm giá"));
            pgg.setTrangThai((byte) 3);
            return ApiResponse.<PhieuGiamGia>builder()
                    .result(phieuGiamGiaRepository.save(pgg))
                    .message("update trạng thái thành 2 (đã xóa)")
                    .build();
        } catch (Exception e) {
            throw new AppException(ErrorCode.DELETE_FAILED);
        }
    }

    @Override
    public PhieuGiamGia capNhatPhieuGiamGia(PhieuGiamGia_UpdateRequest request) {
        try {
            PhieuGiamGia pgg = phieuGiamGiaRepository.findById(request.getId())
                    .orElseThrow(() -> new AppException(ErrorCode.VOUCHER_NOT_FOUND));
            phieuGiamGiaMapper.updatePhieuGiamGia(pgg, request);
            pgg.setNgaySua(System.currentTimeMillis());
            return phieuGiamGiaRepository.save(pgg);
        } catch (Exception e) {
            throw new AppException(ErrorCode.UPDATING_FAILED);
        }
    }

    @Override
    public List<PhieuGiamGiaResponseDTO> danhSachPhieuGiamGia() {
        List<PhieuGiamGia> danhSachPhieu = phieuGiamGiaRepository.findAllWhereTrangThaiNot2Native();
        if (danhSachPhieu == null || danhSachPhieu.isEmpty()) {
            return Collections.emptyList();
        }
        return danhSachPhieu.stream()
                .map(phieuGiamGia -> {
                    PhieuGiamGiaResponseDTO dto = phieuGiamGiaMapper.toPhieuGiamGiaResponse(phieuGiamGia);
                    dto.setDaSuDung(phieuGiamGiaRepository.demSoPhieuDaSuDungTheoId(phieuGiamGia.getId()));
                    dto.setSoLuong(phieuGiamGiaRepository.demSoMaPhieuTheoId(phieuGiamGia.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public PhieuGiamGia capNhatTrangThai(String id) {
        try {
            PhieuGiamGia pgg = phieuGiamGiaRepository.findById(id)
                    .orElseThrow(() -> new AppException(ErrorCode.VOUCHER_NOT_FOUND));
            if (pgg.getTrangThai() == 1) {
                pgg.setTrangThai((byte) 0);
            } else if (pgg.getTrangThai() == 0) {
                pgg.setTrangThai((byte) 1);
            }
            pgg.setNgaySua(System.currentTimeMillis());
            return phieuGiamGiaRepository.save(pgg);
        } catch (Exception e) {
            throw new AppException(ErrorCode.UPDATING_FAILED);
        }
    }

    @Transactional
    @Override
    public int deactivateExpiredVouchers() {
        long nowTimestamp = Instant.now().toEpochMilli();
        List<PhieuGiamGia> expiredVouchers = phieuGiamGiaRepository.findAllByTrangThaiAndNgayKetThucLessThan( (byte)1,nowTimestamp);
        if (expiredVouchers.isEmpty()) {
            return 0;
        }
        for (PhieuGiamGia voucher : expiredVouchers) {
            voucher.setTrangThai((byte)0);
        }
        phieuGiamGiaRepository.saveAll(expiredVouchers);
        return expiredVouchers.size();
    }
}
