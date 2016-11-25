/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.smartech.loganalyser.src.training;

import eu.hansolo.enzo.gauge.AvGauge;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import org.controlsfx.dialog.Dialog;


/**
 * FXML Controller class
 *
 * @author fathi jemli
 */
public class GaugeTrainingController implements Initializable {

    @FXML
    private AnchorPane anchor;
    
    private AvGauge gauge = new AvGauge();
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        gauge.setTitle("Scan in progress");
        gauge.setMaxValue(3666);
        gauge.setInnerValue(1000);
        gauge.setOuterValue(1000);
        gauge.setInnerBarColor(Color.BLUE);
        gauge.setOuterBarColor(Color.BLUE);
        
        anchor.getChildren().add(gauge);
        
        

        /*Gauge radial = GaugeBuilder.create()
         .prefWidth(500)
         .prefHeight(500)
         .gaugeType(GaugeBuilder.GaugeType.RADIAL)
         .frameDesign(Gauge.FrameDesign.STEEL)
         .backgroundDesign(Gauge.BackgroundDesign.DARK_GRAY)
         .lcdDesign(LcdDesign.STANDARD_GREEN)
         .lcdDecimals(2)
         .lcdValueFont(Gauge.LcdFont.LCD)
         .pointerType(Gauge.PointerType.TYPE14)
         .valueColor(ColorDef.RED)
         .knobDesign(Gauge.KnobDesign.METAL)
         .knobColor(Gauge.KnobColor.SILVER)
         .sections(new Section[]{
         new Section(0, 37, Color.LIME),
         new Section(37, 60, Color.YELLOW),
         new Section(60, 75, Color.ORANGE})
         .sectionsVisible(true)
         .areas(new Section[]{new Section(75, 100, Color.RED)})
         .areasVisible(true)
         .markers(new Marker[]{
         new Marker(30, Color.MAGENTA),
         new Marker(75, Color.AQUAMARINE)})
         .markersVisible(true)
         .threshold(40)
         .thresholdVisible(true)
         .glowVisible(true)
         .glowOn(true)
         .trendVisible(true)
         .trend(Gauge.Trend.UP)
         .userLedVisible(true)
         .bargraph(true)
         .title("Temperature")
         .unit("°C")
         .build();*/
    }

}
