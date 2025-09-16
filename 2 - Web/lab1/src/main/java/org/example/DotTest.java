package org.example;

public class DotTest {
    public static boolean checkHit(float x, float y, float r) {
        return inRectangle(x, y, r) || inTriangle(x, y, r) || inCircle(x, y, r);
    }

    private static boolean inRectangle(float x, float y, float r) {
        return x >= -r/2 && x <= 0 && y >= 0 && y <= r;
    }

    private static boolean inTriangle(float x, float y, float r) {
        return x <= 0 && y <= 0 && y >= -r && x >= -r && (y + x) <= -r;
    }

    private static boolean inCircle(float x, float y, float r) {
        return x >= 0 && y >= 0 && x <= r/2 && y <= r/2 && (Math.pow(x, 2) + Math.pow(y, 2) <= Math.pow(r, 2));
    }
}
