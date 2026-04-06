package com.sensors.model;

import java.time.LocalDateTime;

/**
 * Representa um alerta gerado pelo sistema
 */
public class Alert {
    
    public enum AlertType { TEMPERATURE, HUMIDITY, LIGHT, SYSTEM }
    public enum Severity { INFO, WARNING, CRITICAL }
    
    private final AlertType type;
    private final Severity severity;
    private final String message;
    private final LocalDateTime timestamp;
    
    public Alert(AlertType type, Severity severity, String message) {
        this.type = type;
        this.severity = severity;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
    
    // Getters
    public AlertType getType() { return type; }
    public Severity getSeverity() { return severity; }
    public String getMessage() { return message; }
    public LocalDateTime getTimestamp() { return timestamp; }
    
    public boolean isCritical() {
        return severity == Severity.CRITICAL;
    }
    
    public String getNotificationText() {
        String icon = switch(severity) {
            case INFO -> "ℹ️";
            case WARNING -> "⚠️";
            case CRITICAL -> "🚨";
        };
        return String.format("%s [%s] %s", icon, type, message);
    }
    
    @Override
    public String toString() {
        return String.format("[%s] %s: %s", 
            timestamp.toString().substring(11, 19),
            severity, message);
    }
}