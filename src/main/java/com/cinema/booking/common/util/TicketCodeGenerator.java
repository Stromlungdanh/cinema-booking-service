package com.cinema.booking.common.util;

import java.security.SecureRandom;

// Sinh ma ve ngan, de doc (bo cac ky tu de nham lan: 0/O, 1/I). Khong
// retry khi trung `bookings.ticket_code` (UNIQUE) - khong gian 32^8 du
// lon de bo qua rui ro trung o quy mo project hoc tap nay.
public final class TicketCodeGenerator {

    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int LENGTH = 8;
    private static final SecureRandom RANDOM = new SecureRandom();

    private TicketCodeGenerator() {
    }

    public static String generate() {
        StringBuilder code = new StringBuilder("CB-");
        for (int i = 0; i < LENGTH; i++) {
            code.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return code.toString();
    }
}
