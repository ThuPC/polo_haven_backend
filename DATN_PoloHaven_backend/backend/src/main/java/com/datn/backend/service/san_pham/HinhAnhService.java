package com.datn.backend.service.san_pham;

import com.datn.backend.dto.response.HinhAnhResponseDTO;
import org.springframework.web.multipart.MultipartFile;

public interface HinhAnhService {
    HinhAnhResponseDTO uploadImage(MultipartFile file);

}



