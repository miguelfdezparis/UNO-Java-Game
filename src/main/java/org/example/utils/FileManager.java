package org.example.utils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

// se encarga de leer y guardar las puntuaciones en un fichero CSV
// el fichero se crea solo si no existia todavia
public class FileManager {

    private static final String SCORES_FILE = "scores.csv";

    // carga las puntuaciones del fichero CSV y las devuelve en un HashMap
    // si el fichero no existe todavia devuelve un mapa vacio sin error
    public HashMap<String, Integer> loadScores() {
        HashMap<String, Integer> scores = new HashMap<>();
        File file = new File(SCORES_FILE);

        if (!file.exists()) return scores;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.readLine(); // saltamos la primera linea que es la cabecera "name,wins"

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    String name = parts[0].trim();
                    int wins    = Integer.parseInt(parts[1].trim());
                    scores.put(name, wins);
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("  Aviso: no se pudo leer el fichero de puntuaciones - " + e.getMessage());
        }

        return scores;
    }

    // guarda todas las puntuaciones en el fichero, sobreescribiendo lo que habia antes
    // se llama al final de cada partida
    public void saveScores(HashMap<String, Integer> scores) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(SCORES_FILE))) {
            writer.println("name,wins"); // cabecera del CSV
            for (Map.Entry<String, Integer> entry : scores.entrySet()) {
                writer.println(entry.getKey() + "," + entry.getValue());
            }
        } catch (IOException e) {
            System.err.println("  Aviso: no se pudo guardar el fichero de puntuaciones - " + e.getMessage());
        }
    }
}
