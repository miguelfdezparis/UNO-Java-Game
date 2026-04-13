package org.example.model;

// una carta tiene un valor y un color
// las cartas comodin siempre usan Color.BLACK como color
public class Card {

    private Value value;
    private Color color;

    public Card(Value value, Color color) {
        this.value = value;
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public Value getValue() {
        return value;
    }

    // comprueba si esta carta se puede jugar encima de la que hay en la pila
    // las comodin van siempre, si no tiene que coincidir el color activo o el valor
    public boolean isCompatible(Card topCard, Color currentColor) {
        if (this.color == Color.BLACK) return true;         // las comodin van siempre
        if (this.color == currentColor) return true;        // mismo color activo
        if (this.value == topCard.getValue()) return true;  // mismo numero o tipo
        return false;
    }

    @Override
    public String toString() {
        return "[" + color + " " + value + "]";
    }
}
