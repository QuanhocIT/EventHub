package com.eventhub.enums;

public enum LoyaltyTier {
    MEMBER(0, 0, "Thành viên Mới"),
    SILVER(500, 5, "Hội viên Bạc (Silver)"),
    GOLD(1500, 10, "Hội viên Vàng (Gold)"),
    DIAMOND(3000, 15, "Hội viên Kim Cương (VIP Diamond)");

    private final int minPoints;
    private final int discountPercent;
    private final String displayName;

    LoyaltyTier(int minPoints, int discountPercent, String displayName) {
        this.minPoints = minPoints;
        this.discountPercent = discountPercent;
        this.displayName = displayName;
    }

    public int getMinPoints() {
        return minPoints;
    }

    public int getDiscountPercent() {
        return discountPercent;
    }

    public String getDisplayName() {
        return displayName;
    }
}
