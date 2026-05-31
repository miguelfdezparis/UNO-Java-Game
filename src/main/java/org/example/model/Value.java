package org.example.model;

// todos los valores posibles de una carta UNO
public enum Value {

    ZERO("0"), ONE("1"), TWO("2"), THREE("3"), FOUR("4"),
    FIVE("5"), SIX("6"), SEVEN("7"), EIGHT("8"), NINE("9"),

    SKIP("Salta"),
    REVERSE("Reverso"),
    DRAW_TWO("Chupa 2"),

    WILD("Cambio de Color"),
    WILD_DRAW_FOUR("Chupa 4");

    private final String label;

    Value(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
