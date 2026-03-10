package org.example;

public class Card {

    private Value value; //Atributo privado (solo accesible desde esta clase)
    private Color color; //Atributo privado (solo accesible desde esta clase)

    public Card(Value value, Color color) { //Contructor
        this.value = value;
        this.color = color;

    }

    public Color getColor() {
        return color;
    }
    public Value getValue() {
        return value;
    }
    public boolean isCompatible(Card topCard, Color currentColor) {
        if (this.color == Color.BLACK) { // Condición 1: ¿Es un comodín? → siempre se puede jugar
            return true;
        }
        if (this.color == currentColor) { // Condición 2: ¿Coincide el color con el color activo?
            return true;
        }
        if (this.value == topCard.getValue()) { // Condición 3: ¿Coincide el valor con la carta de encima?
            return true;
        }
        return false; // Si no se cumple ninguna condición, no se puede jugar
    }
    @Override
    public String toString() {
        return "[" + color + " " + value + "]";
    }
}