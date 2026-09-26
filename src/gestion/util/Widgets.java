package gestion.util;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import com.formdev.flatlaf.extras.FlatSVGIcon;

public final class Widgets {
    private Widgets() {}

    /**
     * Crée un FlatSVGIcon en injectant le ClassLoader de Widgets.
     * Cela fonctionne à la fois en développement et dans un Fat JAR.
     * @param iconPath chemin absolu de l'icône (ex: "/resources/icons/plus.svg")
     */
    public static FlatSVGIcon svg(String iconPath) {
        // ClassLoader.getResource() ne prend PAS de slash initial
        String path = iconPath.startsWith("/") ? iconPath.substring(1) : iconPath;
        return new FlatSVGIcon(path, 1.0f, Widgets.class.getClassLoader());
    }

    public static FlatSVGIcon svg(String iconPath, int w, int h) {
        String path = iconPath.startsWith("/") ? iconPath.substring(1) : iconPath;
        float scale = (float) w / 16.0f;
        return new FlatSVGIcon(path, scale, Widgets.class.getClassLoader());
    }

    // ════════════════════════════════════════════════════════
    //  Panneau coins arrondis
    // ════════════════════════════════════════════════════════
    public static class RoundPanel extends JPanel {
        private final int r;
        private Color border;
        public RoundPanel(int r)              { this(r, null); }
        public RoundPanel(int r, Color border){ super(); this.r=r; this.border=border; setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = prep(g);
            g2.setColor(getBackground()); g2.fillRoundRect(0,0,getWidth()-1,getHeight()-1,r,r);
            if (border!=null) { g2.setColor(border); g2.setStroke(new BasicStroke(1f)); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,r,r); }
            g2.dispose();
        }
        private Graphics2D prep(Graphics g) {
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            return g2;
        }
    }

    // ════════════════════════════════════════════════════════
    //  Bouton plat
    // ════════════════════════════════════════════════════════
    public static class FlatButton extends JButton {
        private Color normalBg, hoverBg;
        private boolean isOutline;
        private Color outlineColor;

        public FlatButton(String text, Color bg) {
            super(text);
            this.normalBg = bg;
            this.hoverBg  = blend(bg, Color.WHITE, 0.12f);
            style();
        }

        public static FlatButton outline(String text, Color c) {
            FlatButton b = new FlatButton(text, new Color(0,0,0,0));
            b.isOutline    = true;
            b.outlineColor = c;
            b.normalBg     = new Color(0,0,0,0);
            b.hoverBg      = new Color(c.getRed(),c.getGreen(),c.getBlue(),22);
            b.setForeground(c);
            return b;
        }

        private void style() {
            setBackground(normalBg); setForeground(Color.WHITE);
            setFont(Theme.F_SMALL); setFocusPainted(false); setBorderPainted(false);
            setContentAreaFilled(false); setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(8, 16, 8, 16));
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { setBackground(hoverBg); repaint(); }
                public void mouseExited(MouseEvent e)  { setBackground(normalBg); repaint(); }
            });
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground()); g2.fillRoundRect(0,0,getWidth(),getHeight(),Theme.R_SM,Theme.R_SM);
            if (isOutline) { g2.setColor(outlineColor); g2.setStroke(new BasicStroke(1.2f)); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,Theme.R_SM,Theme.R_SM); }
            g2.dispose(); super.paintComponent(g);
        }

        public static FlatButton icon(com.formdev.flatlaf.extras.FlatSVGIcon icon) {
            FlatButton b = new FlatButton(null, new Color(0,0,0,0));
            b.setIcon(icon);
            b.setBorder(new EmptyBorder(4, 4, 4, 4));
            b.hoverBg = new Color(255,255,255,15);
            return b;
        }

        private static Color blend(Color a, Color b, float t) {
            return new Color(
                Math.min(255,(int)(a.getRed()  *(1-t)+b.getRed()  *t)),
                Math.min(255,(int)(a.getGreen()*(1-t)+b.getGreen()*t)),
                Math.min(255,(int)(a.getBlue() *(1-t)+b.getBlue() *t)));
        }
    }

    // ════════════════════════════════════════════════════════
    //  Badge coloré
    // ════════════════════════════════════════════════════════
    public static class Badge extends JLabel {
        private final Color bg;
        public Badge(String text, Color bg) {
            super(text, CENTER);
            this.bg = bg;
            setOpaque(false); setFont(Theme.F_TINY); setForeground(bg);
            setBorder(new EmptyBorder(2,8,2,8));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(bg.getRed(),bg.getGreen(),bg.getBlue(),30));
            g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
            g2.setColor(new Color(bg.getRed(),bg.getGreen(),bg.getBlue(),100));
            g2.setStroke(new BasicStroke(1f)); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,12,12);
            g2.dispose(); super.paintComponent(g);
        }
    }

    // ════════════════════════════════════════════════════════
    //  Avatar
    // ════════════════════════════════════════════════════════
    public static class Avatar extends JPanel {
        private final String init;
        private final Color color;
        private final int size;
        private boolean dot; private Color dotColor;

        public Avatar(String init, Color color, int size) {
            this.init=init; this.color=color; this.size=size;
            setPreferredSize(new Dimension(size,size)); setOpaque(false);
        }
        public void setDot(Color c) { dot=true; dotColor=c; }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(color.getRed(),color.getGreen(),color.getBlue(),45));
            g2.fillOval(0,0,size,size);
            g2.setColor(new Color(color.getRed(),color.getGreen(),color.getBlue(),130));
            g2.setStroke(new BasicStroke(1.4f)); g2.drawOval(0,0,size-1,size-1);
            g2.setFont(Theme.font(Font.BOLD, Math.max(8,size/3)));
            g2.setColor(color.brighter());
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(init, (size-fm.stringWidth(init))/2, (size+fm.getAscent()-fm.getDescent())/2);
            if (dot) {
                int ds=Math.max(6,size/5);
                g2.setColor(dotColor); g2.fillOval(size-ds,size-ds,ds,ds);
                g2.setColor(Theme.BG_PANEL); g2.setStroke(new BasicStroke(1.5f)); g2.drawOval(size-ds,size-ds,ds,ds);
            }
            g2.dispose();
        }
    }

    // ════════════════════════════════════════════════════════
    //  Barre de progression
    // ════════════════════════════════════════════════════════
    public static class ProgressBar extends JComponent {
        private int value;
        private final Color barColor;
        private boolean showLabel = true;

        public ProgressBar(int value, Color color) {
            this.value=value; this.barColor=color;
            setPreferredSize(new Dimension(100,18));
        }
        public void setValue(int v){ this.value=Math.max(0,Math.min(100,v)); repaint(); }
        public void setShowLabel(boolean b){ showLabel=b; }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2=(Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            int h=5, y=(getHeight()-h)/2;
            g2.setColor(new Color(45,45,75)); g2.fillRoundRect(0,y,getWidth(),h,h,h);
            if (value>0) {
                int w = Math.max(h,(int)((getWidth()-1)*value/100.0));
                g2.setColor(barColor); g2.fillRoundRect(0,y,w,h,h,h);
            }
            if (showLabel) {
                g2.setFont(Theme.F_TINY); g2.setColor(Theme.TEXT_SECONDARY);
                String lbl=value+"%";
                FontMetrics fm=g2.getFontMetrics();
                g2.drawString(lbl, getWidth()-fm.stringWidth(lbl), y+h+fm.getAscent());
            }
            g2.dispose();
        }
    }

    // ════════════════════════════════════════════════════════
    //  Toast notification
    // ════════════════════════════════════════════════════════
    public enum ToastType { SUCCESS, INFO, WARNING, ERROR }

    public static class Toast extends JWindow {
        private static final int W=310, H=50;
        private float alpha=0f;
        private Timer fadeIn, fadeOut, hideTimer;

        public Toast(Frame parent, String msg, ToastType type) {
            super(parent);
            setSize(W,H);
            Color accent = switch(type){
                case SUCCESS->Theme.GREEN; case INFO->Theme.ACCENT;
                case WARNING->Theme.ORANGE; case ERROR->Theme.RED;
            };
            String icon = switch(type){
                case SUCCESS->"✓ "; case INFO->"ℹ "; case WARNING->"⚠ "; case ERROR->"✕ ";
            };
            JPanel p = new JPanel(new BorderLayout(8,0)){
                @Override protected void paintComponent(Graphics g){
                    Graphics2D g2=(Graphics2D)g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(24,24,42,235)); g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                    g2.setColor(accent); g2.setStroke(new BasicStroke(1.5f)); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,10,10);
                    g2.fillRoundRect(0,0,4,getHeight(),4,4);
                    g2.dispose();
                }
            };
            p.setOpaque(false); p.setBorder(new EmptyBorder(0,14,0,12));
            JLabel ic=new JLabel(icon); ic.setFont(Theme.font(Font.BOLD,13)); ic.setForeground(accent); ic.setPreferredSize(new Dimension(20,H));
            JLabel ml=new JLabel("<html><body style='width:210px'>"+msg+"</body></html>"); ml.setFont(Theme.F_SMALL); ml.setForeground(Theme.TEXT_PRIMARY);
            p.add(ic,BorderLayout.WEST); p.add(ml,BorderLayout.CENTER);
            setContentPane(p); setBackground(new Color(0,0,0,0));
        }

        public void showToast(Frame parent, int yOff){
            Point loc=parent.getLocation(); Dimension sz=parent.getSize();
            setLocation(loc.x+sz.width-W-20, loc.y+sz.height-H-20-yOff);
            setVisible(true);
            fadeIn=new Timer(15,null);
            fadeIn.addActionListener(e->{ alpha=Math.min(1f,alpha+0.1f); setOpacity(alpha); if(alpha>=1f)fadeIn.stop(); });
            fadeIn.start();
            hideTimer=new Timer(3000,e->fadeOut());
            hideTimer.setRepeats(false); hideTimer.start();
        }

        private void fadeOut(){
            if(fadeIn!=null)fadeIn.stop();
            fadeOut=new Timer(15,null);
            fadeOut.addActionListener(e->{ alpha=Math.max(0f,alpha-0.08f); setOpacity(alpha); if(alpha<=0f){fadeOut.stop();dispose();} });
            fadeOut.start();
        }
    }

    // ════════════════════════════════════════════════════════
    //  ScrollPane stylé
    // ════════════════════════════════════════════════════════
    public static JScrollPane scroll(Component c) {
        JScrollPane sp=new JScrollPane(c);
        sp.setBorder(null);
        sp.getViewport().setBackground(Theme.BG_PANEL);
        // Des barres plus larges (11px) pour être facilement cliquables
        sp.getVerticalScrollBar().setPreferredSize(new Dimension(11,0));
        sp.getHorizontalScrollBar().setPreferredSize(new Dimension(0,11));
        // Vitesse de défilement améliorée (molette de souris fluide)
        sp.getVerticalScrollBar().setUnitIncrement(16);
        sp.getHorizontalScrollBar().setUnitIncrement(16);
        return sp;
    }

    // ════════════════════════════════════════════════════════
    //  Séparateur
    // ════════════════════════════════════════════════════════
    public static JPanel hSep() {
        JPanel s=new JPanel(); s.setBackground(Theme.BORDER);
        s.setMaximumSize(new Dimension(Integer.MAX_VALUE,1));
        s.setPreferredSize(new Dimension(0,1)); return s;
    }

    // ════════════════════════════════════════════════════════
    //  Label de section
    // ════════════════════════════════════════════════════════
    public static JLabel sectionLabel(String text) {
        JLabel l=new JLabel(text.toUpperCase());
        l.setFont(Theme.F_LABEL); l.setForeground(Theme.TEXT_MUTED);
        return l;
    }

    // ════════════════════════════════════════════════════════
    //  Champ texte stylé avec placeholder
    // ════════════════════════════════════════════════════════
    public static JTextField field(String placeholder) {
        JTextField tf=new JTextField();
        Theme.applyTextField(tf);
        if (!placeholder.isEmpty()) {
            tf.putClientProperty("JTextField.placeholderText", placeholder);
        }
        return tf;
    }
 
    public static JTextField searchField(String placeholder, java.util.function.Consumer<String> onSearch) {
        JTextField tf = new JTextField();
        tf.putClientProperty("JTextField.placeholderText", placeholder);
        tf.putClientProperty("JTextField.showClearButton", true);
        FlatSVGIcon icon = svg("/resources/icons/search.svg", 13, 13);
        icon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Theme.CYAN));
        tf.putClientProperty("JTextField.leadingIcon", icon);
        
        tf.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { onSearch.accept(tf.getText()); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { onSearch.accept(tf.getText()); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { onSearch.accept(tf.getText()); }
        });
        
        Theme.applyTextField(tf);
        tf.setPreferredSize(new Dimension(180, 28));
        return tf;
    }

    // ════════════════════════════════════════════════════════
    //  WrapLayout (Layout fluid qui passe à la ligne)
    // ════════════════════════════════════════════════════════
    public static class WrapLayout extends FlowLayout {
        public WrapLayout() { super(FlowLayout.LEFT, 10, 10); }
        public WrapLayout(int align, int h, int v) { super(align, h, v); }
        @Override public Dimension preferredLayoutSize(Container t) { return layout(t, true); }
        @Override public Dimension minimumLayoutSize(Container t) { return layout(t, false); }
        private Dimension layout(Container t, boolean pref) {
            synchronized (t.getTreeLock()) {
                int tw = t.getSize().width;
                if (tw == 0) tw = Integer.MAX_VALUE;
                Insets ins = t.getInsets();
                int max = tw - (ins.left + ins.right + getHgap() * 2);
                Dimension dim = new Dimension(0, 0);
                int rw = 0, rh = 0;
                for (int i = 0; i < t.getComponentCount(); i++) {
                    Component c = t.getComponent(i);
                    if (!c.isVisible()) continue;
                    Dimension d = pref ? c.getPreferredSize() : c.getMinimumSize();
                    if (rw + d.width > max) {
                        dim.width = Math.max(dim.width, rw);
                        dim.height += rh + getVgap();
                        rw = 0; rh = 0;
                    }
                    rw += d.width + getHgap();
                    rh = Math.max(rh, d.height);
                }
                dim.width = Math.max(dim.width, rw);
                dim.height += rh + ins.top + ins.bottom + getVgap() * 2;
                return dim;
            }
        }
    }

    // ════════════════════════════════════════════════════════
    //  AdaptivePanel (Change de layout selon la largeur)
    // ════════════════════════════════════════════════════════
    public static class AdaptivePanel extends JPanel {
        private final int breakpoint;
        private final LayoutManager smallLayout, largeLayout;

        public AdaptivePanel(int breakpoint, LayoutManager small, LayoutManager large) {
            this.breakpoint = breakpoint;
            this.smallLayout = small;
            this.largeLayout = large;
            setOpaque(false);
            addComponentListener(new ComponentAdapter() {
                @Override public void componentResized(ComponentEvent e) { updateLayout(); }
            });
        }

        private void updateLayout() {
            LayoutManager target = getWidth() < breakpoint ? smallLayout : largeLayout;
            if (getLayout() != target) {
                setLayout(target);
                revalidate();
            }
        }
    }
    // ════════════════════════════════════════════════════════
    //  DatePicker (Sélecteur de date moderne)
    // ════════════════════════════════════════════════════════
    public static class DatePicker extends JPanel {
        private final JTextField tf;
        private final JButton btn;
        private java.time.LocalDate current;
        private final java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");

        public DatePicker() { this(java.time.LocalDate.now()); }
        public DatePicker(java.time.LocalDate initial) {
            this.current = initial;
            setLayout(new BorderLayout());
            setOpaque(false);

            tf = new JTextField(current.format(fmt));
            Theme.applyTextField(tf);
            
            btn = new JButton();
            btn.setFocusPainted(false); btn.setBorderPainted(false); btn.setContentAreaFilled(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            FlatSVGIcon icon = svg("/resources/icons/edit.svg", 14, 14);
            icon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Theme.TEXT_SECONDARY));
            btn.setIcon(icon);
            btn.setBorder(new EmptyBorder(0, 8, 0, 8));

            btn.addActionListener(e -> showPopup());
            
            add(tf, BorderLayout.CENTER);
            add(btn, BorderLayout.EAST);
            
            // Sync manuelle si l'utilisateur tape une date
            tf.addFocusListener(new FocusAdapter() {
                @Override public void focusLost(FocusEvent e) {
                    try { current = java.time.LocalDate.parse(tf.getText().trim(), fmt); } catch(Exception ignored){}
                }
            });
        }

        @Override public void setEnabled(boolean b) {
            super.setEnabled(b);
            tf.setEnabled(b);
            btn.setEnabled(b);
        }

        public String getText() { return tf.getText().trim(); }
        public void setText(String t) { tf.setText(t); try { current = java.time.LocalDate.parse(t, fmt); } catch(Exception ignored){} }
        public java.time.LocalDate getDate() { return current; }

        private void showPopup() {
            JPopupMenu pop = new JPopupMenu();
            pop.setBorder(new LineBorder(Theme.BORDER));
            pop.setBackground(Theme.BG_CARD);
            pop.add(new CalendarPanel(pop));
            pop.show(this, 0, getHeight());
        }

        private class CalendarPanel extends JPanel {
            private java.time.LocalDate view;

            public CalendarPanel(JPopupMenu pop) {
                this.view = current;
                setLayout(new BorderLayout());
                setBackground(Theme.BG_CARD);
                build(pop);
            }

            private void build(JPopupMenu pop) {
                removeAll();
                
                // Header (Mois Année + Nav)
                JPanel hdr = new JPanel(new BorderLayout());
                hdr.setOpaque(false);
                hdr.setBorder(new EmptyBorder(5,5,5,5));
                
                JButton prev = navBtn("<"); prev.addActionListener(e -> { view = view.minusMonths(1); build(pop); });
                JButton next = navBtn(">"); next.addActionListener(e -> { view = view.plusMonths(1); build(pop); });
                JLabel lbl = new JLabel(view.getMonth().getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.FRENCH) + " " + view.getYear(), SwingConstants.CENTER);
                lbl.setFont(Theme.F_BODY); lbl.setForeground(Theme.TEXT_PRIMARY);
                
                hdr.add(prev, BorderLayout.WEST);
                hdr.add(lbl, BorderLayout.CENTER);
                hdr.add(next, BorderLayout.EAST);
                add(hdr, BorderLayout.NORTH);

                // Grid
                JPanel grid = new JPanel(new GridLayout(0, 7));
                grid.setOpaque(false);
                grid.setBorder(new EmptyBorder(0,5,5,5));
                
                String[] days = {"Lu", "Ma", "Me", "Je", "Ve", "Sa", "Di"};
                for (String d : days) {
                    JLabel dl = new JLabel(d, SwingConstants.CENTER);
                    dl.setFont(Theme.F_LABEL); dl.setForeground(Theme.TEXT_MUTED);
                    grid.add(dl);
                }

                java.time.LocalDate first = view.withDayOfMonth(1);
                int offset = first.getDayOfWeek().getValue() - 1;
                for (int i = 0; i < offset; i++) grid.add(new JLabel(""));
                
                int len = view.lengthOfMonth();
                for (int i = 1; i <= len; i++) {
                    int day = i;
                    JButton db = new JButton(String.valueOf(day));
                    db.setFont(Theme.F_SMALL); db.setFocusPainted(false);
                    db.setOpaque(false); db.setContentAreaFilled(false);
                    db.setBorder(new EmptyBorder(4,4,4,4));
                    db.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    
                    java.time.LocalDate d = view.withDayOfMonth(day);
                    if (d.equals(current)) {
                        db.setForeground(Color.WHITE);
                        db.setBackground(Theme.ACCENT);
                        db.setOpaque(true);
                    } else if (d.equals(java.time.LocalDate.now())) {
                        db.setForeground(Theme.CYAN);
                    } else {
                        db.setForeground(Theme.TEXT_PRIMARY);
                    }

                    db.addActionListener(e -> {
                        current = d;
                        tf.setText(current.format(fmt));
                        pop.setVisible(false);
                    });
                    
                    grid.add(db);
                }
                add(grid, BorderLayout.CENTER);
                revalidate(); repaint();
            }

            private JButton navBtn(String t) {
                JButton b = new JButton(t); b.setFocusPainted(false); b.setBorderPainted(false);
                b.setContentAreaFilled(false); b.setForeground(Theme.TEXT_SECONDARY);
                b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                return b;
            }
        }
    }
}
