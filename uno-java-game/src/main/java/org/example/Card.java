package org.example;

public class Card {
    private Color color;
    private Value value;

    public Card(Value value, Color color) {
        this.value = value;
        this.color = color;

    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }
}
