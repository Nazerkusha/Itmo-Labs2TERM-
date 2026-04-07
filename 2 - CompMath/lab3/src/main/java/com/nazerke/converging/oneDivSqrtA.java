package com.nazerke.converging;

class oneDivSqrtA implements convergingFunction {

    @Override
    public double evaluate(double x, double a, double b) {
        if (x <= 0) return 0.0;
        return 1.0 / Math.sqrt(x);
    }

    @Override
    public String getName() {
        return "1/√x";
    }

    @Override
    public String getFormula() {
        return "f(x) = 1/√x,  x∈(0,b]";
    }

    @Override
    public boolean isConvergent(double a, double b) {
        return b > 0;
    }

    @Override
    public SingularityType getSingularityType() {
        return SingularityType.a_point;
    }

    @Override
    public double getSingularPoint(double a, double b) {
        return a;
    }

    @Override
    public String getConvergenceInfo(double a, double b) {
        return String.format(
                "Особая точка: x = a = 0.  α = 1/2 < 1  →  СХОДИТСЯ.  " +
                        "Точное: 2·√%.4g = %.6f", b, 2 * Math.sqrt(b));
    }
}
