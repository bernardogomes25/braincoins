package com.lab3.moeda.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

public final class QrCodeUtil {

    private static final Logger log = LoggerFactory.getLogger(QrCodeUtil.class);

    private QrCodeUtil() {}

    public static String gerarQrCodeBase64(String conteudo) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(conteudo, BarcodeFormat.QR_CODE, 200, 200);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", os);
            return Base64.getEncoder().encodeToString(os.toByteArray());
        } catch (Exception e) {
            log.warn("Falha ao gerar QR code para '{}': {}", conteudo, e.getMessage());
            return null;
        }
    }
}
