package com.datn.backend.dto.request;

import lombok.Data;

@Data
public class HinhAnhRequestDTO {
    private String tenAnh;
    private Byte trangThai;
    private String url;
}

