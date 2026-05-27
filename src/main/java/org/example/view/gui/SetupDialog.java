package org.example.view.gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

/**
 * Initial setup screen with two tabs: "Jugar" (config) and "Reglas".
 * Returns a SetupConfig with all configuration, or null if cancelled.
 */
public class SetupDialog extends JDialog {

    // ─── Config returned by the dialog ────────────────────────────────────────

    public record PlayerConfig(String name, boolean isComputer) {}

    public static class SetupConfig {
        public final int dbChoice; // 0=none, 1=postgres, 2=mongo
        public final List<PlayerConfig> players;
        SetupConfig(int dbChoice, List<PlayerConfig> players) {
            this.dbChoice = dbChoice;
            this.players  = players;
        }
    }

    // ─── Fields ───────────────────────────────────────────────────────────────

    private SetupConfig result;

    private final ButtonGroup dbGroup     = new ButtonGroup();
    private final JSpinner    countSpinner = new JSpinner(new SpinnerNumberModel(2, 2, 4, 1));
    private final JPanel      playersPanel = new JPanel();
    private final List<JTextField> nameFields    = new ArrayList<>();
    private final List<JCheckBox>  computerBoxes = new ArrayList<>();

    private static final java.awt.Color BG    = new java.awt.Color(15, 20, 40);
    private static final java.awt.Color PANEL = new java.awt.Color(24, 32, 58);
    private static final java.awt.Color ACC   = new java.awt.Color(220, 35, 35);
    private static final java.awt.Color FG    = java.awt.Color.WHITE;
    private static final java.awt.Color FG2   = new java.awt.Color(180, 185, 200);

    // ─── Constructor ──────────────────────────────────────────────────────────

    public SetupDialog(JFrame parent) {
        super(parent, "UNO – Configuración", true);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());
        setResizable(true);

        add(buildHeader(), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(BG);
        tabs.setForeground(FG);
        tabs.setFont(new Font("SansSerif", Font.BOLD, 13));
        tabs.addTab("  Jugar  ", buildPlayTab());
        tabs.addTab("  Reglas  ", buildRulesTab());
        add(tabs, BorderLayout.CENTER);

        countSpinner.addChangeListener(e -> rebuildPlayers((int) countSpinner.getValue()));
        rebuildPlayers(2);

        pack();
        setMinimumSize(new Dimension(520, 580));
        setSize(Math.max(520, getWidth()), Math.max(580, getHeight()));
        setLocationRelativeTo(parent);
    }

    // ─── Tab builders ─────────────────────────────────────────────────────────

    private JPanel buildPlayTab() {
        JPanel tab = new JPanel(new BorderLayout());
        tab.setBackground(BG);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(BG);
        body.setBorder(BorderFactory.createEmptyBorder(12, 30, 8, 30));

        body.add(buildDbSection());
        body.add(Box.createVerticalStrut(14));

        JPanel countRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        countRow.setBackground(BG);
        countRow.add(label("Jugadores: "));
        styleSpinner(countSpinner);
        countRow.add(countSpinner);
        body.add(countRow);
        body.add(Box.createVerticalStrut(10));

        playersPanel.setLayout(new BoxLayout(playersPanel, BoxLayout.Y_AXIS));
        playersPanel.setBackground(BG);
        body.add(playersPanel);

        tab.add(body, BorderLayout.CENTER);
        tab.add(buildFooter(), BorderLayout.SOUTH);
        return tab;
    }

