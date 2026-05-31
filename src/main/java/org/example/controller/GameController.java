package org.example.controller;

import org.example.db.BarajaDAO;
import org.example.db.MongoBarajaDAO;
import org.example.db.PostgresBarajaDAO;
import org.example.exceptions.CartaNoJugableException;
import org.example.exceptions.MazoVacioException;
import org.example.model.*;
import org.example.utils.BarajaCatalog;
import org.example.utils.CartaConverter;
import org.example.utils.FileManager;
import org.example.view.ConsoleView;

import java.util.ArrayList;
import java.util.HashMap;

// controla todo el flujo del juego: menu, turnos y efectos de las cartas
// los prints van siempre por ConsoleView y los datos estan en GameState
public class GameController {

    private ConsoleView view;
    private FileManager fileManager;
    private BarajaDAO barajaDAO;
    private HashMap<String, Integer> allTimeScores; // puntuaciones cargadas del fichero al arrancar

    public GameController() {
        view = new ConsoleView();
        fileManager = new FileManager();
        allTimeScores = fileManager.loadScores();
        int dbChoice = view.askDatabase();
        barajaDAO = (dbChoice == 1) ? new PostgresBarajaDAO() : new MongoBarajaDAO();
        barajaDAO.inicializar(BarajaCatalog.generar());
    }

    // bucle del menu principal, se queda aqui hasta que el usuario elige salir
    public void start() {
        boolean running = true;
        while (running) {
            view.showMenu();
            int choice = view.getMenuChoice();
            switch (choice) {
                case 1 -> runGame();
                case 2 -> view.showRules();
                case 3 -> view.showHighScores(allTimeScores);
                case 4 -> running = false;
            }
        }
        System.out.println("  Gracias por jugar! Hasta la proxima");
    }

    // prepara los jugadores, reparte las cartas y arranca la partida
    private void runGame() {
        ArrayList<Player> players = setupPlayers();
        ArrayList<Carta> catalogo = barajaDAO.obtenerBaraja();
        if (catalogo.isEmpty()) {
            view.showMessage("Error: no se pudieron cargar las cartas de la BD. Comprueba que la base de datos esta corriendo.");
            return;
        }
        GameState state = new GameState(players, CartaConverter.toCards(catalogo));
        state.dealInitialCards();
        gameLoop(state);
        fileManager.saveScores(allTimeScores);
    }



