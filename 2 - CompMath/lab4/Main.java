import java.util.*;

public class Main {
    static final String RESET  = "\u001B[0m";
    static final String BOLD   = "\u001B[1m";
    static final String GREEN  = "\u001B[32m";
    static final String CYAN   = "\u001B[36m";
    static final String YELLOW = "\u001B[33m";
    static final String RED    = "\u001B[31m";
    static final String DIM    = "\u001B[2m";

    static String bold(String s)   { return BOLD + s + RESET; }
    static String green(String s)  { return GREEN + s + RESET; }
    static String cyan(String s)   { return CYAN + s + RESET; }
    static String yellow(String s) { return YELLOW + s + RESET; }
    static String red(String s)    { return RED + s + RESET; }
    static String dim(String s)    { return DIM + s + RESET; }

    // ────────────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        printBanner();

        boolean running = true;
        while (running) {
            System.out.println(bold("МЕТОД НАИМЕНЬШИХ КВАДРАТОВ – Главное меню"));
            System.out.println(cyan("1") + " Ввод функции формулой");
            System.out.println(cyan("2") + " Ввод таблицы вручную");
            System.out.println(cyan("0") + " Выход");
            System.out.print("\nВаш выбор: ");

            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1" -> runFormulaMode(sc);
                case "2" -> runManualMode(sc);
                case "0" -> running = false;
                default  -> System.out.println(red("Неверный ввод. Введите 0, 1 или 2."));
            }
        }
        System.out.println(bold(green("\nДо свидания!")));
    }

    static void runFormulaMode(Scanner sc) {
        System.out.println();
        System.out.println(bold("Ввод формулы функции"));
        System.out.println(dim("Пример:  15*x / (x^4 + 4) | sin(x) + x^2"));
        System.out.println(dim("Функции: sin cos tan asin acos atan sqrt exp ln log abs"));
        System.out.println(dim("Константы: pi  e"));

        Parser.Expression fn = null;
        String formula = "";
        while (fn == null) {
            System.out.print("\n f(x) = ");
            formula = sc.nextLine().trim();
            if (formula.isEmpty()) { System.out.println(red("Формула не может быть пустой.")); continue; }
            try {
                fn = Parser.parse(formula);
                // quick smoke-test
                fn.eval(1.0);
            } catch (Exception e) {
                System.out.println(red("Ошибка разбора: " + e.getMessage()));
                fn = null;
            }
        }

        double a = readDouble(sc, "Левая граница [a]: ");
        double b;
        do {
            b = readDouble(sc, "Правая граница [b]: ");
            if (b <= a) System.out.println(red("b должно быть > a."));
        } while (b <= a);

        double h;
        int n;
        while (true) {
            h = readDouble(sc, "Шаг h: ");
            if (h <= 0) { System.out.println(red("Шаг должен быть > 0.")); continue; }
            n = (int) Math.round((b - a) / h) + 1;
            if (n < 8)  { System.out.println(red("Слишком мало точек (" + n + "). Уменьшите шаг.")); continue; }
            if (n > 100){ System.out.println(yellow("Предупреждение: " + n + " точек. Будет использовано 100.")); n = 100; }
            break;
        }

        double[] x = new double[n];
        double[] y = new double[n];
        System.out.println();
        System.out.printf("  %-3s  %-15s  %-15s%n", "#", "x_i", "y_i = f(x_i)");
        System.out.println(dim("  " + "─".repeat(36)));

        Parser.Expression finalFn = fn;
        for (int i = 0; i < n; i++) {
            x[i] = a + i * h;
            if (i == n - 1) x[i] = b;
            y[i] = finalFn.eval(x[i]);
            System.out.printf("  %-3d  %-15.6f  %-15.6f%n", i + 1, x[i], y[i]);
        }

        processData(x, y, sc, "f(x) = " + formula);
    }

    static void runManualMode(Scanner sc) {
        System.out.println();
        System.out.println(bold("Ввод таблицы вручную"));
        System.out.println(dim("Введите от 8 до 12 пар, по одной на строке."));
        System.out.println(dim("Пример:  1.0  3.5"));
        System.out.println(dim("Введите пустую строку для завершения."));

        List<Double> xs = new ArrayList<>(), ys = new ArrayList<>();
        int row = 1;
        while (xs.size() < 12) {
            System.out.printf("Точка %2d  (x y): ", row);
            String line = sc.nextLine().trim();
            if (line.isEmpty()) {
                if (xs.size() < 8) {
                    System.out.println(red("Нужно минимум 8 точек. Введено: " + xs.size()));
                    continue;
                }
                break;
            }
            String[] parts = line.split("\\s+");
            if (parts.length < 2) { System.out.println(red("Ожидается: x y")); continue; }
            try {
                double xi = Double.parseDouble(parts[0]);
                double yi = Double.parseDouble(parts[1]);
                xs.add(xi); ys.add(yi);
                row++;
            } catch (NumberFormatException e) {
                System.out.println(red("Некорректное число: " + e.getMessage()));
            }
        }

        double[] x = xs.stream().mapToDouble(Double::doubleValue).toArray();
        double[] y = ys.stream().mapToDouble(Double::doubleValue).toArray();
        processData(x, y, sc, null);
    }

    static void processData(double[] x, double[] y, Scanner sc, String sourceName) {
        int n = x.length;

        System.out.println();
        System.out.println(bold("РЕЗУЛЬТАТЫ АППРОКСИМАЦИИ  (" + n + " точек)"));

        LeastSq.Result[] results = LeastSq.fit(x, y);

        // ── summary table ──
        System.out.printf("%n  %-26s  %-10s  %-10s  %-6s%n",
                "Тип аппроксимации", "S", "σ (stdDev)", "R²");
        System.out.println(dim("  " + "─".repeat(62)));

        LeastSq.Result best = null;
        for (LeastSq.Result r : results) {
            if (!r.valid()) {
                System.out.printf("  %-26s  %s%n", r.name(), dim("не применима: " + r.formula()));
                continue;
            }
            if (best == null || r.stdDev() < best.stdDev()) best = r;
            System.out.printf("  %-26s  %-10.4f  %-10.4f  %-6.4f%n",
                    r.name(), r.S(), r.stdDev(), r.r2());
        }

        // ── best result ──
        System.out.println();
        if (best != null) {
            System.out.println(bold(green("Наилучшее приближение: " + best.name())));
            System.out.println(bold(green(best.formula())));
            System.out.println(green("σ = " + String.format("%.6f", best.stdDev())
                    + "R² = " + String.format("%.6f", best.r2())));
            System.out.println(interpretR2(best.r2()));
        }

        System.out.println();
        System.out.print("  Показать подробные таблицы для каждой функции? [y/n]: ");
        if ("y".equalsIgnoreCase(sc.nextLine().trim()) || "Y".equalsIgnoreCase(sc.nextLine().trim()) ||
                "Да".equalsIgnoreCase(sc.nextLine().trim()) || "да".equalsIgnoreCase(sc.nextLine().trim())) {
            for (LeastSq.Result r : results) {
                if (!r.valid()) continue;
                printDetailTable(r, x, y);
            }
        }
        for (LeastSq.Result r : results) {
            if (!Double.isNaN(r.pearsonR())) {
                System.out.printf("%n  Коэффициент корреляции Пирсона (линейная): %.6f  %s%n",
                        r.pearsonR(), interpretPearson(r.pearsonR()));
            }
        }

        // ── Graphs ──
        System.out.println();
        System.out.print("Построить графики (PNG + окна)? [y/n]: ");
        if ("y".equalsIgnoreCase(sc.nextLine().trim()) || "Y".equalsIgnoreCase(sc.nextLine().trim()) ||
                "Да".equalsIgnoreCase(sc.nextLine().trim()) || "да".equalsIgnoreCase(sc.nextLine().trim())) {
            String title = (sourceName != null ? sourceName : "Таблица");
            String outDir = System.getProperty("user.dir") + "/graphs";
            System.out.println(dim("Сохраняю PNG в: " + outDir));
            List<String> saved = GraphViewer.plot(title, x, y, results, outDir);
            System.out.println(green("Графиков сохранено: " + saved.size()));
            for (String p : saved) System.out.println(dim("    " + p));
        }

        System.out.println(dim("\n[нажмите Enter для возврата в меню]"));
        sc.nextLine();
    }


    static void printDetailTable(LeastSq.Result r, double[] x, double[] y) {
        System.out.println();
        System.out.println(r.formula());
        System.out.printf("Коэффициенты: %s%n", Arrays.toString(r.coeffs()));
        System.out.printf("S = %.6f σ = %.6f R² = %.6f%n", r.S(), r.stdDev(), r.r2());
        System.out.println();
        System.out.printf("  %-4s  %-12s  %-12s  %-12s  %-12s%n", "i", "x_i", "y_i", "φ(x_i)", "ε_i");
        System.out.println(dim("  " + "─".repeat(58)));
        for (int i = 0; i < x.length; i++) {
            System.out.printf("  %-4d  %-12.6f  %-12.6f  %-12.6f  %-12.6f%n",
                    i + 1, x[i], y[i], r.phi()[i], r.eps()[i]);
        }
    }


    static double readDouble(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = sc.nextLine().trim().replace(',', '.');
            try { return Double.parseDouble(s); }
            catch (NumberFormatException e) { System.out.println(red("  Введите число.")); }
        }
    }

    static String interpretR2(double r2) {
        if (r2 >= 0.95) return green("Очень высокое качество аппроксимации");
        if (r2 >= 0.75) return yellow("Приемлемое качество аппроксимации");
        return red("Низкое качество аппроксимации");
    }

    static String interpretPearson(double r) {
        double a = Math.abs(r);
        String dir = r >= 0 ? "прямая" : "обратная";
        if (a > 0.9)  return "(" + dir + ", очень сильная связь)";
        if (a > 0.7)  return "(" + dir + ", сильная связь)";
        if (a > 0.5)  return "(" + dir + ", умеренная связь)";
        if (a > 0.3)  return "(" + dir + ", слабая связь)";
        return "(связь практически отсутствует)";
    }

    static void printBanner() {
        System.out.println(cyan("МНК — Метод наименьших квадратов"));
    }
}
