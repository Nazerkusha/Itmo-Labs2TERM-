package com.nazerke.methods;

import com.nazerke.interfaces.Function;
import com.nazerke.ui.ResultHandler;

public class NewtonMethod {
    private static final int MAX_ITERATIONS = 10000;
    private static final double DERIVATIVE_EPSILON = 1e-10;

    public ResultHandler solve(Function function, double a, double b, double epsilon) {
        ResultHandler result = new ResultHandler("Метод Ньютона");

        double fa = function.f(a);
        double fb = function.f(b);

        if (fa * fb > 0) {
            result.setError("На интервале [" + a + ", " + b + "] нет корня");
            return result;
        }

        double x0 = selectInitialApproximation(function, a, b);
        System.out.println("Начальное приближение: " + x0);

        double x = x0;
        int iter = 0;
        double error = Double.MAX_VALUE;

        while (iter < MAX_ITERATIONS && error > epsilon) {

            double fx = function.f(x);
            double dfx = function.df(x);

            if (Math.abs(dfx) < DERIVATIVE_EPSILON) {
                result.setError("Производная близка к нулю: " + dfx);
                return result;
            }

            double x_next = x - fx / dfx;

            error = Math.abs(x_next - x);

            x = x_next;
            iter++;

            if (Math.abs(function.f(x)) < epsilon) {
                break;
            }
        }

        result.setX(x);
        result.setFx(function.f(x));
        result.setI(iter);
        result.setConverged(error <= epsilon || Math.abs(function.f(x)) <= epsilon);

        if (iter >= MAX_ITERATIONS && !result.isConverged()) {
            result.setError("Не сошелся за " + MAX_ITERATIONS + " итераций");
        }

        return result;
    }

    private double selectInitialApproximation(Function function, double a, double b) {
        double fa = function.f(a);
        double dfa = function.df(a);

        if (fa * dfa > 0) {
            return a;
        } else {
            return b;
        }
    }
}