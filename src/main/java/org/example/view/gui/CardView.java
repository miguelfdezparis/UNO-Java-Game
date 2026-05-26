package org.example.view.gui;

import org.example.model.*;
import javax.swing.*;
import java.awt.Color;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

/**
 * Renders a single UNO card using Java2D.
 * Supports full-size (hand), small (opponents) and face-down modes.
 */
public class CardView extends JPanel {

    public static final int CARD_W = 82;
    public static final int CARD_H = 118;
    private static final int SMALL_W = 36;
    private static final int SMALL_H = 52;
    private static final int ARC = 12;

    private final Card card;
    private final boolean small;
    private boolean faceDown;
    private boolean playable;
    private boolean hovered;

    public CardView(Card card) {
        this(card, false, false);
    }

    public CardView(Card card, boolean faceDown) {
        this(card, faceDown, false);
    }

    public static CardView smallBack() {
        return new CardView(null, true, true);
    }

    private CardView(Card card, boolean faceDown, boolean small) {
        this.card = card;
        this.faceDown = faceDown;
        this.small = small;
        int w = small ? SMALL_W : CARD_W;
        int h = small ? SMALL_H : CARD_H;
        setPreferredSize(new Dimension(w, h));
        setMinimumSize(new Dimension(w, h));
        setMaximumSize(new Dimension(w, h));
        setOpaque(false);

        if (!small && !faceDown) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                @Override public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
            });
        }
    }

    public Card getCard()              { return card; }
    public boolean isPlayable()        { return playable; }
    public void setPlayable(boolean p) { this.playable = p; repaint(); }
    public void setFaceDown(boolean f) { this.faceDown = f; repaint(); }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        hint(g2);
        int w = getWidth(), h = getHeight();
        if (faceDown || card == null) paintBack(g2, w, h);
        else                          paintFront(g2, w, h);
        g2.dispose();
    }

    // ─── Card back ────────────────────────────────────────────────────────────

    private void paintBack(Graphics2D g2, int w, int h) {
        shadow(g2, w, h);
        GradientPaint bg = new GradientPaint(0, 0, new Color(45, 68, 160), w, h, new Color(14, 22, 82));
        g2.setPaint(bg);
        g2.fill(rr(1, 1, w - 3, h - 3));
        g2.setColor(new Color(255, 255, 255, 210));
        g2.setStroke(new BasicStroke(1.8f));
        g2.draw(rr(2.5f, 2.5f, w - 6, h - 6));

        if (w < 44) return;

        Graphics2D gc = copy(g2);
        gc.rotate(Math.toRadians(-25), w / 2.0, h / 2.0);
        gc.setColor(new Color(205, 30, 30));
        gc.fill(new Ellipse2D.Float(w * 0.16f, h * 0.24f, w * 0.68f, h * 0.52f));
        int fs = Math.max(9, (int) (w * 0.22));
        gc.setFont(new Font("SansSerif", Font.BOLD | Font.ITALIC, fs));
        FontMetrics fm = gc.getFontMetrics();
        String uno = "UNO";
        gc.setColor(Color.WHITE);
        gc.drawString(uno, (w - fm.stringWidth(uno)) / 2.0f, h / 2.0f + fm.getAscent() / 2.0f - 2);
        gc.dispose();
    }

    // ─── Card front ───────────────────────────────────────────────────────────

    private void paintFront(Graphics2D g2, int w, int h) {
        Color base  = baseColor(card.getColor());
        Color light = blend(base, Color.WHITE, 0.38f);

        shadow(g2, w, h);
        g2.setPaint(new GradientPaint(0, 0, light, w, h, base));
        g2.fill(rr(1, 1, w - 3, h - 3));
        g2.setColor(new Color(255, 255, 255, 195));
        g2.setStroke(new BasicStroke(2.2f));
        g2.draw(rr(2.5f, 2.5f, w - 6, h - 6));

        if (card.getColor() == org.example.model.Color.BLACK) paintWildOval(g2, w, h);
        else                                                    paintColorOval(g2, w, h, base);

        paintCorners(g2, w, h);

        if (hovered) {
            g2.setColor(new Color(255, 255, 255, 38));
            g2.fill(rr(1, 1, w - 3, h - 3));
        }
        if (playable) {
            g2.setColor(new Color(60, 240, 60, 180));
            g2.setStroke(new BasicStroke(3.5f));
            g2.draw(rr(0.5f, 0.5f, w - 2, h - 2));
            // inner glow
            g2.setColor(new Color(60, 240, 60, 70));
            g2.setStroke(new BasicStroke(2f));
            g2.draw(rr(3f, 3f, w - 7, h - 7));
        }
    }

    private void paintColorOval(Graphics2D g2, int w, int h, Color base) {
        Graphics2D gc = copy(g2);
        gc.rotate(Math.toRadians(-25), w / 2.0, h / 2.0);
        gc.setColor(Color.WHITE);
        gc.fill(new Ellipse2D.Float(w * 0.09f, h * 0.12f, w * 0.82f, h * 0.76f));
        gc.dispose();

        String lbl = centerLabel(card.getValue());
        int fontSize = lbl.length() >= 4 ? 11 : (lbl.length() == 3 ? 15 : 30);
        g2.setFont(new Font("SansSerif", Font.BOLD, fontSize));
        FontMetrics fm = g2.getFontMetrics();
        float tx = (w - fm.stringWidth(lbl)) / 2.0f;
        float ty = h / 2.0f + fm.getAscent() / 2.0f - fm.getDescent();
        g2.setColor(base);
        g2.drawString(lbl, tx, ty);
    }

    private void paintWildOval(Graphics2D g2, int w, int h) {
        Graphics2D gc = copy(g2);
        gc.rotate(Math.toRadians(-25), w / 2.0, h / 2.0);
        float ox = w * 0.09f, oy = h * 0.12f, ow = w * 0.82f, oh = h * 0.76f;
        float cx = ox + ow / 2f, cy = oy + oh / 2f;
        Ellipse2D oval = new Ellipse2D.Float(ox, oy, ow, oh);
        gc.setClip(oval);
        gc.setColor(new Color(210, 35, 35));  gc.fillRect(0,     0,     (int) cx, (int) cy);
        gc.setColor(new Color(28, 105, 215)); gc.fillRect((int) cx, 0,  w,        (int) cy);
        gc.setColor(new Color(238, 193, 0));  gc.fillRect(0,     (int) cy, (int) cx, h);
        gc.setColor(new Color(22, 163, 42));  gc.fillRect((int) cx, (int) cy, w, h);
        gc.setClip(null);
        gc.dispose();

        String lbl = card.getValue() == Value.WILD_DRAW_FOUR ? "+4" : "W";
        g2.setFont(new Font("SansSerif", Font.BOLD, 24));
        FontMetrics fm = g2.getFontMetrics();
        float tx = (w - fm.stringWidth(lbl)) / 2.0f;
        float ty = h / 2.0f + fm.getAscent() / 2.0f - fm.getDescent();
        g2.setColor(new Color(0, 0, 0, 130)); g2.drawString(lbl, tx + 1, ty + 1);
        g2.setColor(Color.WHITE);             g2.drawString(lbl, tx, ty);
    }

    private void paintCorners(Graphics2D g2, int w, int h) {
        String s = shortLabel(card.getValue());
        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        FontMetrics fm = g2.getFontMetrics();

        // top-left
        g2.setColor(new Color(0, 0, 0, 90)); g2.drawString(s, 6, 15);
        g2.setColor(Color.WHITE);             g2.drawString(s, 5, 14);

        // bottom-right (rotated 180°)
        Graphics2D gc = copy(g2);
        gc.setFont(new Font("SansSerif", Font.BOLD, 11));
        gc.rotate(Math.PI, w / 2.0, h / 2.0);
        gc.setColor(new Color(0, 0, 0, 90)); gc.drawString(s, 6, 15);
        gc.setColor(Color.WHITE);             gc.drawString(s, 5, 14);
        gc.dispose();
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private void shadow(Graphics2D g2, int w, int h) {
        g2.setColor(new Color(0, 0, 0, 60));
        g2.fill(new RoundRectangle2D.Float(3, 5, w - 4, h - 4, ARC, ARC));
    }

    private RoundRectangle2D.Float rr(float x, float y, float w, float h) {
        return new RoundRectangle2D.Float(x, y, w, h, ARC, ARC);
    }

    private Graphics2D copy(Graphics2D g2) {
        Graphics2D c = (Graphics2D) g2.create();
        hint(c);
        return c;
    }

    private void hint(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,    RenderingHints.VALUE_STROKE_PURE);
    }

    private Color blend(Color a, Color b, float t) {
        return new Color(
            (int) (a.getRed()   * (1 - t) + b.getRed()   * t),
            (int) (a.getGreen() * (1 - t) + b.getGreen() * t),
            (int) (a.getBlue()  * (1 - t) + b.getBlue()  * t));
    }

    static Color baseColor(org.example.model.Color c) {
        return switch (c) {
            case RED    -> new Color(210, 35, 35);
            case BLUE   -> new Color(28, 105, 215);
            case GREEN  -> new Color(22, 163, 42);
            case YELLOW -> new Color(238, 193, 0);
            case BLACK  -> new Color(22, 22, 22);
        };
    }

    private String centerLabel(Value v) {
        return switch (v) {
            case ZERO -> "0"; case ONE -> "1"; case TWO -> "2";
            case THREE -> "3"; case FOUR -> "4"; case FIVE -> "5";
            case SIX -> "6"; case SEVEN -> "7"; case EIGHT -> "8";
            case NINE -> "9";
            case SKIP           -> "SKIP";
            case REVERSE        -> "REV";
            case DRAW_TWO       -> "+2";
            case WILD           -> "W";
            case WILD_DRAW_FOUR -> "+4";
        };
    }

    private String shortLabel(Value v) {
        return switch (v) {
            case ZERO -> "0"; case ONE -> "1"; case TWO -> "2";
            case THREE -> "3"; case FOUR -> "4"; case FIVE -> "5";
            case SIX -> "6"; case SEVEN -> "7"; case EIGHT -> "8";
            case NINE -> "9";
            case SKIP           -> "S";
            case REVERSE        -> "R";
            case DRAW_TWO       -> "+2";
            case WILD           -> "W";
            case WILD_DRAW_FOUR -> "+4";
        };
    }
}
