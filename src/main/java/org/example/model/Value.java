package org.example.model;

// todos los valores posibles de una carta UNO
public enum Value {

    // cartas numericas
    ZERO, ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE,

    // cartas de accion, tienen color
    SKIP,       // el siguiente jugador pierde el turno
    REVERSE,    // cambia el sentido de la partida
    DRAW_TWO,   // el siguiente roba 2 y pierde el turno

    // cartas comodin, siempre son Color.BLACK
    WILD,           // elige el color que quieras
    WILD_DRAW_FOUR  // elige color y el siguiente roba 4
}
