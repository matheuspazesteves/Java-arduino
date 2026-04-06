#!/bin/bash
# Script para simular Arduino com porta serial virtual

echo "==========================================="
echo "  Simulador de Arduino - Porta Virtual"
echo "==========================================="

# Verifica se socat está instalado
if ! command -v socat &> /dev/null; then
    echo "❌ socat não encontrado. Instalando..."
    sudo apt-get update && sudo apt-get install -y socat
fi

# Cria par de portas seriais virtuais
echo "🔌 Criando portas seriais virtuais..."
echo "   PTY 1: /tmp/arduino_sim (simulador)"
echo "   PTY 2: /tmp/java_app  (aplicação Java)"

# Inicia o par de portas
socat -d -d pty,raw,echo=0,link=/tmp/arduino_sim pty,raw,echo=0,link=/tmp/java_app &
SOCAT_PID=$!

sleep 1

if ! kill -0 $SOCAT_PID 2>/dev/null; then
    echo "❌ Erro ao criar portas virtuais"
    exit 1
fi

echo "✅ Portas criadas com sucesso!"
echo ""
echo "==========================================="
echo "  Iniciando simulador..."
echo "==========================================="

# Roda o simulador enviando para a porta virtual
mvn exec:java -Dexec.mainClass="com.sensors.simulator.ArduinoSimulator" -Dexec.args="/tmp/arduino_sim"

# Limpa ao final
kill $SOCAT_PID 2>/dev/null
rm -f /tmp/arduino_sim /tmp/java_app
