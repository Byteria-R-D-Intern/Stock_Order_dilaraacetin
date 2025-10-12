package com.example.stock_order.util;

public final class CreditCardUtil {
    private CreditCardUtil(){}

    public static boolean luhnValid(String pan) {
        int sum = 0; boolean alt = false;
        for (int i = pan.length() - 1; i >= 0; i--) {
            char c = pan.charAt(i);
            if (!Character.isDigit(c)) return false;
            int n = c - '0';
            if (alt) {
                n *= 2; if (n > 9) n -= 9;
            }
            sum += n; alt = !alt;
        }
        return sum % 10 == 0;
    }

    public static String maskPan(String pan) {
        if (pan == null || pan.length() < 4) return "****";
        String last4 = pan.substring(pan.length()-4);
        return "**** **** **** " + last4;
    }

    public static String brandOf(String pan) {
        if (pan == null || pan.isEmpty()) return "CARD";
        if (pan.startsWith("4")) return "VISA";
        if (pan.startsWith("5")) return "MASTERCARD";
        if (pan.startsWith("34") || pan.startsWith("37")) return "AMEX";
        return "CARD";
    }
}