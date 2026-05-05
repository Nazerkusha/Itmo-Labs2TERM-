package com.example.interpolation;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.io.*;
import java.util.ArrayList;

public class FileInputActivity extends AppCompatActivity {

    private static final String[] FILE_NAMES = {
            "test1.txt", "test2.txt", "test3.txt"
    };
    private static final String[] FILE_LABELS = {
            "Тест 1: равноотстоящие узлы", "Тест 2: неравноотстоящие узлы", "Тест 3: тригонометрическая функция"
    };
    private int selectedFileIndex = 0;
    private TextView tvFilePreview;
    private EditText etXTarget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_input);

        tvFilePreview = findViewById(R.id.tvFilePreview);
        etXTarget = findViewById(R.id.etXTarget);
        Button btnSelectFile = findViewById(R.id.btnSelectFile);
        Button btnCompute = findViewById(R.id.btnCompute);

        btnSelectFile.setOnClickListener(v -> showFileChooser());
        btnCompute.setOnClickListener(v -> compute());

        loadPreview(selectedFileIndex);
    }
    private void showFileChooser() {
        new AlertDialog.Builder(this)
                .setTitle("Выберите тестовый файл")
                .setItems(FILE_LABELS, (dialog, which) -> {
                    selectedFileIndex = which;
                    loadPreview(which);
                })
                .show();
    }
    private void loadPreview(int index) {
        try {
            String content = readAsset(FILE_NAMES[index]);
            tvFilePreview.setText(FILE_LABELS[index] + "\n\n" + content);
        } catch (IOException e) {
            tvFilePreview.setText("Ошибка загрузки файла: " + e.getMessage());
        }
    }

    private String readAsset(String name) throws IOException {
        InputStream is = getAssets().open(name);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line).append("\n");
        }
        br.close();
        return sb.toString().trim();
    }

    private void compute() {
        try {
            String content = readAsset(FILE_NAMES[selectedFileIndex]);
            String[] lines = content.split("\\n");
            ArrayList<Double> xList = new ArrayList<>();
            ArrayList<Double> yList = new ArrayList<>();

            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split("[,;\\s]+");
                if (parts.length >= 2) {
                    xList.add(Double.parseDouble(parts[0].trim()));
                    yList.add(Double.parseDouble(parts[1].trim()));
                }
            }

            if (xList.size() < 2) {
                Toast.makeText(this, "В файле должно быть минимум 2 точки!", Toast.LENGTH_SHORT).show();
                return;
            }

            double[] xs = new double[xList.size()];
            double[] ys = new double[yList.size()];
            for (int i = 0; i < xs.length; i++) {
                xs[i] = xList.get(i);
                ys[i] = yList.get(i);
            }

            String sxTarget = etXTarget.getText().toString().trim();
            if (sxTarget.isEmpty()) {
                Toast.makeText(this, "Введите значение x!", Toast.LENGTH_SHORT).show();
                return;
            }
            double xTarget = Double.parseDouble(sxTarget);

            if (!InterpolationMath.allDistinct(xs)) {
                new AlertDialog.Builder(this)
                        .setTitle("Ошибка в файле")
                        .setMessage("Значения x должны быть различны!")
                        .setPositiveButton("OK", null)
                        .show();
                return;
            }

            sortByX(xs, ys);

            InterpolationData data = new InterpolationData(xs, ys, xTarget,
                    "file:" + FILE_NAMES[selectedFileIndex]);
            Intent intent = new Intent(this, ResultActivity.class);
            intent.putExtra("data", data);
            startActivity(intent);

        } catch (IOException e) {
            Toast.makeText(this, "Ошибка чтения файла!", Toast.LENGTH_SHORT).show();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Неверный формат данных в файле!", Toast.LENGTH_SHORT).show();
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
