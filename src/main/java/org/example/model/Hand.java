package org.example.model;

import java.util.ArrayList;

// la mano del jugador, simplemente la lista de cartas que tiene ahora mismo
public class Hand {

    private ArrayList<Card> cards;

    public Hand() {
        cards = new ArrayList<>();
    }

    // añade una carta a la mano, esto pasa cuando el jugador roba del mazo
    public void addCard(Card card) {
        cards.add(card);
    }

    // juega la carta que esta en esa posicion, la elimina de la mano y la devuelve
    public Card playCard(int index) {
        return cards.remove(index);
    }

    // mira si hay alguna carta jugable en la mano con la situacion actual
    public boolean hasPlayableCard(Card topCard, Color currentColor) {
        for (Card card : cards) {
            if (card.isCompatible(topCard, currentColor)) return true;
        }
        return false;
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public int size() {
        return cards.size();
    }

    // devuelve la carta en esa posicion sin quitarla de la mano
    public Card getCard(int index) {
        return cards.get(index);
    }

    // devuelve toda la lista de cartas, lo usa la vista para pintarlas
    public ArrayList<Card> getCards() {
        return cards;
    }
}
