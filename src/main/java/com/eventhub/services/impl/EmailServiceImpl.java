package com.eventhub.services.impl;

import com.eventhub.services.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    @Async("taskExecutor")
    @Override
    public CompletableFuture<Boolean> sendBookingConfirmation(
            String toEmail,
            String recipientName,
            String bookingCode,
            String eventTitle,
            String totalAmount
    ) {
        log.info("[Async Email] Starting delivery of booking confirmation to: {} on thread: {}",
                toEmail, Thread.currentThread().getName());

        try {
            // Simulate network latency of SMTP relay (50ms)
            Thread.sleep(50);

            log.info("""
                    =================== [EMAIL DISPATCHED] ===================
                    To: {} ({})
                    Subject: [EventHub] Xác nhận đặt vé thành công - Đơn hàng {}
                    Content:
                      Chào {},
                      Chúc mừng bạn đã đặt vé thành công cho sự kiện: '{}'!
                      - Mã đơn hàng: {}
                      - Tổng thanh toán: {} VND
                      - Vui lòng kiểm tra mục 'Vé của tôi' trên ứng dụng để lấy mã QR Code vào cổng.
                      Trân trọng,
                      Đội ngũ EventHub.
                    ==========================================================
                    """, toEmail, recipientName, bookingCode, recipientName, eventTitle, bookingCode, totalAmount);

            return CompletableFuture.completedFuture(true);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[Async Email] Delivery interrupted for: {}", toEmail, e);
            return CompletableFuture.completedFuture(false);
        }
    }

    @Async("taskExecutor")
    @Override
    public CompletableFuture<Boolean> sendEventCancelledNotification(
            String toEmail,
            String recipientName,
            String eventTitle,
            String reason
    ) {
        log.info("[Async Email] Delivering cancellation notice to: {} on thread: {}",
                toEmail, Thread.currentThread().getName());

        try {
            Thread.sleep(50);
            log.info("""
                    =================== [EMAIL DISPATCHED] ===================
                    To: {} ({})
                    Subject: [EventHub] Thông báo hủy sự kiện: '{}'
                    Content:
                      Chào {},
                      Ban tổ chức rất tiếc phải thông báo sự kiện '{}' đã bị hủy.
                      Lý do: {}
                      Khoản tiền đặt vé sẽ được hoàn trả tự động vào tài khoản của bạn trong 3-5 ngày làm việc.
                      Trân trọng,
                      Đội ngũ EventHub.
                    ==========================================================
                    """, toEmail, recipientName, eventTitle, recipientName, eventTitle, reason);

            return CompletableFuture.completedFuture(true);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[Async Email] Delivery interrupted for: {}", toEmail, e);
            return CompletableFuture.completedFuture(false);
        }
    }
}
