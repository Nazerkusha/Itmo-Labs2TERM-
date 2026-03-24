package com.nazerke.interfaces;


public interface Function {

    double f(double x);

    double df(double x);
    double d2f(double x);
    double dPhi(double x);
    double phi(double x);

    String getDescription();
}

