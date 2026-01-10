package com.lab4.geometry.util;

public class HitChecker {

    public static boolean checkHit(double x, double y, double r) {
        return checkCircle(x, y, r) || checkTriangle(x, y, r) || checkRectangle(x, y, r);
    }

    private static boolean checkCircle(double x, double y, double r) {
        return (x >= 0 && y >= 0) && (x * x + y * y <= r * r);
    }

    private static boolean checkTriangle(double x, double y, double r) {
        return (x >= 0 && y <= 0) && (x <= r) && (y >= -r/2) && (y >= -r/2 + x/2);
    }

    private static boolean checkRectangle(double x, double y, double r) {
        return (x <= 0 && y <= 0) && (x >= -r) && (y >= -r/2);
    }
}
