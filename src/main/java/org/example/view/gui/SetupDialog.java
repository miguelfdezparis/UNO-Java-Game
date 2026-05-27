package org.example.view.gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

/**
 * Full-screen setup dialog with custom tab buttons ("Jugar" / "Reglas").
 * Returns a SetupConfig or null if cancelled.
 */
public class SetupDialog extends JDialog {

    // ─── Config ───────────────────────────────────────────────────────────────

    public record PlayerConfig(String name, boolean isComputer) {}

    public static class SetupConfig {
        public final int dbChoice;
        public final List<PlayerConfig> players;
        SetupConfig(int dbChoice, List<PlayerConfig> players) {
            this.dbChoice = dbChoice;
            this.players  = players;
        }
    }

    // ─── Colours ──────────────────────────────────────────────────────────────

    private static final Color BG       = new Color(15, 20, 40);
    private static final Color PANEL    = new Color(24, 32, 58);
    private static final Color ACC      = new Color(220, 35, 35);
    private static final Color TAB_ACT  = new Color(200, 30, 30);   // active tab
    private static final Color TAB_IDLE = new Color(35, 45, 80);    // inactive tab
    private static final Color FG       = Color.WHITE;
    private static final Color FG2      = new Color(180, 185, 200);

    // ─── State ────────────────────────────────────────────────────────────────

    private SetupConfig result;
    private boolean     fullscreen = true;

    // Tab widgets
    private JButton     tabJugar, tabReglas;
    private CardLayout  cardLayout;
    private JPanel      cardPanel;

    // Play-tab fields
    private final ButtonGroup      dbGroup      = new ButtonGroup();
    private final JSpinner         countSpinner = new JSpinner(new SpinnerNumberModel(2, 2, 4, 1));
    private final JPanel           playersPanel = new JPanel();
    private final List<JTextField> nameFields   = new ArrayList<>();
    private final List<JCheckBox>  computerBoxes= new ArrayList<>();

    // ─── Constructor ──────────────────────────────────────────────────────────

    public SetupDialog(JFrame parent) {
        super(parent, "UNO – Configuración", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setUndecorated(false);
        setResizable(true);

        // Open fullscreen
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screen.width, screen.height);
        setLocation(0, 0);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        setContentPane(root);

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildBody(),   BorderLayout.CENTER);

        // wire player count spinner
        countSpinner.addChangeListener(e -> rebuildPlayers((int) countSpinner.getValue()));
        rebuildPlayers(2);

