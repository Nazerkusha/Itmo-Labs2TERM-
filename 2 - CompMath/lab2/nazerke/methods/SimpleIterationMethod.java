package com.nazerke.methods;

import com.nazerke.interfaces.Function;
import com.nazerke.ui.ResultHandler;


/**
 * x_{n+1} = φ(x_n)
 */
public class SimpleIterationMethod {

    private static final int MAX_ITERATIONS = 10000;

    public ResultHandler solve(Function f, double a, double b, double epsilon) {

        ResultHandler result = new ResultHandler("Метод простой итерации");
        double q = findMaxD(f, a, b);

        if (q >= 1) {
            result.setError("Условие сходимости не выполнено: q >= 1");
            return result;
        }

        double x0 = selectInitialApproximation(f, a, b);

        double xPrev = x0;
        double xNext;
        int iter = 0;
        double error = Double.MAX_VALUE;

        while (iter < MAX_ITERATIONS && error > epsilon) {
            xNext = f.phi(xPrev);
            error = Math.abs(xNext - xPrev);
            if (Math.abs(f.phi(xNext) - xNext) < epsilon) {
                xPrev = xNext;
                break;
            }
            xPrev = xNext;
            iter++;
        }

        result.setX(xPrev);
        result.setI(iter);
        result.setConverged(error <= epsilon);

        if (iter >= MAX_ITERATIONS) {
            result.setError("Метод не сошелся");
        }

        return result;
    }

    /**
     * Находит q = max |φ'(x)| на [a, b]
     */
    private double findMaxD(Function f, double a, double b) {
        int steps = 100;
        double max = 0;

        for (int i = 0; i <= steps; i++) {
            double x = a + (b - a) * i / steps;
            double d = Math.abs(f.dPhi(x));
            if (d > max) {
                max = d;
            }
        }

        return max;
    }
    private double selectInitialApproximation(Function f, double a, double b) {

        double fa = f.f(a);
        double f2a = f.d2f(a);

        if (fa * f2a > 0) {
            return a;
        }

        double fb = f.f(b);
        double f2b = f.d2f(b);

        if (fb * f2b > 0) {
            return b;
        }
        return (a + b) / 2;
    }
}