    private JPanel buildRulesTab() {
        JPanel tab = new JPanel(new BorderLayout());
        tab.setBackground(BG);

        JTextArea text = new JTextArea(RULES_TEXT);
        text.setEditable(false);
        text.setBackground(new java.awt.Color(18, 25, 50));
        text.setForeground(new java.awt.Color(200, 210, 240));
        text.setFont(new Font("Monospaced", Font.PLAIN, 13));
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        text.setCaretPosition(0);

        JScrollPane scroll = new JScrollPane(text);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(new java.awt.Color(18, 25, 50));
        tab.add(scroll, BorderLayout.CENTER);
        return tab;
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
        "  antes de que expire la cuenta atrás\n" +
        "  (3 segundos).\n" +
        "  Si no lo haces → ¡recibes 2 cartas de\n" +
        "  penalización!\n\n" +
        "GANAR\n" +
        "  El primero en vaciar su mano gana.\n" +
        "  Los resultados se guardan en el marcador.\n";

    // ─── Section builders ─────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, new java.awt.Color(180, 20, 20), getWidth(), 0, new java.awt.Color(120, 0, 120)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        p.setOpaque(false);
        p.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 18));
        JLabel title = new JLabel("🃏  UNO JAVA GAME");
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        title.setForeground(FG);
        p.add(title);
        return p;
    }

    private JPanel buildDbSection() {
        JPanel p = section("Base de datos");

        JRadioButton rb1 = radio("PostgreSQL",     "pg",   true);
        JRadioButton rb2 = radio("MongoDB",        "mg",   false);
        JRadioButton rb3 = radio("Sin BD (local)", "none", false);

        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
        row.setBackground(PANEL);
        row.add(rb1); row.add(rb2); row.add(rb3);
        p.add(row, BorderLayout.CENTER);
        return p;
    }

    private void rebuildPlayers(int count) {
        playersPanel.removeAll();
        nameFields.clear();
        computerBoxes.clear();

        for (int i = 1; i <= count; i++) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
            row.setBackground(BG);

            JLabel num = new JLabel("Jugador " + i + ":");
            num.setFont(new Font("SansSerif", Font.BOLD, 13));
            num.setForeground(FG2);
            num.setPreferredSize(new Dimension(80, 24));
            row.add(num);

            JTextField tf = new JTextField(i == 1 ? "Jugador " + i : "Computer", 14);
            styleField(tf);
            nameFields.add(tf);
            row.add(tf);

            JCheckBox cb = new JCheckBox("Computer");
            cb.setForeground(FG2);
            cb.setBackground(BG);
            cb.setFont(new Font("SansSerif", Font.PLAIN, 12));
            cb.setSelected(i != 1);
            computerBoxes.add(cb);
            row.add(cb);

            playersPanel.add(row);
        }
        playersPanel.revalidate();
        playersPanel.repaint();
        pack();
        if (getHeight() < 580) setSize(getWidth(), 580);
    }

    private JPanel buildFooter() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 14));
        p.setBackground(BG);
        JButton start = accentButton("  JUGAR  ");
        start.addActionListener(e -> collect());
        p.add(start);
        return p;
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private void collect() {
        int count = (int) countSpinner.getValue();
        List<PlayerConfig> players = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String name = nameFields.get(i).getText().trim();
            if (name.isEmpty()) name = "Jugador" + (i + 1);
            players.add(new PlayerConfig(name, computerBoxes.get(i).isSelected()));
        }
        result = new SetupConfig(selectedDb(), players);
        dispose();
    }

    private int selectedDb() {
        Enumeration<AbstractButton> btns = dbGroup.getElements();
        while (btns.hasMoreElements()) {
            AbstractButton b = btns.nextElement();
            if (b.isSelected()) {
                return switch (b.getActionCommand()) {
                    case "pg"  -> 1;
                    case "mg"  -> 2;
                    default    -> 0;
                };
            }
        }
        return 0;
    }

    private JPanel section(String title) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setBackground(PANEL);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new java.awt.Color(50, 60, 100), 1),
            BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        lbl.setForeground(new java.awt.Color(150, 170, 255));
        p.add(lbl, BorderLayout.NORTH);
        return p;
    }

    private JRadioButton radio(String text, String cmd, boolean selected) {
        JRadioButton rb = new JRadioButton(text, selected);
        rb.setActionCommand(cmd);
        rb.setForeground(FG);
        rb.setBackground(PANEL);
        rb.setFont(new Font("SansSerif", Font.PLAIN, 13));
        rb.setFocusPainted(false);
        dbGroup.add(rb);
        return rb;
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 13));
        l.setForeground(FG);
        return l;
    }

    private void styleSpinner(JSpinner s) {
        s.setFont(new Font("SansSerif", Font.BOLD, 13));
        ((JSpinner.DefaultEditor) s.getEditor()).getTextField().setBackground(new java.awt.Color(30, 40, 70));
        ((JSpinner.DefaultEditor) s.getEditor()).getTextField().setForeground(FG);
        s.setPreferredSize(new Dimension(64, 28));
    }

    private void styleField(JTextField tf) {
        tf.setBackground(new java.awt.Color(28, 38, 68));
        tf.setForeground(FG);
        tf.setCaretColor(FG);
        tf.setFont(new Font("SansSerif", Font.PLAIN, 13));
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new java.awt.Color(60, 80, 140)),
            BorderFactory.createEmptyBorder(3, 8, 3, 8)));
        tf.setPreferredSize(new Dimension(tf.getPreferredSize().width, 28));
    }

    private JButton accentButton(String text) {
        JButton btn = new JButton(text) {
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
                java.awt.Color c = hov ? ACC.brighter() : ACC;
                g2.setColor(c);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.setFont(getFont());
                g2.setColor(java.awt.Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                    (getWidth() - fm.stringWidth(getText())) / 2.0f,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2.0f);
                g2.dispose();
            }
        };
        btn.setFont(new Font("SansSerif", Font.BOLD, 15));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(180, 44));
        return btn;
    }

    public static SetupConfig show(JFrame parent) {
        SetupDialog dlg = new SetupDialog(parent);
        dlg.setVisible(true);
        return dlg.result;
    }
}
