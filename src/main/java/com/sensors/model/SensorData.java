package com.sensors.model;

import java.time.LocalDateTime;

/**
 * Modelo de dados para leituras dos sensores ambientais
 */
public class SensorData {
    private int lightIntensity;    // 0-100
    private float temperature;     // °C
    private int humidity;          // 0-100%
    private LocalDateTime timestamp;

    public SensorData(int lightIntensity, float temperature, int humidity) {
        this.lightIntensity = lightIntensity;
        this.temperature = temperature;
        this.humidity = humidity;
        this.timestamp = LocalDateTime.now();
    }

    // Getters e Setters
    public int getLightIntensity() { return lightIntensity; }
    public void setLightIntensity(int lightIntensity) { this.lightIntensity = lightIntensity; }
    
    public float getTemperature() { return temperature; }
    public void setTemperature(float temperature) { this.temperature = temperature; }
    
    public int getHumidity() { return humidity; }
    public void setHumidity(int humidity) { this.humidity = humidity; }
    
    public LocalDateTime getTimestamp() { return timestamp; }

    /**
     * Valida se os dados estão dentro dos limites esperados
     */
    public boolean isValid() {
        return lightIntensity >= 0 && lightIntensity <= 100 &&
               temperature >= -50 && temperature <= 150 &&
               humidity >= 0 && humidity <= 100;
    }

    @Override
    public String toString() {
        return String.format("[%s] Luz: %d%% | Temp: %.1f°C | Umidade: %d%%",
                timestamp.toString().substring(11, 19),
                lightIntensity, temperature, humidity);
    }
}