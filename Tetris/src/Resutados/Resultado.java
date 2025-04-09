/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Resutados;

import java.io.Serializable;

/**
 *
 * @author Kent
 */
public class Resultado implements Serializable {

    private String nombreJugador;
    private int puntuacion;

    public Resultado(String nombreJugador, int puntuacion) {
        this.nombreJugador = nombreJugador;
        this.puntuacion = puntuacion;
    }

    public String getNombreJugador() {
        return nombreJugador;
    }

    public int getPuntuacion() {
        return puntuacion;
    }

    @Override
    public String toString() {
        return nombreJugador + " - " + puntuacion;
    }
}
