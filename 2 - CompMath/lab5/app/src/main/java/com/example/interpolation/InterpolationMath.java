package com.example.interpolation;

import java.util.ArrayList;
import java.util.List;

public class InterpolationMath {
    /**
     * Вычисляет значение интерполяционного многочлена Лагранжа в точке x
     */
    public static double lagrange(double[] xs, double[] ys, double x) {
        int n = xs.length;
        double result = 0.0;
        for (int i = 0; i < n; i++) {
            double li = ys[i];
            for (int j = 0; j < n; j++) {
                if (j != i) {
                    li *= (x - xs[j]) / (xs[i] - xs[j]);
                }
            }
            result += li;
        }
        return result;
    }

    /**
     * Возвращает таблицу коэффициентов l_i(x) для Лагранжа (для отображения)
     */
    public static double[] lagrangeCoeffs(double[] xs, double[] ys, double x) {
        int n = xs.length;
        double[] li = new double[n];
        for (int i = 0; i < n; i++) {
            li[i] = 1.0;
            for (int j = 0; j < n; j++) {
                if (j != i) {
                    li[i] *= (x - xs[j]) / (xs[i] - xs[j]);
                }
            }
        }
        return li;
    }


    /**
     * Строит таблицу разделённых разностей
     */
    public static double[][] dividedDiffTable(double[] xs, double[] ys) {
        int n = xs.length;
        double[][] table = new double[n][n];
        for (int i = 0; i < n; i++) table[i][0] = ys[i];
        for (int j = 1; j < n; j++) {
            for (int i = 0; i < n - j; i++) {
                table[i][j] = (table[i + 1][j - 1] - table[i][j - 1]) / (xs[i + j] - xs[i]);
            }
        }
        return table;
    }

    /**
     * Многочлен Ньютона с разделёнными разностями формула "вперёд"
     */
    public static double newtonDividedForward(double[] xs, double[][] table, double x) {
        int n = xs.length;
        double result = table[0][0];
        double product = 1.0;
        for (int j = 1; j < n; j++) {
            product *= (x - xs[j - 1]);
            result += table[0][j] * product;
        }
        return result;
    }

    /**
     * Многочлен Ньютона с разделёнными разностями формула "назад"
     */
    public static double newtonDividedBackward(double[] xs, double[][] table, double x) {
        int n = xs.length;
        double result = table[n - 1][0];
        double product = 1.0;
        for (int j = 1; j < n; j++) {
            product *= (x - xs[n - j]);
            result += table[n - 1 - j][j] * product;
        }
        return result;
    }

    /**
     * Строит таблицу конечных разностей
     * deltaTable[i][j] = Delta^j y_i
     */
    public static double[][] finiteDiffTable(double[] ys) {
        int n = ys.length;
        double[][] table = new double[n][n];
        for (int i = 0; i < n; i++) table[i][0] = ys[i];
        for (int j = 1; j < n; j++) {
            for (int i = 0; i < n - j; i++) {
                table[i][j] = table[i + 1][j - 1] - table[i][j - 1];
            }
        }
        return table;
    }

    /**
     * Первая формула Ньютона для равноотстоящих узлов
     * t = (x - x0) / h
     */
    public static double newtonFiniteForward(double[] xs, double[][] delta, double x) {
        int n = xs.length;
        double h = xs[1] - xs[0];
        double t = (x - xs[0]) / h;
        double result = delta[0][0];
        double tProduct = 1.0;
        double factorial = 1.0;
        for (int k = 1; k < n; k++) {
            tProduct *= (t - (k - 1));
            factorial *= k;
            result += (tProduct / factorial) * delta[0][k];
        }
        return result;
    }

