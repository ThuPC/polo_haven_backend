package com.datn.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MauSacResponseDTO {
    private String id;
    private String maMau;
    private String tenMau;
    private String maMauSac;
    private Byte trangThai;
}
