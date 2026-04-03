package com.datn.backend.service.khach_hang;

import com.datn.backend.dto.request.DiaChiRequestDTO;
import com.datn.backend.dto.request.KhachHangRequestDTO;
import com.datn.backend.dto.response.DiaChiResponseDTO;
import com.datn.backend.dto.response.KhachHangResponseDTO;
import com.datn.backend.dto.response.KhachHang_BanHang_ResponseDTO;
import com.datn.backend.entity.DiaChi;
import com.datn.backend.entity.KhachHang;
import com.datn.backend.exception.AppException;
import com.datn.backend.exception.ErrorCode;
import com.datn.backend.mapper.KhachHangMapper;
import com.datn.backend.repository.KhachHangRepository;
import com.datn.backend.service.dia_chi.DiaChiService;
import com.datn.backend.service.ghn.GHNService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KhachHangServiceImpl implements KhachHangService {
    private final KhachHangRepository khachHangRepository;
    private final KhachHangMapper khachHangMapper;
    private final DiaChiService diaChiService;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    @Override
    public Page<KhachHangResponseDTO> findAll(Pageable pageable) {
        return khachHangRepository.findAll(pageable)
                .map(khachHang -> {
                    KhachHangResponseDTO dto = khachHangMapper.toResponseDTO(khachHang);
                    // Đảm bảo địa chỉ mặc định có đầy đủ thông tin
                    if (dto.getDiaChis() != null && !dto.getDiaChis().isEmpty()) {
                        dto.getDiaChis().forEach(diaChi -> {
                            // Nếu thiếu thanhPho nhưng có provinceId, thử tìm từ GHN API
                            if ((diaChi.getThanhPho() == null || diaChi.getThanhPho().trim().isEmpty()) 
                                && diaChi.getProvinceId() != null) {
                                try {
                                    // Có thể thêm logic gọi GHN API để lấy tên tỉnh nếu cần
                                    // Tạm thời để trống, sẽ được xử lý ở frontend
                                } catch (Exception e) {
                                    // Ignore error
                                }
                            }
                        });
                    }
                    return dto;
                });
    }

    @Override
    public Page<KhachHangResponseDTO> findAllWithFilters(Pageable pageable, String keyword, Byte status, Byte gioiTinh) {
        return khachHangRepository.findWithFilters(keyword, status, gioiTinh, pageable)
                .map(khachHang -> {
                    KhachHangResponseDTO dto = khachHangMapper.toResponseDTO(khachHang);
                    // Đảm bảo địa chỉ mặc định có đầy đủ thông tin
                    if (dto.getDiaChis() != null && !dto.getDiaChis().isEmpty()) {
                        dto.getDiaChis().forEach(diaChi -> {
                            // Nếu thiếu thanhPho nhưng có provinceId, thử tìm từ GHN API
                            if ((diaChi.getThanhPho() == null || diaChi.getThanhPho().trim().isEmpty()) 
                                && diaChi.getProvinceId() != null) {
                                try {
                                    // Có thể thêm logic gọi GHN API để lấy tên tỉnh nếu cần
                                    // Tạm thời để trống, sẽ được xử lý ở frontend
                                } catch (Exception e) {
                                    // Ignore error
                                }
                            }
                        });
                    }
                    return dto;
                });
    }

    @Override
    public KhachHangResponseDTO findById(String id) {
        KhachHang khachHang = khachHangRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Khách hàng không tồn tại"));
        return khachHangMapper.toResponseDTO(khachHang);
    }

    private String normalizeEmail(String email) {
        return email != null ? email.trim().toLowerCase() : null;
    }

    private String normalizeSdt(String sdt) {
        return sdt != null ? sdt.replaceAll("[^0-9]", "") : null;
    }

    @Override
    public KhachHangResponseDTO create(KhachHangRequestDTO dto) {
        String normalizedEmail = normalizeEmail(dto.getEmail());
        String normalizedSdt = normalizeSdt(dto.getSdt());

        if (normalizedEmail != null && khachHangRepository.existsByEmail(normalizedEmail)) {
            throw new AppException(ErrorCode.EXISTED_EMAIL);
        }

        if (normalizedSdt != null && khachHangRepository.existsBySdt(normalizedSdt)) {
            throw new AppException(ErrorCode.EXISTED_SDT);
        }

        KhachHang kh = khachHangMapper.toEntity(dto);
        kh.setNgayTao(System.currentTimeMillis());
        String maKhachHang = String.format("KH%05d", khachHangRepository.count() + 1);
        kh.setMaKhachHang(maKhachHang);

        String rawPassword = null;
        if ("LOCAL".equals(dto.getLoginProvider())) {
            rawPassword = generateRandomPassword(8);
            kh.setMatKhau(passwordEncoder.encode(rawPassword));
        } else if ("GOOGLE".equals(dto.getLoginProvider())) {
            kh.setMatKhau(null);
        }
        kh.setLoginProvider(dto.getLoginProvider());

        if (dto.getHinhAnh() != null && !dto.getHinhAnh().isEmpty()) {
            String fileName = saveFile(dto.getHinhAnh());
            kh.setHinhAnh(fileName);
        }

        KhachHang savedKh = khachHangRepository.save(kh);

        // Xử lý địa chỉ từ Excel import hoặc từ DTO
        if (dto.getDiaChi() != null && !dto.getDiaChi().trim().isEmpty()) {
            // Tạo DiaChiRequestDTO từ trường diaChi đơn giản
            DiaChiRequestDTO diaChiDTO = DiaChiRequestDTO.builder()
                    .diaChiChiTiet(dto.getDiaChi())
                    .isDefault(true)
                    .build();
            diaChiService.addAddress(savedKh.getId(), diaChiDTO);
        } else if (dto.getDiaChis() != null && !dto.getDiaChis().isEmpty()) {
            // Xử lý địa chỉ từ DTO thông thường
            DiaChiRequestDTO diaChiDTO = dto.getDiaChis().get(0);
            diaChiDTO.setIsDefault(true);
            diaChiService.addAddress(savedKh.getId(), diaChiDTO);
        }

        if ("LOCAL".equals(dto.getLoginProvider()) && rawPassword != null) {
            sendWelcomeEmail(dto.getEmail(), dto.getTenKhachHang(), maKhachHang, rawPassword);
        }

        return khachHangMapper.toResponseDTO(savedKh);
    }

    @Transactional
    @Override
    public KhachHangResponseDTO update(String id, KhachHangRequestDTO dto) {
        KhachHang kh = khachHangRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Khách hàng không tồn tại"));

        String normalizedEmail = normalizeEmail(dto.getEmail());
        String normalizedSdt = normalizeSdt(dto.getSdt());
        String normalizedOldEmail = normalizeEmail(kh.getEmail());
        String normalizedOldSdt = normalizeSdt(kh.getSdt());

        if (!Objects.equals(normalizedEmail, normalizedOldEmail) && normalizedEmail != null && khachHangRepository.existsByEmail(normalizedEmail)) {
            throw new AppException(ErrorCode.EXISTED_EMAIL);
        }

        if (!Objects.equals(normalizedSdt, normalizedOldSdt) && normalizedSdt != null && khachHangRepository.existsBySdt(normalizedSdt)) {
            throw new AppException(ErrorCode.EXISTED_SDT);
        }

        khachHangMapper.updateEntityFromDTO(dto, kh);

        if ("LOCAL".equals(dto.getLoginProvider()) && dto.getMatKhau() != null && !dto.getMatKhau().isEmpty()) {
            kh.setMatKhau(passwordEncoder.encode(dto.getMatKhau()));
        } else if ("GOOGLE".equals(dto.getLoginProvider())) {
            kh.setMatKhau(null);
        }
        kh.setLoginProvider(dto.getLoginProvider());

        if (dto.getHinhAnh() != null && !dto.getHinhAnh().isEmpty()) {
            String fileName = saveFile(dto.getHinhAnh());
            kh.setHinhAnh(fileName);
        }

        KhachHang savedKh = khachHangRepository.save(kh);

        // Xử lý địa chỉ từ Excel import hoặc từ DTO
        if (dto.getDiaChi() != null && !dto.getDiaChi().trim().isEmpty()) {
            // Tạo DiaChiRequestDTO từ trường diaChi đơn giản
            DiaChiRequestDTO diaChiDTO = DiaChiRequestDTO.builder()
                    .diaChiChiTiet(dto.getDiaChi())
                    .isDefault(true)
                    .build();
            List<DiaChiResponseDTO> existingAddresses = diaChiService.getAddressesByCustomerId(savedKh.getId());
            if (existingAddresses.isEmpty()) {
                diaChiService.addAddress(savedKh.getId(), diaChiDTO);
            } else {
                diaChiService.updateAddress(existingAddresses.get(0).getId(), diaChiDTO);
            }
        } else if (dto.getDiaChis() != null && !dto.getDiaChis().isEmpty()) {
            // Xử lý địa chỉ từ DTO thông thường
            DiaChiRequestDTO diaChiDTO = dto.getDiaChis().get(0);
            List<DiaChiResponseDTO> existingAddresses = diaChiService.getAddressesByCustomerId(savedKh.getId());
            if (existingAddresses.isEmpty()) {
                diaChiDTO.setIsDefault(true);
                diaChiService.addAddress(savedKh.getId(), diaChiDTO);
            } else {
                diaChiService.updateAddress(existingAddresses.get(0).getId(), diaChiDTO);
            }
        }

        return khachHangMapper.toResponseDTO(savedKh);
    }

    @Override
    public void delete(String id) {
        if (!khachHangRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Khách hàng không tồn tại");
        }
        khachHangRepository.deleteById(id);
    }

    @Override
    public void resetPassword(String id) {
        KhachHang kh = khachHangRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Khách hàng không tồn tại"));

        if (!"LOCAL".equals(kh.getLoginProvider())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chỉ tài khoản LOCAL mới có thể đặt lại mật khẩu");
        }

        String newPassword = generateRandomPassword(8);
        kh.setMatKhau(passwordEncoder.encode(newPassword));
        khachHangRepository.save(kh);

        sendResetPasswordEmail(kh.getEmail(), kh.getTenKhachHang(), kh.getMaKhachHang(), newPassword);
    }

    @Override
    public Resource exportExcel() {
        try {
            System.out.println("DEBUG: Starting exportExcel");
            List<KhachHang> khachHangs = khachHangRepository.findAll(Pageable.unpaged()).getContent();
            System.out.println("DEBUG: Retrieved " + khachHangs.size() + " customers");

            if (khachHangs.isEmpty()) {
                System.out.println("WARNING: No customers found for export");
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không có khách hàng để xuất Excel");
            }

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Danh sách khách hàng");

            // Tạo header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Tạo header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Mã KH", "Tên", "Email", "SĐT", "Giới tính", "Tên người nhận", "SĐT người nhận", "Địa chỉ", "Trạng thái"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Điền dữ liệu
            int rowNum = 1;
            for (KhachHang kh : khachHangs) {
                if (kh == null) {
                    System.out.println("WARNING: Null customer found at index: " + rowNum);
                    continue;
                }
                System.out.println("DEBUG: Processing customer: " + kh.getMaKhachHang());
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(kh.getMaKhachHang() != null ? kh.getMaKhachHang() : "");
                row.createCell(1).setCellValue(kh.getTenKhachHang() != null ? kh.getTenKhachHang() : "");
                row.createCell(2).setCellValue(kh.getEmail() != null ? kh.getEmail() : "");
                row.createCell(3).setCellValue(kh.getSdt() != null ? kh.getSdt() : "");
                row.createCell(4).setCellValue(kh.getGioiTinh() != null ? (kh.getGioiTinh() == 1 ? "Nam" : "Nữ") : "Không xác định");

                DiaChi defaultAddress = null;
                try {
                    if (kh.getDiaChis() != null && !kh.getDiaChis().isEmpty()) {
                        defaultAddress = kh.getDiaChis().stream()
                                .filter(dc -> dc != null && dc.getIsDefault() != null && dc.getIsDefault())
                                .findFirst()
                                .orElse(kh.getDiaChis().get(0));
                    }
                } catch (Exception e) {
                    System.out.println("ERROR: Failed to process address for customer " + kh.getMaKhachHang() + ": " + e.getMessage());
                }

                row.createCell(5).setCellValue(defaultAddress != null && defaultAddress.getTenNguoiNhan() != null ? defaultAddress.getTenNguoiNhan() : "");
                row.createCell(6).setCellValue(defaultAddress != null && defaultAddress.getSdt() != null ? defaultAddress.getSdt() : "");
                String address = defaultAddress != null
                        ? String.format("%s, %s, %s, %s",
                        defaultAddress.getSoNha() != null ? defaultAddress.getSoNha() : "",
                        defaultAddress.getXaPhuong() != null ? defaultAddress.getXaPhuong() : "",
                        defaultAddress.getQuanHuyen() != null ? defaultAddress.getQuanHuyen() : "",
                        defaultAddress.getThanhPho() != null ? defaultAddress.getThanhPho() : "")
                        : "";
                row.createCell(7).setCellValue(address);
                row.createCell(8).setCellValue(kh.getTrangThai() != null ? (kh.getTrangThai() == 1 ? "Hoạt động" : "Ngừng hoạt động") : "");
            }

            // Tự động điều chỉnh kích thước cột
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Ghi workbook vào ByteArrayOutputStream
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                workbook.write(out);
                System.out.println("DEBUG: Excel file written to output stream");
                return new ByteArrayResource(out.toByteArray());
            } catch (IOException e) {
                System.out.println("ERROR: Failed to write Excel file: " + e.getMessage());
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Không thể tạo file Excel: " + e.getMessage());
            } finally {
                try {
                    workbook.close();
                } catch (IOException e) {
                    System.out.println("WARNING: Failed to close workbook: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Unknown error: " + e.getClass().getSimpleName();
            System.out.println("ERROR: Export Excel failed: " + errorMessage);
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lỗi khi xuất Excel: " + errorMessage);
        }
    }

    @Override
    public List<KhachHang_BanHang_ResponseDTO> getListKhachHangBanHang() {
        List<KhachHang> listKhachHang = khachHangRepository.findAll();
        return listKhachHang.stream()
                .map(khachHangMapper::khachHangBanHang)
                .collect(Collectors.toList());
    }

    private String saveFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        try {
            String uploadDir = "uploads/nhan_vien/";
            String originalFileName = file.getOriginalFilename();
            String cleanFileName = originalFileName.replaceAll("[^a-zA-Z0-9.-]", "_");
            String fileName = System.currentTimeMillis() + "_" + cleanFileName;
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Không thể lưu file: " + e.getMessage());
        }
    }

    private String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private void sendWelcomeEmail(String email, String tenKhachHang, String maKhachHang, String password) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(email);
            helper.setSubject("Chào mừng bạn đến với hệ thống bán áo Polo Nam Haven");
            String htmlContent = "<h3>Kính chào " + tenKhachHang + ",</h3>" +
                    "<p>Tài khoản của bạn đã được tạo thành công.</p>" +
                    "<p><strong>Mã khách hàng:</strong> " + maKhachHang + "</p>" +
                    "<p><strong>Mật khẩu:</strong> " + password + "</p>" +
                    "<p>Vui lòng sử dụng mật khẩu này để đăng nhập vào hệ thống.</p>" +
                    "<p>Trân trọng,<br>Hệ thống bán áo Polo Nam Haven</p>";
            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Không thể gửi email: " + e.getMessage());
        }
    }

    private void sendResetPasswordEmail(String email, String tenKhachHang, String maKhachHang, String password) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(email);
            helper.setSubject("Đặt lại mật khẩu tài khoản");
            String htmlContent = "<h3>Kính chào " + tenKhachHang + ",</h3>" +
                    "<p>Mật khẩu mới của bạn đã được tạo.</p>" +
                    "<p><strong>Mã khách hàng:</strong> " + maKhachHang + "</p>" +
                    "<p><strong>Mật khẩu mới:</strong> " + password + "</p>" +
                    "<p>Vui lòng đăng nhập và đổi mật khẩu ngay lần đầu sử dụng.</p>" +
                    "<p>Trân trọng,<br>Hệ thống quản lý khách hàng</p>";
            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Không thể gửi email: " + e.getMessage());
        }
    }

    @Override
    public void changePassword(String userId, String currentPassword, String newPassword) {
        KhachHang khachHang = khachHangRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Khách hàng không tồn tại"));

        if (!passwordEncoder.matches(currentPassword, khachHang.getMatKhau())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mật khẩu hiện tại không đúng");
        }

        if (newPassword == null || newPassword.length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mật khẩu mới phải có ít nhất 8 ký tự");
        }

        khachHang.setMatKhau(passwordEncoder.encode(newPassword));
        khachHangRepository.save(khachHang);
    }

    @Override
    public String sendOtpForChangePassword(String userId) {
        KhachHang khachHang = khachHangRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Khách hàng không tồn tại"));

        String otp = generateOtp();

        khachHang.setOtpChangePassword(otp);
        khachHang.setOtpExpiry(System.currentTimeMillis() + (10 * 60 * 1000));
        khachHangRepository.save(khachHang);

        sendOtpEmail(khachHang.getEmail(), khachHang.getTenKhachHang(), otp);

        return otp;
    }

    @Override
    public void changePasswordWithOtp(String userId, String currentPassword, String newPassword, String otp) {
        KhachHang khachHang = khachHangRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Khách hàng không tồn tại"));

        if (!passwordEncoder.matches(currentPassword, khachHang.getMatKhau())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mật khẩu hiện tại không đúng");
        }

        if (khachHang.getOtpChangePassword() == null || !khachHang.getOtpChangePassword().equals(otp)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã xác minh không đúng");
        }

        if (khachHang.getOtpExpiry() == null || System.currentTimeMillis() > khachHang.getOtpExpiry()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã xác minh đã hết hạn");
        }

        if (newPassword == null || newPassword.length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mật khẩu mới phải có ít nhất 8 ký tự");
        }

        khachHang.setMatKhau(passwordEncoder.encode(newPassword));
        khachHang.setOtpChangePassword(null);
        khachHang.setOtpExpiry(null);
        khachHangRepository.save(khachHang);
    }

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }

    private void sendOtpEmail(String email, String tenKhachHang, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(email);
            helper.setSubject("Mã xác minh thay đổi mật khẩu");
            String htmlContent = "<h3>Kính chào " + tenKhachHang + ",</h3>" +
                    "<p>Bạn đã yêu cầu thay đổi mật khẩu tài khoản.</p>" +
                    "<p><strong>Mã xác minh của bạn là:</strong> <span style='font-size: 24px; font-weight: bold; color: #4ba27b;'>" + otp + "</span></p>" +
                    "<p>Mã này có hiệu lực trong 10 phút.</p>" +
                    "<p>Nếu bạn không yêu cầu thay đổi mật khẩu, vui lòng bỏ qua email này.</p>" +
                    "<p>Trân trọng,<br>Hệ thống bán áo Polo Nam Haven</p>";
            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Không thể gửi email OTP: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void importExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File Excel không được để trống");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chỉ hỗ trợ file Excel (.xlsx, .xls)");
        }

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            int successCount = 0;
            int errorCount = 0;
            StringBuilder errorMessages = new StringBuilder();

            // Bỏ qua header row
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    KhachHangRequestDTO dto = parseRowToDto(row);
                    if (dto != null) {
                        create(dto);
                        successCount++;
                    }
                } catch (Exception e) {
                    errorCount++;
                    String rowInfo = "Dòng " + (i + 1) + ": " + e.getMessage();
                    errorMessages.append(rowInfo).append("\n");
                }
            }

            if (errorCount > 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    String.format("Import hoàn tất với %d khách hàng thành công, %d lỗi:\n%s", 
                    successCount, errorCount, errorMessages.toString()));
            }

        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Không thể đọc file Excel: " + e.getMessage());
        }
    }

    @Override
    public Resource downloadTemplate() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Template Khách Hàng");
            
            // Tạo header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Tên khách hàng*", "Email*", "Số điện thoại*", "Giới tính", "Ngày sinh", "Trạng thái", "Địa chỉ"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                
                // Style cho header
                CellStyle headerStyle = workbook.createCellStyle();
                Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerStyle.setFont(headerFont);
                headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                headerStyle.setBorderTop(BorderStyle.THIN);
                headerStyle.setBorderBottom(BorderStyle.THIN);
                headerStyle.setBorderLeft(BorderStyle.THIN);
                headerStyle.setBorderRight(BorderStyle.THIN);
                cell.setCellStyle(headerStyle);
            }
            
            // Tạo sample data row
            Row sampleRow = sheet.createRow(1);
            sampleRow.createCell(0).setCellValue("Nguyễn Văn A");
            sampleRow.createCell(1).setCellValue("nguyenvana@email.com");
            sampleRow.createCell(2).setCellValue("0123456789");
            sampleRow.createCell(3).setCellValue("Nam");
            sampleRow.createCell(4).setCellValue("1990-01-01");
            sampleRow.createCell(5).setCellValue("Hoạt động");
            sampleRow.createCell(6).setCellValue("123 Đường ABC, Quận 1, TP.HCM");
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Convert to Resource
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            byte[] bytes = outputStream.toByteArray();
            
            return new ByteArrayResource(bytes);
            
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Không thể tạo template Excel: " + e.getMessage());
        }
    }

    private KhachHangRequestDTO parseRowToDto(Row row) {
        try {
            String tenKhachHang = getCellValueAsString(row.getCell(0));
            String email = getCellValueAsString(row.getCell(1));
            String sdt = getCellValueAsString(row.getCell(2));
            String gioiTinhStr = getCellValueAsString(row.getCell(3));
            String ngaySinhStr = getCellValueAsString(row.getCell(4));
            String trangThaiStr = getCellValueAsString(row.getCell(5));
            String diaChi = getCellValueAsString(row.getCell(6));

            // Validation
            if (tenKhachHang == null || tenKhachHang.trim().isEmpty()) {
                throw new IllegalArgumentException("Tên khách hàng không được để trống");
            }

            if (email == null || email.trim().isEmpty()) {
                throw new IllegalArgumentException("Email không được để trống");
            }

            if (sdt == null || sdt.trim().isEmpty()) {
                throw new IllegalArgumentException("Số điện thoại không được để trống");
            }

            // Parse giới tính
            Byte gioiTinh = null;
            if (gioiTinhStr != null && !gioiTinhStr.trim().isEmpty()) {
                if (gioiTinhStr.equalsIgnoreCase("Nam") || gioiTinhStr.equals("1")) {
                    gioiTinh = 1;
                } else if (gioiTinhStr.equalsIgnoreCase("Nữ") || gioiTinhStr.equals("0")) {
                    gioiTinh = 0;
                } else {
                    gioiTinh = 1; // Default to Nam
                }
            } else {
                gioiTinh = 1; // Default to Nam
            }

            // Parse trạng thái
            Byte trangThai = null;
            if (trangThaiStr != null && !trangThaiStr.trim().isEmpty()) {
                if (trangThaiStr.equalsIgnoreCase("Hoạt động") || trangThaiStr.equals("1")) {
                    trangThai = 1;
                } else if (trangThaiStr.equalsIgnoreCase("Ngừng hoạt động") || trangThaiStr.equals("0")) {
                    trangThai = 0;
                } else {
                    trangThai = 1; // Default to Hoạt động
                }
            } else {
                trangThai = 1; // Default to Hoạt động
            }

            // Parse ngày sinh
            LocalDate ngaySinh = null;
            if (ngaySinhStr != null && !ngaySinhStr.trim().isEmpty()) {
                try {
                    ngaySinh = LocalDate.parse(ngaySinhStr);
                } catch (Exception e) {
                    // Try different date formats
                    try {
                        ngaySinh = LocalDate.parse(ngaySinhStr, java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    } catch (Exception e2) {
                        ngaySinh = null; // Set to null if can't parse
                    }
                }
            }

            return KhachHangRequestDTO.builder()
                    .tenKhachHang(tenKhachHang.trim())
                    .email(email.trim())
                    .sdt(sdt.trim())
                    .gioiTinh(gioiTinh)
                    .ngaySinh(ngaySinh)
                    .trangThai(trangThai)
                    .diaChi(diaChi != null ? diaChi.trim() : null)
                    .loginProvider("LOCAL")
                    .build();

        } catch (Exception e) {
            throw new IllegalArgumentException("Lỗi parse dữ liệu: " + e.getMessage());
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                return String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }
}