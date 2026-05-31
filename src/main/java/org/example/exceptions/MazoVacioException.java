package org.example.exceptions;

public class MazoVacioException extends RuntimeException {

    public MazoVacioException() {
        super("El mazo esta vacio y no hay cartas en el descarte para reciclar.");
    }
}
