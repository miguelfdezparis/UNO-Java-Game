package org.example.model;

public class WildCard extends Card {

    public WildCard(Value value, Color color) {
        super(value, color);
    }

    @Override
    public boolean hasEffect() {
        return true;
    }

}
