package com.datn.backend.dto.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FilterSPBanHangRequestDTO {
     String searchQuery;
     String kichThuoc; // Thay size thành kichThuoc
     String mauSac; // Thay tenMau thành mauSac
     String chatLieu; // Thay tenChatLieu thành chatLieu
     String thuongHieu; // Thay tenThuongHieu thành thuongHieu
     String xuatXu; // Thay noiXuatXu thành xuatXu
}