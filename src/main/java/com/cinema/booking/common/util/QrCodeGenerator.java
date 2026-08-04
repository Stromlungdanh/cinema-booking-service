package com.cinema.booking.common.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

// Sinh QR code that (khong phai chuoi gia) tu 1 chuoi noi dung, tra ve
// dang data URI de FE nhung thang vao the <img src="...">.
public final class QrCodeGenerator {

    private static final int SIZE_PX = 300;

    private QrCodeGenerator() {
    }

    public static String toBase64Png(String content) {
        try {
            BitMatrix matrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, SIZE_PX, SIZE_PX);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (WriterException | IOException e) {
            throw new IllegalStateException("Khong the sinh QR code cho: " + content, e);
        }
    }
}
