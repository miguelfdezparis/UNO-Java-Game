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
}
