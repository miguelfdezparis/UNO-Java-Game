package org.example.model;

public enum Value {
    // Cartas numéricas
    ZERO,
    ONE,
    TWO,
    THREE,
    FOUR,
    FIVE,
    SIX,
    SEVEN,
    EIGHT,
    NINE,

    // Cartas de acción (tienen color)
    SKIP,           // Saltar turno
    REVERSE,        // Invertir dirección
    DRAW_TWO,       // Robar 2 cartas

    // Comodines (sin color → BLACK)
    WILD,           // Cambiar color
    WILD_DRAW_FOUR  // Cambiar color + robar 4
}