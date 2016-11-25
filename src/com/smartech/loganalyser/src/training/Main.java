package com.smartech.loganalyser.src.training;

import eu.hansolo.enzo.gauge.AvGauge;
import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.controlsfx.dialog.Dialog;

public class Main extends Application {

    @Override
    public void start(final Stage stage) throws IOException {

        Parent root = FXMLLoader.load(getClass().getResource("GaugeTraining.fxml"));
        stage.setTitle("Log Analyser");
        Scene scene = new Scene(root);

        stage.setScene(scene);

        AvGauge gauge = new AvGauge();
        gauge.setTitle("Scan in progress");
        gauge.setMaxValue(3666);
        
        gauge.setInnerBarColor(Color.BLUE);
        gauge.setOuterBarColor(Color.BLUE);
        
        Dialog dialog = new Dialog(stage, "Scan progress");
        dialog.setContent(gauge);
        dialog.setIconifiable(false);
        dialog.setClosable(false);
        
        dialog.show();
        gauge.setInnerValue(1000);
        gauge.setOuterValue(1000);
        dialog.hide();

        stage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}
