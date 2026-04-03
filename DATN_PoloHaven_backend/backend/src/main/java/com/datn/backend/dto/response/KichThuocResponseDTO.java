package com.datn.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KichThuocResponseDTO {
    private String id;
    private String maKichThuoc;
    private String size;
    private Byte trangThai;
}

