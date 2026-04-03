package com.datn.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HinhAnhResponseDTO {
    private String id;
//    private String maAnh;
    private String tenAnh;
    private String url;
}

