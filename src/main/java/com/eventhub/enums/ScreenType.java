package com.eventhub.enums;

import lombok.Getter;

@Getter
public enum ScreenType {
    STANDARD_2D("2D Tiêu Chuẩn"),
    IMAX_LASER("IMAX Laser Cực Đại"),
    DOLBY_ATMOS("Dolby Atmos Đa Chiều"),
    GOLD_CLASS("Gold Class Thượng Hạng"),
    SCREEN_X("ScreenX 270 Độ");

    private final String displayName;

    ScreenType(String displayName) {
        this.displayName = displayName;
    }
}
