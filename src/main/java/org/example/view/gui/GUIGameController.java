package org.example.view.gui;

import org.example.db.*;
import org.example.model.*;
import org.example.utils.*;

import javax.swing.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Game controller for GUI mode.
 * Runs the game loop in a background thread; uses a BlockingQueue to receive
 * human input (card index / -1 for draw) from the Swing EDT.
 */
public class GUIGameController {

    private final UnoGameFrame frame;
    private final List<Player> players;
    private final List<Card>   deckCards;  // null means use built-in deck
    private final HashMap<String, Integer> allTimeScores;
    private final FileManager fileManager = new FileManager();

    // Human player (the first non-computer player found, for single-human games)
    // For multi-human we treat each human's turn separately
    private final BlockingQueue<Integer> inputQueue = new LinkedBlockingQueue<>();

    public GUIGameController(UnoGameFrame frame,
                             SetupDialog.SetupConfig config) {
        this.frame = frame;
        this.allTimeScores = fileManager.loadScores();

        // Build player list
        this.players = new ArrayList<>();
        for (SetupDialog.PlayerConfig pc : config.players) {
            players.add(new Player(pc.name(), pc.isComputer()));
        }

        // Load cards from DB or use in-memory
        List<Card> cards = null;
        if (config.dbChoice != 0) {
            try {
                BarajaDAO dao = config.dbChoice == 1
                    ? new PostgresBarajaDAO()
                    : new MongoBarajaDAO();
                dao.inicializar(BarajaCatalog.generar());
                ArrayList<Carta> catalog = dao.obtenerBaraja();
                if (!catalog.isEmpty()) {
                    cards = CartaConverter.toCards(catalog);
                }
            } catch (Exception e) {
                SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(frame,
                        "No se pudo conectar a la BD (" + e.getMessage() + ").\n" +
                        "Usando mazo local.",
                        "Aviso",
                        JOptionPane.WARNING_MESSAGE));
            }
        }
        this.deckCards = cards;

