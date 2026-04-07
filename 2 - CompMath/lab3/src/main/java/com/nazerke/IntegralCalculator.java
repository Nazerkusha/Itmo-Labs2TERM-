package com.nazerke;

import com.nazerke.functions.*;
import com.nazerke.methods.*;
import com.nazerke.converging.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class IntegralCalculator extends JFrame {

    private static final Color BG        = new Color(255, 209, 252);
    private static final Color white = Color.WHITE;
    private static final Color hoover = new Color(236, 2, 204);
    private static final Color hoover_bord = new Color(121, 0, 159, 118);
    private static final Color right = new Color(255, 0, 153, 186);
    private static final Color right_bord = new Color(255, 238, 254, 255);
    private static final Color error = new Color(186, 23, 66);
    private static final Color err_bg = new Color(255, 200, 220);
    private static final Color border = new Color(0, 53, 96);
    private static final Color text1 = new Color(85, 3, 113, 255);
    private static final Color text2 = new Color(255, 26, 225, 221);
    private static final Color red = new Color(180, 30, 30);
    private static final Color red_bg = new Color(255, 220, 220);


    private static final MathFunction[] FUNCTIONS = {
            new SinFunction(), new PolynomialFunction(), new ExpFunction()
    };

    private static final IntegrationMethod[] METHODS = {
            new LeftRectangleMethod(), new MidRectangleMethod(), new RightRectangleMethod(),
            new TrapezoidMethod(), new SimpsonMethod()
    };

    private static final convergingFunction[] COV_FUNCTIONS = convergingFunctions.all();

    public IntegralCalculator() {
        super("Численное интегрирование");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        buildUI();
        pack();
        setMinimumSize(new Dimension(720, 650));
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(16, 18, 16, 18));

        JLabel title = label("Численное интегрирование", 22, Font.BOLD);
        title.setBorder(new EmptyBorder(0, 0, 12, 0));
        root.add(title, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabs.setBackground(BG);
        tabs.addTab("  Обычный интеграл  ",   new IntegralFirst());
        tabs.addTab("  Несобственный 2 рода  ", new IntegralSec());
        root.add(tabs, BorderLayout.CENTER);

        setContentPane(root);
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(IntegralCalculator::new);
    }


    class IntegralFirst extends JPanel {

        private int selFunc = 0, selMeth = 0;
        private final JToggleButton[] funcButtons = new JToggleButton[FUNCTIONS.length];
        private final JToggleButton[] methButtons = new JToggleButton[METHODS.length];
        private final ButtonGroup funcGroup = new ButtonGroup();
        private final ButtonGroup methGroup = new ButtonGroup();

        private JTextField fieldA, fieldB, fieldEps;
        private JLabel statusLabel;
        private JPanel resultPanel;
        private JLabel resValue, resN, resErr;
        private DefaultTableModel tableModel;
        private JTable iterTable;

        IntegralFirst() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBackground(BG);
            setBorder(new EmptyBorder(14, 4, 4, 4));

            add(section("1. Выберите функцию", buildFuncPanel()));
            add(vgap(10));
            add(section("2. Параметры интегрирования", buildParamsPanel()));
            add(vgap(10));
            add(section("3. Метод интегрирования", buildMethodPanel()));
            add(vgap(10));
            add(buildRunArea());
            add(vgap(10));
            add(buildResultCards());
            add(vgap(10));
            add(buildIterTable());
        }

        private JPanel buildFuncPanel() {
            JPanel p = new JPanel(new GridLayout(1, FUNCTIONS.length, 10, 0));
            p.setBackground(white);
            for (int i = 0; i < FUNCTIONS.length; i++) {
                final int idx = i;
                MathFunction fn = FUNCTIONS[i];
                JToggleButton btn = new JToggleButton();
                btn.setLayout(new BorderLayout(0, 2));
                JLabel top = label(fn.getName(), 15, Font.BOLD);
                JLabel bot = label(fn.getFormula(), 11, Font.PLAIN);
                bot.setForeground(text2);
                btn.add(top, BorderLayout.CENTER);
                btn.add(bot, BorderLayout.SOUTH);
                btn.setPreferredSize(new Dimension(0, 58));
                btn.setFocusPainted(false);
                styleToggle(btn, false, hoover);
                btn.addActionListener(e -> { selFunc = idx; applyToggleGroup(funcButtons, idx, hoover); });
                funcGroup.add(btn);
                funcButtons[i] = btn;
                p.add(btn);
            }
            funcButtons[0].setSelected(true);
            applyToggleGroup(funcButtons, 0, hoover);
            return p;
        }

        private JPanel buildParamsPanel() {
            JPanel p = new JPanel(new GridLayout(1, 3, 12, 0));
            p.setBackground(white);
            fieldA = styledField("0");
            fieldB = styledField("3");
            fieldEps = styledField("0.1");
            p.add(labeledField("Нижний предел  a", fieldA));
            p.add(labeledField("Верхний предел  b", fieldB));
            p.add(labeledField("Точность  ε", fieldEps));
            return p;
        }

        private JPanel buildMethodPanel() {
            JPanel p = new JPanel(new GridLayout(3, 2, 10, 8));
            p.setBackground(white);
            for (int i = 0; i < METHODS.length; i++) {
                final int idx = i;
                JToggleButton btn = new JToggleButton(METHODS[i].getName());
                btn.setFont(new Font("Segue UI", Font.PLAIN, 13));
                btn.setFocusPainted(false);
                styleToggle(btn, false, right);
                btn.addActionListener(e -> { selMeth = idx; applyToggleGroup(methButtons, idx, right); });
                methGroup.add(btn);
                methButtons[i] = btn;
                p.add(btn);
            }
            p.add(new JPanel() {{ setBackground(white); }});
            methButtons[0].setSelected(true);
            applyToggleGroup(methButtons, 0, right);
            return p;
        }

        private JPanel buildRunArea() {
            JPanel p = new JPanel(new BorderLayout(0, 6));
            p.setBackground(BG);
            p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
            p.setAlignmentX(LEFT_ALIGNMENT);
            statusLabel = new JLabel(" ");
            statusLabel.setFont(new Font("Segue UI", Font.PLAIN, 12));
            statusLabel.setForeground(red);
            p.add(makeRunButton("Вычислить интеграл", e -> onCompute()), BorderLayout.WEST);
            p.add(statusLabel, BorderLayout.SOUTH);
            return p;
        }

        private JPanel buildResultCards() {
            resultPanel = new JPanel(new GridLayout(1, 3, 12, 0));
            resultPanel.setBackground(BG);
            resultPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
            resultPanel.setAlignmentX(LEFT_ALIGNMENT);
            resultPanel.setVisible(false);
            resValue = new JLabel("—"); resN = new JLabel("—"); resErr = new JLabel("—");
            String[] caps = {"Значение интеграла", "Число разбиений n", "Погрешность (Рунге)"};
            JLabel[] vals = {resValue, resN, resErr};
            Color[]  bgs  = {new Color(230, 200, 245), right_bord, err_bg};
            Color[]  fgs  = {hoover, right, error};
            for (int i = 0; i < 3; i++) {
                JPanel card = card(bgs[i], fgs[i]);
                JLabel cap = label(caps[i], 11, Font.PLAIN);
                cap.setForeground(fgs[i].darker());
                vals[i].setFont(new Font("Segoe UI", Font.BOLD, 17));
                vals[i].setForeground(fgs[i]);
                card.add(cap, BorderLayout.NORTH);
                card.add(vals[i], BorderLayout.CENTER);
                resultPanel.add(card);
            }
            return resultPanel;
        }

        private JScrollPane buildIterTable() {
            String[] cols = {"n", "I(n)", "I(2n)", "Погрешность (Рунге)", "Статус"};
            tableModel = new DefaultTableModel(cols, 0) {
                public boolean isCellEditable(int r, int c) { return false; }
            };
            iterTable = new JTable(tableModel);
            styleTable(iterTable);
            int[] w = {55, 115, 115, 150, 95};
            for (int i = 0; i < w.length; i++) iterTable.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
            return scrollTable(iterTable);
        }

        private void onCompute() {
            statusLabel.setText(" ");
            double a, b, eps;
            try {
                a   = Double.parseDouble(fieldA.getText().trim().replace(',', '.'));
                b   = Double.parseDouble(fieldB.getText().trim().replace(',', '.'));
                eps = Double.parseDouble(fieldEps.getText().trim().replace(',', '.'));
            } catch (NumberFormatException ex) {
                statusLabel.setText(" Введите корректные числа."); return;
            }
            if (a >= b)  { statusLabel.setText("ижний предел должен быть меньше верхнего."); return; }
            if (eps <= 0){ statusLabel.setText("Точность ε должна быть положительным числом."); return; }

            IntegrationResult result = METHODS[selMeth].compute(FUNCTIONS[selFunc], a, b, eps);
            tableModel.setRowCount(0);
            List<double[]> iters = result.iterations();
            for (int i = 0; i < iters.size(); i++) {
                double[] row = iters.get(i);
                tableModel.addRow(new Object[]{
                        (int) row[0],
                        String.format("%.8f", row[1]),
                        String.format("%.8f", row[2]),
                        String.format("%.3e", row[3]),
                        i == iters.size() - 1 ? "достигнута" : "..."
                });
            }
            iterTable.setDefaultRenderer(Object.class, new IterTableRenderer(iters.size() - 1));
            iterTable.repaint();

            resValue.setText(String.format("%.8f", result.value()));
            resN.setText(String.valueOf(result.n()));
            resErr.setText(String.format("%.3e", result.error()));
            resultPanel.setVisible(true);
            resultPanel.revalidate(); resultPanel.repaint();
        }
    }

    class IntegralSec extends JPanel {

        private int selFunc = 0;
        private final JToggleButton[] funcButtons = new JToggleButton[COV_FUNCTIONS.length];
        private final ButtonGroup funcGroup = new ButtonGroup();

        private JTextField fieldA2, fieldB2, fieldEps2;

        private JPanel convPanel;
        private JLabel convIcon, convTitle, convDetail;

        private JPanel  resultPanel;
        private JLabel  resValue, resEps, resErr;

        private DefaultTableModel tableModel;
        private JTable iterTable;
        private JLabel statusLabel;

        IntegralSec() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBackground(BG);
            setBorder(new EmptyBorder(14, 4, 4, 4));

            add(section("1. Выберите функцию", buildFuncPanel()));
            add(vgap(10));
            add(section("2. Параметры", buildParamsPanel()));
            add(vgap(10));
            add(buildConvergencePanel());
            add(vgap(10));
            add(buildRunArea());
            add(vgap(10));
            add(buildResultCards());
            add(vgap(10));
            add(buildIterTable());

            updateConvergence();
        }

        private JPanel buildFuncPanel() {
            JPanel p = new JPanel(new GridLayout(2, 2, 10, 8));
            p.setBackground(white);
            for (int i = 0; i < COV_FUNCTIONS.length; i++) {
                final int idx = i;
                convergingFunction fn = COV_FUNCTIONS[i];
                JToggleButton btn = new JToggleButton();
                btn.setLayout(new BorderLayout(0, 2));
                JLabel top = label(fn.getName(), 14, Font.BOLD);
                JLabel bot = label(fn.getFormula(), 10, Font.PLAIN);
                bot.setForeground(text2);
                btn.add(top, BorderLayout.CENTER);
                btn.add(bot, BorderLayout.SOUTH);
                btn.setPreferredSize(new Dimension(0, 60));
                btn.setFocusPainted(false);
                styleToggle(btn, false, hoover);
                btn.addActionListener(e -> {
                    selFunc = idx;
                    applyToggleGroup(funcButtons, idx, hoover);
                    updateConvergence();
                    resultPanel.setVisible(false);
                    tableModel.setRowCount(0);
                    statusLabel.setText(" ");
                });
                funcGroup.add(btn);
                funcButtons[i] = btn;
                p.add(btn);
            }
            funcButtons[0].setSelected(true);
            applyToggleGroup(funcButtons, 0, hoover);
            return p;
        }

        private JPanel buildParamsPanel() {
            JPanel p = new JPanel(new GridLayout(1, 3, 12, 0));
            p.setBackground(white);
            fieldA2   = styledField("0");
            fieldB2   = styledField("3");
            fieldEps2 = styledField("0.1");
            p.add(labeledField("Нижний предел  a", fieldA2));
            p.add(labeledField("Верхний предел  b", fieldB2));
            p.add(labeledField("Точность  ε", fieldEps2));
            return p;
        }

        private JPanel buildConvergencePanel() {
            convPanel = new JPanel(new BorderLayout(14, 0));
            convPanel.setAlignmentX(LEFT_ALIGNMENT);
            convPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
            convPanel.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(border, 1, true),
                    new EmptyBorder(12, 16, 12, 16)));

            convIcon = new JLabel("?");
            convIcon.setFont(new Font("Segue UI", Font.BOLD, 32));

            JPanel textPanel = new JPanel();
            textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
            textPanel.setOpaque(false);

            convTitle  = label("", 14, Font.BOLD);
            convDetail = label("", 12, Font.PLAIN);
            convDetail.setForeground(new Color(60, 60, 80));

            textPanel.add(convTitle);
            textPanel.add(Box.createVerticalStrut(4));
            textPanel.add(convDetail);

            convPanel.add(convIcon, BorderLayout.WEST);
            convPanel.add(textPanel, BorderLayout.CENTER);
            return convPanel;
        }

        private JPanel buildRunArea() {
            JPanel p = new JPanel(new BorderLayout(0, 6));
            p.setBackground(BG);
            p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
            p.setAlignmentX(LEFT_ALIGNMENT);
            statusLabel = new JLabel(" ");
            statusLabel.setFont(new Font("Segue UI", Font.PLAIN, 12));
            statusLabel.setForeground(red);
            p.add(makeRunButton("Вычислить несобственный интеграл", e -> onCompute()), BorderLayout.WEST);
            p.add(statusLabel, BorderLayout.SOUTH);
            return p;
        }

        private JPanel buildResultCards() {
            resultPanel = new JPanel(new GridLayout(1, 3, 12, 0));
            resultPanel.setBackground(BG);
            resultPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
            resultPanel.setAlignmentX(LEFT_ALIGNMENT);
            resultPanel.setVisible(false);

            resValue = new JLabel("—"); resEps = new JLabel("—"); resErr = new JLabel("—");
            String[] caps = {"Значение интеграла", "Финальный σ", "Погрешность (Рунге)"};
            JLabel[] vals = {resValue, resEps, resErr};
            Color[]  bgs  = {new Color(230, 200, 245), right_bord, err_bg};
            Color[]  fgs  = {hoover, right, error};
            for (int i = 0; i < 3; i++) {
                JPanel c = card(bgs[i], fgs[i]);
                JLabel cap = label(caps[i], 11, Font.PLAIN);
                cap.setForeground(fgs[i].darker());
                vals[i].setFont(new Font("Segue UI", Font.BOLD, 17));
                vals[i].setForeground(fgs[i]);
                c.add(cap, BorderLayout.NORTH);
                c.add(vals[i], BorderLayout.CENTER);
                resultPanel.add(c);
            }
            return resultPanel;
        }

        private JScrollPane buildIterTable() {
            String[] cols = {"σ", "I(σ)", "I(σ/2)", "Погрешность (Рунге)", "Статус"};
            tableModel = new DefaultTableModel(cols, 0) {
                public boolean isCellEditable(int r, int c) { return false; }
            };
            iterTable = new JTable(tableModel);
            styleTable(iterTable);
            int[] w = {85, 115, 115, 150, 95};
            for (int i = 0; i < w.length; i++) iterTable.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
            return scrollTable(iterTable);
        }

        private void updateConvergence() {
            convergingFunction fn = COV_FUNCTIONS[selFunc];
            double a = parseField(fieldA2, 0.0);
            double b = parseField(fieldB2, 1.0);
            boolean ok = fn.isConvergent(a, b);
            String singType = switch (fn.getSingularityType()) {
                case a_point -> "Разрыв в точке a (левый конец)";
                case b_point -> "Разрыв в точке b (правый конец)";
                case in_point -> "Разрыв внутри отрезка [a, b]";
            };

            if (ok) {
                convPanel.setBackground(new Color(220, 255, 235));
                convIcon.setForeground(new Color(20, 140, 60));
                convTitle.setText("Интеграл СХОДИТСЯ   (" + singType + ")");
                convTitle.setForeground(new Color(20, 120, 50));
            } else {
                convPanel.setBackground(red_bg);
                convIcon.setForeground(red);
                convTitle.setText("Интеграл НЕ СУЩЕСТВУЕТ   (" + singType + ")");
                convTitle.setForeground(red);
            }
            convDetail.setText(fn.getConvergenceInfo(a, b));
            convPanel.repaint();
        }

        private void onCompute() {
            statusLabel.setText(" ");
            resultPanel.setVisible(false);
            tableModel.setRowCount(0);

            convergingFunction fn = COV_FUNCTIONS[selFunc];

            double a, b, eps;
            try {
                a   = Double.parseDouble(fieldA2.getText().trim().replace(',', '.'));
                b   = Double.parseDouble(fieldB2.getText().trim().replace(',', '.'));
                eps = Double.parseDouble(fieldEps2.getText().trim().replace(',', '.'));
            } catch (NumberFormatException ex) {
                statusLabel.setText("Введите корректные числа."); return;
            }
            if (a >= b)  { statusLabel.setText("Нижний предел должен быть меньше верхнего."); return; }
            if (eps <= 0){ statusLabel.setText("Точность должна быть положительным числом."); return; }

            if (!fn.isConvergent(a, b)) {
                statusLabel.setText("Интеграл не существует — вычисление невозможно.");
                JOptionPane.showMessageDialog(this,
                        "Интеграл не существует.\n\n" + fn.getConvergenceInfo(a, b),
                        "Расходящийся интеграл",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            convergingIntegrator integrator = new convergingIntegrator();
            convergingIntegrator.ImproperResult result = integrator.compute(fn, a, b, eps);

            List<double[]> iters = result.iter;
            for (int i = 0; i < iters.size(); i++) {
                double[] row = iters.get(i);
                tableModel.addRow(new Object[]{
                        String.format("%.2e", row[0]),
                        String.format("%.8f", row[1]),
                        String.format("%.8f", row[2]),
                        String.format("%.3e", row[3]),
                        i == iters.size() - 1 ? " достигнута" : "..."
                });
            }
            iterTable.setDefaultRenderer(Object.class, new IterTableRenderer(iters.size() - 1));
            iterTable.repaint();

            resValue.setText(String.format("%.8f", result.v));
            resEps.setText(String.format("%.2e", result.finalSigm));
            resErr.setText(String.format("%.3e", result.error));
            resultPanel.setVisible(true);
            resultPanel.revalidate(); resultPanel.repaint();
        }

        private double parseField(JTextField f, double def) {
            try { return Double.parseDouble(f.getText().trim().replace(',', '.')); }
            catch (Exception e) { return def; }
        }
    }



    private JPanel section(String title, JPanel content) {
        JPanel w = new JPanel(new BorderLayout());
        w.setBackground(white);
        w.setAlignmentX(LEFT_ALIGNMENT);
        w.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        w.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(border, 1, true), new EmptyBorder(10, 14, 12, 14)));
        JLabel lbl = new JLabel(title.toUpperCase());
        lbl.setFont(new Font("Segue UI", Font.BOLD, 10));
        lbl.setForeground(text2);
        lbl.setBorder(new EmptyBorder(0, 0, 8, 0));
        w.add(lbl, BorderLayout.NORTH);
        w.add(content, BorderLayout.CENTER);
        return w;
    }

    private Component vgap(int h) { return Box.createRigidArea(new Dimension(0, h)); }

    private JLabel label(String text, int size, int style) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segue UI", style, size));
        l.setForeground(text1);
        return l;
    }

    private JTextField styledField(String val) {
        JTextField f = new JTextField(val);
        f.setFont(new Font("Segue UI", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(border, 1, true), new EmptyBorder(6, 8, 6, 8)));
        return f;
    }

    private JPanel labeledField(String lbl, JTextField field) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setBackground(white);
        JLabel l = label(lbl, 11, Font.PLAIN);
        l.setForeground(text2);
        p.add(l, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private JPanel card(Color bg, Color fg) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(bg);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(fg.darker(), 1, true), new EmptyBorder(10, 14, 10, 14)));
        return card;
    }

    private void styleToggle(JToggleButton btn, boolean active, Color accent) {
        btn.setBackground(active
                ? new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 40)
                : new Color(250, 240, 252));
        btn.setBorder(new LineBorder(active ? accent : border, active ? 2 : 1, true));
        btn.setForeground(active ? accent.darker() : text1);
    }

    private void applyToggleGroup(JToggleButton[] btns, int sel, Color accent) {
        for (int i = 0; i < btns.length; i++) {
            if (btns[i] == null) continue;
            styleToggle(btns[i], i == sel, accent);
        }
    }

    private JButton makeRunButton(String text, ActionListener al) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segue UI", Font.BOLD, 14));
        btn.setBackground(hoover);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 24, 10, 24));
        btn.addActionListener(al);
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(hoover_bord); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(hoover); }
        });
        return btn;
    }

    private void styleTable(JTable t) {
        t.setFont(new Font("Monospaced", Font.PLAIN, 12));
        t.setRowHeight(22);
        t.getTableHeader().setFont(new Font("Segue UI", Font.BOLD, 11));
        t.getTableHeader().setBackground(new Color(238, 220, 245));
        t.setGridColor(border);
        t.setShowGrid(true);
    }

    private JScrollPane scrollTable(JTable t) {
        JScrollPane sp = new JScrollPane(t);
        sp.setPreferredSize(new Dimension(0, 150));
        sp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        sp.setAlignmentX(LEFT_ALIGNMENT);
        sp.setBorder(new LineBorder(border, 1, true));
        return sp;
    }

    static class IterTableRenderer extends DefaultTableCellRenderer {
        private final int lastRow;
        IterTableRenderer(int lr) { this.lastRow = lr; setHorizontalAlignment(CENTER); }

        @Override
        public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean focus, int row, int col) {
            super.getTableCellRendererComponent(t, v, sel, focus, row, col);
            if (row == lastRow) {
                setBackground(new Color(210, 245, 225));
                setForeground(new Color(8, 80, 65));
                setFont(getFont().deriveFont(Font.BOLD));
            } else {
                setBackground(row % 2 == 0 ? Color.WHITE : new Color(252, 240, 255));
                setForeground(new Color(85, 3, 113));
                setFont(getFont().deriveFont(Font.PLAIN));
            }
            return this;
        }
    }
}