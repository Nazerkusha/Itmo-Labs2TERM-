package com.nazerke.converging;

import java.util.ArrayList;
import java.util.List;

/**
 *
 *   POINT_A  → ∫_{a+ε}^{b}     f(x) dx,   ε → 0
 *   POINT_B  → ∫_{a}^{b-ε}     f(x) dx,   ε → 0
 *   INTERIOR → ∫_{a}^{c-ε} dx + ∫_{c+ε}^{b} dx,   ε → 0
 */
public class convergingIntegrator {

    private static final double s = 0.1;
    private static final int beginningN = 4;
    private static final int maxIter = 25;
    private static final double rungK = 15.0;

    public static class ImproperResult {
        public final double v;
        public final double finalSigm;
        public final int finalN;
        public final double error;
        public final List<double[]> iter;

        ImproperResult(double val, double sigm, int n, double err, List<double[]> iter) {
            this.v = val;
            this.finalSigm = sigm;
            this.finalN = n;
            this.error = err;
            this.iter = new ArrayList<>(iter);
        }
    }

    public ImproperResult compute(convergingFunction fn, double a, double b, double epsilon) {
        List<double[]> iters = new ArrayList<>();
        double sigm = s;
        int n = beginningN;

        double I1 = integrate(fn, a, b, sigm, n);

        for (int iter = 0; iter < maxIter; iter++) {
            double sigm2  = sigm / 2.0;
            double I2    = integrate(fn, a, b, sigm2, n * 2);
            double runge = Math.abs(I2 - I1) / rungK;

            iters.add(new double[]{sigm, I1, I2, runge});

            if (runge < epsilon) {
                return new ImproperResult(I2, sigm2, n * 2, runge, iters);
            }
            I1  = I2;
            sigm = sigm2;
            n  *= 2;
        }

        double[] last = iters.get(iters.size() - 1);
        return new ImproperResult(last[2], sigm / 2, n, last[3], iters);
    }

    private double integrate(convergingFunction fn, double a, double b, double eps, int n) {
        double c = fn.getSingularPoint(a, b);

        switch (fn.getSingularityType()) {
            case a_point: {
                double lo = a + eps;
                return lo < b ? simpson(fn, a, b, lo, b, n) : 0.0;
            }
            case b_point: {
                double hi = b - eps;
                return hi > a ? simpson(fn, a, b, a, hi, n) : 0.0;
            }
            case in_point: {
                double hi1 = c - eps;
                double lo2 = c + eps;
                double p1 = (hi1 > a) ? simpson(fn, a, b, a,   hi1, n) : 0.0;
                double p2 = (lo2 < b) ? simpson(fn, a, b, lo2, b,   n) : 0.0;
                return p1 + p2;
            }
            default: return 0.0;
        }
    }

    private double simpson(convergingFunction fn, double a, double b, double lo, double hi, int n) {
        if (n % 2 != 0) n++;
        double h   = (hi - lo) / n;
        double sum = safe(fn, a, b, lo) + safe(fn, a, b, hi);
        for (int i = 1; i < n; i++) {
            double v = safe(fn, a, b, lo + i * h);
            sum += v * (i % 2 == 0 ? 2.0 : 4.0);
        }
        return sum * h / 3.0;
    }

    private double safe(convergingFunction fn, double a, double b, double x) {
        double v = fn.evaluate(x, a, b);
        return (Double.isFinite(v)) ? v : 0.0;
    }
}