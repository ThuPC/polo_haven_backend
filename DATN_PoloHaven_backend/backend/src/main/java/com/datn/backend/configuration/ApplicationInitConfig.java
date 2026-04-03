package com.datn.backend.configuration;


import com.datn.backend.entity.ChucVu;
import com.datn.backend.entity.NhanVien;
import com.datn.backend.repository.ChucVuRepository;
import com.datn.backend.repository.NhanVienRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {

    @Bean
    ApplicationRunner applicationRunner(NhanVienRepository userStaffRepository, ChucVuRepository chucVuRepository) {
        return args -> {

            if (userStaffRepository.findByEmail("phamthu0827@gmail.com") == null) {
                NhanVien user = NhanVien.builder()
                        .email("phamthu0827@gmail.com")
                        .tenNhanVien("Thu")
                        .trangThai((byte) 1)
                        .loginProvider("google")
                        .build();

                ChucVu chucVu = chucVuRepository.findByMaChucVu("QUANLY");
                if (chucVu == null) {
                    ChucVu chucVuNew = ChucVu.builder()
                            .maChucVu("QUANLY")
                            .tenChucVu("Quản lý")
                            .trangThai((byte) 1)
                            .build();
                    user.setChucVu(chucVuRepository.save(chucVuNew));
                } else {
                    user.setChucVu(chucVu);
                }
                userStaffRepository.save(user);
            }
        };

    }
}
