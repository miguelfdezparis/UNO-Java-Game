package org.example.view;

import org.example.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

// todo lo visual esta aqui: menus, cartas, mensajes y lectura de consola
// no hay logica del juego, solo System.out y Scanner
public class ConsoleView {

    // codigos ANSI para colores en la terminal
    private static final String RESET   = "\u001B[0m";
    private static final String BOLD    = "\u001B[1m";
    private static final String RED     = "\u001B[91m";
    private static final String GREEN   = "\u001B[92m";
    private static final String YELLOW  = "\u001B[93m";
    private static final String BLUE    = "\u001B[94m";
    private static final String MAGENTA = "\u001B[95m";
    private static final String CYAN    = "\u001B[96m";
    private static final String WHITE   = "\u001B[97m";
    private static final String GRAY    = "\u001B[90m";

    private final Scanner scanner;

    public ConsoleView() {
        scanner = new Scanner(System.in);
    }

    // muestra el menu principal con el titulo del juego
    public void showMenu() {
        clearScreen();
        System.out.println();
        System.out.println(BOLD + RED + "  =====================================" + RESET);
        System.out.println(BOLD + RED + "         J U E G O   U N O            " + RESET);
        System.out.println(BOLD + RED + "  =====================================" + RESET);
        System.out.println();
        System.out.println("   " + CYAN + "1." + RESET + "  Nueva Partida");
        System.out.println("   " + CYAN + "2." + RESET + "  Como se juega");
        System.out.println("   " + CYAN + "3." + RESET + "  Puntuaciones");
        System.out.println("   " + CYAN + "4." + RESET + "  Salir");
        System.out.println();
        System.out.print(CYAN + "   > " + RESET);
        System.out.flush();
    }

    public int getMenuChoice() {
        return readInt(1, 4);
    }

    // pregunta cuantos jugadores van a jugar
    public int askPlayerCount() {
        System.out.println();
        System.out.print(BOLD + WHITE + "   Cuantos jugadores? " + GRAY + "(2-4)" + RESET + "  > ");
        System.out.flush();
        return readInt(2, 4);
    }

    // pregunta el nombre del jugador numero 'number'
    public String askPlayerName(int number) {
        System.out.print("   Nombre del jugador " + number + ": ");
        System.out.flush();
        String name = scanner.nextLine().trim();
        return name.isEmpty() ? "Jugador" + number : name;
    }

