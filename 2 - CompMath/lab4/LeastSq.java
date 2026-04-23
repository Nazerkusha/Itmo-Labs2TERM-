import java.util.Arrays;

public class LeastSq {

    public record Result(
            String name,
            String formula,
            double[] coeffs,
            double[] phi,
            double[] eps,
            double S,
            double stdDev,
            double r2,
            double pearsonR,
            boolean valid
    ) {}


    public static Result[] fit(double[] x, double[] y) {
        return new Result[]{
                fitPoly(x, y, 1),
                fitPoly(x, y, 2),
                fitPoly(x, y, 3),
                fitE(x, y),
                fitLog(x, y),
                fitPower(x, y)
        };
    }

    public static Result fitPoly(double[] x, double[] y, int deg) {
        int n = x.length;
        int m = deg + 1;

        double[][] A = new double[m][m];
        double[]   b = new double[m];

        for (int row = 0; row < m; row++) {
            for (int col = 0; col < m; col++) {
                double sum = 0;
                for (int i = 0; i < n; i++) sum += Math.pow(x[i], row + col);
                A[row][col] = sum;
            }
            double sum = 0;
            for (int i = 0; i < n; i++) sum += y[i] * Math.pow(x[i], row);
            b[row] = sum;
        }

        double[] c = gaussianElimination(A, b);
        double[] phi = evalPoly(c, x);
        double[] eps = residuals(phi, y);
        double S      = sumSq(eps);
        double stdDev = Math.sqrt(S / n);
        double r2     = r2(y, phi);
        double pearson = (deg == 1) ? pearsonR(x, y) : Double.NaN;

        String name = switch (deg) {
            case 1  -> "Линейная";
            case 2  -> "Полином 2-й степени";
            default -> "Полином 3-й степени";
        };

        return new Result(name, polyFormula(c), c, phi, eps, S, stdDev, r2, pearson, true);
    }

    public static Result fitE(double[] x, double[] y) {
        for (double v : y) {
            if (v <= 0) return invalid("Экспоненциальная", "y должен быть > 0");
        }
        int n = x.length;
        double[] lny = new double[n];
        for (int i = 0; i < n; i++) lny[i] = Math.log(y[i]);

        Result lin = fitPoly(x, lny, 1);
        double a = Math.exp(lin.coeffs()[0]);
        double b = lin.coeffs()[1];

        double[] phi = new double[n];
        for (int i = 0; i < n; i++) phi[i] = a * Math.exp(b * x[i]);
        double[] eps = residuals(phi, y);
        double S      = sumSq(eps);
        double stdDev = Math.sqrt(S / n);
        double r2     = r2(y, phi);

        String formula = String.format("φ(x) = %.6g · exp(%.6g · x)", a, b);
        return new Result("Экспоненциальная", formula, new double[]{a, b}, phi, eps, S, stdDev, r2, Double.NaN, true);
    }


    public static Result fitLog(double[] x, double[] y) {
        for (double v : x) {
            if (v <= 0) return invalid("Логарифмическая", "x должен быть > 0");
        }
        int n = x.length;
        double[] lnx = new double[n];
        for (int i = 0; i < n; i++) lnx[i] = Math.log(x[i]);

        Result lin = fitPoly(lnx, y, 1);
        double a = lin.coeffs()[0];
        double b = lin.coeffs()[1];

        double[] phi = new double[n];
        for (int i = 0; i < n; i++) phi[i] = a + b * Math.log(x[i]);
        double[] eps = residuals(phi, y);
        double S      = sumSq(eps);
        double stdDev = Math.sqrt(S / n);
        double r2     = r2(y, phi);

        String formula = String.format("φ(x) = %.6g + %.6g · ln(x)", a, b);
        return new Result("Логарифмическая", formula, new double[]{a, b}, phi, eps, S, stdDev, r2, Double.NaN, true);
    }

