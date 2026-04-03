package com.datn.backend.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class FilterSPBanHangResponseDTO {
    private List<String> kichThuoc;
    private List<String> mauSac;
    private List<String> chatLieu;
    private List<String> thuongHieu;
    private List<String> xuatXu;
}
