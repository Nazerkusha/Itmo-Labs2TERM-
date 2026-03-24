package com.nazerke.ui;

import com.nazerke.interfaces.System;
import com.nazerke.methods.SystemIterationMethod;

import java.io.*;
import java.util.Scanner;


public class IOManager {
    private Scanner scanner;

    public IOManager() {
        this.scanner = new Scanner(java.lang.System.in);
    }


    public double[] readEquationInput() {
        java.lang.System.out.println("\n Ввод данных для уравнения");
        java.lang.System.out.println("1 - Ввод с клавиатуры");
        java.lang.System.out.println("2 - Чтение из файла");
        java.lang.System.out.print("Выберите способ ввода: ");

        int choice = readInt();
        double[] data = new double[3];

        if (choice == 1) {
            java.lang.System.out.print("Введите левую границу интервала (a): ");
            data[0] = readDouble();

            java.lang.System.out.print("Введите правую границу интервала (b): ");
            data[1] = readDouble();

            java.lang.System.out.print("Введите точность вычисления (epsilon): ");
            data[2] = readDouble();
        } else {
            try {
                data = readFromFile("equation_input.txt");
            } catch (IOException e) {
                java.lang.System.err.println("Ошибка чтения файла: " + e.getMessage());
                return null;
            }
        }

        if (data[0] >= data[1]) {
            java.lang.System.err.println("Ошибка: левая граница должна быть меньше правой!");
            return null;
        }

        if (data[2] <= 0 || data[2] > 1) {
            java.lang.System.err.println("Ошибка: epsilon должна быть в диапазоне (0, 1)!");
            return null;
        }

        return data;
    }

    public double[] readSystemInput() {
        java.lang.System.out.println("\n Ввод начальных приближений ");
        java.lang.System.out.println("1 - Ввод с клавиатуры");
        java.lang.System.out.println("2 - Чтение из файла");
        java.lang.System.out.print("Выберите способ ввода: ");

        int choice = readInt();
        double[] data = new double[3];

        if (choice == 1) {
            java.lang.System.out.print("Введите начальное приближение x₁₀: ");
            data[0] = readDouble();

            java.lang.System.out.print("Введите начальное приближение x₂₀: ");
            data[1] = readDouble();

            java.lang.System.out.print("Введите точность (epsilon): ");
            data[2] = readDouble();
        } else {
            try {
                data = readFromFile("system_input.txt");
            } catch (IOException e) {
                java.lang.System.err.println("Ошибка чтения файла: " + e.getMessage());
                return null;
            }
        }

        if (data[2] <= 0 || data[2] > 1) {
            java.lang.System.err.println("Ошибка: epsilon должна быть в диапазоне (0, 1)!");
            return null;
        }

        return data;
    }


    private int readInt() {
        while (true) {
            try {
                int value = scanner.nextInt();
                scanner.nextLine();
                return value;
            } catch (java.util.InputMismatchException e) {
                java.lang.System.err.println("Ошибка: введите целое число!");
                scanner.nextLine();
                java.lang.System.out.print("Повторите ввод: ");
            }
        }
    }


    private double readDouble() {
        while (true) {
            try {
                double value = scanner.nextDouble();
                scanner.nextLine();
                return value;
            } catch (java.util.InputMismatchException e) {
                java.lang.System.err.println("Ошибка: введите число (целое или дробное)!");
                scanner.nextLine();
                java.lang.System.out.print("Повторите ввод: ");
            }
        }
    }


    private double[] readFromFile(String fileName) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        double[] data = new double[3];

        String line;
        int index = 0;

        while ((line = reader.readLine()) != null && index < 3) {
            line = line.trim();
            if (!line.isEmpty() && !line.startsWith("#")) {
                try {
                    data[index] = Double.parseDouble(line);
                    index++;
                } catch (NumberFormatException e) {
                    java.lang.System.err.println("Ошибка: некорректное число в файле: " + line);
                    reader.close();
                    throw new IOException("Неверный формат данных в файле");
                }
            }
        }

        reader.close();

        if (index < 3) {
            throw new IOException("Недостаточно данных в файле (требуется 3 числа)");
        }

        return data;
    }

    public void outputResults(ResultHandler result, boolean toFile) {
        String output = result.toString();

        java.lang.System.out.println("\n" + output);

        if (toFile) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("equation_results.txt"))) {
                writer.write(output);
                java.lang.System.out.println("\nРезультаты сохранены в файл: equation_results.txt");
            } catch (IOException e) {
                java.lang.System.err.println("Ошибка при сохранении в файл: " + e.getMessage());
            }
        }
    }


    public void outputSystemResults(SystemIterationMethod.SystemSolution solution,
                                    System system, boolean toFile) {
        StringBuilder sb = new StringBuilder();
        sb.append(" Решение системы нелинейных уравнений \n");
        sb.append("Система: \n").append(system.getDescription()).append("\n\n");
        sb.append(solution.toString()).append("\n");

        String output = sb.toString();
        java.lang.System.out.println("\n" + output);

        if (toFile) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("system_results.txt"))) {
                writer.write(output);
                java.lang.System.out.println("Результаты сохранены в файл: system_results.txt");
            } catch (IOException e) {
                java.lang.System.err.println("Ошибка при сохранении в файл: " + e.getMessage());
            }
        }
    }


    public boolean selectOutputMode() {
        java.lang.System.out.println("\n Способ вывода результатов ");
        java.lang.System.out.println("1 - Вывод только на экран");
        java.lang.System.out.println("2 - Вывод в файл и на экран");
        java.lang.System.out.print("Выберите: ");

        int choice = readInt();
        return choice == 2;
    }

    public boolean readYesNo() {
        String response = scanner.nextLine().trim();
        return response.equalsIgnoreCase("y") || response.equalsIgnoreCase("yes") || response.equalsIgnoreCase("да");
    }

    public void close() {
        scanner.close();
    }
}