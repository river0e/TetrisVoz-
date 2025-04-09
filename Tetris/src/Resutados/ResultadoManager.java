/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Resutados;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author Kent
 */
public class ResultadoManager {

    // Método modificado para permitir sobrescribir el archivo o agregar resultados.
    public static void guardarResultado(Resultado resultado, String rutaArchivo) {
        try {
            // Leer los resultados actuales y almacenarlos en una lista
            List<Resultado> resultadosExistentes = leerResultados(rutaArchivo);

            // Comprobar si el jugador ya existe en el archivo
            for (Resultado r : resultadosExistentes) {
                if (r.getNombreJugador().equals(resultado.getNombreJugador())) {
                    return;  // Si el jugador ya está en el archivo, no se agrega de nuevo
                }
            }

            // Si no hay duplicado, se escribe el nuevo resultado en el archivo
            try (PrintWriter out = new PrintWriter(new FileWriter(rutaArchivo, true))) {
                out.println(resultado.getNombreJugador() + "," + resultado.getPuntuacion());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Resultado> leerResultados(String rutaArchivo) {
        List<Resultado> resultados = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(rutaArchivo))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                String[] partes = linea.split(",");
                if (partes.length == 2) {
                    String nombre = partes[0].trim();
                    int puntuacion = Integer.parseInt(partes[1].trim());
                    resultados.add(new Resultado(nombre, puntuacion));
                }
            }

            // Ordenar de mayor a menor puntuación
            resultados.sort(Comparator.comparingInt(Resultado::getPuntuacion).reversed());

            // Limitar a top 10
            if (resultados.size() > 10) {
                resultados = resultados.subList(0, 10);
            }

        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
        return resultados;
    }
}
