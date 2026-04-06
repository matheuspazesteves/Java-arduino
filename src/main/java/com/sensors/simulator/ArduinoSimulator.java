package com.sensors.simulator;

import java.io.IOException;
import java.util.Random;

/**
 * Simula dados de sensores Arduino enviando para porta serial virtual
 */
public class ArduinoSimulator {

    private final String portName;
    private final Random random = new Random();
    private volatile boolean running = false;

    // Valores atuais dos sensores
    private int lightIntensity = 50;
    private float temperature = 25.0f;
    private int humidity = 60;

    public ArduinoSimulator(String portName) {
        this.portName = portName;
    }

    /**
     * Inicia a simulação de envio de dados
     */
    public void start() throws IOException, InterruptedException {
        running = true;
        System.out.println("🤖 Iniciando simulador Arduino na porta: " + portName);

        int cycle = 0;
        while (running) {
            // Simula variação gradual dos sensores
            simulateSensorChanges(cycle++);

            // Envia dados no formato esperado pelo SensorReader
            sendSensorData();

            Thread.sleep(300); // Mesmo intervalo que o SensorReader usa
        }
    }

    /**
     * Simula mudanças realistas nos sensores
     */
    private void simulateSensorChanges(int cycle) {
        // Variação suave de temperatura (0.1 por ciclo)
        temperature += (random.nextFloat() - 0.5f) * 0.5f;
        temperature = Math.max(10, Math.min(40, temperature));

        // Variação de umidade
        humidity += random.nextInt(5) - 2;
        humidity = Math.max(30, Math.min(90, humidity));

        // Variação de luminosidade (simula dia/noite)
        int timeOfDay = (cycle % 100);
        if (timeOfDay < 50) {
            lightIntensity = 70 + random.nextInt(20); // Dia
        } else {
            lightIntensity = 10 + random.nextInt(15); // Noite
        }

        // Simula evento de alerta a cada 30 ciclos
        if (cycle % 30 == 0) {
            temperature = 38.0f; // Temperatura alta para alerta
            System.out.println("⚠️  Simulando alerta de temperatura alta!");
        }
    }

    /**
     * Envia linha de dados no formato do Arduino
     */
    private void sendSensorData() throws IOException {
        String lightMsg = String.format("Intensidade de Luz = %d\n", lightIntensity);
        String tempMsg = String.format("Temperatura = %.1f°C\n", temperature);
        String humidityMsg = String.format("Umidade = %d%%\n", humidity);

        System.out.print("→ Luz: " + lightIntensity + "% | ");
        System.out.print("Temp: " + temperature + "°C | ");
        System.out.println("Umidade: " + humidity + "%");

        // Envia para stdout (redirecionar para porta serial via socat)
        System.out.print(lightMsg);
        System.out.print(tempMsg);
        System.out.print(humidityMsg);
        System.out.flush();
    }

    public void stop() {
        running = false;
        System.out.println("⏹️ Simulador parado");
    }

    public static void main(String[] args) {
        String port = (args.length > 0) ? args[0] : "/dev/ttyUSB0";

        System.out.println("===========================================");
        System.out.println("  Simulador de Sensor Arduino");
        System.out.println("===========================================");
        System.out.println("Porta: " + port);
        System.out.println("Formato: 'Intensidade de Luz = X', 'Temperatura = X°C', 'Umidade = X%'");
        System.out.println("Pressione Ctrl+C para parar\n");

        ArduinoSimulator simulator = new ArduinoSimulator(port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            simulator.stop();
        }));

        try {
            simulator.start();
        } catch (Exception e) {
            System.err.println("Erro: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
