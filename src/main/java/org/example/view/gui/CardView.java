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

        float cx = w / 2.0f, cy = h / 2.0f;
        Value v = card.getValue();
        if (v == Value.SKIP) {
            drawSkipSymbol(g2, cx, cy, w * 0.28f, base);
        } else if (v == Value.REVERSE) {
            drawReverseSymbol(g2, cx, cy, w * 0.30f, base);
        } else {
            String lbl = centerLabel(v);
            int fontSize = lbl.length() >= 4 ? 11 : (lbl.length() == 3 ? 15 : 30);
            g2.setFont(new Font("SansSerif", Font.BOLD, fontSize));
            FontMetrics fm = g2.getFontMetrics();
            float tx = (w - fm.stringWidth(lbl)) / 2.0f;
            float ty = cy + fm.getAscent() / 2.0f - fm.getDescent();
            g2.setColor(base);
            g2.drawString(lbl, tx, ty);
        }
    }

    /** Circle with a diagonal slash — the UNO skip symbol. */
    private void drawSkipSymbol(Graphics2D g2, float cx, float cy, float r, Color color) {
        float sw = Math.max(2f, r * 0.20f);
        g2.setColor(color);
        g2.setStroke(new BasicStroke(sw, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(new Ellipse2D.Float(cx - r, cy - r, r * 2, r * 2));
        // Diagonal from upper-right to lower-left (like ⊘)
        double rad = Math.toRadians(45);
        float dx = (float) (r * Math.cos(rad));
        float dy = (float) (r * Math.sin(rad));
        g2.draw(new Line2D.Float(cx + dx, cy - dy, cx - dx, cy + dy));
    }

    /**
     * Two curved arrows — UNO reverse symbol.
     *
     * Java2D Arc2D: 0°=right(3h), 90°=down(6h), 180°=left(9h), 270°=up(12h).
     * Positive extent = clockwise on screen.
     * Clockwise tangent at angle θ: (-sinθ, cosθ).
     *
     * Top arrow : tail at 9 o'clock (180°), clockwise 150° → tip at 1:30 (330°).
     * Bottom arrow: tail at 3 o'clock (0°),  clockwise 150° → tip at 7:30 (150°).
     * 30° gap on each side, matching the classic UNO reverse card.
     */
    private void drawReverseSymbol(Graphics2D g2, float cx, float cy, float r, Color color) {
        float sw   = Math.max(2f, r * 0.20f);
        float ahSz = r < 9f ? r * 0.72f : Math.max(sw * 2.8f, r * 0.32f);
        g2.setColor(color);
        g2.setStroke(new BasicStroke(sw, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // Top arc: 180° → 330° (150° clockwise, through 12 o'clock). Arrowhead at 330°.
        g2.draw(new Arc2D.Float(cx - r, cy - r, r * 2, r * 2, 180, 150, Arc2D.OPEN));
        float t1 = (float) Math.toRadians(330);
        drawArrowhead(g2,
            cx + r * (float) Math.cos(t1), cy + r * (float) Math.sin(t1),
            -(float) Math.sin(t1), (float) Math.cos(t1),
            ahSz, color);

        // Bottom arc: 0° → 150° (150° clockwise, through 6 o'clock). Arrowhead at 150°.
        g2.draw(new Arc2D.Float(cx - r, cy - r, r * 2, r * 2, 0, 150, Arc2D.OPEN));
        float t2 = (float) Math.toRadians(150);
        drawArrowhead(g2,
            cx + r * (float) Math.cos(t2), cy + r * (float) Math.sin(t2),
            -(float) Math.sin(t2), (float) Math.cos(t2),
            ahSz, color);
    }

    private void drawArrowhead(Graphics2D g2, float tipX, float tipY,
                               float dx, float dy, float size, Color color) {
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len < 1e-4f) return;
        dx /= len; dy /= len;
        float hw = size * 0.52f;
        float bx = tipX - dx * size, by = tipY - dy * size;
        float[] xs = { tipX, bx - dy * hw, bx + dy * hw };
        float[] ys = { tipY, by + dx * hw, by - dx * hw };
        GeneralPath tri = new GeneralPath();
        tri.moveTo(xs[0], ys[0]);
        tri.lineTo(xs[1], ys[1]);
        tri.lineTo(xs[2], ys[2]);
        tri.closePath();
        Stroke prev = g2.getStroke();
        g2.setStroke(new BasicStroke(1));
        g2.setColor(color);
        g2.fill(tri);
        g2.setStroke(prev);
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
        Value v = card.getValue();
        if (v == Value.SKIP || v == Value.REVERSE) {
            float r = (v == Value.SKIP) ? 5.5f : 5.2f;
            Color shadow = new Color(0, 0, 0, 90);
            // top-left
            paintCornerSymbol(g2, v, 8.5f, 9.5f, r, shadow);
            paintCornerSymbol(g2, v, 7.5f, 8.5f, r, Color.WHITE);
            // bottom-right (rotated 180°)
            Graphics2D gc = copy(g2);
            gc.rotate(Math.PI, w / 2.0, h / 2.0);
            paintCornerSymbol(gc, v, 8.5f, 9.5f, r, shadow);
            paintCornerSymbol(gc, v, 7.5f, 8.5f, r, Color.WHITE);
            gc.dispose();
        } else {
            String s = shortLabel(v);
            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
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
    }

    private void paintCornerSymbol(Graphics2D g2, Value v, float cx, float cy, float r, Color color) {
        if (v == Value.SKIP)    drawSkipSymbol(g2, cx, cy, r, color);
        else                    drawReverseSymbol(g2, cx, cy, r, color);
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
