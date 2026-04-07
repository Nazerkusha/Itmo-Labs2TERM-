package com.nazerke.functions;

/**
 * Функция f(x) = e^x
 */
public class ExpFunction implements MathFunction {

    @Override
    public double evaluate(double x) {
        return Math.exp(x);
    }

    @Override
    public String getName() {
        return "eˣ";
    }

    @Override
    public String getFormula() {
        return "eˣ";
    }
}
