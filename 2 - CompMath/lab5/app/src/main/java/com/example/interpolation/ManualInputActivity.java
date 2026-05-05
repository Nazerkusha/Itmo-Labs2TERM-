package com.example.interpolation;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class ManualInputActivity extends AppCompatActivity {

    private LinearLayout rowsContainer;
    private EditText etXTarget;
    private final ArrayList<EditText> etXList = new ArrayList<>();
    private final ArrayList<EditText> etYList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_input);

        rowsContainer = findViewById(R.id.rowsContainer);
        etXTarget = findViewById(R.id.etXTarget);
        Button btnAddRow = findViewById(R.id.btnAddRow);
        Button btnRemoveRow = findViewById(R.id.btnRemoveRow);
        Button btnCompute = findViewById(R.id.btnCompute);

        for (int i = 0; i < 3; i++) addRow();

        btnAddRow.setOnClickListener(v -> addRow());
        btnRemoveRow.setOnClickListener(v -> removeRow());
        btnCompute.setOnClickListener(v -> compute());
    }

    private void addRow() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 4, 0, 4);

        int index = etXList.size() + 1;

        TextView label = new TextView(this);
        label.setText("x" + index + ":");
        label.setMinWidth(60);
        label.setGravity(Gravity.CENTER_VERTICAL);

        EditText etX = new EditText(this);
        etX.setHint("x");
        etX.setInputType(InputType.TYPE_CLASS_NUMBER
                | InputType.TYPE_NUMBER_FLAG_DECIMAL
                | InputType.TYPE_NUMBER_FLAG_SIGNED);
        LinearLayout.LayoutParams lpX = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        lpX.setMarginEnd(8);
        etX.setLayoutParams(lpX);

        TextView label2 = new TextView(this);
        label2.setText("y" + index + ":");
        label2.setMinWidth(60);
        label2.setGravity(Gravity.CENTER_VERTICAL);

        EditText etY = new EditText(this);
        etY.setHint("y");
        etY.setInputType(InputType.TYPE_CLASS_NUMBER
                | InputType.TYPE_NUMBER_FLAG_DECIMAL
                | InputType.TYPE_NUMBER_FLAG_SIGNED);
        LinearLayout.LayoutParams lpY = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        etY.setLayoutParams(lpY);

        row.addView(label);
        row.addView(etX);
        row.addView(label2);
        row.addView(etY);

        rowsContainer.addView(row);
        etXList.add(etX);
        etYList.add(etY);
    }

    private void removeRow() {
        int n = etXList.size();
        if (n <= 2) {
            Toast.makeText(this, "Минимум 2 узла!", Toast.LENGTH_SHORT).show();
            return;
        }
        rowsContainer.removeViewAt(rowsContainer.getChildCount() - 1);
        etXList.remove(n - 1);
        etYList.remove(n - 1);
    }

    private void compute() {
        try {
            int n = etXList.size();
            double[] xs = new double[n];
            double[] ys = new double[n];

            for (int i = 0; i < n; i++) {
                String sx = etXList.get(i).getText().toString().trim();
                String sy = etYList.get(i).getText().toString().trim();
                if (sx.isEmpty() || sy.isEmpty()) {
                    Toast.makeText(this, "Заполните все поля x и y!", Toast.LENGTH_SHORT).show();
                    return;
                }
                xs[i] = Double.parseDouble(sx);
                ys[i] = Double.parseDouble(sy);
            }

            String sxTarget = etXTarget.getText().toString().trim();
            if (sxTarget.isEmpty()) {
                Toast.makeText(this, "Введите значение x для интерполяции!", Toast.LENGTH_SHORT).show();
                return;
            }
            double xTarget = Double.parseDouble(sxTarget);

            if (!InterpolationMath.allDistinct(xs)) {
                new AlertDialog.Builder(this)
                        .setTitle("Ошибка")
                        .setMessage("Значения x должны быть различны!")
                        .setPositiveButton("OK", null)
                        .show();
                return;
            }

            sortByX(xs, ys);

            InterpolationData data = new InterpolationData(xs, ys, xTarget, "manual");
            Intent intent = new Intent(this, ResultActivity.class);
            intent.putExtra("data", data);
            startActivity(intent);

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Неверный формат числа!", Toast.LENGTH_SHORT).show();
        }
    }

    private void sortByX(double[] xs, double[] ys) {
        int n = xs.length;
        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                if (xs[i] > xs[j]) {
                    double tmp = xs[i]; xs[i] = xs[j]; xs[j] = tmp;
                    tmp = ys[i]; ys[i] = ys[j]; ys[j] = tmp;
                }
            }
        }
    }
}
