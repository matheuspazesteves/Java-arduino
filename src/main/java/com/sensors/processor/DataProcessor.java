package com.sensors.processor;

import com.sensors.model.SensorData;
import com.sensors.model.Alert;
import com.sensors.model.Alert.AlertType;
import com.sensors.model.Alert.Severity;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Processa dados dos sensores e gera alertas baseados em thresholds
 */
public class DataProcessor {
    
    // Thresholds configuráveis para alertas
    private static final float TEMP_MAX = 35.0f;
    private static final float TEMP_MIN = 10.0f;
    private static final int HUMIDITY_MAX = 80;
    private static final int HUMIDITY_MIN = 30;
    private static final int LIGHT_MIN = 20;
    
    private final Queue<SensorData> history = new LinkedList<>();
    private static final int HISTORY_SIZE = 20; // Para média móvel

    /**
     * Processa e valida os dados recebidos
     */
    public SensorData process(SensorData rawData) {
        // Aplica filtro de média móvel para suavizar leituras
        history.offer(rawData);
        if (history.size() > HISTORY_SIZE) history.poll();
        
        return calculateSmoothedData();
    }

    /**
     * Calcula valores com média móvel simples
     */
    private SensorData calculateSmoothedData() {
        if (history.isEmpty()) return null;
        
        int lightSum = 0, humiditySum = 0;
        float tempSum = 0;
        
        for (SensorData data : history) {
            lightSum += data.getLightIntensity();
            tempSum += data.getTemperature();
            humiditySum += data.getHumidity();
        }
        
        int count = history.size();
        return new SensorData(
            lightSum / count,
            tempSum / count,
            humiditySum / count
        );
    }

    /**
     * Verifica condições de alerta baseadas nos thresholds
     */
    public List<Alert> checkAlerts(SensorData data) {
        List<Alert> alerts = new ArrayList<>();
        
        // Alerta de Temperatura
        if (data.getTemperature() > TEMP_MAX) {
            alerts.add(new Alert(
                AlertType.TEMPERATURE,
                Severity.WARNING,
                String.format("Temperatura alta: %.1f°C (limite: %.1f°C)", 
                    data.getTemperature(), TEMP_MAX)
            ));
        } else if (data.getTemperature() < TEMP_MIN) {
            alerts.add(new Alert(
                AlertType.TEMPERATURE,
                Severity.INFO,
                String.format("Temperatura baixa: %.1f°C", data.getTemperature())
            ));
        }
        
        // Alerta de Umidade
        if (data.getHumidity() > HUMIDITY_MAX) {
            alerts.add(new Alert(
                AlertType.HUMIDITY,
                Severity.CRITICAL,
                String.format("Umidade crítica: %d%%", data.getHumidity())
            ));
        } else if (data.getHumidity() < HUMIDITY_MIN) {
            alerts.add(new Alert(
                AlertType.HUMIDITY,
                Severity.WARNING,
                "Ambiente muito seco - umidade abaixo do ideal"
            ));
        }
        
        // Alerta de Iluminação
        if (data.getLightIntensity() < LIGHT_MIN) {
            alerts.add(new Alert(
                AlertType.LIGHT,
                Severity.INFO,
                "Baixa luminosidade detectada"
            ));
        }
        
        return alerts;
    }

    /**
     * Obtém a média móvel de um sensor específico
     */
    public double getMovingAverage(String sensor) {
        if (history.isEmpty()) return 0;
        
        double sum = 0;
        for (SensorData data : history) {
            switch(sensor.toLowerCase()) {
                case "light": sum += data.getLightIntensity(); break;
                case "temperature": sum += data.getTemperature(); break;
                case "humidity": sum += data.getHumidity(); break;
            }
        }
        return sum / history.size();
    }
}