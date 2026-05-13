package com.odyometri.ui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.text.Font;
import javafx.util.StringConverter;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class OdyogramController {

    // --- FXML BİLEŞENLERİ ---
    @FXML private LineChart<Number, Number> chartOdyogram;
    @FXML private NumberAxis xAxis;
    @FXML private NumberAxis yAxis;

    @FXML private ComboBox<String> cbPort;
    @FXML private ComboBox<String> cbBaud;
    @FXML private Button btnRefreshPorts;
    @FXML private Button btnConnect;
    @FXML private Button btnDisconnect;
    @FXML private Button btnStartTest;
    @FXML private Label lblConnectionStatus;

    @FXML private ComboBox<String> cbEar;
    @FXML private Button btnStop;
    @FXML private Button btnClear;
    @FXML private Label lblCurrentFreq;
    @FXML private Label lblCurrentLevel;
    @FXML private Label lblCurrentDirection;
    @FXML private TextArea txtLog;

    // --- GRAFİK SERİLERİ (SADECE HAVA YOLU - AIR CONDUCTION) ---
    private XYChart.Series<Number, Number> rightEarSeries = new XYChart.Series<>();
    private XYChart.Series<Number, Number> leftEarSeries = new XYChart.Series<>();

    private final String[] frequencies = {"250", "500", "1k", "2k", "4k", "8k"};

    @FXML
    public void initialize() {
        setupAxes();
        setupChart();
        setupComboBoxes();
        setupButtonActions();

        logMessage("UI initialized. Please select a port and connect.");
    }

    private void setupAxes() {
        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(-1);
        xAxis.setUpperBound(6);
        xAxis.setTickUnit(1);
        xAxis.setMinorTickCount(0);
        xAxis.setLabel("Frequency (Hz)");

        xAxis.setTickLabelFormatter(new StringConverter<Number>() {
            @Override public String toString(Number object) {
                int index = object.intValue();
                if (index >= 0 && index < frequencies.length) return frequencies[index];
                return "";
            }
            @Override public Number fromString(String string) { return 0; }
        });

        Font labelFont = Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 14);
        xAxis.setTickLabelFont(labelFont);
        yAxis.setTickLabelFont(labelFont);

        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(-120);
        yAxis.setUpperBound(10);
        yAxis.setTickUnit(10);
        yAxis.setMinorTickVisible(false);
        yAxis.setLabel("Hearing Level (dB HL)");

        yAxis.setTickLabelFormatter(new StringConverter<Number>() {
            @Override public String toString(Number object) {
                return String.valueOf(-object.intValue());
            }
            @Override public Number fromString(String string) { return 0; }
        });
    }

    private void setupChart() {
        chartOdyogram.setHorizontalZeroLineVisible(true);
        chartOdyogram.setVerticalZeroLineVisible(false);

        rightEarSeries.setName("Right Ear");
        leftEarSeries.setName("Left Ear");

        chartOdyogram.getData().addAll(rightEarSeries, leftEarSeries);
    }

    private void setupComboBoxes() {
        cbPort.getItems().addAll("COM50", "COM51");
        cbPort.getSelectionModel().select("COM50");

        cbBaud.getItems().addAll("9600", "19200", "38400", "57600", "115200");
        cbBaud.getSelectionModel().select("9600");

        cbEar.getItems().addAll("Right Ear", "Left Ear");
        cbEar.getSelectionModel().select("Right Ear");
    }

    private void setupButtonActions() {
        btnConnect.setOnAction(e -> {
            updateConnectionStatus(true, cbPort.getValue());
            btnConnect.setDisable(true);
            btnDisconnect.setDisable(false);
            btnStartTest.setDisable(false);
            logMessage("Connected to: " + cbPort.getValue() + " @ " + cbBaud.getValue());
        });

        btnDisconnect.setOnAction(e -> {
            updateConnectionStatus(false, "");
            btnConnect.setDisable(false);
            btnDisconnect.setDisable(true);
            btnStartTest.setDisable(true);
            logMessage("Disconnected.");
        });

        btnStartTest.setOnAction(e -> {
            logMessage("Test started...");
            btnStartTest.setDisable(true);
            btnStop.setDisable(false);
            // TODO: HughsonWestlake algoritması tetiklenecek
        });

        btnStop.setOnAction(e -> {
            logMessage("Test stopped.");
            btnStartTest.setDisable(false);
            btnStop.setDisable(true);
            // TODO: Algoritmayı durdur
        });

        btnClear.setOnAction(e -> {
            // Hayalet çizgilerin kalmaması için önce grafikten serileri söküyoruz
            chartOdyogram.getData().clear();

            // Serilerin içindeki noktaları siliyoruz
            rightEarSeries.getData().clear();
            leftEarSeries.getData().clear();

            // Temiz serileri grafiğe geri ekliyoruz
            chartOdyogram.getData().addAll(rightEarSeries, leftEarSeries);

            updateCurrentPresentation("---", "---", "---");
            logMessage("Screen cleared.");
        });

        btnRefreshPorts.setOnAction(e -> {
            logMessage("Refreshing ports...");
        });
    }

    // --- YARDIMCI METODLAR ---

    public void logMessage(String message) {
        Platform.runLater(() -> {
            String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            txtLog.appendText("[" + time + "] " + message + "\n");
        });
    }

    public void updateConnectionStatus(boolean isConnected, String port) {
        Platform.runLater(() -> {
            if (isConnected) {
                lblConnectionStatus.setText("Connected: " + port);
                lblConnectionStatus.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
            } else {
                lblConnectionStatus.setText("Disconnected");
                lblConnectionStatus.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            }
        });
    }

    public void updateCurrentPresentation(String freq, String level, String direction) {
        Platform.runLater(() -> {
            lblCurrentFreq.setText(freq);
            lblCurrentLevel.setText(level);
            lblCurrentDirection.setText(direction);
        });
    }

    // isAir parametresi kaldırıldı. Sadece sağ mı sol mu olduğuna bakıyoruz.
    public void addOdyogramData(int freqIndex, int dbValue, boolean isRight) {
        Platform.runLater(() -> {
            XYChart.Series<Number, Number> targetSeries = isRight ? rightEarSeries : leftEarSeries;

            // Sağ için Kırmızı 'O', Sol için Mavi 'X'
            String symbol = isRight ? "O" : "X";
            String color = isRight ? "red" : "blue";

            targetSeries.getData().removeIf(d -> d.getXValue().intValue() == freqIndex);

            XYChart.Data<Number, Number> newData = new XYChart.Data<>(freqIndex, -dbValue);
            Label symbolLabel = new Label(symbol);
            symbolLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold; -fx-font-size: 18px;");
            newData.setNode(symbolLabel);

            targetSeries.getData().add(newData);
            targetSeries.getData().sort((d1, d2) -> Integer.compare(d1.getXValue().intValue(), d2.getXValue().intValue()));

            String earStr = isRight ? "Right Ear" : "Left Ear";
            logMessage("Point added -> " + earStr + ", " + frequencies[freqIndex] + "Hz, " + dbValue + "dB HL");
        });
    }
    
}