package com.nazerke.converging;


public interface convergingFunction {

    /**
     *   a_point  — разрыв в левом конце  a
     *   b_point  — разрыв в правом конце  б
     *   in_point  — разрыв в интервале
     */
    enum SingularityType {a_point, b_point, in_point}

    double evaluate(double x, double a, double b);

    String getName();
    String getFormula();

    boolean isConvergent(double a, double b);

    SingularityType getSingularityType();

    double getSingularPoint(double a, double b);

    String getConvergenceInfo(double a, double b);
}