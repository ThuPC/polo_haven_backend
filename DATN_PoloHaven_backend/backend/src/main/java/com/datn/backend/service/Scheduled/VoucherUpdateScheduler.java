package com.datn.backend.service.Scheduled; // Bạn có thể đặt trong package service của mình

import com.datn.backend.repository.PhieuGiamGiaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VoucherUpdateScheduler {

    private static final Logger logger = LoggerFactory.getLogger(VoucherUpdateScheduler.class);

    @Autowired
    private PhieuGiamGiaRepository phieuGiamGiaRepository;

    // Các hằng số để code dễ đọc và dễ bảo trì
    private static final byte EXPIRED_STATUS = 2;
    private static final byte ACTIVE_STATUS = 1;

    /**
     * Tự động chạy vào lúc 00:05 sáng mỗi ngày.
     * Tìm các phiếu giảm giá có ngày kết thúc đã qua và trạng thái đang hoạt động,
     * sau đó cập nhật trạng thái của chúng thành 2 (đã hết hạn).
     *
     * Cấu trúc Cron: "giây phút giờ ngày-trong-tháng tháng ngày-trong-tuần"
     * "0 5 0 * * *" = 0 giây, 5 phút, 0 giờ (nửa đêm), mỗi ngày.
     */
    @Scheduled(cron = "0 5 0 * * *")
    @Transactional // BẮT BUỘC phải có khi gọi một phương thức @Modifying trong repository
    public void scheduleVoucherStatusUpdate() {
        logger.info("Scheduler: Bắt đầu tác vụ kiểm tra và cập nhật phiếu giảm giá hết hạn.");

        try {
            // Lấy thời gian hiện tại dưới dạng mili giây để so sánh với trường ngayKetThuc
            long currentTime = System.currentTimeMillis();

            int updatedCount = phieuGiamGiaRepository.updateStatusForExpiredVouchers(
                    currentTime,
                    EXPIRED_STATUS,
                    ACTIVE_STATUS
            );

            if (updatedCount > 0) {
                logger.info("Scheduler: Đã cập nhật thành công {} phiếu giảm giá sang trạng thái hết hạn (2).", updatedCount);
            } else {
                logger.info("Scheduler: Không tìm thấy phiếu giảm giá nào cần cập nhật.");
            }

        } catch (Exception e) {
            logger.error("Scheduler: Gặp lỗi trong quá trình cập nhật trạng thái phiếu giảm giá.", e);
        }

        logger.info("Scheduler: Kết thúc tác vụ.");
    }
}