        selectTab("jugar");
    }

    // ─── Header ───────────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, new Color(160, 18, 18),
                                              getWidth(), 0, new Color(100, 0, 110)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(0, 80));
        p.setBorder(BorderFactory.createEmptyBorder(0, 32, 0, 24));

        JLabel logo = new JLabel("🃏  UNO JAVA GAME");
        logo.setFont(new Font("SansSerif", Font.BOLD, 30));
        logo.setForeground(FG);
        p.add(logo, BorderLayout.WEST);

        // Fullscreen toggle button
        JButton fsBtn = new JButton("⛶") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover()
                    ? new Color(255, 255, 255, 40) : new Color(255, 255, 255, 15));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.setFont(getFont());
                g2.setColor(FG);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                    (getWidth()  - fm.stringWidth(getText())) / 2.0f,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2.0f);
                g2.dispose();
            }
        };
        fsBtn.setFont(new Font("SansSerif", Font.PLAIN, 20));
        fsBtn.setPreferredSize(new Dimension(44, 44));
        fsBtn.setContentAreaFilled(false);
        fsBtn.setBorderPainted(false);
        fsBtn.setFocusPainted(false);
        fsBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        fsBtn.setToolTipText("Pantalla completa / restaurar");
        fsBtn.addActionListener(e -> toggleFullscreen(fsBtn));

        JPanel east = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 18));
        east.setOpaque(false);
        east.add(fsBtn);
        p.add(east, BorderLayout.EAST);

        return p;
    }

    private void toggleFullscreen(JButton fsBtn) {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        if (fullscreen) {
            setSize(860, 660);
            setLocationRelativeTo(null);
            fsBtn.setText("⛶");
            fullscreen = false;
        } else {
            setSize(screen.width, screen.height);
            setLocation(0, 0);
            fsBtn.setText("❐");
            fullscreen = true;
        }
    }

    // ─── Body (tab bar + card panel) ──────────────────────────────────────────

    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(BG);

        body.add(buildTabBar(),     BorderLayout.NORTH);
        body.add(buildCardPanel(),  BorderLayout.CENTER);

        return body;
    }

    private JPanel buildTabBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        bar.setBackground(new Color(10, 14, 30));
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, ACC));

        tabJugar  = tabButton("🎮   JUGAR",  "jugar");
        tabReglas = tabButton("📖   REGLAS", "reglas");

        bar.add(tabJugar);
        bar.add(Box.createHorizontalStrut(6));
        bar.add(tabReglas);

        return bar;
    }

    private JButton tabButton(String text, String card) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean active = card.equals(cardLayout == null ? "" : activeCard());
                Color bg = active ? TAB_ACT : (getModel().isRollover() ? new Color(50, 65, 110) : TAB_IDLE);
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight() + 6, 10, 10));
                g2.setFont(getFont());
                g2.setColor(active ? FG : FG2);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                    (getWidth()  - fm.stringWidth(getText())) / 2.0f,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2.0f - 2);
                g2.dispose();
            }
        };
        btn.setFont(new Font("SansSerif", Font.BOLD, 18));
        btn.setPreferredSize(new Dimension(240, 54));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> selectTab(card));
        return btn;
    }

    private String currentCard = "jugar";

    private String activeCard() { return currentCard; }

    private void selectTab(String card) {
        currentCard = card;
        if (cardLayout != null) cardLayout.show(cardPanel, card);
        if (tabJugar != null)  tabJugar.repaint();
        if (tabReglas != null) tabReglas.repaint();
    }

    private JPanel buildCardPanel() {
        cardLayout = new CardLayout();
        cardPanel  = new JPanel(cardLayout);
        cardPanel.setBackground(BG);

        cardPanel.add(buildPlayCard(),  "jugar");
        cardPanel.add(buildRulesCard(), "reglas");

        return cardPanel;
    }

    // ─── Play card ────────────────────────────────────────────────────────────

    private JPanel buildPlayCard() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(BG);

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBackground(BG);
        inner.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        inner.setMaximumSize(new Dimension(600, Integer.MAX_VALUE));

        inner.add(buildDbSection());
        inner.add(Box.createVerticalStrut(18));

        // Player count row
        JPanel countRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        countRow.setBackground(BG);
        countRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        countRow.add(label("Número de jugadores: "));
        styleSpinner(countSpinner);
        countRow.add(countSpinner);
        inner.add(countRow);
        inner.add(Box.createVerticalStrut(12));

        playersPanel.setLayout(new BoxLayout(playersPanel, BoxLayout.Y_AXIS));
        playersPanel.setBackground(BG);
        playersPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        inner.add(playersPanel);
        inner.add(Box.createVerticalStrut(24));

        // JUGAR button
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnRow.setBackground(BG);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton start = accentButton("  ▶   JUGAR  ");
        start.addActionListener(e -> collect());
        btnRow.add(start);
        inner.add(btnRow);

        outer.add(inner);
        return outer;
    }

    // ─── Rules card ───────────────────────────────────────────────────────────

    private JPanel buildRulesCard() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG);

        JTextArea text = new JTextArea(RULES_TEXT);
        text.setEditable(false);
        text.setBackground(new Color(18, 25, 50));
        text.setForeground(new Color(210, 220, 255));
        text.setFont(new Font("Monospaced", Font.PLAIN, 15));
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setBorder(BorderFactory.createEmptyBorder(24, 40, 24, 40));
        text.setCaretPosition(0);

        JScrollPane scroll = new JScrollPane(text);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(new Color(18, 25, 50));
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private static final String RULES_TEXT =
        "R E G L A S   D E L   U N O\n" +
        "════════════════════════════════════\n\n" +
        "OBJETIVO\n" +
        "  Ser el primero en quedarse sin cartas.\n\n" +
        "PREPARACIÓN\n" +
        "  • Cada jugador recibe 7 cartas al inicio.\n" +
        "  • La primera carta del mazo se voltea para\n" +
        "    iniciar la pila de descarte.\n\n" +
        "CÓMO JUGAR\n" +
        "  En tu turno debes hacer una de estas cosas:\n" +
        "  1. Jugar una carta que coincida en COLOR\n" +
        "     o NÚMERO/SÍMBOLO con la del descarte.\n" +
        "  2. Jugar un comodín (siempre válido).\n" +
        "  3. Si no puedes jugar, robar una carta del\n" +
        "     mazo. Si es jugable, puedes usarla ya.\n\n" +
        "CARTAS ESPECIALES\n" +
        "  🚫 Salta        El siguiente jugador pierde\n" +
        "                  su turno.\n" +
        "  🔄 Reverso      Invierte el sentido del juego.\n" +
        "  +2 Roba Dos     El siguiente jugador roba 2\n" +
        "                  cartas y pierde su turno.\n" +
        "  🌈 Comodín      Elige el color activo.\n" +
        "  🌈 Comodín +4   Elige el color Y el siguiente\n" +
        "                  jugador roba 4 cartas y pierde\n" +
        "                  su turno.\n\n" +
        "DECIR UNO  ⚠\n" +
        "  Cuando te quede solo 1 carta, pulsa el\n" +
        "  botón  ¡UNO!  que aparece en pantalla\n" +
        "  antes de que expire la cuenta atrás (3 s).\n" +
        "  Si no lo haces → ¡recibes 2 cartas de\n" +
        "  penalización!\n\n" +
        "GANAR\n" +
        "  El primero en vaciar su mano gana.\n" +
        "  Los resultados se guardan en el marcador.\n";

    // ─── Section builders ─────────────────────────────────────────────────────

    private JPanel buildDbSection() {
        JPanel p = section("Base de datos");

        JRadioButton rb1 = radio("PostgreSQL",      "pg",   true);
        JRadioButton rb2 = radio("MongoDB",         "mg",   false);
        JRadioButton rb3 = radio("Sin BD (local)",  "none", false);

        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setBackground(PANEL);
        col.add(rb1); col.add(Box.createVerticalStrut(4));
        col.add(rb2); col.add(Box.createVerticalStrut(4));
        col.add(rb3);
        p.add(col, BorderLayout.CENTER);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(600, 160));
        return p;
    }

    private void rebuildPlayers(int count) {
        playersPanel.removeAll();
        nameFields.clear();
        computerBoxes.clear();

        for (int i = 1; i <= count; i++) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
            row.setBackground(BG);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel num = new JLabel("Jugador " + i + ":");
            num.setFont(new Font("SansSerif", Font.BOLD, 14));
            num.setForeground(FG2);
            num.setPreferredSize(new Dimension(95, 28));
            row.add(num);

            JTextField tf = new JTextField(i == 1 ? "Jugador " + i : "Computer", 16);
            styleField(tf);
            nameFields.add(tf);
            row.add(tf);

            JCheckBox cb = new JCheckBox("CPU");
            cb.setForeground(FG2);
            cb.setBackground(BG);
            cb.setFont(new Font("SansSerif", Font.BOLD, 13));
            cb.setSelected(i != 1);
            computerBoxes.add(cb);
            row.add(cb);

            playersPanel.add(row);
        }
        playersPanel.revalidate();
        playersPanel.repaint();
    }

    // ─── Collect & close ──────────────────────────────────────────────────────

    private void collect() {
        int count = (int) countSpinner.getValue();
        List<PlayerConfig> plist = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String name = nameFields.get(i).getText().trim();
            if (name.isEmpty()) name = "Jugador" + (i + 1);
            plist.add(new PlayerConfig(name, computerBoxes.get(i).isSelected()));
        }
        result = new SetupConfig(selectedDb(), plist);
        dispose();
    }

    private int selectedDb() {
        Enumeration<AbstractButton> btns = dbGroup.getElements();
        while (btns.hasMoreElements()) {
            AbstractButton b = btns.nextElement();
            if (b.isSelected()) return switch (b.getActionCommand()) {
                case "pg"  -> 1;
                case "mg"  -> 2;
                default    -> 0;
            };
        }
        return 0;
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private JPanel section(String title) {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(PANEL);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(50, 60, 110), 1),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)));
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 14));
        lbl.setForeground(new Color(160, 180, 255));
        p.add(lbl, BorderLayout.NORTH);
        return p;
    }

    private JRadioButton radio(String text, String cmd, boolean selected) {
        JRadioButton rb = new JRadioButton(text, selected);
        rb.setActionCommand(cmd);
        rb.setForeground(FG);
        rb.setBackground(PANEL);
        rb.setFont(new Font("SansSerif", Font.PLAIN, 14));
        rb.setFocusPainted(false);
        dbGroup.add(rb);
        return rb;
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 14));
        l.setForeground(FG);
        return l;
    }

    private void styleSpinner(JSpinner s) {
        s.setFont(new Font("SansSerif", Font.BOLD, 14));
        ((JSpinner.DefaultEditor) s.getEditor()).getTextField().setBackground(new Color(30, 40, 70));
        ((JSpinner.DefaultEditor) s.getEditor()).getTextField().setForeground(FG);
        s.setPreferredSize(new Dimension(70, 32));
    }

    private void styleField(JTextField tf) {
        tf.setBackground(new Color(28, 38, 68));
        tf.setForeground(FG);
        tf.setCaretColor(FG);
        tf.setFont(new Font("SansSerif", Font.PLAIN, 14));
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 80, 140)),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)));
        tf.setPreferredSize(new Dimension(tf.getPreferredSize().width, 32));
    }

    private JButton accentButton(String text) {
        JButton btn = new JButton(text) {
            private boolean hov;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hov = true;  repaint(); }
                    @Override public void mouseExited(MouseEvent e)  { hov = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hov ? ACC.brighter() : ACC);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
                g2.setFont(getFont());
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                    (getWidth()  - fm.stringWidth(getText())) / 2.0f,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2.0f);
                g2.dispose();
            }
        };
        btn.setFont(new Font("SansSerif", Font.BOLD, 20));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(260, 58));
        return btn;
    }

    // ─── Static factory ───────────────────────────────────────────────────────

    public static SetupConfig show(JFrame parent) {
        SetupDialog dlg = new SetupDialog(parent);
        dlg.setVisible(true);
        return dlg.result;
    }
}
