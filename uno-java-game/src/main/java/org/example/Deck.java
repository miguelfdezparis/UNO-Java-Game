package org.example;
import java.util.ArrayList;
public class Deck {
    private ArrayList<Card> cards = new ArrayList<>();

    public Deck(ArrayList<Card> cards) {
        this.cards = cards;
        createDeck();
    }

    private void createDeck() {
        // Loop that iterates all colors
        for (Color color : Color.values()) {
            // Skip black (we'll work with them in the end)
            if (color == Color.BLACK) {
                continue;
            }
            // Adds 4 cards of value 0
            cards.add(new Card(Value.ZERO, color));
            // Loop that iterates all values
            for (Value value : Value.values()) {
                // Skips zero and wild values
                if (value == Value.ZERO || value == Value.WILD || value == Value.WILD_DRAW_FOUR) {
                    continue;
                }
                // Adds numbers from 1-9, skip, reverse, draw-two of each color twice
                cards.add(new Card(value, color));
                cards.add(new Card(value, color));
            }

        }

        // Loop that adds wild cards
        for (int i = 0; i < 4; i++) {
            cards.add(new Card(Value.WILD, Color.BLACK));
        }

        for (int i = 0; i < 4; i++) {
            cards.add(new Card(Value.WILD_DRAW_FOUR, Color.BLACK));
        }
    }

}