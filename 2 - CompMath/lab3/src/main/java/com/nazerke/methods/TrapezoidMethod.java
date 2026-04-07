package com.nazerke.methods;

import com.nazerke.functions.MathFunction;

public class TrapezoidMethod extends IntegrationMethod {

    @Override
    protected double computeStep(MathFunction fn, double a, double b, int n) {
        double h = (b - a) / n;
        double sum = fn.evaluate(a) / 2.0 + fn.evaluate(b) / 2.0;
        for (int i = 1; i < n; i++) {
            sum += fn.evaluate(a + i * h);
        }
        return sum * h;
    }

    @Override
    protected double rungeK() {
        return 3.0;
    }

    @Override
    public String getName() {
        return "Метод трапеций";
    }
}
