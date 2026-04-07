package com.nazerke.methods;

import java.util.List;
import java.util.ArrayList;

public record IntegrationResult(double value, int n, double error, List<double[]> iterations) {

    public IntegrationResult(double value, int n, double error, List<double[]> iterations) {
        this.value = value;
        this.n = n;
        this.error = error;
        this.iterations = new ArrayList<>(iterations);
    }
}