        // Register input callback
        frame.setCardCallback(idx -> inputQueue.add(idx));
    }

    private final MusicPlayer music = new MusicPlayer();

    /** Starts the game loop in a background thread. */
    public void start() {
        music.play();
        Thread gameThread = new Thread(this::gameLoop, "uno-game-loop");
        gameThread.setDaemon(true);
        gameThread.start();
    }

    // ─── Main game loop ───────────────────────────────────────────────────────

    private void gameLoop() {
        ArrayList<Player> playerList = new ArrayList<>(players);
        GameState state = deckCards != null
            ? new GameState(playerList, new ArrayList<>(deckCards))
            : new GameState(playerList);
        state.dealInitialCards();

        // Find a "representative" human player for frame updates
        // (the current human player changes each turn)
        Player anyHuman = players.stream()
            .filter(p -> !p.isComputer())
            .findFirst()
            .orElse(players.get(0));

        updateFrame(state, anyHuman);
        msg("¡Comienza la partida! " + state.getPlayers().size() + " jugadores.");

        while (state.isGameRunning()) {
            state.incrementTurnCount();
            Player current = state.getCurrentPlayer();

            updateFrame(state, current.isComputer() ? anyHuman : current);
            sleep(300);

            Card played;
            if (current.isComputer()) {
                played = doComputerTurn(state, current);
            } else {
                played = doHumanTurn(state, current);
            }

            // Check winner
            if (current.getHand().isEmpty()) {
                SoundEffect.win();
                updateFrame(state, current);
                msg("🏆 ¡" + current.getName() + " gana la partida!");
                state.setGameRunning(false);

                allTimeScores.merge(current.getName(), 1, Integer::sum);
                fileManager.saveScores(allTimeScores);

                SoundEffect.win(music);
                sleep(3000);
                music.stop();
                boolean restart = frame.showWinnerOverlay(current);
                if (restart) {
                    SwingUtilities.invokeLater(() -> {
                        SetupDialog.SetupConfig newConfig = SetupDialog.show(frame);
                        if (newConfig == null) { System.exit(0); return; }
                        new GUIGameController(frame, newConfig).start();
                    });
                } else {
                    System.exit(0);
                }
                return;
            }

            if (current.getHand().size() == 1) {
                SoundEffect.uno(music);
                msg("⚠ ¡UNO! — a " + current.getName() + " le queda 1 carta.");
            }

            if (played != null) {
                applyEffect(played, state, current);
            } else {
                state.nextPlayer();
            }

            updateFrame(state, current.isComputer() ? anyHuman : state.getCurrentPlayer());
        }
    }

    // ─── Computer turn ────────────────────────────────────────────────────────

    private Card doComputerTurn(GameState state, Player computer) {
        frame.showWaitingHand(computer);
        sleep(900);

        Card best = null;
        int bestIdx = -1;
        for (int i = 0; i < computer.getHand().size(); i++) {
            Card c = computer.getHand().getCard(i);
            if (c.isCompatible(state.getTopCard(), state.getCurrentColor())) {
                if (best == null || isAction(c)) { best = c; bestIdx = i; }
            }
        }

        if (best != null) {
            computer.getHand().playCard(bestIdx);
            state.playCard(best);
            SoundEffect.playCard();
            msg(computer.getName() + " jugó " + cardName(best));
            handleWild(best, state, computer);
            return best;
        }

        Card drawn = state.drawFromDeck();
        computer.getHand().addCard(drawn);
        SoundEffect.drawCard();
        msg(computer.getName() + " robó una carta.");

        if (drawn.isCompatible(state.getTopCard(), state.getCurrentColor())) {
            computer.getHand().playCard(computer.getHand().size() - 1);
            state.playCard(drawn);
            msg(computer.getName() + " jugó la carta robada: " + cardName(drawn));
            handleWild(drawn, state, computer);
            return drawn;
        }
        return null;
    }

    // ─── Human turn ───────────────────────────────────────────────────────────

    private Card doHumanTurn(GameState state, Player human) {
        // Show hand and enable clicks
        frame.showPlayerHand(human, state.getTopCard(), state.getCurrentColor(), true);

        int choice = waitForInput();

        frame.disableAllInput();

        if (choice == -1) {
            // Draw a card
            Card drawn = state.drawFromDeck();
            human.getHand().addCard(drawn);
            SoundEffect.drawCard();
            msg(human.getName() + " robó una carta.");

            if (drawn.isCompatible(state.getTopCard(), state.getCurrentColor())) {
                // Ask if they want to play the drawn card
                boolean[] playIt = {false};
                CountDownLatch latch = new CountDownLatch(1);
                SwingUtilities.invokeLater(() -> {
                    int ans = JOptionPane.showConfirmDialog(frame,
                        "Robaste: " + cardName(drawn) + "\n¿La juegas ahora?",
                        "Carta jugable", JOptionPane.YES_NO_OPTION);
                    playIt[0] = (ans == JOptionPane.YES_OPTION);
                    latch.countDown();
                });
                try { latch.await(); } catch (InterruptedException ignored) {}

                if (playIt[0]) {
                    human.getHand().playCard(human.getHand().size() - 1);
                    state.playCard(drawn);
                    msg(human.getName() + " jugó " + cardName(drawn));
                    handleWild(drawn, state, human);
                    return drawn;
                }
            }
            return null;
        }

        // Play a card
        Card played = human.getHand().playCard(choice);
        state.playCard(played);
        SoundEffect.playCard();
        msg(human.getName() + " jugó " + cardName(played));
        handleWild(played, state, human);

        // Refresh hand after playing
        frame.showPlayerHand(human, state.getTopCard(), state.getCurrentColor(), false);
        return played;
    }

    // ─── Wild card color choice ───────────────────────────────────────────────

    private void handleWild(Card card, GameState state, Player player) {
        if (card.getColor() != Color.BLACK) return;
        SoundEffect.wild();

        if (player.isComputer()) {
            Color chosen = computerPickColor(player);
            state.setCurrentColor(chosen);
            msg(player.getName() + " cambió el color a " + spanishColor(chosen));
        } else {
            Color[] chosen = {Color.RED};
            CountDownLatch latch = new CountDownLatch(1);
            SwingUtilities.invokeLater(() -> {
                chosen[0] = ColorPickerDialog.show(frame);
                latch.countDown();
            });
            try { latch.await(); } catch (InterruptedException ignored) {}
            state.setCurrentColor(chosen[0]);
            msg(player.getName() + " eligió " + spanishColor(chosen[0]));
        }
    }

    // ─── Effect application ───────────────────────────────────────────────────

    private void applyEffect(Card card, GameState state, Player who) {
        switch (card.getValue()) {
            case SKIP -> {
                SoundEffect.skip();
                state.nextPlayer();
                msg(state.getCurrentPlayer().getName() + " pierde el turno.");
                state.nextPlayer();
            }
            case REVERSE -> {
                SoundEffect.reverse();
                state.reverseDirection();
                if (state.getPlayers().size() == 2) {
                    msg("Reverso (2 jugadores = salta turno).");
                    state.nextPlayer(); state.nextPlayer();
                } else {
                    msg("Sentido invertido: ahora " + (state.isClockwise() ? "→" : "←"));
                    state.nextPlayer();
                }
            }
            case DRAW_TWO -> {
                SoundEffect.drawTwo();
                state.nextPlayer();
                Player victim = state.getCurrentPlayer();
                victim.getHand().addCard(state.drawFromDeck());
                victim.getHand().addCard(state.drawFromDeck());
                msg(victim.getName() + " roba 2 cartas y pierde el turno.");
                state.nextPlayer();
            }
            case WILD_DRAW_FOUR -> {
                SoundEffect.drawFour();
                state.nextPlayer();
                Player victim = state.getCurrentPlayer();
                for (int i = 0; i < 4; i++) victim.getHand().addCard(state.drawFromDeck());
                msg(victim.getName() + " roba 4 cartas y pierde el turno.");
                state.nextPlayer();
            }
            default -> state.nextPlayer();
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private void updateFrame(GameState state, Player humanPlayer) {
        frame.updateState(state, players, humanPlayer);
        if (!humanPlayer.isComputer()) {
            frame.showPlayerHand(humanPlayer, state.getTopCard(), state.getCurrentColor(), false);
        }
    }

    private int waitForInput() {
        try {
            return inputQueue.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return -1;
        }
    }

    private Color computerPickColor(Player computer) {
        HashMap<Color, Integer> count = new HashMap<>();
        for (int i = 0; i < computer.getHand().size(); i++) {
            Color c = computer.getHand().getCard(i).getColor();
            if (c != Color.BLACK) count.merge(c, 1, Integer::sum);
        }
        Color best = Color.RED;
        int max = -1;
        for (Map.Entry<Color, Integer> e : count.entrySet()) {
            if (e.getValue() > max) { max = e.getValue(); best = e.getKey(); }
        }
        return best;
    }

    private boolean isAction(Card c) {
        return switch (c.getValue()) {
            case SKIP, REVERSE, DRAW_TWO, WILD, WILD_DRAW_FOUR -> true;
            default -> false;
        };
    }

    private void msg(String text) {
        frame.appendMessage(text);
    }

    private void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }

    private String cardName(Card c) {
        if (c.getColor() == Color.BLACK) {
            return "[" + (c.getValue() == Value.WILD ? "Comodín" : "Comodín +4") + "]";
        }
        return "[" + spanishColor(c.getColor()) + " " + spanishValue(c.getValue()) + "]";
    }

    private String spanishColor(Color c) {
        return switch (c) {
            case RED -> "Rojo"; case BLUE -> "Azul";
            case GREEN -> "Verde"; case YELLOW -> "Amarillo";
            case BLACK -> "Comodín";
        };
    }

    private String spanishValue(Value v) {
        return switch (v) {
            case SKIP -> "Salta"; case REVERSE -> "Reverso";
            case DRAW_TWO -> "+2"; case WILD -> "Comodín";
            case WILD_DRAW_FOUR -> "+4";
            default -> v.name().replace("_", " ");
        };
    }
}
