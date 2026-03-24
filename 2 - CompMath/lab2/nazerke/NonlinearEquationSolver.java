package com.nazerke;

import com.nazerke.functions.Functions;
import com.nazerke.functions.Systems;
import com.nazerke.interfaces.Function;
import com.nazerke.interfaces.System;
import com.nazerke.methods.ChordsMethod;
import com.nazerke.methods.NewtonMethod;
import com.nazerke.methods.SimpleIterationMethod;
import com.nazerke.methods.SystemIterationMethod;
import com.nazerke.ui.FunctionPlotter;
import com.nazerke.ui.IOManager;
import com.nazerke.ui.ResultHandler;

import java.io.IOException;
import java.util.Scanner;

/**
 * Главный класс приложения для решения нелинейных уравнений и систем
 */
public class NonlinearEquationSolver {
    private IOManager ioManager;
    private FunctionPlotter plotter;
    private Scanner scanner;

    public NonlinearEquationSolver() {
        this.ioManager = new IOManager();
        this.plotter = new FunctionPlotter();
        this.scanner = new Scanner(java.lang.System.in);
    }


    public void run() throws IOException {
        boolean running = true;

        while (running) {
            java.lang.System.out.println("РЕШЕНИЕ НЕЛИНЕЙНЫХ УРАВНЕНИЙ И СИСТЕМ");
            java.lang.System.out.println("1 - Решение нелинейного уравнения");
            java.lang.System.out.println("2 - Решение системы нелинейных уравнений");
            java.lang.System.out.println("0 - Выход");
            java.lang.System.out.print("Выберите режим: ");

            int mainChoice = scanner.nextInt();

            switch (mainChoice) {
                case 1:
                    solveEquation();
                    break;
                case 2:
                    solveSystem();
                    break;
                case 0:
                    running = false;
                    java.lang.System.out.println("До свидания!");
                    break;
                default:
                    java.lang.System.err.println("Неверный выбор!");
            }
        }

        ioManager.close();
        scanner.close();
    }


    private void solveEquation() throws IOException {
        java.lang.System.out.println("\n Выбор функции ");
        java.lang.System.out.println("1 - f(x) = x³ - 1,89x² - 2x + 1,76");
        java.lang.System.out.println("2 - f(x) = eˣ - 3x");
        java.lang.System.out.println("3 - f(x) = cos(x) - x");
        java.lang.System.out.print("Выберите функцию: ");

        int funcChoice = scanner.nextInt();
        Function function;

        switch (funcChoice) {
            case 1:
                function = new Functions.Function1();
                break;
            case 2:
                function = new Functions.Function2();
                break;
            case 3:
                function = new Functions.Function3();
                break;
            default:
                java.lang.System.err.println("Неверный выбор функции!");
                return;
        }

        java.lang.System.out.println("\nВыбранная функция: " + function.getDescription());

        double[] data = ioManager.readEquationInput();
        if (data == null) return;

        double a = data[0];
        double b = data[1];
        double epsilon = data[2];

        java.lang.System.out.println("\nПараметры:");
        java.lang.System.out.println("Интервал: [" + a + ", " + b + "]");
        java.lang.System.out.println("Точность: " + epsilon);

        double fa = function.f(a);
        double fb = function.f(b);

        java.lang.System.out.println("\nПроверка наличия корня:");
        java.lang.System.out.println("f(" + a + ") = " + fa);
        java.lang.System.out.println("f(" + b + ") = " + fb);

        if (fa * fb > 0) {
            java.lang.System.err.println("ОШИБКА: На интервале [" + a + ", " + b + "] нет корня!");
            java.lang.System.err.println("Признак отсутствия корня: f(a) и f(b) имеют одинаковый знак");
            return;
        }

        java.lang.System.out.println("\n Выбор метода решения ");
        java.lang.System.out.println("1 - Метод хорд");
        java.lang.System.out.println("2 - Метод Ньютона");
        java.lang.System.out.println("3 - Метод простой итерации");
        java.lang.System.out.print("Выберите метод: ");

        int methodChoice = scanner.nextInt();
        ResultHandler result = null;

        switch (methodChoice) {
            case 1:
                ChordsMethod chordsMethod = new ChordsMethod();
                result = chordsMethod.solve(function, a, b, epsilon);
                break;
            case 2:
                NewtonMethod newtonMethod = new NewtonMethod();
                result = newtonMethod.solve(function, a, b, epsilon);
                break;
            case 3:
                SimpleIterationMethod iterationMethod = new SimpleIterationMethod();
                result = iterationMethod.solve(function, a, b, epsilon);
                break;
            default:
                java.lang.System.err.println("Неверный выбор метода!");
                return;
        }

        if (result != null && result.isConverged()) {
            boolean toFile = ioManager.selectOutputMode();
            ioManager.outputResults(result, toFile);

            java.lang.System.out.print("\nВывести график функции? (y/n): ");
            if (ioManager.readYesNo()) {
                double[] roots = {result.getX()};
                plotter.plot(function, a, b, roots);

                java.lang.System.out.print("Сохранить данные графика в CSV? (y/n): ");
                if (ioManager.readYesNo()) {
                    plotter.saveChart();
                }
            }
        } else {
            java.lang.System.err.println("Метод не сошелся!");
        }
    }

    private void solveSystem() throws IOException {
        java.lang.System.out.println("\n Выбор системы уравнений ");
        java.lang.System.out.println("1 - Система: x₁² + x₂² = 4, x₁ - x₂ = 1");
        java.lang.System.out.println("2 - Система: sin(x+y) - 1.2x = 0.2, x² + 2y² = 1");
        java.lang.System.out.print("Выберите систему: ");

        int sysChoice = scanner.nextInt();
        System system;

        switch (sysChoice) {
            case 1:
                system = new Systems.System1();
                break;
            case 2:
                system = new Systems.System2();
                break;
            default:
                java.lang.System.err.println("Неверный выбор системы!");
                return;
        }

        java.lang.System.out.println("\nВыбранная система:");
        java.lang.System.out.println(system.getDescription());

        double[] data = ioManager.readSystemInput();
        if (data == null) return;

        double x1_0 = data[0];
        double x2_0 = data[1];
        double epsilon = data[2];

        java.lang.System.out.println("\nНачальные приближения:");
        java.lang.System.out.println("x₁₀ = " + x1_0);
        java.lang.System.out.println("x₂₀ = " + x2_0);
        java.lang.System.out.println("Точность: " + epsilon);


        SystemIterationMethod method = new SystemIterationMethod();
        if (system instanceof Systems.System2) {
            ((Systems.System2) system).setRegion(x1_0);
        }
        SystemIterationMethod.SystemSolution solution = method.solve(system, x1_0, x2_0, epsilon);

        if (solution != null && solution.converged) {
            boolean toFile = ioManager.selectOutputMode();
            ioManager.outputSystemResults(solution, system, toFile);

            java.lang.System.out.print("\nВывести график функции? (y/n): ");
            if (ioManager.readYesNo()) {
                double[] rootsX1 = {solution.x1};
                double[] rootsX2 = {solution.x2};
                plotter.plotSystem(system, -3, 3, rootsX1,  rootsX2);

                java.lang.System.out.print("Сохранить данные графика в CSV? (y/n): ");
                if (ioManager.readYesNo()) {
                    plotter.saveChart();
                }
            }
        } else {
            java.lang.System.err.println("Метод не сошелся!");
        }



    }

    public static void main(String[] args) throws IOException {
        NonlinearEquationSolver solver = new NonlinearEquationSolver();
        solver.run();
    }
}