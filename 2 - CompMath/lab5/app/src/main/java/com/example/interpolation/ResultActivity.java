package com.example.interpolation;

import android.graphics.Color;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;

public class ResultActivity extends AppCompatActivity {

    private InterpolationData data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        data = (InterpolationData) getIntent().getSerializableExtra("data");
        if (data == null) {
            finish();
            return;
        }

        setupTabs();
    }

    private void setupTabs() {
        TabHost tabHost = findViewById(R.id.tabHost);
        tabHost.setup();

        TabHost.TabSpec tab1 = tabHost.newTabSpec("results");
        tab1.setIndicator("Результаты");
        tab1.setContent(R.id.tabResults);
        tabHost.addTab(tab1);

        TabHost.TabSpec tab2 = tabHost.newTabSpec("divided");
        tab2.setIndicator("Разд. разн.");
        tab2.setContent(R.id.tabDivided);
        tabHost.addTab(tab2);

        TabHost.TabSpec tab3 = tabHost.newTabSpec("finite");
        tab3.setIndicator("Кон. разн.");
        tab3.setContent(R.id.tabFinite);
        tabHost.addTab(tab3);

        TabHost.TabSpec tab4 = tabHost.newTabSpec("graph");
        tab4.setIndicator("График");
        tab4.setContent(R.id.tabGraph);
        tabHost.addTab(tab4);

        fillResults();
        fillDividedTable();
        fillFiniteTable();
        fillGraph();
    }

    private void fillResults() {
        TextView tv = findViewById(R.id.tvResults);
        double[] xs = data.xs;
        double[] ys = data.ys;
        double x = data.xTarget;
        int n = xs.length;

        StringBuilder sb = new StringBuilder();
        sb.append("Источник: ").append(data.sourceName).append("\n");
        sb.append("Число узлов: ").append(n).append("\n");
        sb.append("x* = ").append(x).append("\n");
        sb.append("Равноотстоящие: ")
                .append(InterpolationMath.isEquallySpaced(xs) ? "ДА" : "НЕТ").append("\n");

        sb.append("Узлы интерполяции:\n");
        for (int i = 0; i < n; i++) {
            sb.append(String.format(Locale.US, "  x%-2d = %10.6f  y%-2d = %10.6f\n", i, xs[i], i, ys[i]));
        }
        sb.append("\n");

        if (x < xs[0] || x > xs[n - 1]) {
            sb.append("ВНИМАНИЕ: x* вне диапазона — ЭКСТРАПОЛЯЦИЯ!\n\n");
        }

        sb.append("Многочлен Лагранжа\n");
        double lagResult = InterpolationMath.lagrange(xs, ys, x);
        double[] liCoeffs = InterpolationMath.lagrangeCoeffs(xs, ys, x);
        sb.append("Коэффициенты l_i(x*):\n");
        for (int i = 0; i < n; i++) {
            sb.append(String.format(Locale.US, "  l_%d = %10.6f\n", i, liCoeffs[i]));
        }
        sb.append(String.format(Locale.US, "L_n(x*) = %.8f\n\n", lagResult));

        double[][] divTable = InterpolationMath.dividedDiffTable(xs, ys);
        double newtonDivFwd = InterpolationMath.newtonDividedForward(xs, divTable, x);
        double newtonDivBwd = InterpolationMath.newtonDividedBackward(xs, divTable, x);

        sb.append("Ньютон (разд. разности)\n");
        sb.append("Формула 1 (вперёд): ");
        sb.append(String.format(Locale.US, "N_n(x*) = %.8f\n", newtonDivFwd));
        sb.append("Формула 2 (назад):  ");
        sb.append(String.format(Locale.US, "N_n(x*) = %.8f\n\n", newtonDivBwd));

        boolean isEqual = InterpolationMath.isEquallySpaced(xs);
        sb.append("Ньютон (конечные разности)\n");
        if (!isEqual) {
            sb.append("Узлы неравноотстоящие — формулы Ньютона\n");
            sb.append("с конечными разностями НЕ применимы!\n\n");
        } else {
            double[][] finTable = InterpolationMath.finiteDiffTable(ys);
            double h = xs[1] - xs[0];
            double t1 = (x - xs[0]) / h;
            double t2 = (x - xs[n - 1]) / h;
            double newtonFin1 = InterpolationMath.newtonFiniteForward(xs, finTable, x);
            double newtonFin2 = InterpolationMath.newtonFiniteBackward(xs, finTable, x);

            sb.append(String.format(Locale.US, "h = %.6f\n", h));
            sb.append(String.format(Locale.US, "t (вперёд) = (x*-x0)/h = %.4f\n", t1));
            sb.append(String.format(Locale.US, "t (назад)  = (x*-xn)/h = %.4f\n", t2));

            String advice1 = (t1 >= 0 && t1 <= 0.5) ? " ✓ " : "";
            String advice2 = (t2 >= -0.5 && t2 <= 0) ? " ✓ " : "";

            sb.append(String.format(Locale.US, "\nФормула 1 (вперёд)%s:\n  N_n(x*) = %.8f\n", advice1, newtonFin1));
            sb.append(String.format(Locale.US, "Формула 2 (назад)%s:\n  N_n(x*) = %.8f\n\n", advice2, newtonFin2));

        }

        if (isEqual && n >= 5) {
            double[][] finTable2 = InterpolationMath.finiteDiffTable(ys);
            double h = xs[1] - xs[0];
            int center = n / 2;
            double t = (x - xs[center]) / h;

            sb.append("Стирлинг (|t|≤0.25)\n");
            sb.append(String.format(Locale.US, "Центр: x0=%.4f, t=%.4f\n", xs[center], t));
            if (Math.abs(t) <= 0.25) {
                double stirResult = InterpolationMath.stirling(xs, finTable2, x);
                sb.append(String.format(Locale.US, "P_n(x*) = %.8f ✓\n\n", stirResult));
            } else {
                sb.append("|t| > 0.25 — не рекомендуется\n\n");
            }

            sb.append("Бессель (0.25≤|t|≤0.75)\n");
            int cleft = n / 2 - 1;
            if (cleft >= 0) {
                double tBessel = (x - xs[cleft]) / h;
                sb.append(String.format(Locale.US, "Центр-лев: x0=%.4f, t=%.4f\n", xs[cleft], tBessel));
                if (Math.abs(tBessel) >= 0.25 && Math.abs(tBessel) <= 0.75) {
                    double bessResult = InterpolationMath.bessel(xs, finTable2, x);
                    sb.append(String.format(Locale.US, "P_n(x*) = %.8f ✓\n\n", bessResult));
                } else {
                    sb.append("|t| вне [0.25, 0.75] — не рекомендуется\n\n");
                }
            }
        }


        sb.append("Сравнение результатов\n");
        sb.append(String.format(Locale.US, "Лагранж:         %.8f\n", lagResult));
        sb.append(String.format(Locale.US, "Ньютон (разд.↑): %.8f\n", newtonDivFwd));
        sb.append(String.format(Locale.US, "Ньютон (разд.↓): %.8f\n", newtonDivBwd));
        if (isEqual) {
            double[][] finTable3 = InterpolationMath.finiteDiffTable(ys);
            sb.append(String.format(Locale.US, "Ньютон (кон.впер.):  %.8f\n",
                    InterpolationMath.newtonFiniteForward(xs, finTable3, x)));
            sb.append(String.format(Locale.US, "Ньютон (кон.назад):  %.8f\n",
                    InterpolationMath.newtonFiniteBackward(xs, finTable3, x)));
        }

        if (data.sourceName.startsWith("function:")) {
            String fname = data.sourceName.substring("function:".length());
            double trueVal = getTrueValue(fname, x);
            if (!Double.isNaN(trueVal)) {
                sb.append(String.format(Locale.US, "\nИстинное значение: %.8f\n", trueVal));
                sb.append(String.format(Locale.US, "Погрешность Лагранж: %.2e\n",
                        Math.abs(lagResult - trueVal)));
            }
        }

        tv.setText(sb.toString());
        tv.setTextSize(13f);
        tv.setTypeface(android.graphics.Typeface.MONOSPACE);
    }

    private double getTrueValue(String fname, double x) {
        if (fname.contains("sin")) return Math.sin(x);
        if (fname.contains("e^x")) return Math.exp(x);
        if (fname.contains("x²")) return x * x + 2 * x + 1;
        return Double.NaN;
    }

    private void fillDividedTable() {
        TableLayout tl = findViewById(R.id.tableDivided);
        tl.removeAllViews();

        double[] xs = data.xs;
        double[] ys = data.ys;
        int n = xs.length;
        double[][] table = InterpolationMath.dividedDiffTable(xs, ys);

        TableRow header = new TableRow(this);
        addCell(header, "i", true);
        addCell(header, "xᵢ", true);
        addCell(header, "f(xᵢ)", true);
        for (int j = 1; j < n; j++) {
            addCell(header, "f[" + j + "]", true);
        }
        tl.addView(header);

        for (int i = 0; i < n; i++) {
            TableRow row = new TableRow(this);
            row.setBackgroundColor(i % 2 == 0 ? Color.WHITE : Color.parseColor("#F5F5F5"));
            addCell(row, String.valueOf(i), false);
            addCell(row, fmt(xs[i]), false);
            for (int j = 0; j < n - i; j++) {
                addCell(row, fmt(table[i][j]), false);
            }
            for (int j = n - i; j < n; j++) {
                addCell(row, "", false);
            }
            tl.addView(row);
        }

        TextView tvNote = findViewById(R.id.tvDividedNote);
        StringBuilder sb = new StringBuilder("Верхняя строка (формула 1): ");
        for (int j = 0; j < n; j++) {
            sb.append(fmt(table[0][j]));
            if (j < n - 1) sb.append(" | ");
        }
        tvNote.setText(sb.toString());
    }

    private void fillFiniteTable() {
        TableLayout tl = findViewById(R.id.tableFinite);
        tl.removeAllViews();
        TextView tvNote = findViewById(R.id.tvFiniteNote);

        double[] xs = data.xs;
        double[] ys = data.ys;
        int n = xs.length;

        if (!InterpolationMath.isEquallySpaced(xs)) {
            tvNote.setText(" Узлы неравноотстоящие.\nТаблица конечных разностей неприменима.");
            return;
        }

        double[][] table = InterpolationMath.finiteDiffTable(ys);
        double h = xs[1] - xs[0];
        tvNote.setText(String.format(Locale.US, "Шаг h = %.6f", h));

        TableRow header = new TableRow(this);
        addCell(header, "i", true);
        addCell(header, "xᵢ", true);
        addCell(header, "yᵢ", true);
        for (int j = 1; j < n; j++) {
            addCell(header, "Δ" + superscript(j), true);
        }
        tl.addView(header);

        for (int i = 0; i < n; i++) {
            TableRow row = new TableRow(this);
            row.setBackgroundColor(i % 2 == 0 ? Color.WHITE : Color.parseColor("#F5F5F5"));
            addCell(row, String.valueOf(i), false);
            addCell(row, fmt(xs[i]), false);
            for (int j = 0; j < n - i; j++) {
                addCell(row, fmt(table[i][j]), false);
            }
            for (int j = n - i; j < n; j++) {
                addCell(row, "", false);
            }
            tl.addView(row);
        }
    }

    private String superscript(int n) {
        String[] sup = {"⁰","¹","²","³","⁴","⁵","⁶","⁷","⁸","⁹"};
        if (n < 10) return sup[n];
        return String.valueOf(n);
    }

    private void fillGraph() {
        GraphView gv = findViewById(R.id.graphView);
        double[] xs = data.xs;
        double[] ys = data.ys;
        double x = data.xTarget;

        int graphPoints = 200;
        double[][] lagrangePoints = InterpolationMath.lagrangeGraphPoints(xs, ys, graphPoints);

        double[][] divTable = InterpolationMath.dividedDiffTable(xs, ys);
        double[][] newtonPoints = InterpolationMath.newtonDividedGraphPoints(xs, divTable, graphPoints, true);

        double lagY = InterpolationMath.lagrange(xs, ys, x);

        gv.setData(xs, ys, lagrangePoints, newtonPoints, x, lagY);
    }

    private void addCell(TableRow row, String text, boolean header) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setPadding(12, 8, 12, 8);
        tv.setTypeface(android.graphics.Typeface.MONOSPACE);
        tv.setTextSize(12f);
        if (header) {
            tv.setBackgroundColor(Color.parseColor("#1565C0"));
            tv.setTextColor(Color.WHITE);
            tv.setTypeface(null, android.graphics.Typeface.BOLD);
        }
        row.addView(tv);
    }

    private String fmt(double v) {
        if (Double.isNaN(v) || Double.isInfinite(v)) return "—";
        return String.format(Locale.US, "%9.5f", v);
    }
}
