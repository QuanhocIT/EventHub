package com.eventhub.enums;

public enum LoyaltyActivityType {
    SIGNUP_BONUS("Điểm chào mừng thành viên mới"),
    BOOKING_REWARD("Tích điểm đặt vé sự kiện/phim"),
    REVIEW_WITH_PHOTO("Thưởng đánh giá kèm hình ảnh thực tế"),
    GATE_CHECKIN_BONUS("Thưởng check-in tại cổng sự kiện"),
    VOUCHER_REDEMPTION("Đổi điểm nhận voucher giảm giá");

    private final String description;

    LoyaltyActivityType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