    /**
     * Вторая формула Ньютона для равноотстоящих узлов
     * t = (x - xn) / h
     */
    public static double newtonFiniteBackward(double[] xs, double[][] delta, double x) {
        int n = xs.length;
        double h = xs[1] - xs[0];
        double t = (x - xs[n - 1]) / h;
        double result = delta[n - 1][0];
        double tProduct = 1.0;
        double factorial = 1.0;
        for (int k = 1; k < n; k++) {
            tProduct *= (t + (k - 1));
            factorial *= k;
            result += (tProduct / factorial) * delta[n - 1 - k][k];
        }
        return result;
    }


    public static double stirling(double[] xs, double[][] delta, double x) {
        int n = xs.length;
        int center = n / 2;
        double h = xs[1] - xs[0];
        double t = (x - xs[center]) / h;

        double result = delta[center][0];

        if (center >= 1 && center < n) {
            result += t * (delta[center - 1][1] + delta[center][1]) / 2.0;
        }

        if (center >= 1) {
            result += (t * t / 2.0) * delta[center - 1][2];
        }

        if (center >= 2 && n - center > 1) {
            double coeff = t * (t * t - 1) / 6.0;
            result += coeff * (delta[center - 2][3] + delta[center - 1][3]) / 2.0;
        }

        if (center >= 2) {
            double coeff = t * t * (t * t - 1) / 24.0;
            result += coeff * delta[center - 2][4];
        }

        return result;
    }


    public static double bessel(double[] xs, double[][] delta, double x) {
        int n = xs.length;
        int center = n / 2 - 1;
        double h = xs[1] - xs[0];
        double t = (x - xs[center]) / h;

        double result = (delta[center][0] + delta[center + 1][0]) / 2.0;

        result += (t - 0.5) * delta[center][1];

        if (center >= 1) {
            result += (t * (t - 1) / 2.0) * (delta[center - 1][2] + delta[center][2]) / 2.0;
        }

        if (center >= 1) {
            result += ((t - 0.5) * t * (t - 1) / 6.0) * delta[center - 1][3];
        }

        if (center >= 2) {
            result += (t * (t - 1) * (t + 1) * (t - 2) / 24.0)
                    * (delta[center - 2][4] + delta[center - 1][4]) / 2.0;
        }

        return result;
    }

    /**
     * Проверяет, являются ли узлы равноотстоящими
     */
    public static boolean isEquallySpaced(double[] xs) {
        if (xs.length < 2) return true;
        double h = xs[1] - xs[0];
        for (int i = 2; i < xs.length; i++) {
            if (Math.abs((xs[i] - xs[i - 1]) - h) > 1e-9) return false;
        }
        return true;
    }

    /**
     * Проверяет, что все значения x различны
     */
    public static boolean allDistinct(double[] xs) {
        for (int i = 0; i < xs.length; i++)
            for (int j = i + 1; j < xs.length; j++)
                if (Math.abs(xs[i] - xs[j]) < 1e-12) return false;
        return true;
    }


    /**
     * Генерирует точки для графика многочлена Лагранжа
     */
    public static double[][] lagrangeGraphPoints(double[] xs, double[] ys, int points) {
        double xMin = xs[0], xMax = xs[xs.length - 1];
        double step = (xMax - xMin) / (points - 1);
        double[][] result = new double[2][points];
        for (int i = 0; i < points; i++) {
            double xi = xMin + i * step;
            result[0][i] = xi;
            result[1][i] = lagrange(xs, ys, xi);
        }
        return result;
    }

    /**
     * Генерирует точки для графика Ньютона с разделёнными разностями
     */
    public static double[][] newtonDividedGraphPoints(double[] xs, double[][] table, int points, boolean forward) {
        double xMin = xs[0], xMax = xs[xs.length - 1];
        double step = (xMax - xMin) / (points - 1);
        double[][] result = new double[2][points];
        for (int i = 0; i < points; i++) {
            double xi = xMin + i * step;
            result[0][i] = xi;
            result[1][i] = forward
                    ? newtonDividedForward(xs, table, xi)
                    : newtonDividedBackward(xs, table, xi);
        }
        return result;
    }

}
