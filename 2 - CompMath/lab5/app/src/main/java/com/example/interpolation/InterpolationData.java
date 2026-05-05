package com.example.interpolation;

import java.io.Serializable;

public class InterpolationData implements Serializable {
    public double[] xs;
    public double[] ys;
    public double xTarget;
    public String sourceName;

    public InterpolationData(double[] xs, double[] ys, double xTarget, String sourceName) {
        this.xs = xs;
        this.ys = ys;
        this.xTarget = xTarget;
        this.sourceName = sourceName;
    }
}
