package com.nazerke.methods;

import com.nazerke.interfaces.System;


public class SystemIterationMethod {
    private static final int MAX_ITERATIONS = 10000;

    /**
     * Класс для хранения результата решения системы
     */
    public static class SystemSolution {
        public double x1;
        public double x2;
        public int iter;
        public String error;
        public boolean converged;

        public SystemSolution() {
            this.converged = false;
        }

        @Override
        public String toString() {
            if (error != null) {
                return String.format(
                        "Ошибка в решении системы: %s%n",
                        error
                );
            } else
                return String.format(
                    "x₁ = %.10f%n" +
                            "x₂ = %.10f%n" +
                            "Итерации: %d%n" +
                            "Сходимость: %s",
                    x1, x2, iter, converged ? "ДА" : "НЕТ"
            );
        }
    }



    public SystemSolution solve(System system, double x1_0, double x2_0, double epsilon) {
        SystemSolution solution = new SystemSolution();

        double testPhi1 = system.phi1(x1_0, x2_0);
        double testPhi2 = system.phi2(x1_0, x2_0);
        if (Double.isNaN(testPhi1) || Double.isNaN(testPhi2)) {
            solution.error = "Начальное приближение вне области определения φ";
            return solution;
        }

        double norm = computeJacobianNorm(system, x1_0, x2_0);
        java.lang.System.out.println("Норма матрицы Якобиана φ: " + String.format("%.6f", norm));

        double omega;
        if (norm < 1.0) {
            omega = 1.0; // сходится — полный шаг
        } else {
            omega = 0.9 / norm; // автоподбор
            java.lang.System.err.println("ВНИМАНИЕ: Условие сходимости может не быть выполнено!");
            java.lang.System.out.printf("Авто-релаксация: ω = %.6f%n", omega);
        }

        double x1 = x1_0;
        double x2 = x2_0;
        int iter = 0;

        while (iter < MAX_ITERATIONS) {
            double x1_new = system.phi1(x1, x2);
            double x2_new = system.phi2(x1, x2);

            if (Double.isNaN(x1_new) || Double.isNaN(x2_new) ||
                    Double.isInfinite(x1_new) || Double.isInfinite(x2_new)) {
                solution.error = "Метод расходится (NaN/Inf)";
                return solution;
            }

            double x1_next = (1 - omega) * x1 + omega * x1_new;
            double x2_next = (1 - omega) * x2 + omega * x2_new;

            double error = Math.max(Math.abs(x1_next - x1), Math.abs(x2_next - x2));

            x1 = x1_next;
            x2 = x2_next;
            iter++;

            if (error < epsilon) break;
        }

        solution.x1 = x1;
        solution.x2 = x2;
        solution.iter = iter;
        solution.converged = iter < MAX_ITERATIONS;

        double f1 = system.f1(x1, x2);
        double f2 = system.f2(x1, x2);
        java.lang.System.out.println("\nПроверка решения:");
        java.lang.System.out.printf("f₁(x₁, x₂) = %.2e%n", f1);
        java.lang.System.out.printf("f₂(x₁, x₂) = %.2e%n", f2);

        return solution;
    }

    private double computeJacobianNorm(System system, double x1, double x2) {
        double h = 1e-6;

        double phi1_x1 = (system.phi1(x1 + h, x2) - system.phi1(x1 - h, x2)) / (2 * h);
        double phi1_x2 = (system.phi1(x1, x2 + h) - system.phi1(x1, x2 - h)) / (2 * h);
        double phi2_x1 = (system.phi2(x1 + h, x2) - system.phi2(x1 - h, x2)) / (2 * h);
        double phi2_x2 = (system.phi2(x1, x2 + h) - system.phi2(x1, x2 - h)) / (2 * h);

        double row1 = Math.abs(phi1_x1) + Math.abs(phi1_x2);
        double row2 = Math.abs(phi2_x1) + Math.abs(phi2_x2);

        return Math.max(row1, row2);
    }

    private boolean checkConvergenceCondition(System system, double x1, double x2) {
        double norm = computeJacobianNorm(system, x1, x2);
        java.lang.System.out.println("Норма матрицы Якобиана φ: " + String.format("%.6f", norm));
        return norm < 1.0;
    }
}