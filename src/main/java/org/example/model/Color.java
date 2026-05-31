package org.example.model;

// los cuatro colores del juego, mas BLACK que solo usan las cartas comodin
public enum Color {
    RED("Rojo"),
    BLUE("Azul"),
    GREEN("Verde"),
    YELLOW("Amarillo"),
    BLACK("Comodín");

    private final String label;

    Color(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
