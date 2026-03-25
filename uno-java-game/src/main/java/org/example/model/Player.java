package org.example.model;

/**
 * Clase Player: representa a un jugador del UNO.
 *
 * Cada jugador tiene un nombre y una mano con sus cartas
 * Esta clase agrupa un nombre con una mano de cartas.
 * Cada jugador tiene una mano. Si el jugador desaparece, su mano también.
 */
public class Player {

    private String name;  // Nombre del jugador
    private Hand hand;    // Mano de cartas del jugador

    /**
     * Constructor: crea un jugador con un nombre y una mano vacía.
     */
    public Player(String name) {
        this.name = name;
        this.hand = new Hand(); // Cada jugador empieza con la mano vacía
    }

    /**
     * Getter (nombre del jugador).
     */
    public String getName() {
        return name;
    }

    /**
     * Getter (mano del jugador).
     * Devolvemos la referencia a Hand para poder: (añadir cartas, jugar cartas, etc.)
     */
    public Hand getHand() {
        return hand;
    }
}