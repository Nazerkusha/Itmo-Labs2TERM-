package com.nazerke.converging;

class oneDivA implements convergingFunction {

    @Override
    public double evaluate(double x, double a, double b) {
        if (Math.abs(x) < 1e-14) return 0.0;
        return 1.0 / x;
    }

    @Override
    public String getName() {
        return "1/x";
    }

    @Override
    public String getFormula() {
        return "f(x) = 1/x,  x∈(0,b]";
    }

    @Override
    public boolean isConvergent(double a, double b) {
        return false;
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
        return "Особая точка: x = 0.  α = 1 ≥ 1  →  РАСХОДИТСЯ.  " +
                "Интеграл не существует.";
    }
}
