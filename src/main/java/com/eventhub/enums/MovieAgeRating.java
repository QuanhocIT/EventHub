package com.eventhub.enums;

import lombok.Getter;

@Getter
public enum MovieAgeRating {
    P("P", "Phim được phép phổ biến cho người xem ở mọi độ tuổi"),
    K("K", "Người xem dưới 13 tuổi cần có người giám hộ đi cùng"),
    T13("T13", "Phim được phổ biến cho người xem từ đủ 13 tuổi trở lên"),
    T16("T16", "Phim được phổ biến cho người xem từ đủ 16 tuổi trở lên"),
    T18("T18", "Phim cấm phổ biến cho người xem dưới 18 tuổi"),
    C("C", "Phim không được phép phổ biến");

    private final String code;
    private final String description;

    MovieAgeRating(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
