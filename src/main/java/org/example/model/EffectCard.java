package org.example.model;

public class EffectCard extends Card {

    public EffectCard(Value value, Color color) {
        super(value, color);
    }

    @Override
    public boolean hasEffect() {
        return true;
    }

}
