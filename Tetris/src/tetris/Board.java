/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tetris;

import Resutados.Resultado;
import Resutados.ResultadoManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import tetris.Shape.Tetrominoes;
import java.util.List;

/**
 *
 * @author Kent
 */
public class Board extends JPanel implements ActionListener {

    private final int BOARD_WIDTH = 10;
    private final int BOARD_HEIGHT = 20;
    private int DELAY = 800;
    private int nextThreshold = 500;

    private Timer timer;
    private boolean isDimensionalPiece = false; // Indicador de si la pieza es Dimensional
    private int dimensionalShapeTimer = 0;
    private final int DIMENSIONAL_CHANGE_INTERVAL = 10;  // Cambiar cada 20 ciclos del temporizador, ajusta como desees
    private boolean isFallingFinished = false;
    private boolean isStarted = false;
    private boolean isPaused = false;
    private int score = 0;
    private int currentX = 0;
    private int currentY = 0;
    private Shape currentPiece;
    private Shape.Tetrominoes[] board;
    private Tetris parent;
    private Shape nextPiece;
    private boolean isJumping = false; // Variable para asegurarnos de que no se realicen múltiples saltos al mismo tiempo

    public Board(Tetris parent) {
        this.parent = parent;
        currentPiece = new Shape();
        nextPiece = new Shape();
        nextPiece.setRandomShape();
        initBoard();
    }

    private void initBoard() {
        setFocusable(true);
        addKeyListener(new TAdapter());
        clearBoard();
        timer = new Timer(DELAY, this);
    }

    private void clearBoard() {
        board = new Shape.Tetrominoes[BOARD_WIDTH * BOARD_HEIGHT];
        for (int i = 0; i < BOARD_WIDTH * BOARD_HEIGHT; i++) {
            board[i] = Shape.Tetrominoes.NoShape;
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(BOARD_WIDTH * parent.getBlockSize(), BOARD_HEIGHT * parent.getBlockSize());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int blockSize = parent.getBlockSize();

        // Fondo del terreno
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, BOARD_WIDTH * blockSize, BOARD_HEIGHT * blockSize);

        drawBoard(g);
        if (currentPiece != null && currentPiece.getShape() != Shape.Tetrominoes.NoShape) {
            drawCurrentPiece(g);
        }
    }

    private void drawBoard(Graphics g) {
        int blockSize = parent.getBlockSize();

        for (int i = 1; i < BOARD_HEIGHT; i++) {
            for (int j = 0; j < BOARD_WIDTH; j++) {
                Shape.Tetrominoes shape = shapeAt(j, BOARD_HEIGHT - i - 1);
                if (shape != Shape.Tetrominoes.NoShape) {
                    drawSquare(g, j * blockSize, i * blockSize, shape);
                }
            }
        }
    }

    private void drawCurrentPiece(Graphics g) {
        int blockSize = parent.getBlockSize();

        for (int i = 0; i < 4; i++) {
            int x = currentX + currentPiece.x(i);
            int y = currentY - currentPiece.y(i);
            drawSquare(g, x * blockSize, (BOARD_HEIGHT - y - 1) * blockSize, currentPiece.getShape());
        }
    }

    private void drawSquare(Graphics g, int x, int y, Shape.Tetrominoes shape) {
        Color[] colors = {
            new Color(0, 0, 0), new Color(255, 153, 153),
            new Color(153, 255, 153), new Color(153, 153, 255),
            new Color(255, 255, 153), new Color(255, 153, 255),
            new Color(153, 255, 255), new Color(255, 204, 153)
        };

        Color color = colors[shape.ordinal()];
        int blockSize = parent.getBlockSize();

        g.setColor(color);
        g.fillRect(x + 1, y + 1, blockSize - 2, blockSize - 2);

        g.setColor(color.brighter());
        g.drawLine(x, y + blockSize - 1, x, y);
        g.drawLine(x, y, x + blockSize - 1, y);

        g.setColor(color.darker());
        g.drawLine(x + 1, y + blockSize - 1, x + blockSize - 1, y + blockSize - 1);
        g.drawLine(x + blockSize - 1, y + blockSize - 1, x + blockSize - 1, y + 1);
    }

    private Shape.Tetrominoes shapeAt(int x, int y) {
        return board[y * BOARD_WIDTH + x];
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (isFallingFinished) {
            isFallingFinished = false;
            newPiece();
        } else {
            oneLineDown();
        }
    }

    public void togglePause() {
        if (!isStarted) {
            return;
        }

        isPaused = !isPaused;
        if (isPaused) {
            timer.stop();
        } else {
            timer.start();
        }

        repaint();
    }

    public void startGame() {
        if (isPaused) {
            togglePause(); // Si está pausado, lo reanuda
            return;
        }

        isDimensionalPiece = true; // Habilita la pieza Dimensional
        isStarted = true;
        isFallingFinished = false;
        score = 0;
        clearBoard();
        newPiece();
        timer.start();
    }