    // pregunta cuantos jugadores hay y sus nombres, devuelve la lista con todos
    private ArrayList<Player> setupPlayers() {
        int count = view.askPlayerCount();
        ArrayList<Player> players = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            String name = view.askPlayerName(i);
            boolean isComputer = view.askIsComputer(name);
            players.add(new Player(name, isComputer));
        }
        return players;
    }

    // bucle principal de turnos, sigue hasta que alguien se queda sin cartas
    private void gameLoop(GameState state) {
        while (state.isGameRunning()) {
            state.incrementTurnCount();
            view.showGameState(state);

            Player current = state.getCurrentPlayer();
            Card played;

            try {
                if (current.isComputer()) {
                    view.showComputerTurn(current);
                    played = computerTurn(state);
                } else {
                    view.showHand(current);
                    played = humanTurn(state);
                }
            } catch (MazoVacioException e) {
                view.showMessage("No quedan cartas disponibles. La partida termina en empate.");
                state.setGameRunning(false);
                view.pressEnter();
                return;
            }

            // miramos si ha ganado antes de aplicar el efecto de la carta
            if (current.getHand().isEmpty()) {
                view.showGameState(state);
                view.showWinner(current);
                view.showGameSummary(current.getName(), state.getTurnCount());
                state.setGameRunning(false);

                // sumamos una victoria al ganador en el marcador
                String winner = current.getName();
                if (allTimeScores.containsKey(winner)) {
                    allTimeScores.put(winner, allTimeScores.get(winner) + 1);
                } else {
                    allTimeScores.put(winner, 1);
                }

                view.pressEnter();
                return;
            }

            // si le queda solo una carta avisamos de UNO
            if (current.getHand().size() == 1) {
                view.showUnoWarning(current);
            }

            // si jugo carta aplicamos el efecto, si solo robo pasamos turno directamente
            if (played != null) {
                applyEffect(played, state);
            } else {
                state.nextPlayer();
            }

            view.pressEnter();
        }
    }

    // gestiona el turno de un jugador humano, devuelve la carta jugada o null si solo robo
    private Card humanTurn(GameState state) {
        Player player = state.getCurrentPlayer();
        int choice = view.getCardChoice(player, state);

        if (choice == -1) {
            // el jugador quiere robar
            Card drawn = state.drawFromDeck();
            player.getHand().addCard(drawn);

            // si la carta robada es jugable le preguntamos si la quiere jugar ya
            if (drawn.isCompatible(state.getTopCard(), state.getCurrentColor())) {
                boolean play = view.askPlayDrawnCard(drawn);
                if (play) {
                    int lastIndex = player.getHand().size() - 1;
                    Card toPlay = player.getHand().playCard(lastIndex);
                    state.playCard(toPlay);
                    handleWild(toPlay, state);
                    return toPlay;
                }
            } else {
                view.showMessage("Robaste: " + view.renderCard(drawn) + "  (no es jugable, te la quedas)");
            }
            return null;
        }

        // juega la carta que eligio
        try {
            Card toPlay = player.getHand().playCard(choice, state.getTopCard(), state.getCurrentColor());
            state.playCard(toPlay);
            handleWild(toPlay, state);
            return toPlay;
        } catch (CartaNoJugableException e) {
            view.showError(e.getMessage());
            return humanTurn(state);
        }
    }

    // gestiona el turno del computer: intenta jugar la mejor carta, si no tiene roba
    private Card computerTurn(GameState state) {
        Player computer = state.getCurrentPlayer();

        Card best = null;
        int bestIndex = -1;

        // buscamos la mejor carta jugable (prefiere las de accion)
        for (int i = 0; i < computer.getHand().size(); i++) {
            Card card = computer.getHand().getCard(i);
            if (card.isCompatible(state.getTopCard(), state.getCurrentColor())) {
                if (best == null || isActionCard(card)) {
                    best = card;
                    bestIndex = i;
                }
            }
        }

        if (best != null) {
            computer.getHand().playCard(bestIndex);
            state.playCard(best);
            view.showMessage(computer.getName() + " juega " + view.renderCard(best));
            handleWild(best, state);
            return best;
        }

        // no tiene carta jugable, roba una
        Card drawn = state.drawFromDeck();
        computer.getHand().addCard(drawn);
        view.showMessage(computer.getName() + " roba una carta.");

        if (drawn.isCompatible(state.getTopCard(), state.getCurrentColor())) {
            computer.getHand().playCard(computer.getHand().size() - 1);
            state.playCard(drawn);
            view.showMessage(computer.getName() + " juega la carta que acaba de robar: " + view.renderCard(drawn));
            handleWild(drawn, state);
            return drawn;
        }

        return null;
    }

    // aplica el efecto especial de la carta y avanza el turno
    private void applyEffect(Card card, GameState state) {
        switch (card.getValue()) {

            case SKIP -> {
                state.nextPlayer();
                view.showMessage(state.getCurrentPlayer().getName() + " pierde el turno!");
                state.nextPlayer();
            }

            case REVERSE -> {
                state.reverseDirection();
                // con 2 jugadores el reverse funciona igual que un skip
                if (state.getPlayers().size() == 2) {
                    state.nextPlayer();
                    view.showMessage("Sentido invertido! (con 2 jugadores actua como skip)");
                    state.nextPlayer();
                } else {
                    view.showMessage("Sentido invertido! Ahora va " + (state.isClockwise() ? "->" : "<-"));
                    state.nextPlayer();
                }
            }

            case DRAW_TWO -> {
                state.nextPlayer();
                Player victim = state.getCurrentPlayer();
                victim.getHand().addCard(state.drawFromDeck());
                victim.getHand().addCard(state.drawFromDeck());
                view.showMessage(victim.getName() + " roba 2 cartas y pierde el turno!");
                state.nextPlayer();
            }

            case WILD_DRAW_FOUR -> {
                state.nextPlayer();
                Player victim = state.getCurrentPlayer();
                for (int i = 0; i < 4; i++) {
                    victim.getHand().addCard(state.drawFromDeck());
                }
                view.showMessage(victim.getName() + " roba 4 cartas y pierde el turno!");
                state.nextPlayer();
            }

            // cartas normales y comodin sin robo, simplemente pasamos al siguiente
            default -> state.nextPlayer();
        }
    }

    // si se jugo una comodin hay que elegir color, el humano elige y el computer calcula
    private void handleWild(Card card, GameState state) {
        if (card.getColor() != Color.BLACK) return;

        Player current = state.getCurrentPlayer();
        if (current.isComputer()) {
            Color chosen = computerPickColor(current);
            state.setCurrentColor(chosen);
            view.showMessage(current.getName() + " cambia el color a " + chosen.name());
        } else {
            Color chosen = view.chooseColor();
            state.setCurrentColor(chosen);
            view.showMessage("Color cambiado a " + chosen.name() + "!");
        }
    }

    // el computer elige el color que mas veces aparece en su mano
    // usamos el HashMap para contar cuantas cartas hay de cada color
    private Color computerPickColor(Player computer) {
        HashMap<Color, Integer> conteo = new HashMap<>();

        for (int i = 0; i < computer.getHand().size(); i++) {
            Color c = computer.getHand().getCard(i).getColor();
            if (c != Color.BLACK) {
                if (conteo.containsKey(c)) {
                    conteo.put(c, conteo.get(c) + 1);
                } else {
                    conteo.put(c, 1);
                }
            }
        }

        // buscamos el color con mas cartas en la mano
        Color mejor = Color.RED;
        int max = 0;
        for (Color c : conteo.keySet()) {
            if (conteo.get(c) > max) {
                max = conteo.get(c);
                mejor = c;
            }
        }
        return mejor;
    }

    // devuelve true si la carta es especial (skip, reverse, +2, comodin o comodin+4)
    private boolean isActionCard(Card card) {
        Value v = card.getValue();
        return v == Value.SKIP
            || v == Value.REVERSE
            || v == Value.DRAW_TWO
            || v == Value.WILD
            || v == Value.WILD_DRAW_FOUR;
    }
}
