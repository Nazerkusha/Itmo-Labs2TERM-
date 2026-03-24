package com.nazerke.ui;

/**
 * Класс для хранения результатов решения уравнения
 */
public class ResultHandler {
    private double x;
    private double fx;
    private int i;
    private String error;
    private boolean converged;
    private String methodName;

    public ResultHandler(String methodName) {
        this.methodName = methodName;
        this.converged = false;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getFx() {
        return fx;
    }

    public void setFx(double fx) {
        this.fx = fx;
    }

    public int getI() {
        return i;
    }

    public void setI(int i) {
        this.i = i;
    }


    public void setError(String error) {
        this.error = error;
    }

    public boolean isConverged() {
        return converged;
    }

    public void setConverged(boolean converged) {
        this.converged = converged;
    }

    public String getMethodName() {
        return methodName;
    }

    @Override
    public String toString() {
        if (!converged) {
            return String.format("Метод: %s\nОшибка: Метод не сошелся за максимальное число итераций", methodName);
        }
        if (error != null) {
            return String.format(
                    "Метод: %s%n" +
                            "Ошибка: %s%n",
                    methodName, error
            );
        }
        return String.format(
                "Метод: %s%n" +
                        "Найденный корень: %.10f%n" +
                        "Значение функции в корне: %.2e%n" +
                        "Число итераций: %d%n",
                methodName, x, fx, i
        );
    }
}