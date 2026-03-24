package com.nazerke.methods;

import com.nazerke.interfaces.Function;
import com.nazerke.ui.ResultHandler;

/**
 * Метод хорд
 */
public class ChordsMethod {
    private static final int MAX_ITERATIONS = 10000;

    public ResultHandler solve(Function function, double a, double b, double epsilon) {
        ResultHandler result = new ResultHandler("Метод хорд");
        try {
            double fa = function.f(a);
            double fb = function.f(b);

            if (fa * fb > 0) {
                result.setError("На интервале [" + a + ", " + b + "] нет корня");
                return result;
            }

            if (Math.abs(fa) < epsilon) {
                result.setX(a);
                result.setFx(fa);
                result.setI(0);
                result.setConverged(true);
                return result;
            }

            if (Math.abs(fb) < epsilon) {
                result.setX(b);
                result.setFx(fb);
                result.setI(0);
                result.setConverged(true);
                return result;
            }

            double x_prev = a;
            double x_curr = b;
            double f_prev = fa;
            double f_curr = fb;

            int iter = 0;
            double error = Double.MAX_VALUE;

            while (iter < MAX_ITERATIONS && error > epsilon) {

                if (Math.abs(f_curr - f_prev) < 1e-15) {
                    result.setError("Функция почти линейна (знаменатель близок к нулю)");
                    return result;
                }

                double x_next = x_curr - f_curr * (x_curr - x_prev) / (f_curr - f_prev);
                double f_next = function.f(x_next);

                error = Math.abs(x_next - x_curr);

                x_prev = x_curr;
                f_prev = f_curr;
                x_curr = x_next;
                f_curr = f_next;

                iter++;

                if (Math.abs(f_curr) < epsilon) {
                    break;
                }
            }

            result.setX(x_curr);
            result.setFx(function.f(x_curr));
            result.setI(iter);
            result.setConverged(error <= epsilon || Math.abs(function.f(x_curr)) <= epsilon);

            if (iter >= MAX_ITERATIONS && !result.isConverged()) {
                result.setError("Не сошелся за " + MAX_ITERATIONS + " итераций");
            }

            return result;
        } catch (Exception e){
            result.setError("Ошибка: " + e.getMessage());
            return  result;
        }
    }
}