    private void newPiece() {
        currentPiece = new Shape();
        currentPiece.setRandomShape();
        currentX = BOARD_WIDTH / 2 - 1;
        currentY = BOARD_HEIGHT - 1 + currentPiece.minY();

        if (!tryMove(currentPiece, currentX, currentY)) {
            currentPiece.setShape(Shape.Tetrominoes.NoShape);
            timer.stop();
            isStarted = false;

            String rutaArchivo = "resultados.txt";
            String nombre = JOptionPane.showInputDialog(parent, "Game Over! Tu puntuación: " + score + "\nIngresa tu nombre:");
            if (nombre == null || nombre.trim().isEmpty()) {
                nombre = "Jugador";
            }

            Resultado resultado = new Resultado(nombre, score);
            ResultadoManager.guardarResultado(resultado, rutaArchivo);

            // Leer y mostrar resultados guardados
            ResultadoManager.guardarResultado(resultado, "resultados.txt");

            List<Resultado> resultados = ResultadoManager.leerResultados("resultados.txt");
            StringBuilder mensaje = new StringBuilder("Ranking (si no encuentras tu nombre, haber estudiao¨):\n");
            for (Resultado r : resultados) {
                mensaje.append(r.toString()).append("\n");
            }

            JOptionPane.showMessageDialog(parent, mensaje.toString(), "Ranking - resultaditos", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void changePiece() {
        // Si la pieza actual es Dimensional, cambia su forma.
        if (currentPiece.getShape() == Tetrominoes.DimensionalShape) {
            currentPiece.setDimensionalShape();  // Cambia a una forma aleatoria
        } else {
            currentPiece.setRandomShape();  // O simplemente selecciona una nueva pieza aleatoria
        }
    }

    public boolean tryMove(Shape newPiece, int newX, int newY) {
        for (int i = 0; i < 4; i++) {
            int x = newX + newPiece.x(i);
            int y = newY - newPiece.y(i);
            if (x < 0 || x >= BOARD_WIDTH || y < 0 || y >= BOARD_HEIGHT) {
                return false;
            }
            if (shapeAt(x, y) != Shape.Tetrominoes.NoShape) {
                return false;
            }
        }
        currentPiece = newPiece;
        currentX = newX;
        currentY = newY;
        repaint();
        return true;
    }

    private void oneLineDown() {
        if (!tryMove(currentPiece, currentX, currentY - 1)) {
            pieceDropped();
        }

        // Si la pieza es Dimensional, cambia su forma al caer
        if (currentPiece.getShape() == Tetrominoes.DimensionalShape) {
            currentPiece.setDimensionalShape();  // Cambia de forma
        }
    }

    private void pieceDropped() {
        for (int i = 0; i < 4; i++) {
            int x = currentX + currentPiece.x(i);
            int y = currentY - currentPiece.y(i);

            // Asegúrate de que las coordenadas sean válidas antes de asignar
            if (x >= 0 && x < BOARD_WIDTH && y >= 0 && y < BOARD_HEIGHT) {
                board[y * BOARD_WIDTH + x] = currentPiece.getShape();
            }
        }

        removeFullLines();

        if (!isFallingFinished) {
            newPiece();
        }
    }

    private void removeFullLines() {
        int numFullLines = 0;
        for (int i = BOARD_HEIGHT - 1; i >= 0; i--) {
            boolean lineIsFull = true;
            for (int j = 0; j < BOARD_WIDTH; j++) {
                if (shapeAt(j, i) == Shape.Tetrominoes.NoShape) {
                    lineIsFull = false;
                    break;
                }
            }
            if (lineIsFull) {
                numFullLines++;
                for (int k = i; k < BOARD_HEIGHT - 1; k++) {
                    for (int j = 0; j < BOARD_WIDTH; j++) {
                        board[k * BOARD_WIDTH + j] = shapeAt(j, k + 1);
                    }
                }
            }
        }
        if (numFullLines > 0) {
            score += numFullLines * 100;
            isFallingFinished = true;
            currentPiece.setShape(Shape.Tetrominoes.NoShape);
            repaint();
        }

        if (score >= nextThreshold) {
            if (DELAY > 100) {
                DELAY -= 100;
                timer.setDelay(DELAY); // aplica la nueva velocidad
            }
            nextThreshold += 500; // prepara el siguiente salto de dificultad

            // Mostrar mensaje de nivel
            JOptionPane.showMessageDialog(this, "¡Bienvenido al siguiente nivel!", "Nivel aumentado", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void dropDown() {
        int newY = currentY;
        while (newY > 0) {
            if (!tryMove(currentPiece, currentX, newY - 1)) {
                break;
            }
            newY--;
            try {
                Thread.sleep(50); // Espera de 50 milisegundos entre movimientos
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        pieceDropped();
    }

    public void moveLeft() {
        if (canMove()) {
            tryMove(currentPiece, currentX - 1, currentY);
        }
    }

    public void moveRight() {
        if (canMove()) {
            tryMove(currentPiece, currentX + 1, currentY);
        }
    }

    public void moveRasRight() {
        int newX = currentX;
        int steps = 3;  // Número de casillas a mover a la vez (puedes ajustar este valor)

        for (int i = 0; i < steps; i++) {
            if (newX < BOARD_WIDTH - 1 && tryMove(currentPiece, newX + 1, currentY)) {
                newX++;  // Mueve la pieza una casilla a la derecha
            } else {
                break;  // Si no se puede mover más a la derecha, salimos del bucle
            }

            try {
                Thread.sleep(50);  // Retardo para que el movimiento sea visible
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Asegúrate de que el ciclo de caída continúe, pero NO llames a pieceDropped() aquí
        // Si necesitas asegurarte de que la pieza siga cayendo, invoca el ciclo de caída aquí de alguna manera, o
        // solo espera la siguiente iteración en el juego
        if (!tryMove(currentPiece, currentX, currentY - 1)) {
            // Si no puede caer más, la pieza ha tocado el fondo
            pieceDropped();  // Solo se debe llamar si realmente ha tocado el fondo
        }
    }

    public void moveRasLeft() {
        int newX = currentX;
        int steps = 3;  // Número de casillas a mover a la vez (puedes ajustar este valor)

        for (int i = 0; i < steps; i++) {
            if (newX > 0 && tryMove(currentPiece, newX - 1, currentY)) {
                newX--;  // Mueve la pieza una casilla a la izquierda
            } else {
                break;  // Si no se puede mover más a la izquierda, salimos del bucle
            }

            try {
                Thread.sleep(50);  // Retardo para que el movimiento sea visible
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Asegúrate de que el ciclo de caída continúe, pero NO llames a pieceDropped() aquí
        // Si necesitas asegurarte de que la pieza siga cayendo, invoca el ciclo de caída aquí de alguna manera, o
        // solo espera la siguiente iteración en el juego
        if (!tryMove(currentPiece, currentX, currentY - 1)) {
            // Si no puede caer más, la pieza ha tocado el fondo
            pieceDropped();  // Solo se debe llamar si realmente ha tocado el fondo
        }
    }

    public void moveUp() {
        if (canMove()) {
            // Si la pieza está bajando, detenemos su caída temporalmente.
            if (currentY < BOARD_HEIGHT - 1) {
                tryMove(currentPiece, currentX, currentY + 1);  // Movemos hacia arriba.
            }
        }
    }

    public void moveDown() {
        if (canMove()) {
            // Si la pieza es Dimensional y ha llegado el momento de cambiar de forma
            if (isDimensionalPiece && dimensionalShapeTimer % DIMENSIONAL_CHANGE_INTERVAL == 0) {
                currentPiece.setDimensionalShape(); // Cambia la forma de la pieza Dimensional
            }

            // Mueve la pieza una línea hacia abajo
            oneLineDown();

            // Incrementa el contador de tiempo para el cambio de forma
            dimensionalShapeTimer++;
        }
    }

    public void rotate() {
        if (canMove()) {
            tryMove(currentPiece.rotateRight(), currentX, currentY);
        }
    }

    public void hardDrop() {
        if (canMove()) {
            dropDown();
        }
    }

    private boolean canMove() {
        return isStarted && !isPaused && currentPiece.getShape() != Tetrominoes.NoShape;
    }

    public void pause() {
        togglePause(); // Ahora solo llama a togglePause para mantener compatibilidad
    }

    public void jumpUp() {
        if (canMove() && !isJumping) {
            isJumping = true; // Evita que se inicie un nuevo salto antes de que el anterior termine

            // Usamos un Timer para hacer el salto de manera animada
            Timer timer = new Timer(100, e -> { // Intervalo de 100ms
                if (currentY < BOARD_HEIGHT - 1) { // Evita que la pieza se salga del tablero por el límite superior
                    // Subimos 3 bloques por salto
                    if (tryMove(currentPiece, currentX, currentY + 3)) {
                        repaint(); // Actualiza la interfaz para mostrar el salto
                    } else {
                        ((Timer) e.getSource()).stop(); // Detenemos el timer si la pieza no puede moverse más
                        isJumping = false; // Reiniciamos la variable de salto para permitir otro salto
                    }
                } else {
                    ((Timer) e.getSource()).stop(); // Detenemos el timer cuando la pieza no pueda saltar más
                    isJumping = false; // Reiniciamos la variable de salto
                }
            });
            timer.setRepeats(true); // Hace que el Timer repita la acción
            timer.start(); // Inicia la animación
        }
    }

    class TAdapter extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {
            if (!isStarted || currentPiece.getShape() == Shape.Tetrominoes.NoShape) {
                return;
            }

            int keycode = e.getKeyCode();
            switch (keycode) {
                case KeyEvent.VK_LEFT:
                    tryMove(currentPiece, currentX - 1, currentY);
                    break;
                case KeyEvent.VK_RIGHT:
                    tryMove(currentPiece, currentX + 1, currentY);
                    break;
                case KeyEvent.VK_DOWN:
                    tryMove(currentPiece, currentX, currentY - 1);
                    break;
                case KeyEvent.VK_UP:
                    tryMove(currentPiece.rotateRight(), currentX, currentY);
                    break;
                case KeyEvent.VK_SPACE:
                    dropDown();
                    break;
                case KeyEvent.VK_P:
                    pause();
                    break;
            }
        }
    }

}
