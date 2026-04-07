package com.nazerke.methods;

import com.nazerke.functions.MathFunction;


public class SimpsonMethod extends IntegrationMethod {

    @Override
    protected double computeStep(MathFunction fn, double a, double b, int n) {
        if (n % 2 != 0) n++;
        double h = (b - a) / n;
        double sum = fn.evaluate(a) + fn.evaluate(b);
        for (int i = 1; i < n; i++) {
            double k = (i % 2 == 0) ? 2.0 : 4.0;
            sum += k * fn.evaluate(a + i * h);
        }
        return sum * h / 3.0;
    }

    @Override
    protected double rungeK() {
        return 15.0;
    }

    @Override
    public String getName() {
        return "Метод Симпсона";
    }
}
