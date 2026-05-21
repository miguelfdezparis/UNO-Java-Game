package org.example.db;

import org.example.model.Carta;
import java.util.ArrayList;

// interfaz que tienen que implementar los dos dao, postgres y mongo
public interface BarajaDAO {
    ArrayList<Carta> obtenerBaraja();
    void guardarBaraja(ArrayList<Carta> cartas);
}
