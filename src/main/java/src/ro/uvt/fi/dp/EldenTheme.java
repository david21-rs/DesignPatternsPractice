package src.ro.uvt.fi.dp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import javax.swing.Timer;

/// all the visual stuff shared between both guis
/// colours, fonts, reusable components - keeps things consistent
public class EldenTheme {

    // ── palette ──────────────────────────────────────────────────────────
    public static final Color BG       = new Color(7,  5,  3);
    public static final Color PANEL    = new Color(16, 12,  7);
    public static final Color CARD     = new Color(26, 20, 11);
    public static final Color GOLD     = new Color(190, 148, 52);
    public static final Color GOLD_HI  = new Color(228, 192, 98);
    public static final Color GOLD_LO  = new Color(88,  67, 15);
    public static final Color PARCH    = new Color(205, 182, 138);
    public static final Color PARCH_D  = new Color(118, 100, 66);
    public static final Color BLOOD    = new Color(125, 18, 18);
    public static final Color FOREST   = new Color(30,  80, 30);
    public static final Color ARCANE   = new Color(65,  30, 110);
    public static final Color ASH      = new Color(50,  42, 30);

    // ── font - tries palatino first, falls back to georgia/serif ─────────
    private static final String[] SERIFS = {
            "Palatino Linotype", "Palatino", "Book Antiqua", "Georgia", "Serif"
    };
    public static Font serif(int style, float size) {
        for (String name : SERIFS) {
            Font f = new Font(name, Font.PLAIN, 12);
            if (!f.getFamily().equalsIgnoreCase("dialog"))
                return f.deriveFont(style, size);
        }
        return new Font("Serif", style, (int) size);
    }

    // ── paint utilities ───────────────────────────────────────────────────

    /// vignette darkens the edges - pretty standard cinematic effect
    public static void paintVignette(Graphics2D g2, int w, int h) {
        g2.setPaint(new RadialGradientPaint(
                new Point2D.Float(w / 2f, h / 2f),
                Math.max(w, h) * 0.72f,
                new float[]{ 0f, 1f },
                new Color[]{ new Color(0,0,0,0), new Color(0,0,0,160) }
        ));
        g2.fillRect(0, 0, w, h);
    }

