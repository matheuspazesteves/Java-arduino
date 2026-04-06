package com.sensors;

import com.sensors.ui.MobileDisplayUI;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MobileApp extends Application {
    @Override
    public void start(Stage stage) {
        Scene scene = new Scene(new MobileDisplayUI(), 360, 640);
        stage.setTitle("Monitor Ambiental");
        stage.setScene(scene);
        stage.show();
    }
}
