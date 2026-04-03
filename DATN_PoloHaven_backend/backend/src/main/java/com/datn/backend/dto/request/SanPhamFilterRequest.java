package com.datn.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SanPhamFilterRequest {
    private List<String> mauSacIds;
    private List<String> kichThuocIds;
    private List<String> thuongHieuIds;
    private List<String> chatLieuIds;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal giaMin;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal giaMax;
}

//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//public class SanPhamFilterRequest {
//    private List<String> mauSacIds = new ArrayList<>();
//    private List<String> kichThuocIds = new ArrayList<>();
//    private List<String> thuongHieuIds = new ArrayList<>();
//    private List<String> chatLieuIds = new ArrayList<>();
//    private BigDecimal giaMin;
//    private BigDecimal giaMax;
//}
