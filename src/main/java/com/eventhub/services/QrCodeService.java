package com.eventhub.services;

public interface QrCodeService {

    String generateQrCodeBase64(String content, int width, int height);

    byte[] generateQrCodePng(String content, int width, int height);
}
