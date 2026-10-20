package com.eventhub.services;

import com.eventhub.services.impl.QrCodeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class QrCodeServiceTest {

    private QrCodeServiceImpl qrCodeService;

    @BeforeEach
    void setUp() {
        qrCodeService = new QrCodeServiceImpl();
    }

    @Test
    @DisplayName("Should generate valid Base64 PNG QR code string")
    void generateQrCodeBase64_Success() {
        String content = "eventhub://tickets/TK-20261021-TEST";
        String base64 = qrCodeService.generateQrCodeBase64(content, 200, 200);

        assertThat(base64).isNotNull();
        assertThat(base64).startsWith("data:image/png;base64,");
        assertThat(base64.length()).isGreaterThan(50);
    }

    @Test
    @DisplayName("Should generate valid PNG byte array")
    void generateQrCodePng_Success() {
        String content = "https://eventhub.com";
        byte[] bytes = qrCodeService.generateQrCodePng(content, 200, 200);

        assertThat(bytes).isNotNull();
        assertThat(bytes.length).isGreaterThan(0);
        // Standard PNG magic bytes: 0x89 0x50 0x4E 0x47
        assertThat(bytes[0]).isEqualTo((byte) 0x89);
        assertThat(bytes[1]).isEqualTo((byte) 0x50);
        assertThat(bytes[2]).isEqualTo((byte) 0x4E);
        assertThat(bytes[3]).isEqualTo((byte) 0x47);
    }
}
