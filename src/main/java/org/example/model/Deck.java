package org.example.model;

import org.example.exceptions.MazoVacioException;
import java.util.ArrayList;
import java.util.Collections;

// el monton de robar, crea las 108 cartas del UNO al construirse y las baraja
public class Deck {

    private ArrayList<Card> cards;

    public Deck() {
        cards = new ArrayList<>();
        buildDeck();
        shuffle();
    }

    public Deck(ArrayList<Card> cardsFromDB) {
        this.cards = cardsFromDB;
        shuffle();
    }

    // crea el mazo completo: 4 colores con sus cartas normales y especiales, mas las comodin
    private void buildDeck() {
        for (Color color : Color.values()) {
            if (color == Color.BLACK) continue; // las comodin se añaden al final aparte

            cards.add(new ColorCard(Value.ZERO, color)); // un solo 0 por color

            for (Value value : Value.values()) {
                if (value == Value.ZERO
                        || value == Value.WILD
                        || value == Value.WILD_DRAW_FOUR
                        || value == Value.DRAW_TWO
                        || value == Value.REVERSE
                        || value == Value.SKIP) continue;
                // dos de cada carta para cada color (1-9)
                cards.add(new ColorCard(value, color));
                cards.add(new ColorCard(value, color));
            }
        }

        //recorre enum de colores
        for (Color color : Color.values()) {
            if (color == Color.BLACK) continue;

            // para cada color añade 2 especiales de cada tipo (cambio de turno, +2, prohibido)
            cards.add(new EffectCard(Value.REVERSE, color));
            cards.add(new EffectCard(Value.REVERSE, color));
            cards.add(new EffectCard(Value.DRAW_TWO, color));
            cards.add(new EffectCard(Value.DRAW_TWO, color));
            cards.add(new EffectCard(Value.SKIP, color));
            cards.add(new EffectCard(Value.SKIP, color));
        }

        // 4 comodin y 4 comodin+4
        for (int i = 0; i < 4; i++) {
            cards.add(new WildCard(Value.WILD, Color.BLACK));
            cards.add(new WildCard(Value.WILD_DRAW_FOUR, Color.BLACK));
        }
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    // saca la carta de arriba del mazo, lanza excepcion si esta vacio
    public Card draw() {
        if (cards.isEmpty()) {
            throw new MazoVacioException();
        }
        return cards.remove(0);
    }

    public int size() {
        return cards.size();
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    // mete las cartas recicladas del descarte de vuelta al mazo y vuelve a barajar
    public void addCards(ArrayList<Card> recycled) {
        cards.addAll(recycled);
        shuffle();
    }
}
