/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tetris;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 *
 * @author Kent
 */
public class VoicePanel extends JPanel {

    private final Board board;
    private final JButton voiceButton;
    private final JLabel statusLabel;
    private VoiceControl voiceControl;
    private boolean voiceActive = false;

    public VoicePanel(Board board) {
        this.board = board;
        setLayout(new FlowLayout());

        voiceButton = new JButton("Activar Voz");
        voiceButton.setFont(new Font("Arial", Font.BOLD, 14));
        voiceButton.addActionListener(this::toggleVoiceControl);

        statusLabel = new JLabel("Voz: Desactivada");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        add(voiceButton);
        add(statusLabel);

        initVoiceControl();
    }

    private void initVoiceControl() {
        voiceControl = new VoiceControl(command -> {
            try {
                switch (command) {
                    case "LEFT":
                        board.moveLeft();
                        break;
                    case "RIGHT":
                        board.moveRight();
                        break;
                    case "RASTRIGHT":
                        board.moveRasRight(); // Llamamos al nuevo método para el movimiento rápido a la derecha
                        break;
                    case "RASTLEFT":
                        board.moveRasLeft(); // Llamamos al nuevo método para el movimiento rápido a la izquierda
                        break;
                    case "DOWN":
                        board.moveDown();
                        break;
                    case "ROTATE":
                        board.rotate();
                        break;
                    case "DROP":
                        board.hardDrop();
                        break;
                    case "PAUSE":
                        board.togglePause();
                        break;
                    case "START":
                        board.startGame();
                        break;
                    case "UP":
                        board.moveUp();
                        break;
                    case "JUMP":
                        board.jumpUp();
                        break;
                    default:
                        System.out.println("[VoiceControl] Comando no reconocido: " + command);
                }
            } catch (Exception e) {
                System.err.println("[VoiceControl] Error procesando comando: " + e.getMessage());
            }
        });
    }

    private void toggleVoiceControl(ActionEvent e) {
        voiceActive = !voiceActive;

        if (voiceActive) {
            voiceControl.startListening();
            voiceButton.setText("Desactivar Voz");
            statusLabel.setText("Voz: Activada - Escuchando...");
        } else {
            voiceControl.stopListening();
            voiceButton.setText("Activar Voz");
            statusLabel.setText("Voz: Desactivada");
        }
    }
}
