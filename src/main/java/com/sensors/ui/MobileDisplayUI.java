package com.sensors.ui;

import com.sensors.model.SensorData;
import com.sensors.listener.SensorDataListener;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;

/**
 * Interface visual responsiva para dispositivos móveis
 * Compatível com JavaFX + Gluon Mobile para deploy em Android/iOS
 */
public class MobileDisplayUI extends VBox implements SensorDataListener {
    
    private Label lblLight, lblTemp, lblHumidity;
    private ProgressBar barLight, barHumidity;
    private Label lblTempValue, lblStatus;
    private LineChart<Number, Number> tempChart;
    private XYChart.Series<Number, Number> tempSeries;
    private ListView<String> alertListView;
    
    private int dataPointCount = 0;
    private static final int MAX_CHART_POINTS = 20;

    public MobileDisplayUI() {
        initializeCharts();
        setupMobileLayout();
        bindStyles();
    }

    private void setupMobileLayout() {
        // Configurações para toque e responsividade
        this.setSpacing(12);
        this.setPadding(new Insets(16));
        this.setStyle("-fx-background-color: #f5f5f5;");
        
        // Header
        Label header = new Label("🌡️ Monitor Ambiental");
        header.setFont(Font.font(18));
        header.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        // Cards de sensores (estilo mobile)
        VBox.setMargin(header, new Insets(0, 0, 8, 0));
        this.getChildren().add(header);
        this.getChildren().add(createSensorCard("☀️ Luz", lblLight = new Label("0%"), 
            barLight = new ProgressBar(0)));
        this.getChildren().add(createSensorCard("🌡️ Temperatura", 
            lblTempValue = new Label("0.0°C"), lblTemp = new Label("Aguardando...")));
        this.getChildren().add(createSensorCard("💧 Umidade", lblHumidity = new Label("0%"), 
            barHumidity = new ProgressBar(0)));
        
        // Área de alertas
        alertListView = new ListView<>();
        alertListView.setPrefHeight(120);
        alertListView.setStyle("-fx-background-radius: 8; -fx-border-radius: 8;");
        this.getChildren().add(createSection("🔔 Alertas", alertListView));

        // Status
        lblStatus = new Label();
        lblStatus.setFont(Font.font(16));
        lblStatus.setStyle("-fx-font-weight: bold;");
        this.getChildren().add(lblStatus);

        // Gráfico de temperatura (histórico)
        tempChart.setPrefHeight(150);
        tempChart.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 8;");
        this.getChildren().add(createSection("📈 Histórico de Temperatura", tempChart));

        // Botões de ação (touch-friendly)
        HBox actions = new HBox(10, 
            createActionButton("🔄", "Atualizar", e -> refreshData()),
            createActionButton("⚙️", "Config", e -> showSettings())
        );
        actions.setAlignment(Pos.CENTER);
        VBox.setMargin(actions, new Insets(16, 0, 0, 0));
        this.getChildren().add(actions);
    }

    private VBox createSensorCard(String title, Label valueLabel, Node indicator) {
        VBox card = new VBox(8);
        card.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 12;
            -fx-padding: 16;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);
        """);
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font(14));
        titleLabel.setStyle("-fx-font-weight: bold;");
        
        valueLabel.setFont(Font.font(20));
        valueLabel.setStyle("-fx-text-fill: #2c3e50;");
        
        if (indicator instanceof ProgressBar bar) {
            bar.setPrefWidth(250);
            bar.setPrefHeight(20);
            bar.setStyle("-fx-accent: #3498db; -fx-background-radius: 10;");
        }
        
        card.getChildren().addAll(titleLabel, valueLabel, indicator);
        return card;
    }

    private void initializeCharts() {
        // Gráfico de temperatura com eixo X fixo
        final NumberAxis xAxis = new NumberAxis(0, MAX_CHART_POINTS - 1, 1);
        final NumberAxis yAxis = new NumberAxis(0, 50, 5);
        xAxis.setForceZeroInRange(true);
        xAxis.setAutoRanging(false);
        yAxis.setAutoRanging(false);
        xAxis.setVisible(false);
        yAxis.setVisible(false);

        tempChart = new LineChart<>(xAxis, yAxis);
        tempSeries = new XYChart.Series<>();
        tempChart.getData().add(tempSeries);
        tempChart.setPrefHeight(150);
        tempChart.setCreateSymbols(false);
        tempChart.setStyle("-fx-background-color: transparent;");

        // Pre-popula com dados vazios
        for (int i = 0; i < MAX_CHART_POINTS; i++) {
            tempSeries.getData().add(new XYChart.Data<>(i, 0));
        }
    }

    @Override
    public void onSensorDataUpdated(SensorData data) {
        // Atualiza UI na thread JavaFX
        Platform.runLater(() -> {
            lblLight.setText(data.getLightIntensity() + "%");
            barLight.setProgress(data.getLightIntensity() / 100.0);
            
            lblTempValue.setText(String.format("%.1f°C", data.getTemperature()));
            updateTempStatus(data.getTemperature());
            
            lblHumidity.setText(data.getHumidity() + "%");
            barHumidity.setProgress(data.getHumidity() / 100.0);
            
            // Atualiza gráfico em estilo "rolling" (circular)
            int index = dataPointCount % MAX_CHART_POINTS;
            tempSeries.getData().get(index).setYValue(data.getTemperature());
            dataPointCount++;
        });
    }

    @Override
    public void onAlertTriggered(com.sensors.model.Alert alert) {
        Platform.runLater(() -> {
            String alertText = alert.getNotificationText();
            alertListView.getItems().add(alertText);

            // Destaque visual para alertas críticos
            if (alert.isCritical()) {
                lblStatus.setText("🚨 ATENÇÃO");
                lblStatus.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");

                // Vibração em dispositivos móveis (via Gluon)
                // DeviceService.create().vibrate(500);
            }
        });
    }

    private void updateTempStatus(float temp) {
        String status;
        Color color;
        
        if (temp < 15) {
            status = "❄️ Frio";
            color = Color.web("#3498db");
        } else if (temp < 28) {
            status = "✅ Confortável";
            color = Color.web("#27ae60");
        } else {
            status = "🔥 Quente";
            color = Color.web("#e67e22");
        }
        
        lblTemp.setText(status);
        lblTemp.setStyle("-fx-text-fill: " + toHex(color) + ";");
    }

    private String toHex(Color c) {
        return String.format("#%02X%02X%02X", 
            (int)(c.getRed()*255), (int)(c.getGreen()*255), (int)(c.getBlue()*255));
    }

    private VBox createSection(String title, Node content) {
        VBox section = new VBox(8);
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        section.getChildren().addAll(titleLabel, content);
        return section;
    }

    private Button createActionButton(String icon, String text, EventHandler<ActionEvent> handler) {
        Button btn = new Button(icon + " " + text);
        btn.setMinSize(120, 45); // Tamanho touch-friendly
        btn.setStyle("""
            -fx-background-radius: 25;
            -fx-background-color: #3498db;
            -fx-text-fill: white;
            -fx-font-weight: bold;
        """);
        btn.setOnAction(handler);
        return btn;
    }

    private void refreshData() {
        // Lógica de refresh
        alertListView.getItems().clear();
    }

    private void showSettings() {
        // Diálogo de configurações
        javafx.scene.control.Alert settings = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        settings.setTitle("Configurações");
        settings.setContentText("Thresholds e preferências...");
        settings.showAndWait();
    }

    private void bindStyles() {
        // Suporte a tema escuro/claro automático
        this.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.getStylesheets().add(getClass().getResource("/mobile-styles.css").toExternalForm());
            }
        });
    }
}