package com.datn.backend.service.nhan_vien;

import com.datn.backend.dto.request.NhanVienRequestDTO;
import com.datn.backend.dto.response.NhanVienResponseDTO;
import com.datn.backend.entity.ChucVu;
import com.datn.backend.entity.NhanVien;
import com.datn.backend.exception.AppException;
import com.datn.backend.exception.ErrorCode;
import com.datn.backend.mapper.NhanVienMapper;
import com.datn.backend.repository.ChucVuRepository;
import com.datn.backend.repository.NhanVienRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NhanVienServiceImpl implements NhanVienService {
    private final NhanVienRepository nhanVienRepository;
    private final ChucVuRepository chucVuRepository;
    private final NhanVienMapper nhanVienMapper;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    @Override
    public Page<NhanVienResponseDTO> findAll(Pageable pageable) {
        return nhanVienRepository.findAll(pageable)
                .map(nhanVienMapper::toResponseDTO);
    }

    @Override
    public Page<NhanVienResponseDTO> findAllWithFilters(Pageable pageable, String keyword, Byte status, Byte gioiTinh) {
        return nhanVienRepository.findWithFilters(keyword, status, gioiTinh, pageable)
                .map(nhanVienMapper::toResponseDTO);
    }

    @Override
    public NhanVienResponseDTO findById(String id) {
        NhanVien nv = nhanVienRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nhân viên không tồn tại"));
        return nhanVienMapper.toResponseDTO(nv);
    }

    @Override
    public NhanVienResponseDTO create(NhanVienRequestDTO dto) {
        // Kiểm tra trùng lặp
        if (nhanVienRepository.existsByEmail(dto.getEmail())) {
            throw new AppException(ErrorCode.EXISTED_EMAIL);
        }
        if (nhanVienRepository.existsBySdt(dto.getSdt())) {
            throw new AppException(ErrorCode.EXISTED_SDT);
        }
        if (nhanVienRepository.existsByCccd(dto.getCccd())) {
            throw new AppException(ErrorCode.EXISTED_CCCD);
        }

        NhanVien nv = nhanVienMapper.toEntity(dto);
        nv.setNgayTao(System.currentTimeMillis());
        nv.setNgaySua(System.currentTimeMillis());

        // Sinh mã nhân viên
        long count = nhanVienRepository.count();
        String maNhanVien = String.format("NV%05d", count + 1);
        nv.setMaNhanVien(maNhanVien);

        // Xử lý mật khẩu và loginProvider
        String rawPassword = null;
        if ("LOCAL".equals(dto.getLoginProvider())) {
            rawPassword = generateRandomPassword(8); // Tạo mật khẩu ngẫu nhiên
            nv.setMatKhau(passwordEncoder.encode(rawPassword));
        } else if ("GOOGLE".equals(dto.getLoginProvider())) {
            nv.setMatKhau(null);
        }
        nv.setLoginProvider(dto.getLoginProvider());

        // Lưu file ảnh (nếu có)
        if (dto.getHinhAnh() != null && !dto.getHinhAnh().isEmpty()) {
            String fileName = saveFile(dto.getHinhAnh());
            nv.setHinhAnh(fileName);
        }

        // Gán chức vụ cố định là NHANVIEN
        ChucVu chucVu = chucVuRepository.findByMaChucVu("NHANVIEN");
        if (chucVu == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chức vụ NHANVIEN không tồn tại");
        }
        nv.setChucVu(chucVu);

        // Đảm bảo gioiTinh không null
        if (nv.getGioiTinh() == null) {
            nv.setGioiTinh((byte) 1); // Mặc định là Nam
        }

        nhanVienRepository.save(nv);

        // Gửi email nếu là Local
        if ("LOCAL".equals(dto.getLoginProvider()) && rawPassword != null) {
            sendWelcomeEmail(dto.getEmail(), dto.getTenNhanVien(), maNhanVien, rawPassword);
        }

        return nhanVienMapper.toResponseDTO(nv);
    }

    @Override
    public NhanVienResponseDTO update(String id, NhanVienRequestDTO dto) {
        NhanVien nv = nhanVienRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nhân viên không tồn tại"));

        // Kiểm tra trùng lặp (bỏ qua nếu giá trị không thay đổi)
        if (!nv.getEmail().equals(dto.getEmail()) && nhanVienRepository.existsByEmail(dto.getEmail())) {
            throw new AppException(ErrorCode.EXISTED_EMAIL);
        }
        if (!nv.getSdt().equals(dto.getSdt()) && nhanVienRepository.existsBySdt(dto.getSdt())) {
            throw new AppException(ErrorCode.EXISTED_SDT);
        }
        if (!nv.getCccd().equals(dto.getCccd()) && nhanVienRepository.existsByCccd(dto.getCccd())) {
            throw new AppException(ErrorCode.EXISTED_CCCD);
        }

        nhanVienMapper.updateEntityFromDTO(dto, nv);
        nv.setNgaySua(System.currentTimeMillis());

        // Xử lý mật khẩu và loginProvider
        if ("LOCAL".equals(dto.getLoginProvider()) && dto.getMatKhau() != null && !dto.getMatKhau().isEmpty()) {
            nv.setMatKhau(passwordEncoder.encode(dto.getMatKhau()));
        } else if ("GOOGLE".equals(dto.getLoginProvider())) {
            nv.setMatKhau(null);
        }
        nv.setLoginProvider(dto.getLoginProvider());

        // Cập nhật file ảnh mới nếu có
        if (dto.getHinhAnh() != null && !dto.getHinhAnh().isEmpty()) {
            String fileName = saveFile(dto.getHinhAnh());
            nv.setHinhAnh(fileName);
        }

        // Gán chức vụ cố định là NHANVIEN
        ChucVu chucVu = chucVuRepository.findByMaChucVu("NHANVIEN");
        if (chucVu == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chức vụ NHANVIEN không tồn tại");
        }
        nv.setChucVu(chucVu);

        // Đảm bảo gioiTinh không null
        if (nv.getGioiTinh() == null) {
            nv.setGioiTinh((byte) 1); // Mặc định là Nam
        }

        nhanVienRepository.save(nv);
        return nhanVienMapper.toResponseDTO(nv);
    }

    @Override
    public void delete(String id) {
        if (!nhanVienRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Nhân viên không tồn tại");
        }
        nhanVienRepository.deleteById(id);
    }

    @Override
    public void resetPassword(String id) {
        NhanVien nv = nhanVienRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nhân viên không tồn tại"));

        if (!"LOCAL".equals(nv.getLoginProvider())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chỉ tài khoản LOCAL mới có thể đặt lại mật khẩu");
        }

        String newPassword = generateRandomPassword(8);
        nv.setMatKhau(passwordEncoder.encode(newPassword));
        nv.setNgaySua(System.currentTimeMillis());
        nhanVienRepository.save(nv);

        sendResetPasswordEmail(nv.getEmail(), nv.getTenNhanVien(), nv.getMaNhanVien(), newPassword);
    }

    @Override
    public Resource exportExcel() {
        List<NhanVien> nhanViens = nhanVienRepository.findAll(Pageable.unpaged()).getContent();
        System.out.println("Found " + nhanViens.size() + " employees");
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Danh sách nhân viên");

        // Tạo style cho header
        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Tạo header
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Mã NV", "Tên", "Email", "SĐT", "Ngày sinh", "Giới tính", "Chức vụ", "Địa chỉ", "Trạng thái"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Định dạng ngày tháng
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Điền dữ liệu
        int rowNum = 1;
        for (NhanVien nv : nhanViens) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(nv.getMaNhanVien() != null ? nv.getMaNhanVien() : "");
            row.createCell(1).setCellValue(nv.getTenNhanVien() != null ? nv.getTenNhanVien() : "");
            row.createCell(2).setCellValue(nv.getEmail() != null ? nv.getEmail() : "");
            row.createCell(3).setCellValue(nv.getSdt() != null ? nv.getSdt() : "");
            row.createCell(4).setCellValue(nv.getNgaySinh() != null ? nv.getNgaySinh().format(dateFormatter) : "");
            row.createCell(5).setCellValue(nv.getGioiTinh() != null ? (nv.getGioiTinh() == 1 ? "Nam" : "Nữ") : "Không xác định");
            row.createCell(6).setCellValue(nv.getChucVu() != null ? nv.getChucVu().getTenChucVu() : "");
            row.createCell(7).setCellValue(nv.getDiaChi() != null ? nv.getDiaChi() : "");
            row.createCell(8).setCellValue(nv.getTrangThai() != null ? (nv.getTrangThai() == 1 ? "Hoạt động" : "Ngừng hoạt động") : "");
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            workbook.write(out);
            return new ByteArrayResource(out.toByteArray());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Không thể tạo file Excel");
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }

    @Override
    public List<NhanVienResponseDTO> importExcel(MultipartFile file) {
        List<NhanVienResponseDTO> results = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                NhanVienRequestDTO dto = new NhanVienRequestDTO();
                try {
                    // Lấy và làm sạch CCCD
                    String cccd = row.getCell(0) != null ? row.getCell(0).toString().trim() : "";
                    if (cccd.isEmpty()) {
                        throw new Exception("CCCD không được để trống tại dòng " + (i + 1));
                    }
                    System.out.println("Processing CCCD: '" + cccd + "' at row " + (i + 1));
                    if (nhanVienRepository.existsByCccd(cccd)) {
                        throw new Exception("CCCD đã tồn tại: '" + cccd + "' tại dòng " + (i + 1));
                    }
                    dto.setCccd(cccd);

                    // Lấy và làm sạch các trường khác
                    String tenNhanVien = row.getCell(1) != null ? row.getCell(1).toString().trim() : "";
                    if (tenNhanVien.isEmpty()) {
                        throw new Exception("Tên nhân viên không được để trống tại dòng " + (i + 1));
                    }
                    dto.setTenNhanVien(tenNhanVien);

                    String email = row.getCell(2) != null ? row.getCell(2).toString().trim() : "";
                    if (email.isEmpty()) {
                        throw new Exception("Email không được để trống tại dòng " + (i + 1));
                    }
                    if (nhanVienRepository.existsByEmail(email)) {
                        throw new Exception("Email đã tồn tại: '" + email + "' tại dòng " + (i + 1));
                    }
                    dto.setEmail(email);

                    String sdt = row.getCell(3) != null ? row.getCell(3).toString().trim() : "";
                    if (sdt.isEmpty()) {
                        throw new Exception("Số điện thoại không được để trống tại dòng " + (i + 1));
                    }
                    if (nhanVienRepository.existsBySdt(sdt)) {
                        throw new Exception("Số điện thoại đã tồn tại: '" + sdt + "' tại dòng " + (i + 1));
                    }
                    dto.setSdt(sdt);

                    String dateStr = row.getCell(4) != null ? row.getCell(4).toString().trim() : "";
                    if (!dateStr.isEmpty()) {
                        try {
                            dto.setNgaySinh(LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                        } catch (Exception e) {
                            throw new Exception("Ngày sinh không đúng định dạng (dd/MM/yyyy): '" + dateStr + "' tại dòng " + (i + 1));
                        }
                    }

                    String gioiTinhStr = row.getCell(5) != null ? row.getCell(5).toString().trim() : "";
                    dto.setGioiTinh(gioiTinhStr.equalsIgnoreCase("Nam") ? (byte) 1 : (byte) 0);

                    // Cố định chức vụ là NHANVIEN
                    dto.setChucVu("NHANVIEN");

                    dto.setDiaChi(row.getCell(7) != null ? row.getCell(7).toString().trim() : "");

                    String trangThaiStr = row.getCell(8) != null ? row.getCell(8).toString().trim() : "";
                    // Chấp nhận cả "Kích hoạt" lẫn "Hoạt động"
                    dto.setTrangThai((trangThaiStr.equalsIgnoreCase("Kích hoạt") || trangThaiStr.equalsIgnoreCase("Hoạt động")) ? (byte) 1 : (byte) 0);

                    // Tránh gửi mail khi import: tự tạo tài khoản nhanh tại đây
                    dto.setLoginProvider("LOCAL");

                    // Map sang entity và lưu trực tiếp để tránh overhead (email, xử lý ảnh,...)
                    NhanVien nv = nhanVienMapper.toEntity(dto);
                    nv.setNgayTao(System.currentTimeMillis());
                    nv.setNgaySua(System.currentTimeMillis());

                    // Sinh mã nhân viên
                    long count = nhanVienRepository.count();
                    String maNhanVien = String.format("NV%05d", count + 1);
                    nv.setMaNhanVien(maNhanVien);

                    // Mặc định giới tính nếu null
                    if (nv.getGioiTinh() == null) {
                        nv.setGioiTinh((byte) 1);
                    }

                    // Gán chức vụ NHANVIEN
                    ChucVu chucVu = chucVuRepository.findByMaChucVu("NHANVIEN");
                    if (chucVu == null) {
                        throw new Exception("Chức vụ NHANVIEN không tồn tại");
                    }
                    nv.setChucVu(chucVu);

                    // Tạo mật khẩu ngẫu nhiên và mã hoá
                    String rawPassword = generateRandomPassword(8);
                    nv.setMatKhau(passwordEncoder.encode(rawPassword));

                    // Không xử lý ảnh ở import để tránh I/O lâu
                    nv.setHinhAnh(null);

                    nhanVienRepository.save(nv);
                    results.add(nhanVienMapper.toResponseDTO(nv));
                } catch (Exception e) {
                    errors.add(e.getMessage());
                    System.err.println("Error at row " + (i + 1) + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không thể đọc file Excel: " + e.getMessage());
        }
        // Không ném lỗi tổng thể để tránh frontend phải reload. Trả về danh sách thành công;
        // Controller sẽ gói cả errors (nếu có) trong response body.
        return results;
    }

    private String saveFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        try {
            String uploadDir = "Uploads/nhan_vien/";
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

    @Override
    public Resource downloadTemplate() {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Template Nhan Vien");

            // Tạo header (tùy chỉnh theo field của NhanVien)
            String[] headers = {
                    "Tên nhân viên", "Email", "Số điện thoại", "Giới tính (Nam/Nữ)",
                    "Ngày sinh (YYYY-MM-DD)", "Trạng thái (Hoạt động/Ngừng hoạt động)",
                    "Địa chỉ", "CCCD"  // Thêm các field đặc thù của nhân viên
            };
            Row headerRow = sheet.createRow(0);
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
            sampleRow.createCell(7).setCellValue("012345678901");  // Sample CCCD

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

    private void sendWelcomeEmail(String email, String tenNhanVien, String maNhanVien, String password) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(email);
            helper.setSubject("Chào mừng bạn đến với hệ thống bán áo Polo Nam Haven");
            String htmlContent = "<h3>Kính chào " + tenNhanVien + ",</h3>" +
                    "<p>Tài khoản của bạn đã được tạo thành công.</p>" +
                    "<p><strong>Mã nhân viên:</strong> " + maNhanVien + "</p>" +
                    "<p><strong>Mật khẩu:</strong> " + password + "</p>" +
                    "<p>Vui lòng sử dụng mật khẩu này để đăng nhập vào hệ thống.</p>" +
                    "<p>Trân trọng,<br>Hệ thống bán áo Polo Nam Haven</p>";
            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Không thể gửi email: " + e.getMessage());
        }
    }

    private void sendResetPasswordEmail(String email, String tenNhanVien, String maNhanVien, String password) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(email);
            helper.setSubject("Đặt lại mật khẩu tài khoản");
            String htmlContent = "<h3>Kính chào " + tenNhanVien + ",</h3>" +
                    "<p>Mật khẩu mới của bạn đã được tạo.</p>" +
                    "<p><strong>Mã nhân viên:</strong> " + maNhanVien + "</p>" +
                    "<p><strong>Mật khẩu mới:</strong> " + password + "</p>" +
                    "<p>Vui lòng đăng nhập và đổi mật khẩu ngay lần đầu sử dụng.</p>" +
                    "<p>Trân trọng,<br>Hệ thống quản lý nhân viên</p>";
            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Không thể gửi email: " + e.getMessage());
        }
    }
}