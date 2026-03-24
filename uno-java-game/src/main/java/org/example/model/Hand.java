package org.example.model;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase Hand: representa la mano de un jugador, es decir, las cartas que tiene.
 *
 * Cada jugador tiene su propia mano. Las cartas se guardan en una lista
 * porque así podemos acceder a ellas por posición (por ejemplo, cuando el
 * jugador elige jugar la carta número 3).
 */
public class Hand {

    // Lista de cartas en la mano del jugador
    private List<Card> cards;

    /**
     * Constructor: crea una mano vacía.
     * Al principio del juego la mano está vacía y luego se le reparten cartas.
     */
    public Hand() {
        cards = new ArrayList<>();
    }

    /**
     * Añade una carta a la mano.
     * Se usa cuando el jugador roba una carta del mazo.
     */
    public void addCard(Card card) {
        cards.add(card);
    }

    /**
     * Juega una carta de la mano según su posición.
     * Al usar remove(index), la carta se elimina de la lista y se devuelve.
     * Las demás cartas se recolocan automáticamente.
     * Ejemplo: si tengo [A, B, C, D] y uso playCard(1),
     * devuelve B y la mano queda [A, C, D].
     */
    public Card playCard(int index) {
        return cards.remove(index);
    }

    /**
     * Comprueba si el jugador tiene alguna carta que pueda jugar.
     * Sirve para saber si puede tirar carta o si tiene que robar.
     * Recorre la mano y, si encuentra una carta válida, devuelve true.
     */
    public boolean hasPlayableCard(Card topCard, Color currentColor) {
        for (Card card : cards) {
            if (card.isCompatible(topCard, currentColor)) {
                return true; // Con encontrar UNA carta jugable es suficiente
            }
        }
        return false; // Ninguna carta es jugable
    }

    /**
     * Comprueba si la mano está vacía (el jugador se quedó sin cartas → ¡ganó!).
     */
    public boolean isEmpty() {
        return cards.isEmpty();
    }

    /**
     * Devuelve el número de cartas en la mano.
     */
    public int size() {
        return cards.size();
    }

    /**
     * Devuelve la carta en una posición concreta SIN quitarla de la mano.
     * Se usa para comprobar si una carta es compatible antes de jugarla.
     */
    public Card getCard(int index) {
        return cards.get(index);
    }

    /**
     * Saca por consola todas las carts de la mano con su índice.
     * Ejemplo de salida:
     *   0: [RED FIVE]
     *   1: [BLUE SKIP]
     *   2: [BLACK WILD]
     * El índice es importante porque el jugador lo usa para elegir qué carta jugar.
     */
    public void show() {
        for (int i = 0; i < cards.size(); i++) {
            System.out.println("  " + i + ": " + cards.get(i));
        }
    }
}