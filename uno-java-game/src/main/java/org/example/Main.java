package org.example;

import org.example.controller.GameController;

public class Main {

    // aqui solo arrancamos el juego, nada mas va en el main
    public static void main(String[] args) {
        new GameController().start();
    }
}
