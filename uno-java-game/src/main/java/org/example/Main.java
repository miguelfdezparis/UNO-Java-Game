package org.example;
import org.example.model.*;

import java.sql.SQLOutput;

public class Main {
    static void main() {
        System.out.println("Hola");
        Card carta = new Card(Value.ONE, Color.BLACK);
        System.out.println(carta);
    }
}