package com.nazerke.interfaces;


public interface System {

    double f1(double x1, double x2);

    double f2(double x1, double x2);

    double df1_dx(double x, double y);

    double df1_dy(double x, double y);

    double df2_dx(double x, double y);

    double df2_dy(double x, double y);

    double phi1(double x, double y);

    double phi2(double x, double y);

    String getDescription();
}
