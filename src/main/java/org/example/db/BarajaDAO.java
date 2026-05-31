package org.example.db;

import org.example.model.Carta;
import java.util.ArrayList;

// interfaz que tienen que implementar los dos dao, postgres y mongo
public interface BarajaDAO {
    void inicializar(ArrayList<Carta> catalogo);
    ArrayList<Carta> obtenerBaraja();
    void guardarBaraja(ArrayList<Carta> cartas);
    void actualizarApariciones(String valor, String color, int nuevas);
    void eliminarCarta(String valor, String color);
}
