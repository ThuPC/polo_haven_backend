package com.datn.backend.service.HTTT;

import com.datn.backend.entity.HTTT;
import java.util.List;

public interface HTTTService {
    List<HTTT> getAllActivePaymentMethods();
    HTTT getPaymentMethodById(String id);
}