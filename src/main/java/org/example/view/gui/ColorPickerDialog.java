package org.example.view.gui;

import org.example.model.Color;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

/**
 * Modal dialog shown after playing a wild card.
 * Shows four big colored buttons; user clicks one to choose the new color.
 */
public class ColorPickerDialog extends JDialog {

    private Color chosen = Color.RED;

    public ColorPickerDialog(JFrame parent) {
        super(parent, "Elige un color", true);
        setUndecorated(true);
        setBackground(new java.awt.Color(0, 0, 0, 0));

        JPanel root = new JPanel(new BorderLayout(0, 14)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new java.awt.Color(18, 18, 32, 240));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                g2.dispose();
            }
        };
        root.setOpaque(false);
        root.setBorder(BorderFactory.createEmptyBorder(24, 30, 24, 30));

        JLabel title = new JLabel("Elige el color", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(java.awt.Color.WHITE);
        root.add(title, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2, 2, 12, 12));
        grid.setOpaque(false);

        grid.add(colorButton(Color.RED,    "ROJO",     new java.awt.Color(210, 35, 35)));
        grid.add(colorButton(Color.BLUE,   "AZUL",     new java.awt.Color(28, 105, 215)));
        grid.add(colorButton(Color.YELLOW, "AMARILLO", new java.awt.Color(238, 193, 0)));
        grid.add(colorButton(Color.GREEN,  "VERDE",    new java.awt.Color(22, 163, 42)));

        root.add(grid, BorderLayout.CENTER);
        setContentPane(root);
        pack();
        setSize(320, 240);
        setLocationRelativeTo(parent);
    }

    private JButton colorButton(Color gameColor, String label, java.awt.Color bg) {
        JButton btn = new JButton(label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean hovering = getModel().isRollover();
                java.awt.Color fill = hovering ? bg.brighter() : bg;
                g2.setColor(fill);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.setColor(java.awt.Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                    (getWidth() - fm.stringWidth(getText())) / 2.0f,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2.0f);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(110, 70); }
        };
        btn.setFont(new Font("SansSerif", Font.BOLD, 15));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> { chosen = gameColor; dispose(); });
        return btn;
    }

    /** Blocks until the user picks a color, then returns it. */
    public static Color show(JFrame parent) {
        ColorPickerDialog dlg = new ColorPickerDialog(parent);
        dlg.setVisible(true);
        return dlg.chosen;
    }
}
