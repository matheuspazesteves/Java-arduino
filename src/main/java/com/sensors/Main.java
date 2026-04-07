package com.sensors;

import com.sensors.ui.MobileDisplayUI;
import com.sensors.io.SensorReader;
import com.sensors.processor.DataProcessor;
import com.sensors.model.SensorData;
import com.sensors.listener.SensorDataListener;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main extends Application {

    private SensorReader reader;
    private DataProcessor processor;
    private MobileDisplayUI ui;
    private ScheduledExecutorService simulatorExecutor;
    private boolean simulationMode = false;

    /**
     * Modo de simulação - gera dados aleatórios sem Arduino
     */
    private void startSimulation() {
        simulationMode = true;
        Random random = new Random();

        simulatorExecutor = Executors.newSingleThreadScheduledExecutor();
        simulatorExecutor.scheduleAtFixedRate(() -> {
            // Gera dados simulados
            int light = 40 + random.nextInt(50);
            float temp = 20.0f + (random.nextFloat() * 15);
            int humidity = 40 + random.nextInt(40);

            SensorData data = new SensorData(light, temp, humidity);

            // Processa e atualiza UI
            SensorData processed = processor.process(data);
            ui.onSensorDataUpdated(processed);

            // Verifica alertas
            processor.checkAlerts(processed).forEach(ui::onAlertTriggered);

            System.out.printf("Simulação: Luz=%d%% Temp=%.1f°C Umidade=%d%%%n", light, temp, humidity);
        }, 0, 1000, TimeUnit.MILLISECONDS);

        System.out.println("Modo de simulação ATIVADO (dados aleatórios)");
    }

    @Override
    public void start(Stage stage) {
        // 1. Inicializar componentes
        processor = new DataProcessor();
        ui = new MobileDisplayUI();

        // 2. Configurar UI
        Scene scene = new Scene(ui, 360, 640);
        stage.setTitle("Monitor Ambiental");
        stage.setScene(scene);
        stage.show();

        // 3. Tenta conectar ao Arduino, ou usa simulação
        reader = new SensorReader();
        String[] ports = SensorReader.getAvailablePorts();

        if (ports.length > 0 && reader.connect(ports[0])) {
            reader.addListener(new SensorDataListener() {
                @Override
                public void onSensorDataUpdated(SensorData data) {
                    SensorData processed = processor.process(data);
                    ui.onSensorDataUpdated(processed);
                    processor.checkAlerts(processed).forEach(ui::onAlertTriggered);
                }

                @Override
                public void onAlertTriggered(com.sensors.model.Alert alert) {
                    ui.onAlertTriggered(alert);
                }
            });

            reader.startReading();
            System.out.println("Monitoramento iniciado na porta: " + ports[0]);
        } else {
            System.out.println("Nenhuma porta serial encontrada - iniciando simulação...");
            startSimulation();
        }
    }

    @Override
    public void stop() {
        if (reader != null) {
            reader.stopReading();
        }
        if (simulatorExecutor != null) {
            simulatorExecutor.shutdown();
        }
        if (simulationMode) {
            System.out.println("Simulação parada");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
