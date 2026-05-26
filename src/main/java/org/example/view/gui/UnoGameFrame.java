package org.example.view.gui;

import org.example.model.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.Color;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * Main game window.
 *
 * Layout (green felt table):
 *   NORTH  – info bar (turn / direction / deck / active color)
 *   CENTER – split panel: opponents (top) + game table (center)
 *   SOUTH  – current player hand + draw button
 *   EAST   – message log
 */
public class UnoGameFrame extends JFrame {

    // ─── UI colors ────────────────────────────────────────────────────────────
    private static final Color TABLE  = new Color(22, 88, 38);    // dark green felt
    private static final Color TABLE2 = new Color(16, 68, 28);
    private static final Color BG     = new Color(12, 46, 18);
    private static final Color TEXT   = Color.WHITE;
    private static final Color TEXT2  = new Color(180, 220, 170);
    private static final Color ACCENT = new Color(210, 35, 35);

    // ─── Panels ───────────────────────────────────────────────────────────────
    private final JPanel    infoBar       = new JPanel(new FlowLayout(FlowLayout.CENTER, 24, 10));
    private final JPanel    opponentArea  = new JPanel();
    private final JPanel    tableCenterPanel;
    private final JPanel    handPanel;
    private final JTextArea messageLog;
    private       JLabel    handTitle;

    // Info bar labels
    private final JLabel lblTurn  = infoLabel("Turno 0");
    private final JLabel lblDir   = infoLabel("→");
    private final JLabel lblDeck  = infoLabel("Mazo: —");
    private final JLabel lblColor = new JLabel();

    // Table elements
    private CardView discardView;
    private final CardView deckView = new CardView(null, true);

    // Draw button
    private JButton drawBtn;

    // Input callback: receives Integer (card index or -1 for draw)
    private Consumer<Integer> cardCallback;

    // ─── Constructor ──────────────────────────────────────────────────────────

    public UnoGameFrame() {
        super("UNO Java Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 700));
        setPreferredSize(new Dimension(1060, 720));

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(BG);
        setContentPane(root);

        // ── info bar ──
        infoBar.setBackground(new Color(8, 30, 12));
        infoBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(40, 90, 50)));
        infoBar.add(unoLogo());
        infoBar.add(separator());
        infoBar.add(lblTurn);
        infoBar.add(separator());
        infoBar.add(lblDir);
        infoBar.add(separator());
        infoBar.add(lblDeck);
        infoBar.add(separator());
        infoBar.add(new JLabel("Color activo:"));
        infoBar.add(lblColor);
        styleInfoLabels();
        root.add(infoBar, BorderLayout.NORTH);

        // ── center area ──
        JPanel center = new JPanel(new BorderLayout(0, 6));
        center.setBackground(BG);

