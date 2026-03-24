package com.nazerke.functions;

import com.nazerke.interfaces.System;

public class Systems {

    /**
     * x^2 + y^2 = 4
     * x - y = 1
     */
    public static class System1 implements System {
        @Override
        public double f1(double x, double y) {
            return x * x + y * y - 4;
        }

        @Override
        public double f2(double x, double y) {
            return x - y - 1;
        }

        @Override
        public double df1_dx(double x, double y) {
            return 2 * x;
        }

        @Override
        public double df1_dy(double x, double y) {
            return 2 * y;
        }

        @Override
        public double df2_dx(double x, double y) {
            return 1;
        }

        @Override
        public double df2_dy(double x, double y) {
            return -1;
        }

        @Override
        public double phi1(double x, double y) {
            double val = 4 - y * y;
            if (val < 0) return x; // защита

            return Math.signum(x) == 0 ? Math.sqrt(val) : Math.signum(x) * Math.sqrt(val);
        }

        @Override
        public double phi2(double x, double y) {
            return x - 1;
        }

        @Override
        public String getDescription() {
            return "x² + y² = 4\nx - y = 1";
        }
    }

    /**
     * 𝑠𝑖𝑛(𝑥+𝑦) − 1,2𝑥 - 0,2 = 0
     * 𝑥^2 + 2𝑦^2 - 1 = 0
     */
    public static class System2 implements System {
        @Override
        public double f1(double x, double y) {
            return Math.sin(x + y) - 1.2 * x - 0.2;
        }

        @Override
        public double f2(double x, double y) {
            return x * x + 2 * y * y - 1;
        }

        @Override
        public double df1_dx(double x, double y) { return Math.cos(x + y) - 1.2; }
        @Override
        public double df1_dy(double x, double y) { return Math.cos(x + y); }
        @Override
        public double df2_dx(double x, double y) { return 2 * x; }
        @Override
        public double df2_dy(double x, double y) { return 4 * y; }

        private boolean positiveRegion = true;

        public void setRegion(double x0) {
            this.positiveRegion = (x0 >= 0);
        }

        @Override
        public double phi1(double x, double y) {
            if (positiveRegion) {
                // x из f1: x = (sin(x+y) - 0.2) / 1.2
                return (Math.sin(x + y) - 0.2) / 1.2;
            } else {
                // x из f2: x = -sqrt(1 - 2y²)
                double val = 1 - 2 * y * y;
                if (val < 0) return x;
                return -Math.sqrt(val);
            }
        }

        @Override
        public double phi2(double x, double y) {
            if (positiveRegion) {
                // y из f2: y = +sqrt((1-x²)/2)
                double val = (1 - x * x) / 2;
                if (val < 0) return y;
                return Math.sqrt(val);
            } else {
                // y из f1: y = asin(1.2x+0.2) - x
                double arg = 1.2 * x + 0.2;
                if (Math.abs(arg) > 1.0) return y;
                return Math.asin(arg) - x;
            }
        }

        @Override
        public String getDescription() {
            return "sin(x+y) - 1.2x = 0.2\nx² + 2y² = 1";
        }
    }
}
