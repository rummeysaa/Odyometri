package com.odyometri.ui;

import javafx.fxml.FXML;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.StringConverter;

public class OdyogramController {
    @FXML private ScatterChart<Number, Number> chartOdyogram;
    @FXML private NumberAxis xAxis;
    @FXML private NumberAxis yAxis;

    @FXML
    public void initialize() {
        // 1. X-AXIS: Sadece 6 ana değer, fazlalıklar silindi (0-5 Arası)
        String[] frequencies = {"250", "500", "1k", "2k", "4k", "8k"};
        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(-1);
        xAxis.setUpperBound(6);
        xAxis.setTickUnit(1);
        xAxis.setMinorTickCount(0); // Aradaki küçük çizgileri sil
        xAxis.setLabel("Frequency (Hz)");

        xAxis.setTickLabelFormatter(new StringConverter<Number>() {
            @Override
            public String toString(Number object) {
                int index = object.intValue();
                if (index >= 0 && index < frequencies.length) return frequencies[index];
                return "";
            }
            @Override
            public Number fromString(String string) { return 0; }
        });

        // 2. Y-AXIS: MATEMATİKSEL İNVERSİYON
        // Mantık: -120 en aşağıda (lower), 10 en yukarıda (upper) durur.
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(-120);
        yAxis.setUpperBound(10);
        yAxis.setTickUnit(10);
        yAxis.setMinorTickVisible(false);
        yAxis.setLabel("Hearing Level (dB HL)");

        // 3. YAZI DÜZELTME (Sihir Burada)
        // Ekranda -40 yerine 40 görünmesi için değeri -1 ile çarpıyoruz.
        yAxis.setTickLabelFormatter(new StringConverter<Number>() {
            @Override
            public String toString(Number object) {
                int val = object.intValue();
                // -10'u 10, -40'ı 40, 0'ı 0 yapar.
                return String.valueOf(-val);
            }
            @Override
            public Number fromString(String string) { return 0; }
        });

        // 4. GRAFİK AYARLARI
        // Hiçbir şeyi ters çevirmiyoruz (setScaleY falan yok!), yazılar jilet gibi düz kalıyor.
        chartOdyogram.setHorizontalZeroLineVisible(true);
        chartOdyogram.setVerticalZeroLineVisible(false);

        Font boldFont = Font.font("Arial", FontWeight.BOLD, 15);
        xAxis.setTickLabelFont(boldFont);
        yAxis.setTickLabelFont(boldFont);

        // 5. STİL: 0 dB Çizgisi
        // Sistem matematiksel olarak 0 noktasını bildiği için bu CSS çizgiyi tam yerine çizer.
        chartOdyogram.setStyle(
                ".chart-horizontal-zero-line { -fx-stroke: black; -fx-stroke-width: 2.5px; }" +
                        ".chart-vertical-grid-lines { -fx-stroke: #f0f0f0; }" +
                        ".chart-horizontal-grid-lines { -fx-stroke: #f0f0f0; }"
        );
    }
}