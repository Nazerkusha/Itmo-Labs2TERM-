package com.nazerke.methods;

import com.nazerke.functions.MathFunction;

import java.util.ArrayList;
import java.util.List;

public abstract class IntegrationMethod {

    private static final int beginningN = 4;

    private static final int maxDouble = 25;

    public IntegrationResult compute(MathFunction fn, double a, double b, double eps) {
        List<double[]> iterations = new ArrayList<>();
        int n = beginningN;
        double I1 = computeStep(fn, a, b, n);

        for (int iter = 0; iter < maxDouble; iter++) {
            double I2 = computeStep(fn, a, b, n * 2);
            double runge = Math.abs(I2 - I1) / rungeK();
            iterations.add(new double[]{n, I1, I2, runge});
            if (runge < eps) {
                return new IntegrationResult(I2, n * 2, runge, iterations);
            }
            I1 = I2;
            n *= 2;
        }
        double[] last = iterations.getLast();
        return new IntegrationResult(last[2], n * 2, last[3], iterations);
    }

    protected abstract double computeStep(MathFunction fn, double a, double b, int n);

    protected abstract double rungeK();

    public abstract String getName();
}
