package gestion.util;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

public final class Widgets {
    private Widgets() {}

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
        sp.getVerticalScrollBar().setPreferredSize(new Dimension(5,0));
        sp.getHorizontalScrollBar().setPreferredSize(new Dimension(0,5));
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
            tf.setForeground(Theme.TEXT_MUTED);
            tf.setText(placeholder);
            tf.addFocusListener(new FocusAdapter(){
                boolean first=true;
                public void focusGained(FocusEvent e){ if(first){tf.setText("");tf.setForeground(Theme.TEXT_PRIMARY);first=false;} }
            });
        }
        return tf;
    }
}
