/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tetris;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import javax.swing.JFrame;

/**
 *
 * @author Kent
 */
public class Tetris extends JFrame {

    private final int BLOCK_SIZE = 25;

    private Board board;

    public Tetris() {
        initUI();
    }

    private void initUI() {
        setTitle("Tetris a voces");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        board = new Board(this);
        VoicePanel voicePanel = new VoicePanel(board);

        setLayout(new BorderLayout());
        add(board, BorderLayout.CENTER);      // Solo terreno de juego
        add(voicePanel, BorderLayout.SOUTH);  // Panel de voz abajo

        pack(); // Ajusta el JFrame al tamaño preferido de los componentes

        board.startGame(); // Iniciar juego
    }

    public int getBlockSize() {
        return BLOCK_SIZE;
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            Tetris game = new Tetris();
            game.setVisible(true);
        });
    }
}
