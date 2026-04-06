package com.sensors.io;

import com.fazecast.jSerialComm.SerialPort;
import com.sensors.model.SensorData;
import com.sensors.listener.SensorDataListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Responsável pela comunicação serial com o Arduino
 */
public class SensorReader {
    private SerialPort serialPort;
    private final int BAUD_RATE = 9600;
    private final List<SensorDataListener> listeners = new ArrayList<>();
    private ScheduledExecutorService executor;
    private boolean isReading = false;

    /**
     * Conecta à porta serial especificada
     */
    public boolean connect(String portName) {
        serialPort = SerialPort.getCommPort(portName);
        serialPort.setBaudRate(BAUD_RATE);
        serialPort.setNumDataBits(8);
        serialPort.setNumStopBits(1);
        serialPort.setParity(SerialPort.NO_PARITY);
        
        return serialPort.openPort();
    }

    /**
     * Inicia a leitura contínua dos dados
     */
    public void startReading() {
        if (!serialPort.isOpen()) return;
        
        isReading = true;
        executor = Executors.newSingleThreadScheduledExecutor();
        
        executor.scheduleAtFixedRate(this::readSensorData, 0, 300, TimeUnit.MILLISECONDS);
    }

    /**
     * Lê e processa uma linha do buffer serial
     */
    private void readSensorData() {
        if (!serialPort.isOpen() || !isReading) return;

        byte[] buffer = new byte[1024];
        int bytesRead = serialPort.readBytes(buffer, buffer.length);
        
        if (bytesRead > 0) {
            String line = new String(buffer, 0, bytesRead).trim();
            SensorData data = parseSerialLine(line);
            
            if (data != null && data.isValid()) {
                notifyListeners(data);
            }
        }
    }

    /**
     * Parseia a linha do serial no formato do Arduino
     * Ex: "Intensidade de Luz = 75"
     */
    public SensorData parseSerialLine(String line) {
        Map<String, Object> values = new HashMap<>();
        
        // Extrai valores usando expressões regulares
        if (line.contains("Intensidade de Luz")) {
            String[] parts = line.split("=");
            if (parts.length == 2) {
                values.put("light", Integer.parseInt(parts[1].trim()));
            }
        }
        else if (line.contains("Temperatura")) {
            String[] parts = line.split("=");
            if (parts.length == 2) {
                String tempStr = parts[1].replaceAll("[^\\d.]", "");
                values.put("temp", Float.parseFloat(tempStr));
            }
        }
        else if (line.contains("Umidade")) {
            String[] parts = line.split("=");
            if (parts.length == 2) {
                values.put("humidity", Integer.parseInt(parts[1].replace("%", "").trim()));
            }
        }
        
        // Retorna objeto completo quando tiver os 3 valores
        if (values.size() == 3) {
            return new SensorData(
                (Integer) values.get("light"),
                (Float) values.get("temp"),
                (Integer) values.get("humidity")
            );
        }
        return null;
    }

    /**
     * Registra um listener para receber atualizações
     */
    public void addListener(SensorDataListener listener) {
        listeners.add(listener);
    }

    /**
     * Notifica todos os listeners sobre novos dados
     */
    private void notifyListeners(SensorData data) {
        for (SensorDataListener listener : listeners) {
            listener.onSensorDataUpdated(data);
        }
    }

    /**
     * Para a leitura e fecha a conexão
     */
    public void stopReading() {
        isReading = false;
        if (executor != null) executor.shutdown();
        if (serialPort.isOpen()) serialPort.closePort();
    }

    /**
     * Lista as portas seriais disponíveis
     */
    public static String[] getAvailablePorts() {
        SerialPort[] ports = SerialPort.getCommPorts();
        String[] names = new String[ports.length];
        for (int i = 0; i < ports.length; i++) {
            names[i] = ports[i].getSystemPortName();
        }
        return names;
    }
}