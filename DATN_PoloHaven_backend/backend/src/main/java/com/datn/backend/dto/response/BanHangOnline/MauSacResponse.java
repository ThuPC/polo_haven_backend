package com.datn.backend.dto.response.BanHangOnline;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class MauSacResponse {
    private String id;
    private String ten;
    private String maMauSac;
}