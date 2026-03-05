public class Color {
    public static final String[] COLORES = {"ROJO", "AZUL", "AMARILLO", "VERDE", "NEGRO"};

    private String color;

    public Color(String color) {
        this.color = color;
    }

    public String getColor() {
        return color;
    }

    @Override
    public String toString() {
        return color;
    }
}