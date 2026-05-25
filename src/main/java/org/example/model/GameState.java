package org.example.model;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

// guarda todo el estado de la partida: jugadores, mazo, descarte, turno, direccion...
// el controller lo modifica y la vista solo lo lee para mostrarlo
public class GameState {

    private ArrayList<Player> players;
    private Deck deck;
    private ArrayList<Card> discardPile;
    private int currentPlayerIndex;
    private boolean clockwise;    // true = orden normal, false = invertido
    private Color currentColor;   // cambia cuando se juega una comodin
    private boolean gameRunning;
    private HashMap<String, Integer> scoreBoard; // nombre del jugador -> victorias en la sesion
    private int turnCount;

    public GameState(ArrayList<Player> players) {
        this.players = players;
        this.deck = new Deck();
        this.discardPile = new ArrayList<>();
        this.currentPlayerIndex = 0;
        this.clockwise = true;
        this.gameRunning = true;
        this.scoreBoard = new HashMap<>();
        this.turnCount = 0;

        // todos empiezan con 0 victorias en el marcador
        for (Player p : players) {
            scoreBoard.put(p.getName(), 0);
        }
    }

    public GameState (ArrayList<Player> players, ArrayList<Card> cardsFromDB) {
        this(players);
        this.deck = new Deck(cardsFromDB);
    }

    // reparte 7 cartas a cada jugador y voltea la primera carta para el descarte
    // si la primera carta es una comodin la vuelve a meter y saca otra
    public void dealInitialCards() {
        for (int i = 0; i < 7; i++) {
            for (Player p : players) {
                p.getHand().addCard(deck.draw());
            }
        }

        // la primera carta no puede ser comodin
        Card first = deck.draw();
        while (first.getColor() == Color.BLACK) {
            ArrayList<Card> tmp = new ArrayList<>();
            tmp.add(first);
            deck.addCards(tmp);
            first = deck.draw();
        }

        discardPile.add(first);
        currentColor = first.getColor();
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    // pasa al siguiente jugador segun el sentido de juego actual
    public void nextPlayer() {
        if (clockwise) {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        } else {
            currentPlayerIndex = (currentPlayerIndex - 1 + players.size()) % players.size();
        }
    }

    public void reverseDirection() {
        clockwise = !clockwise;
    }

    // mira la carta que hay en la cima del descarte sin quitarla
    public Card getTopCard() {
        return discardPile.get(discardPile.size() - 1);
    }

    // pone la carta en el descarte y actualiza el color activo si no es comodin
    // el color de las comodin lo asigna el controller despues con setCurrentColor
    public void playCard(Card card) {
        discardPile.add(card);
        if (card.getColor() != Color.BLACK) {
            currentColor = card.getColor();
        }
    }

    // roba una carta del mazo; si esta vacio recicla el descarte antes de robar
    public Card drawFromDeck() {
        if (deck.isEmpty()) {
            Card top = discardPile.remove(discardPile.size() - 1);
            deck.addCards(discardPile);
            discardPile.clear();
            discardPile.add(top);
        }
        return deck.draw();
    }

    public void incrementTurnCount() {
        turnCount++;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public Deck getDeck() {
        return deck;
    }

    public ArrayList<Card> getDiscardPile() {
        return discardPile;
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public boolean isClockwise() {
        return clockwise;
    }

    public Color getCurrentColor() {
        return currentColor;
    }

    public void setCurrentColor(Color color) {
        this.currentColor = color;
    }

    public boolean isGameRunning() {
        return gameRunning;
    }

    public void setGameRunning(boolean running) {
        this.gameRunning = running;
    }

    public HashMap<String, Integer> getScoreBoard() {
        return scoreBoard;
    }

    public void setScoreBoard(HashMap<String, Integer> board) {
        this.scoreBoard = board;
    }

    public int getTurnCount() {
        return turnCount;
    }
}
