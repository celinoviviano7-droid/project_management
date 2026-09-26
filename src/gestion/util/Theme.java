package gestion.util;

import gestion.model.Tache;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

public final class Theme {
    private Theme() {}

    // ── Backgrounds ──────────────────────────────────────────
    public static final Color BG_APP        = new Color(11, 11, 20);
    public static final Color BG_SIDEBAR    = new Color(14, 14, 25);
    public static final Color BG_TOPBAR     = new Color( 9,  9, 18);
    public static final Color BG_PANEL      = new Color(17, 17, 30);
    public static final Color BG_CARD       = new Color(22, 22, 38);
    public static final Color BG_CARD_HOVER = new Color(28, 28, 48);
    public static final Color BG_INPUT      = new Color(20, 20, 36);
    public static final Color BG_SELECTED   = new Color(35, 45, 95);
    public static final Color BG_ROW_ODD    = new Color(17, 17, 30);
    public static final Color BG_ROW_EVEN   = new Color(20, 20, 34);

    // ── Accents ──────────────────────────────────────────────
    public static final Color ACCENT        = new Color(99, 120, 255);
    public static final Color ACCENT_HOVER  = new Color(119,138,255);
    public static final Color GREEN         = new Color(52, 199, 120);
    public static final Color ORANGE        = new Color(255,159,  48);
    public static final Color RED           = new Color(218,  68,  68);
    public static final Color PURPLE        = new Color(168,100, 255);
    public static final Color CYAN          = new Color( 56,189, 220);
    public static final Color GOLD          = new Color(255,196,  52);

    // ── Textes ───────────────────────────────────────────────
    public static final Color TEXT_PRIMARY   = new Color(228, 224, 248);
    public static final Color TEXT_SECONDARY = new Color(180, 175, 200); // Brightened from (145, 135, 172)
    public static final Color TEXT_MUTED     = new Color(120, 115, 145); // Brightened from (72, 68, 96)

    // ── Bordures ─────────────────────────────────────────────
    public static final Color BORDER         = new Color(34, 34, 58);
    public static final Color BORDER_LIGHT   = new Color(48, 48, 75);

    // ── Statuts ──────────────────────────────────────────────
    public static Color statutColor(Tache.Statut s) {
        return switch (s) {
            case TERMINE       -> GREEN;
            case EN_COURS      -> ACCENT;
            case EN_ATTENTE    -> ORANGE;
            case BLOQUE        -> RED;
            case NON_COMMENCE  -> new Color(85, 82, 115);
        };
    }

    public static Color prioriteColor(Tache.Priorite p) {
        return switch (p) {
            case CRITIQUE -> RED;
            case HAUTE    -> ORANGE;
            case NORMALE  -> ACCENT;
            case BASSE    -> GREEN;
        };
    }

    // ── Polices ──────────────────────────────────────────────
    public static Font font(int style, int size) {
        return new Font("Segoe UI", style, size);
    }
    public static final Font F_TITLE    = font(Font.BOLD,   21);
    public static final Font F_SUBTITLE = font(Font.BOLD,   14);
    public static final Font F_BODY     = font(Font.PLAIN,  13);
    public static final Font F_SMALL    = font(Font.PLAIN,  12);
    public static final Font F_TINY     = font(Font.PLAIN,  11);
    public static final Font F_LABEL    = font(Font.BOLD,   11);

    // ── Espacements ──────────────────────────────────────────
    public static final int GAP_XS = 4;
    public static final int GAP_SM = 8;
    public static final int GAP_MD = 14;
    public static final int GAP_LG = 22;
    public static final int GAP_XL = 32;

    // ── Rayons ───────────────────────────────────────────────
    public static final int R_SM = 6;
    public static final int R_MD = 10;
    public static final int R_LG = 16;

    // ── Helpers champs ───────────────────────────────────────
    public static void applyTextField(JTextField tf) {
        tf.setBackground(BG_INPUT);
        tf.setForeground(TEXT_PRIMARY);
        tf.setCaretColor(TEXT_PRIMARY);
        tf.setFont(F_BODY);
        tf.setBorder(new CompoundBorder(
            new LineBorder(BORDER, 1),
            new EmptyBorder(7, 10, 7, 10)));
        tf.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                tf.setBorder(new CompoundBorder(new LineBorder(ACCENT,1), new EmptyBorder(7,10,7,10)));
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                tf.setBorder(new CompoundBorder(new LineBorder(BORDER,1), new EmptyBorder(7,10,7,10)));
            }
        });
    }

    public static void applyTextArea(JTextArea ta) {
        ta.setBackground(BG_INPUT);
        ta.setForeground(TEXT_PRIMARY);
        ta.setCaretColor(TEXT_PRIMARY);
        ta.setFont(F_BODY);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setBorder(new EmptyBorder(7, 10, 7, 10));
    }

    public static void applyCombo(JComboBox<?> cb) {
        cb.setBackground(BG_INPUT);
        cb.setForeground(TEXT_PRIMARY);
        cb.setFont(F_BODY);
    }
}
