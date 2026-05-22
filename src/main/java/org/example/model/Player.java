package org.example.model;

// representa a un jugador: tiene nombre, su mano de cartas y si es computer o humano
public class Player {

    private String name;
    private Hand hand;
    private boolean computer;

    public Player(String name, boolean computer) {
        this.name = name;
        this.hand = new Hand();
        this.computer = computer;
    }

    public String getName() {
        return name;
    }

    public Hand getHand() {
        return hand;
    }

    // devuelve true si este jugador lo controla el ordenador
    public boolean isComputer() {
        return computer;
    }

    @Override
    public String toString() {
        return name;
    }
}
