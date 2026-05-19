package org.example.model;

public class ColorCard extends Card {

    public ColorCard(Value value, Color color) {
        super(value, color);
    }

    @Override
    public boolean hasEffect() {
        return false;
    }

}
