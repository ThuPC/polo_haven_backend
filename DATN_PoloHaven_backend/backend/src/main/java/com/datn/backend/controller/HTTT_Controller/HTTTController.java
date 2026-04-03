package com.datn.backend.controller.HTTT_Controller;

import com.datn.backend.entity.HTTT;
import com.datn.backend.service.HTTT.HTTTService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/httt")
public class HTTTController {

    @Autowired
    private HTTTService htttService;

    @GetMapping("/all")
    public ResponseEntity<List<HTTT>> getAllPaymentMethods() {
        List<HTTT> paymentMethods = htttService.getAllActivePaymentMethods();
        return ResponseEntity.ok(paymentMethods);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HTTT> getPaymentMethodById(@PathVariable String id) {
        HTTT httt = htttService.getPaymentMethodById(id);
        if (httt != null) {
            return ResponseEntity.ok(httt);
        }
        return ResponseEntity.notFound().build();
    }
}