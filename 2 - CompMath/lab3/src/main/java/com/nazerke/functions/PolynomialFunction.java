package com.nazerke.functions;

/**
 * Функция f(x) = x² + 2x
 */
public class PolynomialFunction implements MathFunction {

    @Override
    public double evaluate(double x) {
        return x * x + 2 * x;
    }

    @Override
    public String getName() {
        return "x² + 2x";
    }

    @Override
    public String getFormula() {
        return "x² + 2 · x";
    }
}