        opponentArea.setBackground(BG);
        opponentArea.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 8));
        opponentArea.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        center.add(opponentArea, BorderLayout.NORTH);

        tableCenterPanel = buildTableCenter();
        center.add(tableCenterPanel, BorderLayout.CENTER);

        root.add(center, BorderLayout.CENTER);

        // ── hand panel ──
        handPanel = buildHandPanel();
        root.add(handPanel, BorderLayout.SOUTH);

        // ── message log ──
        messageLog = new JTextArea(8, 22);
        messageLog.setEditable(false);
        messageLog.setBackground(new Color(8, 28, 12));
        messageLog.setForeground(new Color(160, 210, 160));
        messageLog.setFont(new Font("Monospaced", Font.PLAIN, 12));
        messageLog.setWrapStyleWord(true);
        messageLog.setLineWrap(true);
        messageLog.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        JScrollPane logScroll = new JScrollPane(messageLog);
        logScroll.setPreferredSize(new Dimension(200, 200));
        logScroll.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(40, 90, 50)));
        logScroll.getViewport().setBackground(new Color(8, 28, 12));
        JPanel logWrapper = new JPanel(new BorderLayout());
        logWrapper.setBackground(new Color(8, 28, 12));
        JLabel logTitle = new JLabel("  Historial", SwingConstants.LEFT);
        logTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
        logTitle.setForeground(new Color(100, 180, 100));
        logTitle.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(40, 90, 50)));
        logWrapper.add(logTitle, BorderLayout.NORTH);
        logWrapper.add(logScroll, BorderLayout.CENTER);
        root.add(logWrapper, BorderLayout.EAST);

        pack();
        setLocationRelativeTo(null);
    }

    // ─── Sub-panel builders ───────────────────────────────────────────────────

    private JPanel buildTableCenter() {
        JPanel p = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, TABLE, getWidth(), getHeight(), TABLE2));
                g2.fillRoundRect(12, 6, getWidth() - 24, getHeight() - 12, 40, 40);
                // Felt border ring
                g2.setColor(new Color(35, 115, 55, 180));
                g2.setStroke(new BasicStroke(3f));
                g2.drawRoundRect(12, 6, getWidth() - 24, getHeight() - 12, 40, 40);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(600, 220));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(0, 30, 0, 30);

        // Deck pile
        JPanel deckPanel = pilePanel("MAZO", deckView);
        gc.gridx = 0; gc.gridy = 0;
        p.add(deckPanel, gc);

        // Discard pile placeholder
        discardView = new CardView(null, true);
        JPanel discardPanel = pilePanel("DESCARTE", discardView);
        gc.gridx = 1; gc.gridy = 0;
        p.add(discardPanel, gc);

        return p;
    }

    private JPanel buildHandPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 4));
        wrapper.setBackground(BG);
        wrapper.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(40, 90, 50)),
            BorderFactory.createEmptyBorder(8, 12, 10, 12)));

        handTitle = new JLabel("Tu mano", SwingConstants.LEFT);
        handTitle.setFont(new Font("SansSerif", Font.BOLD, 13));
        handTitle.setForeground(new Color(120, 200, 120));
        wrapper.add(handTitle, BorderLayout.NORTH);

        JPanel cardsRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 2));
        cardsRow.setName("cardsRow");
        cardsRow.setBackground(BG);

        drawBtn = buildDrawButton();
        cardsRow.add(drawBtn);
        wrapper.add(cardsRow, BorderLayout.CENTER);

        return wrapper;
    }

    private JButton buildDrawButton() {
        JButton btn = new JButton("ROBAR") {
            private boolean hov;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hov = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e)  { hov = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = isEnabled() ? (hov ? new Color(60, 140, 70) : new Color(40, 110, 50)) : new Color(30, 60, 35);
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.setFont(getFont());
                g2.setColor(isEnabled() ? Color.WHITE : new Color(100, 130, 100));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                    (getWidth() - fm.stringWidth(getText())) / 2.0f,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2.0f);
                g2.dispose();
            }
        };
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(90, CardView.CARD_H));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setEnabled(false);
        btn.addActionListener(e -> {
            if (cardCallback != null) {
                disableAllInput();
                cardCallback.accept(-1);
            }
        });
        return btn;
    }

    // ─── State updates (called from game thread via invokeLater) ──────────────

    public void setCardCallback(Consumer<Integer> cb) {
        this.cardCallback = cb;
    }

    /** Full refresh of the game state. */
    public void updateState(GameState state, List<Player> allPlayers, Player humanPlayer) {
        SwingUtilities.invokeLater(() -> {
            // Info bar
            lblTurn.setText("Turno " + state.getTurnCount());
            lblDir.setText(state.isClockwise() ? "→" : "←");
            lblDeck.setText("Mazo: " + state.getDeck().size());
            updateColorDot(state.getCurrentColor());

            // Discard pile
            Card top = state.getTopCard();
            JPanel discardPanel = (JPanel) discardView.getParent();
            discardView = new CardView(top, false);
            discardPanel.removeAll();
            discardPanel.add(pileLabel("DESCARTE"), BorderLayout.NORTH);
            discardPanel.add(discardView, BorderLayout.CENTER);
            discardPanel.revalidate();
            discardPanel.repaint();

            // Opponents
            updateOpponents(state, allPlayers, humanPlayer);

            revalidate();
            repaint();
        });
    }

    private void updateOpponents(GameState state, List<Player> allPlayers, Player humanPlayer) {
        opponentArea.removeAll();
        Player current = state.getCurrentPlayer();

        for (Player p : allPlayers) {
            if (p == humanPlayer) continue;
            JPanel opPanel = buildOpponentPanel(p, p == current);
            opponentArea.add(opPanel);
        }
        opponentArea.revalidate();
        opponentArea.repaint();
    }

    private JPanel buildOpponentPanel(Player p, boolean isCurrentTurn) {
        JPanel panel = new JPanel(new BorderLayout(0, 4)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = isCurrentTurn ? new Color(50, 120, 60, 200) : new Color(20, 50, 28, 200);
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                if (isCurrentTurn) {
                    g2.setColor(new Color(100, 230, 100, 180));
                    g2.setStroke(new BasicStroke(2f));
                    g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, 12, 12));
                }
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));

        // Name
        String tag = p.isComputer() ? " 🤖" : " 👤";
        JLabel nameLabel = new JLabel(p.getName() + tag + "  (" + p.getHand().size() + ")");
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        nameLabel.setForeground(isCurrentTurn ? new Color(120, 255, 130) : TEXT2);
        panel.add(nameLabel, BorderLayout.NORTH);

        // Card backs
        int cardCount = p.getHand().size();
        int shown = Math.min(cardCount, 10);
        JPanel cardsRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 0));
        cardsRow.setOpaque(false);
        for (int i = 0; i < shown; i++) {
            cardsRow.add(CardView.smallBack());
        }
        if (cardCount > 10) {
            JLabel more = new JLabel("+" + (cardCount - 10));
            more.setFont(new Font("SansSerif", Font.BOLD, 10));
            more.setForeground(TEXT2);
            cardsRow.add(more);
        }
        panel.add(cardsRow, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Refresh the human player's hand and enable clicking.
     * Highlights playable cards with green glow.
     */
    public void showPlayerHand(Player player, Card topCard, org.example.model.Color currentColor, boolean enableInput) {
        SwingUtilities.invokeLater(() -> {
            JPanel cardsRow = getCardsRow();
            cardsRow.removeAll();

            List<Card> cards = player.getHand().getCards();
            for (int i = 0; i < cards.size(); i++) {
                Card c = cards.get(i);
                CardView cv = new CardView(c, false);
                boolean canPlay = c.isCompatible(topCard, currentColor);
                cv.setPlayable(canPlay && enableInput);

                if (enableInput && canPlay) {
                    final int idx = i;
                    cv.addMouseListener(new MouseAdapter() {
                        @Override public void mouseClicked(MouseEvent e) {
                            if (cardCallback != null && cv.isPlayable()) {
                                disableAllInput();
                                cardCallback.accept(idx);
                            }
                        }
                    });
                }
                cardsRow.add(cv);
            }

            // Draw button lives in cardsRow; re-add it after every rebuild
            cardsRow.add(drawBtn);
            drawBtn.setEnabled(enableInput);

            handTitle.setText("Tu mano – " + player.getName() + "  (" + cards.size() + " cartas)");

            cardsRow.revalidate();
            cardsRow.repaint();
            handPanel.revalidate();
            handPanel.repaint();
        });
    }

    /** Shows a "waiting" message in the hand area during computer/other player turns. */
    public void showWaitingHand(Player player) {
        SwingUtilities.invokeLater(() -> {
            JPanel cardsRow = getCardsRow();
            cardsRow.removeAll();

            String msg = player.isComputer()
                ? "  " + player.getName() + " está pensando..."
                : "  Turno de " + player.getName() + "...";
            JLabel wait = new JLabel(msg);
            wait.setFont(new Font("SansSerif", Font.ITALIC, 14));
            wait.setForeground(TEXT2);
            cardsRow.add(wait);
            drawBtn.setEnabled(false);
            handTitle.setText(msg.trim());

            cardsRow.revalidate();
            cardsRow.repaint();
            handPanel.revalidate();
            handPanel.repaint();
        });
    }

    public void disableAllInput() {
        SwingUtilities.invokeLater(() -> {
            drawBtn.setEnabled(false);
            JPanel row = getCardsRow();
            for (Component comp : row.getComponents()) {
                if (comp instanceof CardView cv) cv.setPlayable(false);
            }
        });
    }

    public void appendMessage(String msg) {
        SwingUtilities.invokeLater(() -> {
            messageLog.append("• " + msg + "\n");
            messageLog.setCaretPosition(messageLog.getDocument().getLength());
        });
    }

    /**
     * Shows a full-screen overlay announcing the winner.
     * Blocks until the user chooses; returns true if they want to play again.
     */
    public boolean showWinnerOverlay(Player winner) {
        boolean[] restart = {false};
        try {
            SwingUtilities.invokeAndWait(() -> {
                Object[] options = {"Jugar de nuevo", "Salir"};
                int choice = JOptionPane.showOptionDialog(this,
                    buildWinnerPanel(winner),
                    "¡Fin del juego!",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.PLAIN_MESSAGE,
                    null, options, options[0]);
                restart[0] = (choice == 0);
            });
        } catch (Exception ignored) {}
        return restart[0];
    }

    private JPanel buildWinnerPanel(Player winner) {
        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setBackground(new Color(15, 25, 45));
        p.setBorder(BorderFactory.createEmptyBorder(24, 40, 24, 40));

        JLabel title = new JLabel("¡UNO!  GANADOR", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setForeground(new Color(255, 220, 0));
        p.add(title, BorderLayout.NORTH);

        JLabel name = new JLabel(winner.getName(), SwingConstants.CENTER);
        name.setFont(new Font("SansSerif", Font.BOLD, 40));
        name.setForeground(Color.WHITE);
        p.add(name, BorderLayout.CENTER);

        JLabel sub = new JLabel("¡Se ha quedado sin cartas!", SwingConstants.CENTER);
        sub.setFont(new Font("SansSerif", Font.ITALIC, 16));
        sub.setForeground(new Color(160, 200, 160));
        p.add(sub, BorderLayout.SOUTH);

        return p;
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private JPanel pilePanel(String labelText, CardView cv) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);
        p.add(pileLabel(labelText), BorderLayout.NORTH);
        p.add(cv, BorderLayout.CENTER);
        return p;
    }

    private JLabel pileLabel(String text) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(new Font("SansSerif", Font.BOLD, 11));
        l.setForeground(new Color(140, 200, 140));
        return l;
    }

    private void updateColorDot(org.example.model.Color c) {
        Color awtColor = CardView.baseColor(c);
        lblColor.setIcon(new Icon() {
            @Override public void paintIcon(Component comp, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(awtColor);
                g2.fillOval(x, y, 20, 20);
                g2.setColor(new Color(255, 255, 255, 80));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(x, y, 20, 20);
                g2.dispose();
            }
            @Override public int getIconWidth()  { return 22; }
            @Override public int getIconHeight() { return 22; }
        });
        lblColor.repaint();
    }

    private JLabel infoLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 13));
        l.setForeground(TEXT);
        return l;
    }

    private void styleInfoLabels() {
        for (Component c : infoBar.getComponents()) {
            if (c instanceof JLabel l && l.getText() != null) {
                l.setForeground(TEXT);
            }
        }
    }

    private JLabel separator() {
        JLabel sep = new JLabel("│");
        sep.setForeground(new Color(60, 100, 65));
        return sep;
    }

    private JLabel unoLogo() {
        JLabel l = new JLabel("UNO");
        l.setFont(new Font("SansSerif", Font.BOLD, 20));
        l.setForeground(ACCENT);
        return l;
    }

    private JPanel getCardsRow() {
        for (Component c : handPanel.getComponents()) {
            if (c instanceof JPanel p && "cardsRow".equals(p.getName())) return p;
        }
        return (JPanel) handPanel.getComponent(1);
    }
}
