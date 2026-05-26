package org.example.view.gui;

import org.example.model.Card;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Dialog shown when the human draws a playable card.
 * Renders the card visually and asks whether to play it.
 */
public class DrawnCardDialog extends JDialog {

    private boolean playIt = false;

    private DrawnCardDialog(JFrame parent, Card card) {
        super(parent, "Carta robada", true);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        JPanel root = new JPanel(new BorderLayout(0, 18)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(18, 24, 48));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                g2.dispose();
            }
        };
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(28, 36, 24, 36));

        // Title
        JLabel title = new JLabel("¡Carta jugable!", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(new Color(200, 210, 255));
        root.add(title, BorderLayout.NORTH);

        // Card centered
        CardView cv = new CardView(card);
        JPanel cardPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        cardPanel.setOpaque(false);
        cardPanel.add(cv);
        root.add(cardPanel, BorderLayout.CENTER);

        // Buttons
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        btns.setOpaque(false);

        JButton yes = button("Jugar", new Color(40, 160, 80));
        JButton no  = button("Pasar", new Color(80, 80, 100));

        yes.addActionListener(e -> { playIt = true;  dispose(); });
        no.addActionListener(e ->  { playIt = false; dispose(); });

        btns.add(yes);
        btns.add(no);
        root.add(btns, BorderLayout.SOUTH);

        setContentPane(root);
        pack();
        setLocationRelativeTo(parent);
    }

    private JButton button(String text, Color bg) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? bg.brighter() : bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                    (getWidth() - fm.stringWidth(getText())) / 2f,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2f);
                g2.dispose();
            }
        };
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setPreferredSize(new Dimension(110, 36));
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    /** Shows the dialog and returns true if the player wants to play the card. */
    public static boolean show(JFrame parent, Card card) {
        DrawnCardDialog dlg = new DrawnCardDialog(parent, card);
        dlg.setVisible(true);
        return dlg.playIt;
    }
}
