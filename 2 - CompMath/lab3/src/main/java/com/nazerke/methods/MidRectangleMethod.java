package com.nazerke.methods;

import com.nazerke.functions.MathFunction;

public class MidRectangleMethod extends IntegrationMethod {

    @Override
    protected double computeStep(MathFunction fn, double a, double b, int n) {
        double h = (b - a) / n;
        double sum = 0.0;
        for (int i = 0; i < n; i++) {
            sum += fn.evaluate(a + (i + 0.5) * h);
        }
        return sum * h;
    }

    @Override
    protected double rungeK() {
        return 3.0;
    }

    @Override
    public String getName() {
        return "Прямоугольники (средние)";
    }
}
