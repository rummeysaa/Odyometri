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

        // 2. Y-AXIS: -10'dan 120'ye (Klinik Standart)
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(-10);
        yAxis.setUpperBound(120);
        yAxis.setTickUnit(10);
        yAxis.setMinorTickCount(0);
        yAxis.setLabel("Hearing Level (dB HL)");

        // 3. GRAFİK AYARLARI (0 dB ve 250 Hz Çizgi Kontrolü)
        chartOdyogram.setHorizontalZeroLineVisible(true);  // Yatay 0 dB çizgisini aç
        chartOdyogram.setVerticalZeroLineVisible(false);    // 250 Hz'deki dikey kalın çizgiyi SİL

        // 4. KLİNİK İNVERSİYON (Double Flip)
        chartOdyogram.setScaleY(-1); // Grafiği takla attır (-10 yukarı, 120 aşağı)
        xAxis.setScaleY(-1);        // Yazıları düzelt
        yAxis.setScaleY(-1);        // Yazıları düzelt

        // 5. FONT VE SİYAH ÇİZGİ STİLİ (Beyaz Ekran Vermeyen Güvenli Yol)
        Font boldFont = Font.font("Arial", FontWeight.BOLD, 15);
        xAxis.setTickLabelFont(boldFont);
        yAxis.setTickLabelFont(boldFont);

        // 0 dB çizgisini siyaha ve ızgaraları belirgin hale getiren CSS
        chartOdyogram.setStyle(
                ".chart-horizontal-zero-line { -fx-stroke: black; -fx-stroke-width: 2px; }" +
                        ".chart-vertical-grid-lines { -fx-stroke: #e0e0e0; }" +
                        ".chart-horizontal-grid-lines { -fx-stroke: #e0e0e0; }"
        );
    }
}