    // pregunta si el jugador con ese nombre es controlado por el ordenador
    // solo acepta s/si/n/no, si ponen otra cosa repite la pregunta
    public boolean askIsComputer(String name) {
        while (true) {
            System.out.print("   Es " + BOLD + name + RESET + " un jugador Computer? " + GRAY + "(s/n)" + RESET + "  > ");
            System.out.flush();
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("s") || input.equals("si")) return true;
            if (input.equals("n") || input.equals("no")) return false;
            showError("Escribe 's' para si o 'n' para no.");
        }
    }

    // muestra el estado actual de la partida: turno, carta superior y jugadores
    public void showGameState(GameState state) {
        clearScreen();
        System.out.println();
        System.out.println(BOLD + GRAY + "  ================================================" + RESET);
        System.out.println(BOLD + WHITE + "  Turno " + state.getTurnCount()
                + "   Sentido: " + (state.isClockwise() ? "->" : "<-")
                + "   Mazo: " + state.getDeck().size() + " cartas" + RESET);
        System.out.println(BOLD + GRAY + "  ================================================" + RESET);

        System.out.println();
        System.out.println(BOLD + WHITE + "  Carta actual:" + RESET);
        System.out.println("  " + renderCardBig(state.getTopCard(), state.getCurrentColor()));
        System.out.println();

        System.out.println(BOLD + WHITE + "  Jugadores:" + RESET);
        for (Player p : state.getPlayers()) {
            boolean isCurrent = p == state.getCurrentPlayer();
            String arrow = isCurrent ? CYAN + BOLD + " >> " + RESET : "    ";
            String tag   = p.isComputer() ? GRAY + " [Computer]" + RESET : "";
            System.out.println(arrow + BOLD + p + RESET + tag
                    + GRAY + "   " + p.getHand().size() + " carta(s)" + RESET);
        }
        System.out.println();
    }

    // muestra la mano del jugador con un numero delante de cada carta para elegir
    public void showHand(Player player) {
        System.out.println();
        System.out.println(BOLD + WHITE + "  Mano de " + player + ":" + RESET);
        System.out.println();

        ArrayList<Card> cards = player.getHand().getCards();
        for (int i = 0; i < cards.size(); i++) {
            System.out.println("   " + BOLD + CYAN + "[" + i + "]" + RESET + "  " + renderCard(cards.get(i)));
        }
        System.out.println();
    }

    // devuelve el texto de una carta con color ANSI y emoji, con nombres en español
    // los comodines no muestran nombre de color porque no tienen uno fijo
    public String renderCard(Card card) {
        String ansi  = ansiForColor(card.getColor());
        String emoji = emojiForColor(card.getColor());
        String value = labelForValue(card.getValue());
        if (card.getColor() == Color.BLACK) {
            return ansi + BOLD + emoji + " " + value + RESET;
        }
        String color = spanishColor(card.getColor());
        return ansi + BOLD + emoji + " " + color + " " + value + RESET;
    }

    // version mas destacada de la carta para mostrarla como carta actual
    private String renderCardBig(Card card, Color currentColor) {
        String ansi  = ansiForColor(card.getColor());
        String emoji = emojiForColor(card.getColor());
        String color = spanishColor(card.getColor());
        String value = labelForValue(card.getValue());

        if (card.getColor() == Color.BLACK) {
            // comodin: mostramos el tipo y el color que esta activo
            return ansi + BOLD + emoji + "  " + value + "  ->  " + spanishColor(currentColor) + RESET;
        }
        return ansi + BOLD + emoji + "  " + color + "   [ " + value + " ]" + RESET;
    }

    // pregunta que carta quiere jugar, devuelve el indice o -1 si elige robar
    public int getCardChoice(Player player, GameState state) {
        System.out.println(BOLD + WHITE + "  Tu turno, " + player + "!" + RESET);
        System.out.println(GRAY + "  Escribe el numero de una carta para jugarla, o 'r' para robar." + RESET);

        while (true) {
            System.out.print(CYAN + "  > " + RESET);
            System.out.flush();
            String input = scanner.nextLine().trim().toLowerCase();

            if (input.equals("r")) return -1;

            try {
                int index = Integer.parseInt(input);
                if (index < 0 || index >= player.getHand().size()) {
                    showError("No hay ninguna carta en esa posicion. Prueba otra vez.");
                    continue;
                }
                Card chosen = player.getHand().getCard(index);
                if (!chosen.isCompatible(state.getTopCard(), state.getCurrentColor())) {
                    showError("Esa carta no encaja, no coincide el color ni el numero. Elige otra o roba.");
                    continue;
                }
                return index;
            } catch (NumberFormatException e) {
                showError("Escribe un numero (0, 1, 2...) o 'r' para robar.");
            }
        }
    }

    // si la carta robada es jugable, pregunta si quiere jugarla ahora mismo
    public boolean askPlayDrawnCard(Card card) {
        System.out.println("  Robaste: " + renderCard(card));
        System.out.print("  Es jugable, la juegas ahora? " + GRAY + "(s/n)" + RESET + "  > ");
        System.out.flush();
        String input = scanner.nextLine().trim().toLowerCase();
        return input.equals("s") || input.equals("si");
    }

    // muestra el menu para elegir color despues de jugar un comodin
    public Color chooseColor() {
        System.out.println();
        System.out.println(BOLD + WHITE + "  Elige un color:" + RESET);
        System.out.println("   " + RED    + BOLD + "1. ROJO"     + RESET);
        System.out.println("   " + BLUE   + BOLD + "2. AZUL"     + RESET);
        System.out.println("   " + GREEN  + BOLD + "3. VERDE"    + RESET);
        System.out.println("   " + YELLOW + BOLD + "4. AMARILLO" + RESET);
        System.out.print(CYAN + "  > " + RESET);
        System.out.flush();

        return switch (readInt(1, 4)) {
            case 1 -> Color.RED;
            case 2 -> Color.BLUE;
            case 3 -> Color.GREEN;
            case 4 -> Color.YELLOW;
            default -> Color.RED;
        };
    }

    public void showMessage(String msg) {
        System.out.println("  " + WHITE + msg + RESET);
    }

    public void showError(String msg) {
        System.out.println("  " + RED + "(!) " + msg + RESET);
    }

    // avisa cuando al jugador le queda solo una carta
    public void showUnoWarning(Player player) {
        System.out.println();
        System.out.println(BOLD + YELLOW + "  *** UNO! --- " + player.getName() + " solo le queda 1 carta! ***" + RESET);
    }

    // muestra el mensaje de turno del computer con una pequeña pausa para que se vea
    public void showComputerTurn(Player computer) {
        System.out.println();
        System.out.println(GRAY + "  [Computer]  " + computer.getName() + " esta pensando..." + RESET);
        try { Thread.sleep(900); } catch (InterruptedException ignored) {}
    }

    // muestra la pantalla de victoria con el nombre del ganador
    public void showWinner(Player player) {
        System.out.println();
        System.out.println(BOLD + YELLOW + "  =====================================" + RESET);
        System.out.println(BOLD + YELLOW + "   *** UNO!  --  FIN DEL JUEGO ***    " + RESET);
        System.out.println(BOLD + WHITE  + "         " + player.getName() + " GANA!         " + RESET);
        System.out.println(BOLD + YELLOW + "  =====================================" + RESET);
        System.out.println();
    }

    // pequeño resumen al final de la partida
    public void showGameSummary(String winnerName, int turns) {
        System.out.println(GRAY + "  Partida terminada en " + turns + " turno(s)." + RESET);
        System.out.println(GRAY + "  Puntuacion guardada." + RESET);
        System.out.println();
    }

    // pantalla con las reglas del juego
    public void showRules() {
        clearScreen();
        System.out.println();
        System.out.println(BOLD + CYAN + "  ============ COMO SE JUEGA ============" + RESET);
        System.out.println();
        System.out.println("  Tienes que jugar una carta que coincida con la de arriba");
        System.out.println("  por COLOR o por NUMERO. El primero en quedarse sin cartas gana!");
        System.out.println();
        System.out.println(BOLD + WHITE + "  Cartas especiales:" + RESET);
        System.out.println();
        System.out.println("   " + YELLOW + BOLD + "SALTA"   + RESET + "      el siguiente jugador pierde el turno");
        System.out.println("   " + YELLOW + BOLD + "REVERSO" + RESET + "   el sentido de la partida se invierte");
        System.out.println("   " + YELLOW + BOLD + "CHUPA 2" + RESET + "      el siguiente roba 2 y pierde el turno");
        System.out.println("   " + MAGENTA + BOLD + "CAMBIO DE COLOR" + RESET + "  se puede jugar siempre, eliges el nuevo color");
        System.out.println("   " + MAGENTA + BOLD + "CHUPA 4"         + RESET + "         eliges color y el siguiente roba 4 y pierde el turno");
        System.out.println();
        System.out.println(BOLD + WHITE + "  Tu turno:" + RESET);
        System.out.println("  - Escribe el numero de la carta para jugarla");
        System.out.println("  - Escribe 'r' para robar del mazo");
        System.out.println("  - Si robas una carta jugable puedes jugarla en el momento");
        System.out.println();
        System.out.println("  El juego avisa solo cuando te queda 1 carta (UNO!).");
        System.out.println();
        pressEnter();
    }

    // muestra las puntuaciones ordenadas de mayor a menor victorias
    public void showHighScores(HashMap<String, Integer> scores) {
        clearScreen();
        System.out.println();
        System.out.println(BOLD + CYAN + "  ============ PUNTUACIONES ============" + RESET);
        System.out.println();

        if (scores.isEmpty()) {
            System.out.println(GRAY + "  Todavia no hay puntuaciones, juega una partida primero!" + RESET);
        } else {
            // pasamos las entradas del HashMap a una lista para poder ordenarlas
            ArrayList<Map.Entry<String, Integer>> lista = new ArrayList<>(scores.entrySet());
            lista.sort((a, b) -> b.getValue() - a.getValue());
            for (Map.Entry<String, Integer> entry : lista) {
                System.out.printf("   " + BOLD + WHITE + "%-20s" + RESET + YELLOW + "%3d victoria(s)%n" + RESET,
                        entry.getKey(), entry.getValue());
            }
        }

        System.out.println();
        pressEnter();
    }

    // espera a que el usuario pulse enter para continuar
    // leemos directo de System.in para evitar que el scanner consuma un salto de linea
    // que haya quedado en el buffer de una lectura anterior
    public void pressEnter() {
        System.out.print(GRAY + "  Pulsa Enter para continuar..." + RESET);
        System.out.flush();
        try {
            int c;
            do {
                c = System.in.read();
            } while (c != '\n' && c != -1);
        } catch (Exception ignored) {}
    }

    // limpia la pantalla del terminal
    private void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    // lee un numero entre min y max, repite si el input no es valido
    private int readInt(int min, int max) {
        while (true) {
            try {
                int val = Integer.parseInt(scanner.nextLine().trim());
                if (val >= min && val <= max) return val;
                System.out.print("  Escribe un numero entre " + min + " y " + max + ": ");
                System.out.flush();
            } catch (NumberFormatException e) {
                System.out.print("  Eso no es un numero, prueba otra vez: ");
                System.out.flush();
            }
        }
    }

    // devuelve el codigo ANSI correspondiente al color de la carta
    private String ansiForColor(Color color) {
        return switch (color) {
            case RED    -> RED;
            case BLUE   -> BLUE;
            case GREEN  -> GREEN;
            case YELLOW -> YELLOW;
            case BLACK  -> MAGENTA;
        };
    }

    // devuelve el emoji que representa cada color de carta
    private String emojiForColor(Color color) {
        return switch (color) {
            case RED    -> "🔴";
            case BLUE   -> "🔵";
            case GREEN  -> "🟢";
            case YELLOW -> "🟡";
            case BLACK  -> "⚫";
        };
    }

    // devuelve el nombre del color en español
    private String spanishColor(Color color) {
        return switch (color) {
            case RED    -> "ROJO";
            case BLUE   -> "AZUL";
            case GREEN  -> "VERDE";
            case YELLOW -> "AMARILLO";
            case BLACK  -> "COMODIN";
        };
    }

    // devuelve el texto corto que se muestra en la carta (numero o nombre en español)
    private String labelForValue(Value value) {
        return switch (value) {
            case ZERO  -> "0"; case ONE   -> "1"; case TWO   -> "2";
            case THREE -> "3"; case FOUR  -> "4"; case FIVE  -> "5";
            case SIX   -> "6"; case SEVEN -> "7"; case EIGHT -> "8";
            case NINE  -> "9";
            case SKIP           -> "SALTA";
            case REVERSE        -> "REVERSO";
            case DRAW_TWO       -> "CHUPA 2";
            case WILD           -> "CAMBIO DE COLOR";
            case WILD_DRAW_FOUR -> "CHUPA 4";
        };
    }
}
