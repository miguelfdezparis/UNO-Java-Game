package org.example.model;

public class SpecialCard extends Card {

    public SpecialCard(Value value, Color color) {
        super(value, color);
    }

    @Override
    public boolean hasEffect() {
        return true;
    }

}
