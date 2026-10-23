package com.eventhub.enums;

import lombok.Getter;

@Getter
public enum ConcessionCategory {
    COMBO("Combo Bắp & Nước Ưu Đãi"),
    POPCORN("Bắp Rang Bơ Nóng Giòn"),
    BEVERAGE("Nước Giải Khát & Trà"),
    SNACK("Món Ăn Vặt & Snack");

    private final String title;

    ConcessionCategory(String title) {
        this.title = title;
    }
}
