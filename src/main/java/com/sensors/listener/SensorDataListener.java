package com.sensors.listener;

import com.sensors.model.SensorData;
import com.sensors.model.Alert;
import java.util.List;

/**
 * Interface para receber atualizações em tempo real
 */
public interface SensorDataListener {
    /**
     * Chamado quando novos dados dos sensores são recebidos
     */
    void onSensorDataUpdated(SensorData data);

    /**
     * Chamado quando um ou mais alertas são disparados
     */
    default void onAlertsTriggered(List<Alert> alerts) {
        for (Alert alert : alerts) {
            onAlertTriggered(alert);
        }
    }

    /**
     * Chamado para cada alerta individual
     */
    void onAlertTriggered(Alert alert);
}
