package com.nazerke.ui;

import com.nazerke.interfaces.*;

import java.io.*;

import com.nazerke.interfaces.System;
import org.jfree.chart.*;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.*;

public class FunctionPlotter {
    JFreeChart chart;

    public void plot(Function function, double a, double b, double[] roots) {
        XYSeries series = new XYSeries("f(x)");
        XYSeries rootSeries = new XYSeries("Корни");

        int samples = 500;
        for (int i = 0; i <= samples; i++) {
            double x = a + (b - a) * i / samples;
            series.add(x, function.f(x));
        }

        for (double root : roots) {
            rootSeries.add(root, function.f(root));
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);
        dataset.addSeries(rootSeries);

        JFreeChart chart = ChartFactory.createXYLineChart(
                function.getDescription(), "X", "Y", dataset
        );
        this.chart = chart;

        XYPlot plot = chart.getXYPlot();
        plot.getRenderer().setSeriesShape(1, new java.awt.geom.Ellipse2D.Double(-5, -5, 10, 10));

        ChartFrame frame = new ChartFrame("График", chart);
        frame.setSize(800, 600);
        frame.setVisible(true);

    }
    public void plotSystem(System system,
                           double xMin, double xMax,
                           double[] rootsX, double[] rootsY) {

        XYSeries s1 = new XYSeries("f1 (верх)");
        XYSeries s2 = new XYSeries("f1 (низ)");
        XYSeries s3 = new XYSeries("f2");
        XYSeries roots = new XYSeries("Решение");

        int samples = 1000;

        for (int i = 0; i <= samples; i++) {
            double x = xMin + (xMax - xMin) * i / samples;

            // --- System1 ---
            if (system instanceof com.nazerke.functions.Systems.System1) {

                double val = 4 - x * x;
                if (val >= 0) {
                    double y1 = Math.sqrt(val);
                    double y2 = -Math.sqrt(val);

                    s1.add(x, y1);
                    s2.add(x, y2);
                }

                s3.add(x, x - 1);
            }

            else if (system instanceof com.nazerke.functions.Systems.System2) {

                double val = (1 - x * x) / 2;
                if (val >= 0) {
                    double y1 = Math.sqrt(val);
                    double y2 = -Math.sqrt(val);
                    s1.add(x, y1);
                    s2.add(x, y2);
                }

                double arg = 1.2 * x + 0.2;
                if (Math.abs(arg) <= 1.0) {
                    double y = Math.asin(arg) - x;
                    s3.add(x, y);
                }
            }
        }

        for (int i = 0; i < rootsX.length; i++) {
            roots.add(rootsX[i], rootsY[i]);
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(s1);
        dataset.addSeries(s2);
        dataset.addSeries(s3);
        dataset.addSeries(roots);

        JFreeChart chart = ChartFactory.createXYLineChart(
                system.getDescription(), "X", "Y", dataset
        );

        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer r = new XYLineAndShapeRenderer();

        // линии
        r.setSeriesLinesVisible(0, true);
        r.setSeriesShapesVisible(0, false);

        r.setSeriesLinesVisible(1, true);
        r.setSeriesShapesVisible(1, false);

        r.setSeriesLinesVisible(2, true);
        r.setSeriesShapesVisible(2, false);

        // точка
        r.setSeriesLinesVisible(3, false);
        r.setSeriesShapesVisible(3, true);

        plot.setRenderer(r);

        ChartFrame frame = new ChartFrame("Система", chart);
        frame.setSize(800, 600);
        frame.setVisible(true);
    }
    public void saveChart() throws IOException {
        ChartUtils.saveChartAsPNG(new File("graph.png"), chart, 800, 600);
    }
}
