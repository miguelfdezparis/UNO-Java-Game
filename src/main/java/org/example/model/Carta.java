package org.example.model;

// para guardar cada tipo de carta en la bd con cuantas veces sale en la baraja
public class Carta {

    private String tipo;
    private String valor;
    private String color;
    private int apariciones;

    public Carta(String tipo, String valor, String color, int apariciones) {
        this.tipo = tipo;
        this.valor = valor;
        this.color = color;
        this.apariciones = apariciones;
    }

    public String getTipo()     { return tipo; }
    public String getValor()    { return valor; }
    public String getColor()    { return color; }
    public int getApariciones() { return apariciones; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Carta otra = (Carta) obj;
        return valor.equals(otra.valor) && color.equals(otra.color);
    }
    
}
