package com.nazerke.functions;

import com.nazerke.interfaces.Function;

public class Functions {

    /**
     * Функция 1: f(x) = x^3 − 1,89𝑥^2 − 2𝑥 + 1,76
     */
    public static class Function1 implements Function {
        @Override
        public double f(double x) {
            return x * x * x - 1.89 * x * x - 2 * x + 1.76;
        }
        @Override
        public double df(double x) {
            return 3 * x * x - 3.78 * x - 2;
        }
        @Override
        public double d2f(double x) {
            return 6 * x - 3.78;
        }
        @Override
        public double phi(double x) {
            return Math.cbrt(1.89 * x * x + 2 * x - 1.76);
        }
        @Override
        public double dPhi(double x) {
            return (3.78 * x + 2) / (3 * Math.pow((1.89 * x * x + 2 * x - 1.76), 2.0/3.0));
        }

        @Override
        public String getDescription() {
            return "f(x) = x³ - 1,89x² - 2x + 1,76";
        }
    }

    /**
     * Функция 2: f(x) = e^x - 3x
     */
    public static class Function2 implements Function {
        @Override
        public double f(double x) {
            return Math.exp(x) - 3 * x;
        }

        @Override
        public double df(double x) {
            return Math.exp(x) - 3;
        }
        @Override
        public double d2f(double x) {
            return Math.exp(x);
        }
        @Override
        public double phi(double x) {
            return Math.pow(Math.exp(x), x)/3;
        }
        @Override
        public double dPhi(double x) {
            return Math.pow(Math.exp(x), x)/3;
        }

        @Override
        public String getDescription() {
            return "f(x) = e^x - 3x";
        }
    }

    /**
     * Функция 3: f(x) = cos(x) - x
     */
    public static class Function3 implements Function {
        @Override
        public double f(double x) {
            return Math.cos(x) - x;
        }

        @Override
        public double df(double x) {
            return -Math.sin(x) - 1;
        }
        @Override
        public double d2f(double x) {
            return -Math.cos(x);
        }
        @Override
        public double phi(double x) {
            return Math.cos(x);
        }
        @Override
        public double dPhi(double x) {
            return -Math.sin(x);
        }

        @Override
        public String getDescription() {
            return "f(x) = cos(x) - x ";
        }
    }
}