    /// L-shaped corner ornaments - the little decorative brackets you see in the UI
    public static void paintCorners(Graphics2D g2, int x, int y, int w, int h, Color c, int arm) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(c);
        // top-left
        g2.drawLine(x, y+arm, x, y);  g2.drawLine(x, y, x+arm, y);
        // top-right
        g2.drawLine(x+w-arm, y, x+w, y);  g2.drawLine(x+w, y, x+w, y+arm);
        // bottom-left
        g2.drawLine(x, y+h-arm, x, y+h);  g2.drawLine(x, y+h, x+arm, y+h);
        // bottom-right
        g2.drawLine(x+w-arm, y+h, x+w, y+h);  g2.drawLine(x+w, y+h, x+w, y+h-arm);
    }

    /// filled dark panel with gold border and corner ornaments
    public static void paintGoldPanel(Graphics2D g2, int x, int y, int w, int h) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(CARD);
        g2.fillRect(x, y, w, h);
        g2.setStroke(new BasicStroke(1f));
        g2.setColor(new Color(GOLD.getRed(), GOLD.getGreen(), GOLD.getBlue(), 85));
        g2.drawRect(x, y, w, h);
        paintCorners(g2, x, y, w, h, GOLD, 14);
    }

    /// horizontal divider with a little diamond in the middle, fades out to edges
    public static void paintSep(Graphics2D g2, int x, int y, int w) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int mid = x + w / 2;
        g2.setStroke(new BasicStroke(1f));
        g2.setPaint(new GradientPaint(x, y,
                new Color(GOLD.getRed(), GOLD.getGreen(), GOLD.getBlue(), 0), mid, y,
                new Color(GOLD.getRed(), GOLD.getGreen(), GOLD.getBlue(), 155)));
        g2.drawLine(x, y, mid, y);
        g2.setPaint(new GradientPaint(mid, y,
                new Color(GOLD.getRed(), GOLD.getGreen(), GOLD.getBlue(), 155), x+w, y,
                new Color(GOLD.getRed(), GOLD.getGreen(), GOLD.getBlue(), 0)));
        g2.drawLine(mid, y, x+w, y);
        g2.setColor(GOLD_HI);
        g2.fillPolygon(new int[]{mid, mid+4, mid, mid-4}, new int[]{y-3, y, y+3, y}, 4);
    }

    static Color lerp(Color a, Color b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        return new Color(
                (int)(a.getRed()   + (b.getRed()   - a.getRed())   * t),
                (int)(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                (int)(a.getBlue()  + (b.getBlue()  - a.getBlue())  * t)
        );
    }

    //  REUSABLE COMPONENTS

    /// the animated panel that both guis use as their root
    /// draws dark bg + upward drifting gold sparks (like grace pillars in the game)
    public static class EldenBackground extends JPanel {

        static class Spark {
            float x, y, vx, vy, life, max, size;

            void reset(int pw, int ph) {
                x    = (float)(Math.random() * pw);
                y    = ph + 4;
                vx   = (float)(Math.random() * 0.5 - 0.25);
                vy   = (float)(-(Math.random() * 0.9 + 0.35));
                max  = (float)(90  + Math.random() * 130);
                life = (float)(Math.random() * max);   /// staggered so they dont all start at once
                size = (float)(1.0 + Math.random() * 1.8);
            }

            void tick() {
                x   += vx;
                y   += vy;
                life--;
                vx  += (float)((Math.random() - 0.5) * 0.04);  /// slight wobble
            }

            float alpha() {
                float r = life / max;
                if (r > 0.85f) return (1f - r) / 0.15f;
                if (r < 0.25f) return r / 0.25f;
                return 1f;
            }

            boolean dead() { return life <= 0 || y < -8; }
        }

        private final List<Spark> sparks = new ArrayList<>();

        public EldenBackground() {
            setBackground(BG);
            setOpaque(true);
            for (int i = 0; i < 40; i++) sparks.add(new Spark());

            /// ~30fps is plenty for particles - 60fps would just waste cpu
            new Timer(33, e -> {
                int pw = getWidth(), ph = getHeight();
                if (pw < 1 || ph < 1) return;
                for (Spark s : sparks) {
                    s.tick();
                    if (s.dead()) s.reset(pw, ph);
                }
                repaint();
            }).start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);   /// fills the BG color we set
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            paintVignette(g2, getWidth(), getHeight());
            for (Spark s : sparks) {
                float a = s.alpha();
                if (a < 0.02f) continue;
                g2.setColor(new Color(GOLD.getRed(), GOLD.getGreen(), GOLD.getBlue(), (int)(a * 215)));
                int sz = Math.max(1, (int) s.size);
                g2.fillOval((int) s.x, (int) s.y, sz, sz);
                /// faint halo on brighter sparks
                if (a > 0.55f) {
                    g2.setColor(new Color(255, 220, 130, (int)(a * 50)));
                    g2.fillOval((int) s.x - 1, (int) s.y - 1, sz + 2, sz + 2);
                }
            }
            g2.dispose();
        }
    }

    /// custom button - dark tinted bg, gold border, L-corners appear on hover
    /// the whole hover effect is driven by a timer so its smooth not snappy
    public static class EldenButton extends JButton {
        private final Color accent;
        private float hover = 0f;
        private Timer hvTimer;

        public EldenButton(String label, Color accent) {
            super(label);
            this.accent = accent;
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setFont(serif(Font.BOLD, 11f));
            setForeground(PARCH_D);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { anim(true);  }
                @Override public void mouseExited (MouseEvent e) { anim(false); }
            });
        }

        private void anim(boolean in) {
            if (hvTimer != null) hvTimer.stop();
            hvTimer = new Timer(14, e -> {
                hover = Math.max(0f, Math.min(1f, hover + (in ? 0.1f : -0.1f)));
                setForeground(lerp(PARCH_D, GOLD_HI, hover));
                repaint();
                if ((in && hover >= 1f) || (!in && hover <= 0f)) ((Timer) e.getSource()).stop();
            });
            hvTimer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth() - 1, h = getHeight() - 1;

            /// bg darkens/brightens based on hover progress
            float brt = 0.10f + hover * 0.12f;
            g2.setColor(new Color(
                    Math.min(255, (int)(accent.getRed()   * brt)),
                    Math.min(255, (int)(accent.getGreen() * brt)),
                    Math.min(255, (int)(accent.getBlue()  * brt))
            ));
            g2.fillRect(0, 0, w, h);

            /// border goes from dim to bright gold as you hover
            g2.setColor(lerp(
                    new Color(GOLD_LO.getRed(), GOLD_LO.getGreen(), GOLD_LO.getBlue(), 120),
                    GOLD_HI, hover
            ));
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRect(0, 0, w, h);

            /// L-corners fade in with hover
            if (hover > 0.05f) {
                int ci = (int)(hover * 220);
                g2.setColor(new Color(GOLD_HI.getRed(), GOLD_HI.getGreen(), GOLD_HI.getBlue(), ci));
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int s = 7;
                g2.drawLine(0,s,0,0);     g2.drawLine(0,0,s,0);
                g2.drawLine(w-s,0,w,0);   g2.drawLine(w,0,w,s);
                g2.drawLine(0,h-s,0,h);   g2.drawLine(0,h,s,h);
                g2.drawLine(w-s,h,w,h);   g2.drawLine(w,h,w,h-s);
            }
            g2.dispose();
            super.paintComponent(g);   /// jbutton draws the text on top
        }
    }

    /// label that smoothly counts to its new value - ease-out cubic so it decelerates nicely
    public static class AnimLabel extends JLabel {
        private double cur = 0, tgt = 0;
        private Timer anim;

        public AnimLabel() {
            setFont(serif(Font.BOLD, 22f));
            setForeground(GOLD_HI);
            setHorizontalAlignment(CENTER);
            setText("—");
        }

        public void animateTo(double v, String suffix) {
            tgt = v;
            if (anim != null) anim.stop();
            double start = cur;
            long[] t0 = { System.currentTimeMillis() };
            anim = new Timer(16, e -> {
                double p = Math.min(1.0, (System.currentTimeMillis() - t0[0]) / 650.0);
                p = 1 - Math.pow(1 - p, 3);   /// cubic ease-out
                cur = start + (tgt - start) * p;
                setText(String.format("%.2f  %s", cur, suffix));
                if (p >= 1.0) ((Timer) e.getSource()).stop();
            });
            anim.start();
        }

        public void setInstant(double v, String suffix) {
            if (anim != null) anim.stop();
            cur = tgt = v;
            setText(String.format("%.2f  %s", v, suffix));
        }
    }

    //  factory helpers for standard components

    public static JTextField styledField(int cols) {
        JTextField f = new JTextField(cols);
        f.setBackground(new Color(12, 9, 5));
        f.setForeground(PARCH);
        f.setCaretColor(GOLD);
        f.setFont(serif(Font.PLAIN, 14f));
        f.setSelectionColor(new Color(GOLD.getRed(), GOLD.getGreen(), GOLD.getBlue(), 85));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GOLD_LO, 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        return f;
    }

    public static JComboBox<String> styledCombo() {
        JComboBox<String> cb = new JComboBox<>();
        cb.setBackground(new Color(12, 9, 5));
        cb.setForeground(PARCH);
        cb.setFont(serif(Font.PLAIN, 13f));
        cb.setBorder(BorderFactory.createLineBorder(GOLD_LO, 1));
        cb.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> l, Object v,
                                                          int i, boolean sel, boolean foc) {
                super.getListCellRendererComponent(l, v, i, sel, foc);
                setBackground(sel ? new Color(36, 26, 9) : new Color(12, 9, 5));
                setForeground(sel ? GOLD_HI : PARCH);
                setFont(serif(Font.PLAIN, 13f));
                setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
                return this;
            }
        });
        return cb;
    }

    /// call this once at app start so JOptionPane dialogs also look dark
    public static void styleDialogs() {
        UIManager.put("OptionPane.background",        PANEL);
        UIManager.put("Panel.background",             PANEL);
        UIManager.put("OptionPane.messageForeground", PARCH);
        UIManager.put("OptionPane.buttonAreaBorder",  BorderFactory.createEmptyBorder(6,0,6,0));
        UIManager.put("Button.background",            CARD);
        UIManager.put("Button.foreground",            PARCH);
        UIManager.put("Button.border",
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(GOLD_LO, 1),
                        BorderFactory.createEmptyBorder(4, 12, 4, 12)
                )
        );
        UIManager.put("TextField.background",         new Color(12, 9, 5));
        UIManager.put("TextField.foreground",         PARCH);
        UIManager.put("TextField.caretForeground",    GOLD);
        UIManager.put("Label.foreground",             PARCH);
        UIManager.put("ComboBox.background",          new Color(12, 9, 5));
        UIManager.put("ComboBox.foreground",          PARCH);
    }
}