package com.example.interpolation;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class FunctionInputActivity extends AppCompatActivity {

    private static final String[] FUNCTION_NAMES = {
            "sin(x)",
            "e^x",
            "x² + 2x + 1",
    };

    private Spinner spinnerFunction;
    private EditText etXMin, etXMax, etNPoints, etXTarget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_function_input);

        spinnerFunction = findViewById(R.id.spinnerFunction);
        etXMin = findViewById(R.id.etXMin);
        etXMax = findViewById(R.id.etXMax);
        etNPoints = findViewById(R.id.etNPoints);
        etXTarget = findViewById(R.id.etXTarget);
        Button btnCompute = findViewById(R.id.btnCompute);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, FUNCTION_NAMES);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFunction.setAdapter(adapter);

        etXMin.setText("0");
        etXMax.setText("1");
        etNPoints.setText("5");
        etXTarget.setText("0.5");

        btnCompute.setOnClickListener(v -> compute());
    }

    private double evalFunction(int funcIndex, double x) {
        switch (funcIndex) {
            case 0: return Math.sin(x);
            case 2: return Math.exp(x);
            case 4: return x * x + 2 * x + 1;
            default: return 0;
        }
    }

    private void compute() {
        try {
            double xMin = Double.parseDouble(etXMin.getText().toString().trim());
            double xMax = Double.parseDouble(etXMax.getText().toString().trim());
            int nPoints = Integer.parseInt(etNPoints.getText().toString().trim());
            double xTarget = Double.parseDouble(etXTarget.getText().toString().trim());
            int funcIndex = spinnerFunction.getSelectedItemPosition();

            if (xMin >= xMax) {
                Toast.makeText(this, "xMin должен быть меньше xMax!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (nPoints < 2) {
                Toast.makeText(this, "Минимум 2 точки!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (nPoints > 20) {
                Toast.makeText(this, "Максимум 20 точек!", Toast.LENGTH_SHORT).show();
                return;
            }

            double[] xs = new double[nPoints];
            double[] ys = new double[nPoints];
            double step = (xMax - xMin) / (nPoints - 1);

            for (int i = 0; i < nPoints; i++) {
                xs[i] = xMin + i * step;
                ys[i] = evalFunction(funcIndex, xs[i]);
                if (Double.isNaN(ys[i])) {
                    Toast.makeText(this, "Функция не определена в точке x=" + xs[i],
                            Toast.LENGTH_LONG).show();
                    return;
                }
            }

            String funcName = FUNCTION_NAMES[funcIndex];
            InterpolationData data = new InterpolationData(xs, ys, xTarget, "function:" + funcName);
            Intent intent = new Intent(this, ResultActivity.class);
            intent.putExtra("data", data);
            startActivity(intent);

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Неверный формат числа!", Toast.LENGTH_SHORT).show();
        }
    }
}
