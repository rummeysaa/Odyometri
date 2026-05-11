package com.odyometri.ui;

import javafx.fxml.FXML;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart; // 1. BU IMPORT ŞART
import javafx.scene.control.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.StringConverter;

public class OdyogramController {
    @FXML
    private LineChart<Number, Number> chartOdyogram;
    @FXML
    private NumberAxis xAxis;
    @FXML
    private NumberAxis yAxis;

    @FXML
    private ChoiceBox<String> cbFrequency;
    @FXML
    private Spinner<Integer> spinnerDb;
    @FXML
    private ToggleButton btnRightEar;
    @FXML
    private ToggleButton btnLeftEar;
    @FXML
    private ToggleButton btnAir;
    @FXML
    private ToggleButton btnBone;
    @FXML
    private Button btnClear;
    @FXML
    private Button btnHeard;
    @FXML
    private Button btnPlay;

    // --- 2. VERİ SERİLERİNİ TANIMLIYORUZ (Sınıf düzeyinde olmalı) ---
    private XYChart.Series<Number, Number> rightAirSeries = new XYChart.Series<>();
    private XYChart.Series<Number, Number> rightBoneSeries = new XYChart.Series<>();
    private XYChart.Series<Number, Number> leftAirSeries = new XYChart.Series<>();
    private XYChart.Series<Number, Number> leftBoneSeries = new XYChart.Series<>();

    @FXML
    public void initialize() {
        // 3. EKSEN AYARLARI (Senin yazdığın kısımlar aynen kalıyor)
        String[] frequencies = { "250", "500", "1k", "2k", "4k", "8k" };
        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(-1);
        xAxis.setUpperBound(6);
        xAxis.setTickUnit(1);
        xAxis.setMinorTickCount(0);
        xAxis.setLabel("Frequency (Hz)");

        xAxis.setTickLabelFormatter(new StringConverter<Number>() {
            @Override
            public String toString(Number object) {
                int index = object.intValue();
                if (index >= 0 && index < frequencies.length)
                    return frequencies[index];
                return "";
            }

            @Override
            public Number fromString(String string) {
                return 0;
            }
        });
        Font labelFont = Font.font("Arial", FontWeight.BOLD, 16);
        xAxis.setTickLabelFont(labelFont);
        yAxis.setTickLabelFont(labelFont);

        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(-120);
        yAxis.setUpperBound(10);
        yAxis.setTickUnit(10);
        yAxis.setMinorTickVisible(false);
        yAxis.setLabel("Hearing Level (dB HL)");

        yAxis.setTickLabelFormatter(new StringConverter<Number>() {
            @Override
            public String toString(Number object) {
                return String.valueOf(-object.intValue());
            }

            @Override
            public Number fromString(String string) {
                return 0;
            }
        });

        // 4. GRAFİK VE SERİ BAĞLANTISI
        chartOdyogram.setHorizontalZeroLineVisible(true);
        chartOdyogram.setVerticalZeroLineVisible(false);
        // SERİLERİ GRAFİĞE EKLEYELİM:
        chartOdyogram.getData().add(rightAirSeries);
        chartOdyogram.getData().add(rightBoneSeries);
        chartOdyogram.getData().add(leftAirSeries);
        chartOdyogram.getData().add(leftBoneSeries);

        // 5. CHOICEBOX VE SPINNER BAŞLATMA
        cbFrequency.getItems().addAll(frequencies);
        cbFrequency.getSelectionModel().select(0);
        spinnerDb.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-10, 120, 0, 1));

        // 6. PLAY BUTONU
        btnPlay.setOnAction(e -> {
            int freqIndex = cbFrequency.getSelectionModel().getSelectedIndex();
            int dbValue = spinnerDb.getValue();
            System.out.println("Sesi Çalıyor: Freq Index: " + freqIndex + ", dB: " + dbValue);
        });

        // 7. HEARD (DUYDUM) BUTONU - SEMBOL MANTIĞI
        btnHeard.setOnAction(e -> {
            int freqIndex = cbFrequency.getSelectionModel().getSelectedIndex();
            int dbValue = spinnerDb.getValue();
            boolean isRight = btnRightEar.isSelected();
            boolean isAir = btnAir.isSelected();

            // 3. DOĞRU SERİYİ SEÇ (Kritik nokta)
            XYChart.Series<Number, Number> targetSeries;
            if (isRight) {
                targetSeries = isAir ? rightAirSeries : rightBoneSeries;
            } else {
                targetSeries = isAir ? leftAirSeries : leftBoneSeries;
            }

            String symbol = isRight ? (isAir ? "O" : "[") : (isAir ? "X" : "]");
            String color = isRight ? "red" : "blue";

            // Aynı frekanstaki eski noktayı ilgili seriden sil
            targetSeries.getData().removeIf(d -> d.getXValue().intValue() == freqIndex);

            XYChart.Data<Number, Number> newData = new XYChart.Data<>(freqIndex, -dbValue);
            Label symbolLabel = new Label(symbol);
            symbolLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold; -fx-font-size: 18px;");
            newData.setNode(symbolLabel);

            targetSeries.getData().add(newData);

            // Sırala
            targetSeries.getData()
                    .sort((d1, d2) -> Integer.compare(d1.getXValue().intValue(), d2.getXValue().intValue()));
        });

        // 8. CLEAR BUTONU (Tüm serileri ve çizgileri sıfırlar)
        btnClear.setOnAction(e -> {
            // Tüm "otobanları" tek tek boşaltıyoruz
            rightAirSeries.getData().clear();
            rightBoneSeries.getData().clear();
            leftAirSeries.getData().clear();
            leftBoneSeries.getData().clear();

            chartOdyogram.getData().clear();

            // 3. Tertemiz serileri grafiğe geri bağla (Yeni test için hazırla)
            chartOdyogram.getData().add(rightAirSeries);
            chartOdyogram.getData().add(rightBoneSeries);
            chartOdyogram.getData().add(leftAirSeries);
            chartOdyogram.getData().add(leftBoneSeries);

        });
    }
}