    public static Result fitPower(double[] x, double[] y) {
        for (double v : x) if (v <= 0) return invalid("Степенная", "x должен быть > 0");
        for (double v : y) if (v <= 0) return invalid("Степенная", "y должен быть > 0");

        int n = x.length;
        double[] lnx = new double[n];
        double[] lny = new double[n];
        for (int i = 0; i < n; i++) { lnx[i] = Math.log(x[i]); lny[i] = Math.log(y[i]); }

        Result lin = fitPoly(lnx, lny, 1);
        double a = Math.exp(lin.coeffs()[0]);
        double b = lin.coeffs()[1];

        double[] phi = new double[n];
        for (int i = 0; i < n; i++) phi[i] = a * Math.pow(x[i], b);
        double[] eps = residuals(phi, y);
        double S      = sumSq(eps);
        double stdDev = Math.sqrt(S / n);
        double r2     = r2(y, phi);

        String formula = String.format("φ(x) = %.6g · x^%.6g", a, b);
        return new Result("Степенная", formula, new double[]{a, b}, phi, eps, S, stdDev, r2, Double.NaN, true);
    }

    static double[] evalPoly(double[] c, double[] x) {
        int n = x.length;
        double[] phi = new double[n];
        for (int i = 0; i < n; i++) {
            double v = 0;
            for (int k = c.length - 1; k >= 0; k--) v = v * x[i] + c[k];
            phi[i] = v;
        }
        return phi;
    }

    static double[] residuals(double[] phi, double[] y) {
        double[] eps = new double[y.length];
        for (int i = 0; i < y.length; i++) eps[i] = phi[i] - y[i];
        return eps;
    }

    static double sumSq(double[] v) {
        double s = 0; for (double d : v) s += d * d; return s;
    }

    static double r2(double[] y, double[] phi) {
        double mean = Arrays.stream(y).average().orElse(0);
        double ssTot = 0, ssRes = 0;
        for (int i = 0; i < y.length; i++) {
            ssTot += (y[i] - mean) * (y[i] - mean);
            ssRes += (phi[i] - y[i]) * (phi[i] - y[i]);
        }
        if (ssTot == 0) return 1.0;
        return 1.0 - ssRes / ssTot;
    }

    static double pearsonR(double[] x, double[] y) {
        int n = x.length;
        double mx = Arrays.stream(x).average().orElse(0);
        double my = Arrays.stream(y).average().orElse(0);
        double num = 0, dx2 = 0, dy2 = 0;
        for (int i = 0; i < n; i++) {
            num  += (x[i] - mx) * (y[i] - my);
            dx2  += (x[i] - mx) * (x[i] - mx);
            dy2  += (y[i] - my) * (y[i] - my);
        }
        return num / Math.sqrt(dx2 * dy2);
    }

    static double[] gaussianElimination(double[][] A, double[] b) {
        int n = A.length;
        double[][] aug = new double[n][n + 1];
        for (int i = 0; i < n; i++) {
            System.arraycopy(A[i], 0, aug[i], 0, n);
            aug[i][n] = b[i];
        }
        for (int col = 0; col < n; col++) {
            int max = col;
            for (int row = col + 1; row < n; row++)
                if (Math.abs(aug[row][col]) > Math.abs(aug[max][col])) max = row;
            double[] tmp = aug[col]; aug[col] = aug[max]; aug[max] = tmp;

            if (Math.abs(aug[col][col]) < 1e-12)
                throw new RuntimeException("Матрица вырождена (система несовместна)");

            for (int row = col + 1; row < n; row++) {
                double f = aug[row][col] / aug[col][col];
                for (int k = col; k <= n; k++) aug[row][k] -= f * aug[col][k];
            }
        }
        double[] x = new double[n];
        for (int i = n - 1; i >= 0; i--) {
            x[i] = aug[i][n];
            for (int j = i + 1; j < n; j++) x[i] -= aug[i][j] * x[j];
            x[i] /= aug[i][i];
        }
        return x;
    }

    static String polyFormula(double[] c) {
        StringBuilder sb = new StringBuilder("φ(x) = ");
        boolean first = true;
        for (int k = 0; k < c.length; k++) {
            double v = c[k];
            if (!first) sb.append(v >= 0 ? " + " : " - ");
            else if (v < 0) sb.append("-");
            sb.append(String.format("%.6g", Math.abs(v)));
            if (k == 1) sb.append("·x");
            if (k >= 2) sb.append("·x^").append(k);
            first = false;
        }
        return sb.toString();
    }

    static Result invalid(String name, String reason) {
        return new Result(name, "Неприменима (" + reason + ")", new double[0],
                new double[0], new double[0], Double.NaN, Double.NaN, Double.NaN, Double.NaN, false);
    }
}
