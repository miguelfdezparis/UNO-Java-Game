package org.example.utils;

import org.example.model.Carta;
import java.util.ArrayList;

// genera las 54 filas unicas del catalogo (108 cartas en total sumando apariciones)
public class BarajaCatalog {

    public static ArrayList<Carta> generar() {
        ArrayList<Carta> cartas = new ArrayList<>();
        String[] colores = {"RED", "BLUE", "GREEN", "YELLOW"};

        for (String color : colores) {
            cartas.add(new Carta("ColorCard", "ZERO", color, 1));
            for (String v : new String[]{"ONE","TWO","THREE","FOUR","FIVE","SIX","SEVEN","EIGHT","NINE"}) {
                cartas.add(new Carta("ColorCard", v, color, 2));
            }
            cartas.add(new Carta("EffectCard", "SKIP",      color, 2));
            cartas.add(new Carta("EffectCard", "REVERSE",   color, 2));
            cartas.add(new Carta("EffectCard", "DRAW_TWO",  color, 2));
        }

        cartas.add(new Carta("WildCard", "WILD",           "BLACK", 4));
        cartas.add(new Carta("WildCard", "WILD_DRAW_FOUR", "BLACK", 4));

        return cartas;
    }
}
