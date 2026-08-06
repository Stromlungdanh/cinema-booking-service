package com.cinema.booking.common.util;

import java.security.SecureRandom;
import java.util.Base64;

// Sinh token reset mat khau - can entropy cao (khac TicketCodeGenerator
// la ma ngan de nguoi doc), khong the doan duoc.
public final class PasswordResetTokenGenerator {

    private static final int BYTE_LENGTH = 32;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordResetTokenGenerator() {
    }

    public static String generate() {
        byte[] bytes = new byte[BYTE_LENGTH];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
