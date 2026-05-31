package org.example.exceptions;

public class CartaNoJugableException extends Exception {

    public CartaNoJugableException(String cartaJugada, String cartaActual) {
        super("La carta " + cartaJugada + " no se puede jugar sobre " + cartaActual + ": no coincide el color ni el valor.");
    }
}
