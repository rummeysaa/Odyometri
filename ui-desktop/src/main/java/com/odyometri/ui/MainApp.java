package com.odyometri.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/OdyogramView.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1100, 700);
        stage.setTitle("Odyometry Desktop UI");
        stage.setResizable(false); // Klinik cihaz havası için boyutu sabitleyebilirsin
        stage.setScene(scene);

        
        stage.setMinWidth(1000);
        stage.setMinHeight(650);


        stage.show();
    }
    public static void main(String[] args) { launch(); }
}