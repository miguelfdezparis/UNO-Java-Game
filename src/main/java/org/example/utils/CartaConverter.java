package org.example.utils;

import org.example.model.*;
import org.example.model.Carta;

import java.util.ArrayList;

// convierte los registros de la BD (Carta) en cartas reales del juego (Card)
// cada Carta tiene apariciones, asi que genera esa cantidad de copias por fila
public class CartaConverter {

    public static ArrayList<Card> toCards(ArrayList<Carta> cartas) {
        ArrayList<Card> result = new ArrayList<>();
        for (Carta c : cartas) {
            Value value = Value.valueOf(c.getValor());
            Color color = Color.valueOf(c.getColor());
            for (int i = 0; i < c.getApariciones(); i++) {
                result.add(switch (c.getTipo()) {
                    case "ColorCard"  -> new ColorCard(value, color);
                    case "EffectCard" -> new EffectCard(value, color);
                    default           -> new WildCard(value, color);
                });
            }
        }
        return result;
    }
}
