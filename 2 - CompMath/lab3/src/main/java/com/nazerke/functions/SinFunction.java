package com.nazerke.functions;

/**
 * Функция f(x) = sin(x)
 */
public class SinFunction implements MathFunction {

    @Override
    public double evaluate(double x) {
        return Math.sin(x);
    }

    @Override
    public String getName() {
        return "sin(x)";
    }

    @Override
    public String getFormula() {
        return "sin(x)";
    }
}


