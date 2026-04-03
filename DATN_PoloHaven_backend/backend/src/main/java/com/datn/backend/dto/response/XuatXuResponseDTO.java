package com.datn.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class XuatXuResponseDTO {
    private String id;
    private String maXuatXu;
    private String noiXuatXu;
    private Byte trangThai;
}
