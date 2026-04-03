package com.datn.backend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "media_tra_hang")
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MediaTraHang {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    
    @ManyToOne
    @JoinColumn(name = "id_don_tra_hang")
    @JsonBackReference
    DonTraHang donTraHang;
    
    String duongDan;
    
    // 0: Hình ảnh, 1: Video
    Byte loaiMedia;
}