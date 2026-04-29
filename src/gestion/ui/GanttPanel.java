package gestion.ui;

import gestion.model.*;
import gestion.util.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class GanttPanel extends JPanel {

    private static final int HDR_H  = 60;
    private static final int ROW_H  = 48;
    private static final int LBL_W  = 250;
    private static final int CELL   = 28;
    private static final int JALON_ROW_H = 38;

    private Projet projet;
    private LocalDate t0, t1;
    private int days;
    private int hovered=-1, selected=-1;
    private SelectionListener listener;

    public interface SelectionListener { void onSelect(Tache t); }

    public GanttPanel() {
        setBackground(Theme.BG_PANEL);
        addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e){ onClick(e); }
        });
        addMouseMotionListener(new MouseMotionAdapter(){
            public void mouseMoved(MouseEvent e){ onMove(e); }
        });
        ToolTipManager.sharedInstance().setInitialDelay(200);
    }

    public void setProjet(Projet p){
        projet=p; selected=-1;
        if(p==null||p.getTaches().isEmpty()){ setPreferredSize(new Dimension(900,400)); repaint(); return; }
        t0=p.getDateDebutReelle().minusDays(3);
        t1=p.getDateFinReelle().plusDays(4);
        // Include jalon dates in time range
        for(gestion.model.Jalon j : p.getJalons()){
            if(j.getDate().isBefore(t0)) t0=j.getDate().minusDays(2);
            if(j.getDate().isAfter(t1))  t1=j.getDate().plusDays(2);
        }
        days=(int)ChronoUnit.DAYS.between(t0,t1)+1;
        // Extra height: JALON_ROW at bottom + legend
        int jaH = p.getJalons().isEmpty() ? 0 : JALON_ROW_H + 8;
        setPreferredSize(new Dimension(Math.max(LBL_W+days*CELL,900),
            Math.max(HDR_H + p.getTaches().size()*ROW_H + jaH + 50, 400)));
        revalidate(); repaint();
    }

    public void setListener(SelectionListener l){ listener=l; }

    private void onClick(MouseEvent e){
        if(projet==null) return;
        int r=(e.getY()-HDR_H)/ROW_H;
        if(r>=0&&r<projet.getTaches().size()){
            selected=r; repaint();
            if(listener!=null) listener.onSelect(projet.getTaches().get(r));
        }
    }

    private void onMove(MouseEvent e){
        if(projet==null) return;
        int mx = e.getX(), my = e.getY();
        int r=(my-HDR_H)/ROW_H;
        int nh=(r>=0&&r<projet.getTaches().size())?r:-1;
        if(nh!=hovered){ hovered=nh; repaint(); }
        DateTimeFormatter f=DateTimeFormatter.ofPattern("dd/MM/yyyy");
        if(hovered>=0){
            Tache t=projet.getTaches().get(hovered);
            setToolTipText("<html><div style='padding:4px'><b>"+t.getNom()+"</b><br>"
                +"📅 "+t.getDateDebut().format(f)+" → "+t.getDateFin().format(f)+"<br>"
                +"⏱ "+t.getDureeJours()+" jours &nbsp;•&nbsp; 📊 "+t.getProgression()+"%<br>"
                +"👤 "+t.getResponsable()+"</div></html>");
            return;
        }
        // Tooltip jalon
        int jRowTop = HDR_H + projet.getTaches().size() * ROW_H;
        if (my >= jRowTop && my <= jRowTop + JALON_ROW_H) {
            for (gestion.model.Jalon j : projet.getJalons()) {
                long off = ChronoUnit.DAYS.between(t0, j.getDate());
                if (off < 0 || off >= days) continue;
                int jx = LBL_W + (int)off * CELL + CELL/2;
                if (Math.abs(mx - jx) <= 14) {
                    setToolTipText("<html><div style='padding:4px'>"
                        + j.getType().getIcone()+" <b>"+j.getNom()+"</b><br>"
                        + "📅 "+j.getDate().format(f)+"<br>"
                        + "📌 "+j.getType().getLibelle()+"  •  "+j.getStatut().getLibelle()
                        + (j.getDescription().isEmpty()?"":"<br><i>"+j.getDescription()+"</i>")
                        + "</div></html>");
                    return;
                }
            }
        }
        setToolTipText(null);
    }

    @Override protected void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2=(Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        if(projet==null||projet.getTaches().isEmpty()){ drawEmpty(g2); return; }
        drawBg(g2); drawGrid(g2); drawHeader(g2);
        drawTasks(g2); drawJalons(g2); drawToday(g2); drawLegend(g2);
    }

    private void drawBg(Graphics2D g){
        List<Tache> ts=projet.getTaches();
        for(int i=0;i<ts.size();i++){
            int y=HDR_H+i*ROW_H;
            g.setColor(i==selected?Theme.BG_SELECTED:i==hovered?Theme.BG_CARD_HOVER:i%2==0?Theme.BG_ROW_ODD:Theme.BG_ROW_EVEN);
            g.fillRect(0,y,getWidth(),ROW_H);
        }
        g.setColor(Theme.BORDER);
        g.fillRect(0,HDR_H+ts.size()*ROW_H,getWidth(),1);
    }

    private void drawGrid(Graphics2D g){
        for(int i=0;i<days;i++){
            LocalDate d=t0.plusDays(i);
            int x=LBL_W+i*CELL;
            boolean we=d.getDayOfWeek().getValue()>=6;
            boolean fom=d.getDayOfMonth()==1;
            boolean mon=d.getDayOfWeek().getValue()==1;
            if(we){ g.setColor(new Color(35,35,58,65)); g.fillRect(x,HDR_H,CELL,getHeight()-HDR_H); }
            if(fom){ g.setColor(new Color(70,70,108,130)); g.setStroke(new BasicStroke(1f)); g.drawLine(x,HDR_H/2,x,getHeight()); }
            else if(mon){ g.setColor(new Color(45,45,72,80)); g.setStroke(new BasicStroke(0.7f)); g.drawLine(x,HDR_H,x,getHeight()); }
        }
        g.setStroke(new BasicStroke(1f));
    }

    private void drawHeader(Graphics2D g){
        g.setColor(Theme.BG_TOPBAR); g.fillRect(0,0,getWidth(),HDR_H);
        g.setColor(Theme.BORDER);    g.fillRect(0,HDR_H-1,getWidth(),1);
        g.setColor(Theme.BG_SIDEBAR);g.fillRect(0,0,LBL_W,HDR_H);
        g.setColor(Theme.BORDER_LIGHT); g.fillRect(LBL_W-1,0,1,HDR_H);
        g.setFont(Theme.F_LABEL); g.setColor(Theme.TEXT_SECONDARY);
        g.drawString("TÂCHES",16,HDR_H/2+4);

        // Mois
        g.setFont(Theme.font(Font.BOLD,10));
        String curM=""; int mx=LBL_W;
        DateTimeFormatter mf=DateTimeFormatter.ofPattern("MMM yyyy");
        for(int i=0;i<=days;i++){
            LocalDate d=t0.plusDays(i); String m=d.format(mf).toUpperCase();
            if(!m.equals(curM)){
                int x=LBL_W+i*CELL;
                if(!curM.isEmpty()){ g.setColor(new Color(130,148,255)); ctr(g,curM,(mx+x)/2,13); }
                curM=m; mx=x;
            }
        }
        g.setColor(new Color(130,148,255)); ctr(g,curM,(mx+LBL_W+days*CELL)/2,13);

        // Jours
        g.setFont(Theme.font(Font.PLAIN,9));
        for(int i=0;i<days;i++){
            LocalDate d=t0.plusDays(i); int x=LBL_W+i*CELL;
            boolean we=d.getDayOfWeek().getValue()>=6;
            boolean show=CELL>=22||d.getDayOfMonth()==1||d.getDayOfMonth()==15;
            if(show){ g.setColor(we?Theme.TEXT_MUTED:new Color(90,85,120)); ctr(g,""+d.getDayOfMonth(),x+CELL/2,HDR_H-6); }
        }
    }

    private void drawTasks(Graphics2D g){
        List<Tache> ts=projet.getTaches();
        for(int i=0;i<ts.size();i++) drawRow(g,ts.get(i),HDR_H+i*ROW_H,i==selected);
        g.setColor(Theme.BORDER_LIGHT); g.setStroke(new BasicStroke(1f));
        g.drawLine(LBL_W,HDR_H,LBL_W,HDR_H+ts.size()*ROW_H);
    }

    private void drawRow(Graphics2D g, Tache t, int y, boolean sel){
        Color pc=Theme.prioriteColor(t.getPriorite());
        Color sc=Theme.statutColor(t.getStatut());

        // Pastille priorité
        g.setColor(pc); g.fillOval(10,y+ROW_H/2-4,8,8);

        // Nom
        g.setFont(Theme.font(Font.BOLD,11));
        g.setColor(sel?Color.WHITE:Theme.TEXT_PRIMARY);
        g.drawString(cut(t.getNom(),25),24,y+ROW_H/2+2);

        // Sous-titre
        g.setFont(Theme.F_TINY); g.setColor(Theme.TEXT_SECONDARY);
        g.drawString("👤 "+cut(t.getResponsable(),22),24,y+ROW_H/2+14);

        // Barre Gantt
        int off=(int)ChronoUnit.DAYS.between(t0,t.getDateDebut());
        int bx=LBL_W+off*CELL;
        int bw=Math.max(CELL,t.getDureeJours()*CELL);
        int by=y+11, bh=ROW_H-22;

        // Ombre
        g.setColor(new Color(0,0,0,22)); g.fillRoundRect(bx+1,by+2,bw,bh,8,8);

        // Fond
        g.setColor(new Color(sc.getRed(),sc.getGreen(),sc.getBlue(),28)); g.fillRoundRect(bx,by,bw,bh,8,8);

        // Progression (gradient)
        if(t.getProgression()>0){
            int pw=Math.max(8,(int)(bw*t.getProgression()/100.0));
            GradientPaint gp=new GradientPaint(bx,by,sc.brighter(),bx,by+bh,sc);
            g.setPaint(gp);
            Shape clip=g.getClip();
            g.setClip(new RoundRectangle2D.Float(bx,by,pw,bh,8,8));
            g.fillRoundRect(bx,by,bw,bh,8,8);
            g.setClip(clip);
        }

        // Bordure
        g.setPaint(sc); g.setStroke(new BasicStroke(1.2f)); g.drawRoundRect(bx,by,bw,bh,8,8);
        g.setStroke(new BasicStroke(1f));

        // Label %
        if(bw>36){ g.setFont(Theme.font(Font.BOLD,9)); g.setColor(new Color(255,255,255,200)); ctr(g,t.getProgression()+"%",bx+bw/2,by+bh/2+4); }

        // Flèches dépendances
        for(int dep : t.getDependances()){
            projet.getTache(dep).ifPresent(from->{
                int fi=projet.getTaches().indexOf(from);
                if(fi>=0){
                    int fend=(int)ChronoUnit.DAYS.between(t0,from.getDateFin());
                    int fx=LBL_W+(fend+1)*CELL;
                    int fy=HDR_H+fi*ROW_H+ROW_H/2;
                    int ty2=y+ROW_H/2;
                    g.setColor(new Color(140,130,190,65));
                    g.setStroke(new BasicStroke(1f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND,0,new float[]{4,3},0));
                    g.drawLine(fx,fy,bx-5,fy); g.drawLine(bx-5,fy,bx-5,ty2);
                    g.setStroke(new BasicStroke(1.3f)); g.setColor(new Color(140,130,190,95));
                    g.fillPolygon(new int[]{bx-5,bx-10,bx-10},new int[]{ty2,ty2-4,ty2+4},3);
                    g.setStroke(new BasicStroke(1f));
                }
            });
        }
    }


    // ── Jalons ────────────────────────────────────────────────
    private void drawJalons(Graphics2D g) {
        List<gestion.model.Jalon> jalons = projet.getJalons();
        if (jalons.isEmpty()) return;

        int rowTop = HDR_H + projet.getTaches().size() * ROW_H;

        // Section header
        g.setColor(new Color(28, 28, 50));
        g.fillRect(0, rowTop, getWidth(), JALON_ROW_H);
        g.setColor(Theme.BORDER);
        g.fillRect(0, rowTop, getWidth(), 1);
        g.fillRect(0, rowTop + JALON_ROW_H - 1, getWidth(), 1);

        // Label colonne
        g.setColor(Theme.BG_SIDEBAR);
        g.fillRect(0, rowTop, LBL_W, JALON_ROW_H);
        g.setColor(Theme.BORDER_LIGHT); g.fillRect(LBL_W - 1, rowTop, 1, JALON_ROW_H);
        g.setFont(Theme.F_LABEL); g.setColor(Theme.TEXT_SECONDARY);
        g.drawString("JALONS", 14, rowTop + JALON_ROW_H / 2 + 4);

        // Lignes verticales jalons sur toute la hauteur du Gantt
        for (gestion.model.Jalon j : jalons) {
            long off = ChronoUnit.DAYS.between(t0, j.getDate());
            if (off < 0 || off >= days) continue;
            int jx = LBL_W + (int) off * CELL + CELL / 2;
            java.awt.Color jc = j.getStatut().getCouleur();

            // Ligne verticale pointillée sur toute la hauteur des tâches
            g.setColor(new java.awt.Color(jc.getRed(), jc.getGreen(), jc.getBlue(), 35));
            g.setStroke(new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                0, new float[]{4, 4}, 0));
            g.drawLine(jx, HDR_H, jx, rowTop);
            g.setStroke(new BasicStroke(1f));

            // Losange (symbole jalon standard)
            drawDiamond(g, jx, rowTop + JALON_ROW_H / 2, 12, jc, j.getStatut());

            // Nom du jalon
            g.setFont(Theme.font(java.awt.Font.BOLD, 9));
            g.setColor(Theme.TEXT_PRIMARY);
            String label = cut(j.getNom(), 14);
            int lw = g.getFontMetrics().stringWidth(label);
            // Position label : centré sur le losange, décalé si déborde
            int lx = Math.max(LBL_W + 4, Math.min(getWidth() - lw - 4, jx - lw / 2));
            g.drawString(label, lx, rowTop + JALON_ROW_H / 2 - 14);

            // Date sous le losange
            g.setFont(Theme.font(java.awt.Font.PLAIN, 8));
            g.setColor(Theme.TEXT_SECONDARY);
            String ds = j.getDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM"));
            int dw = g.getFontMetrics().stringWidth(ds);
            g.drawString(ds, jx - dw / 2, rowTop + JALON_ROW_H / 2 + 18);
        }

        // Tooltip zone jalons
        jalons.forEach(j -> {
            long off = ChronoUnit.DAYS.between(t0, j.getDate());
            if (off >= 0 && off < days) {
                // Tooltip géré dans onMove
            }
        });
    }

    private void drawDiamond(Graphics2D g, int cx, int cy, int r,
                              java.awt.Color color, gestion.model.Jalon.Statut statut) {
        int[] xs = {cx, cx + r, cx, cx - r};
        int[] ys = {cy - r, cy, cy + r, cy};

        // Fond
        g.setColor(new java.awt.Color(color.getRed(), color.getGreen(), color.getBlue(), 50));
        g.fillPolygon(xs, ys, 4);

        // Remplissage si atteint
        if (statut == gestion.model.Jalon.Statut.ATTEINT) {
            GradientPaint gp = new GradientPaint(cx, cy - r, color.brighter(), cx, cy + r, color);
            g.setPaint(gp);
            g.fillPolygon(xs, ys, 4);
        }

        // Bordure
        g.setPaint(color);
        g.setStroke(new BasicStroke(1.8f));
        g.drawPolygon(xs, ys, 4);
        g.setStroke(new BasicStroke(1f));

        // Point central si atteint
        if (statut == gestion.model.Jalon.Statut.ATTEINT) {
            g.setColor(java.awt.Color.WHITE);
            g.fillOval(cx - 3, cy - 3, 6, 6);
        }
    }

    private void drawToday(Graphics2D g){
        LocalDate now=LocalDate.now();
        if(now.isBefore(t0)||now.isAfter(t1)) return;
        int x=LBL_W+(int)ChronoUnit.DAYS.between(t0,now)*CELL+CELL/2;
        g.setColor(Theme.GOLD);
        g.setStroke(new BasicStroke(1.8f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND,0,new float[]{5,3},0));
        g.drawLine(x,HDR_H,x,HDR_H+projet.getTaches().size()*ROW_H);
        g.setStroke(new BasicStroke(1f));
        g.setFont(Theme.font(Font.BOLD,8));
        String lbl="AUJOURD'HUI"; int lw=g.getFontMetrics().stringWidth(lbl);
        g.fillRoundRect(x-lw/2-5,HDR_H+3,lw+10,14,4,4);
        g.setColor(new Color(18,15,0)); ctr(g,lbl,x,HDR_H+13);
    }

    private void drawLegend(Graphics2D g){
        if(projet==null) return;
        int y=HDR_H+projet.getTaches().size()*ROW_H+14, x=LBL_W+12;
        g.setFont(Theme.F_TINY);
        String[] legendL={"Terminé","En cours","En attente","Bloqué","Non commencé"};
        Color[] legendC={Theme.GREEN,Theme.ACCENT,Theme.ORANGE,Theme.RED,new Color(85,82,115)};
        for(int li=0;li<legendL.length;li++){
            Color c=legendC[li]; String lb=legendL[li];
            g.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),50)); g.fillRoundRect(x,y+2,12,10,4,4);
            g.setColor(c); g.drawRoundRect(x,y+2,12,10,4,4);
            g.setColor(Theme.TEXT_SECONDARY); g.drawString(lb,x+16,y+12);
            x+=g.getFontMetrics().stringWidth(lb)+32;
        }
        // Symbole jalon dans la légende
        if(!projet.getJalons().isEmpty()){
            drawDiamond(g,x+8,y+7,7,Theme.GOLD,gestion.model.Jalon.Statut.PREVU);
            g.setColor(Theme.TEXT_SECONDARY); g.drawString("Jalon",x+20,y+12);
        }
    }

    private void drawEmpty(Graphics2D g){
        g.setColor(Theme.TEXT_MUTED); g.setFont(Theme.F_SUBTITLE);
        ctr(g,"Sélectionnez un projet pour afficher le diagramme de Gantt",getWidth()/2,getHeight()/2-8);
        g.setFont(Theme.F_SMALL);
        ctr(g,"Cliquez sur un projet dans la liste à gauche",getWidth()/2,getHeight()/2+14);
    }

    private void ctr(Graphics2D g,String s,int cx,int y){ g.drawString(s,cx-g.getFontMetrics().stringWidth(s)/2,y); }
    private String cut(String s,int n){ return s.length()>n?s.substring(0,n-1)+"…":s; }
}
