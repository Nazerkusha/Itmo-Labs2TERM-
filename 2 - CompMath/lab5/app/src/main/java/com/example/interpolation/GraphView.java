package com.example.interpolation;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;

public class GraphView extends View {

    private double[] xs, ys; // original data points
    private double[][] lagrangePoints;
    private double[][] newtonPoints;
    private double xTarget;
    private double yTarget;

    private final Paint axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint nodePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint lagrangePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint newtonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint targetPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint legendPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private static final int PADDING = 80;

    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaints();
    }

    public GraphView(Context context) {
        super(context);
        initPaints();
    }

    private void initPaints() {
        axisPaint.setColor(Color.BLACK);
        axisPaint.setStrokeWidth(3f);
        axisPaint.setStyle(Paint.Style.STROKE);

        gridPaint.setColor(Color.LTGRAY);
        gridPaint.setStrokeWidth(1f);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setPathEffect(new DashPathEffect(new float[]{8, 8}, 0));

        nodePaint.setColor(Color.BLACK);
        nodePaint.setStyle(Paint.Style.FILL);

        lagrangePaint.setColor(Color.parseColor("#E53935")); // red
        lagrangePaint.setStrokeWidth(4f);
        lagrangePaint.setStyle(Paint.Style.STROKE);
        lagrangePaint.setStrokeJoin(Paint.Join.ROUND);
        lagrangePaint.setStrokeCap(Paint.Cap.ROUND);

        newtonPaint.setColor(Color.parseColor("#1565C0")); // blue
        newtonPaint.setStrokeWidth(3f);
        newtonPaint.setStyle(Paint.Style.STROKE);
        newtonPaint.setStrokeJoin(Paint.Join.ROUND);
        newtonPaint.setStrokeCap(Paint.Cap.ROUND);
        newtonPaint.setPathEffect(new DashPathEffect(new float[]{16, 8}, 0));

        targetPaint.setColor(Color.parseColor("#388E3C")); // green
        targetPaint.setStrokeWidth(2f);
        targetPaint.setStyle(Paint.Style.STROKE);
        targetPaint.setPathEffect(new DashPathEffect(new float[]{6, 6}, 0));

        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(28f);

        legendPaint.setTextSize(30f);
    }

    public void setData(double[] xs, double[] ys,
                        double[][] lagrangePoints,
                        double[][] newtonPoints,
                        double xTarget, double yTarget) {
        this.xs = xs;
        this.ys = ys;
        this.lagrangePoints = lagrangePoints;
        this.newtonPoints = newtonPoints;
        this.xTarget = xTarget;
        this.yTarget = yTarget;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (xs == null || lagrangePoints == null) return;

        int w = getWidth();
        int h = getHeight();
        int plotW = w - 2 * PADDING;
        int plotH = h - 2 * PADDING - 60; // leave room for legend

        // Compute data bounds
        double xMin = xs[0], xMax = xs[xs.length - 1];
        double yMin = Double.MAX_VALUE, yMax = Double.MIN_VALUE;

        for (double y : ys) { yMin = Math.min(yMin, y); yMax = Math.max(yMax, y); }
        for (int i = 0; i < lagrangePoints[1].length; i++) {
            yMin = Math.min(yMin, lagrangePoints[1][i]);
            yMax = Math.max(yMax, lagrangePoints[1][i]);
        }
        if (newtonPoints != null) {
            for (int i = 0; i < newtonPoints[1].length; i++) {
                yMin = Math.min(yMin, newtonPoints[1][i]);
                yMax = Math.max(yMax, newtonPoints[1][i]);
            }
        }
        yMin = Math.min(yMin, yTarget);
        yMax = Math.max(yMax, yTarget);

        // Add margin
        double yRange = yMax - yMin;
        if (yRange < 1e-10) yRange = 1.0;
        yMin -= yRange * 0.1;
        yMax += yRange * 0.1;

        // Coordinate transforms
        final double fxMin = xMin, fxMax = xMax, fyMin = yMin, fyMax = yMax;
        final int fplotW = plotW, fplotH = plotH;

        java.util.function.Function<Double, Float> toScreenX =
                xv -> PADDING + (float) ((xv - fxMin) / (fxMax - fxMin) * fplotW);
        java.util.function.Function<Double, Float> toScreenY =
                yv -> PADDING + (float) ((fyMax - yv) / (fyMax - fyMin) * fplotH);

        // Background
        canvas.drawColor(Color.WHITE);

        // Grid
        int nGridX = 6, nGridY = 5;
        for (int i = 0; i <= nGridX; i++) {
            double gx = xMin + i * (xMax - xMin) / nGridX;
            float sx = toScreenX.apply(gx);
            canvas.drawLine(sx, PADDING, sx, PADDING + plotH, gridPaint);
            canvas.drawText(String.format("%.2f", gx), sx - 20, PADDING + plotH + 28, textPaint);
        }
        for (int i = 0; i <= nGridY; i++) {
            double gy = yMin + i * (yMax - yMin) / nGridY;
            float sy = toScreenY.apply(gy);
            canvas.drawLine(PADDING, sy, PADDING + plotW, sy, gridPaint);
            canvas.drawText(String.format("%.2f", gy), 2, sy + 10, textPaint);
        }

        // Axes
        float yZero = toScreenY.apply(0.0);
        if (yZero >= PADDING && yZero <= PADDING + plotH) {
            canvas.drawLine(PADDING, yZero, PADDING + plotW, yZero, axisPaint);
        }
        float xZero = toScreenX.apply(0.0);
        if (xZero >= PADDING && xZero <= PADDING + plotW) {
            canvas.drawLine(xZero, PADDING, xZero, PADDING + plotH, axisPaint);
        }

        Paint borderPaint = new Paint();
        borderPaint.setColor(Color.DKGRAY);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(2f);
        canvas.drawRect(PADDING, PADDING, PADDING + plotW, PADDING + plotH, borderPaint);

        if (lagrangePoints != null && lagrangePoints[0].length > 1) {
            Path path = new Path();
            float sx0 = toScreenX.apply(lagrangePoints[0][0]);
            float sy0 = toScreenY.apply(lagrangePoints[1][0]);
            path.moveTo(sx0, sy0);
            for (int i = 1; i < lagrangePoints[0].length; i++) {
                float sx = toScreenX.apply(lagrangePoints[0][i]);
                float sy = toScreenY.apply(lagrangePoints[1][i]);
                path.lineTo(sx, sy);
            }
            canvas.drawPath(path, lagrangePaint);
        }

        if (newtonPoints != null && newtonPoints[0].length > 1) {
            Path path = new Path();
            float sx0 = toScreenX.apply(newtonPoints[0][0]);
            float sy0 = toScreenY.apply(newtonPoints[1][0]);
            path.moveTo(sx0, sy0);
            for (int i = 1; i < newtonPoints[0].length; i++) {
                float sx = toScreenX.apply(newtonPoints[0][i]);
                float sy = toScreenY.apply(newtonPoints[1][i]);
                path.lineTo(sx, sy);
            }
            canvas.drawPath(path, newtonPaint);
        }

        float txSx = toScreenX.apply(xTarget);
        canvas.drawLine(txSx, PADDING, txSx, PADDING + plotH, targetPaint);

        for (int i = 0; i < xs.length; i++) {
            float sx = toScreenX.apply(xs[i]);
            float sy = toScreenY.apply(ys[i]);
            canvas.drawCircle(sx, sy, 8f, nodePaint);
        }

        float tySy = toScreenY.apply(yTarget);
        Paint tpFill = new Paint(Paint.ANTI_ALIAS_FLAG);
        tpFill.setColor(Color.parseColor("#388E3C"));
        tpFill.setStyle(Paint.Style.FILL);
        canvas.drawCircle(txSx, tySy, 10f, tpFill);

        int legendY = PADDING + plotH + 55;
        lagrangePaint.setPathEffect(null);
        canvas.drawLine(PADDING, legendY, PADDING + 50, legendY, lagrangePaint);
        lagrangePaint.setPathEffect(null);
        legendPaint.setColor(Color.parseColor("#E53935"));
        canvas.drawText("Лагранж", PADDING + 58, legendY + 10, legendPaint);

        newtonPaint.setPathEffect(null);
        canvas.drawLine(PADDING + 200, legendY, PADDING + 250, legendY, newtonPaint);
        legendPaint.setColor(Color.parseColor("#1565C0"));
        canvas.drawText("Ньютон", PADDING + 258, legendY + 10, legendPaint);

        tpFill.setColor(Color.parseColor("#388E3C"));
        canvas.drawCircle(PADDING + 380, legendY, 8f, tpFill);
        legendPaint.setColor(Color.parseColor("#388E3C"));
        canvas.drawText("x*", PADDING + 395, legendY + 10, legendPaint);
    }
}
