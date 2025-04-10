/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tetris;

import org.vosk.*;
import javax.sound.sampled.*;
import java.util.function.Consumer;

/**
 *
 * @author Kent
 */
public class VoiceControl {

    private Recognizer recognizer;
    private TargetDataLine microphone;
    private volatile boolean listening = false;
    private final Consumer<String> commandHandler;

    // Usa la ruta relativa donde tengas el modelo
    private static final String MODEL_PATH = "recursos/vosk-model-small-es-0.42";

    public VoiceControl(Consumer<String> commandHandler) {
        this.commandHandler = commandHandler;
    }

    public void startListening() {
        if (listening) {
            return;
        }

        new Thread(() -> {
            try {
                // Configuración mínima para mejor rendimiento
                LibVosk.setLogLevel(LogLevel.WARNINGS);

                // Carga el modelo desde la ruta donde lo tengas
                Model model = new Model(MODEL_PATH);
                recognizer = new Recognizer(model, 16000);

                // Configuración de audio optimizada
                AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
                DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

                microphone = (TargetDataLine) AudioSystem.getLine(info);
                microphone.open(format, 2048 * 2);  // Buffer mayor para evitar bloqueos
                microphone.start();

                listening = true;
                byte[] buffer = new byte[1024]; // Buffer reducido para menor latencia

                while (listening) {
                    int bytesRead = microphone.read(buffer, 0, buffer.length);
                    if (bytesRead > 0) {
                        if (recognizer.acceptWaveForm(buffer, bytesRead)) {
                            String result = recognizer.getResult();
                            processCommand(result);
                        }
                    }
                }
            } catch (Exception ex) {
                System.err.println("Error en VoiceControl: " + ex.getMessage());
                ex.printStackTrace();
            } finally {
                stopListening();
            }
        }).start();
    }

    public void stopListening() {
        if (!listening) {
            return; // Prevenir múltiples llamadas
        }
        listening = false;

        try {
            if (microphone != null) {
                try {
                    microphone.stop(); // Detener escucha
                } catch (Exception e) {
                    System.err.println("Error al detener micrófono: " + e.getMessage());
                } finally {
                    try {
                        microphone.close(); // Cerrar micrófono
                    } catch (Exception e) {
                        System.err.println("Error al cerrar micrófono: " + e.getMessage());
                    }
                    microphone = null;
                }
            }

            if (recognizer != null) {
                try {
                    recognizer.close(); // Libera recursos nativos
                } catch (Exception e) {
                    System.err.println("Error al cerrar recognizer: " + e.getMessage());
                }
                recognizer = null;
            }

        } catch (Exception ex) {
            System.err.println("Error al detener reconocimiento: " + ex.getMessage());
        }
    }

    private void processCommand(String result) {
        if (result == null || result.isEmpty()) {
            return;
        }

        String normalized = result.toLowerCase().trim();

        try {
            if (normalized.contains("izquierda")) {
                commandHandler.accept("LEFT");
            } else if (normalized.contains("derecha")) {
                commandHandler.accept("RIGHT");
            } else if (normalized.contains("diestro")) {
                commandHandler.accept("RASTRIGHT");
            } else if (normalized.contains("zurdo")) {
                commandHandler.accept("RASTLEFT");
            } else if (normalized.contains("abajo") || normalized.contains("dale")) {
                commandHandler.accept("DOWN");
            } else if (normalized.contains("gira") || normalized.contains("girar")) {
                commandHandler.accept("ROTATE");
            } else if (normalized.contains("cayendo") || normalized.contains("rápido")) {
                commandHandler.accept("DROP");
            } else if (normalized.contains("pausa") || normalized.contains("detener")) {
                commandHandler.accept("PAUSE");
            } else if (normalized.contains("empezar") || normalized.contains("inicio")) {
                commandHandler.accept("START");
            } else if (normalized.contains("arriba")) {
                commandHandler.accept("UP");
            } else if (normalized.contains("salto")) {
                commandHandler.accept("JUMP");
            } else if (normalized.contains("cambio")) {
                // Si se dice "cambio", se ejecuta el comando para cambiar la pieza
                commandHandler.accept("CHANGE");
            } else {
                System.out.println("[VoiceControl] Comando no reconocido: " + result);
            }
        } catch (Exception e) {
            System.err.println("[VoiceControl] Error procesando comando: " + e.getMessage());
        }
    }
}
