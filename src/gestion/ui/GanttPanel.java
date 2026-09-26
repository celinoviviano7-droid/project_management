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
    private static final int JALON_ROW_H = 38;

    private Point dragStartScreen;
    private Point dragStartView;
    private int zoom = 28; // Largeur d'une journée en pixels
    private Projet projet;
    private LocalDate t0, t1;
    private int days;
    private int hovered=-1, selected=-1;
    private SelectionListener listener;
    private String searchQuery = "";
    private java.util.Set<Integer> cheminCritique = java.util.Collections.emptySet();

    public interface SelectionListener { void onSelect(Tache t, Projet p); }

    public GanttPanel() {
        setBackground(Theme.BG_PANEL);
        setOpaque(true);
        addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e){ onClick(e); }
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    dragStartScreen = e.getLocationOnScreen();
                    JViewport vp = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, GanttPanel.this);
                    if (vp != null) {
                        dragStartView = vp.getViewPosition();
                        setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                    }
                }
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                dragStartScreen = null;
            }
        });
        addMouseMotionListener(new MouseMotionAdapter(){
            public void mouseMoved(MouseEvent e){ onMove(e); }
            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragStartScreen != null && dragStartView != null) {
                    JViewport vp = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, GanttPanel.this);
                    if (vp != null) {
                        Point currentScreen = e.getLocationOnScreen();
                        int dx = dragStartScreen.x - currentScreen.x;
                        int dy = dragStartScreen.y - currentScreen.y;
                        
                        int nx = Math.max(0, Math.min(dragStartView.x + dx, getWidth() - vp.getWidth()));
                        int ny = Math.max(0, Math.min(dragStartView.y + dy, getHeight() - vp.getHeight()));
                        
                        vp.setViewPosition(new Point(nx, ny));
                    }
                }
            }
        });
        addMouseWheelListener(e -> {
            if (e.isControlDown()) {
                if (e.getWheelRotation() < 0) zoomIn();
                else zoomOut();
            } else {
                getParent().dispatchEvent(e);
            }
        });
        ToolTipManager.sharedInstance().setInitialDelay(200);
        setMinimumSize(new Dimension(400,300));
    }

    public void zoomIn() { 
        if (zoom < 120) { zoom += 5; updateSize(); }
    }
    
    public void zoomOut() { 
        if (zoom > 10) { zoom -= 5; updateSize(); }
    }
    
    public void resetZoom() { 
        zoom = 28; updateSize(); 
    }

    private void updateSize() {
        if (projet == null) return;
        int jaH = projet.getJalons().isEmpty() ? 0 : JALON_ROW_H + 8;
        setPreferredSize(new Dimension(Math.max(LBL_W + days * zoom, 900),
            Math.max(HDR_H + getFilteredTasks().size() * ROW_H + jaH + 50, 400)));
        revalidate();
        repaint();
    }

    public void setProjet(Projet p){
        projet=p; selected=-1;
        if(p==null||p.getTaches().isEmpty()){ setPreferredSize(new Dimension(900,400)); repaint(); return; }
        t0=p.getDateDebutReelle().minusDays(3);
        t1=p.getDateFinReelle().plusDays(4);
        for(gestion.model.Jalon j : p.getJalons()){
            if(j.getDate().isBefore(t0)) t0=j.getDate().minusDays(2);
            if(j.getDate().isAfter(t1))  t1=j.getDate().plusDays(2);
        }
        // Toujours inclure aujourd'hui dans la plage affichée
        // pour que l'indicateur d'écoulement du temps soit toujours visible
        LocalDate now = LocalDate.now();
        if(now.isBefore(t0)) t0 = now.minusDays(3);
        if(now.isAfter(t1))  t1 = now.plusDays(3);
        days=(int)ChronoUnit.DAYS.between(t0,t1)+1;
        updateSize();
    }

    public void setListener(SelectionListener l){ listener=l; }
    public void setSearchQuery(String q){ this.searchQuery = q.toLowerCase(); }
    public void setCriticalPath(java.util.Set<Integer> cp){ this.cheminCritique = cp != null ? cp : java.util.Collections.emptySet(); repaint(); }
 
    private java.util.List<Tache> getFilteredTasks() {
        if (projet == null) return java.util.Collections.emptyList();
        if (searchQuery == null || searchQuery.isEmpty()) return projet.getTaches();
        java.util.List<Tache> filtered = new java.util.ArrayList<>();
        for (Tache t : projet.getTaches()) {
            if (t.getNom().toLowerCase().contains(searchQuery)) {
                filtered.add(t);
            }
        }
        return filtered;
    }

    private void onClick(MouseEvent e){
        if(projet==null) return;
        List<Tache> ts = getFilteredTasks();
        int r=(e.getY()-HDR_H)/ROW_H;
        if(r>=0&&r<ts.size()){
            Tache t = ts.get(r);
            selected=r; repaint();
            if(listener!=null) listener.onSelect(t, projet);
            
            // Si on clique dans la colonne fixe (noms), on navigue vers la barre
            int sx = getScrollX();
            if (e.getX() >= sx && e.getX() <= sx + LBL_W) {
                scrollToTask(t);
            }
        }
    }

    private void scrollToTask(Tache t) {
        if (t == null || t0 == null) return;
        JViewport vp = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, this);
        if (vp != null) {
            long off = ChronoUnit.DAYS.between(t0, t.getDateDebut());
            int bx = LBL_W + (int)off * zoom;
            
            // On veut que bx soit visible juste après la colonne LBL_W
            int targetX = bx - (LBL_W + 50); // Marge de 50px
            targetX = Math.max(0, Math.min(targetX, getWidth() - vp.getWidth()));
            
            Point pos = vp.getViewPosition();
            vp.setViewPosition(new Point(targetX, pos.y));
        }
    }

    private void onMove(MouseEvent e){
        if(projet==null) return;
        List<Tache> ts = getFilteredTasks();
        int mx = e.getX(), my = e.getY();
        int r=(my-HDR_H)/ROW_H;
        int nh=(r>=0&&r<ts.size())?r:-1;
        if(nh!=hovered){ hovered=nh; repaint(); }
        DateTimeFormatter f=DateTimeFormatter.ofPattern("dd/MM/yyyy");
        if(hovered>=0){
            Tache t=ts.get(hovered);
            // On force un fond clair et texte sombre pour une lisibilité maximale type "Post-it" pro
            setToolTipText("<html><div style='padding:8px; background-color: #ffffdd; color: #111111; border: 1px solid #ccccaa;'>"
                +"<b style='font-size:13px;'>"+t.getNom()+"</b><br>"
                +"<div style='margin-top:5px; font-size:11px;'>"
                +"<span style='color: #d32f2f;'>📅</span> "+t.getDateDebut().format(f)+" → "+t.getDateFin().format(f)+"<br>"
                +"<span style='color: #1976d2;'>⏱</span> "+t.getDureeJours()+" jours &nbsp;•&nbsp; <span style='color: #388e3c;'>📊</span> "+t.getProgression()+"%<br>"
                +"<span style='color: #5d4037;'>👤</span> "+t.getResponsable()+"</div>"
                +"</div></html>");
            return;
        }
        // Tooltip jalon
        int jRowTop = HDR_H + ts.size() * ROW_H;
        if (my >= jRowTop && my <= jRowTop + JALON_ROW_H) {
            for (gestion.model.Jalon j : projet.getJalons()) {
                long off = ChronoUnit.DAYS.between(t0, j.getDate());
                if (off < 0 || off >= days) continue;
                int jx = LBL_W + (int)off * zoom + zoom/2;
                if (Math.abs(mx - jx) <= zoom/2 + 2) {
                    setToolTipText("<html><div style='padding:8px; background-color: #e3f2fd; color: #111111; border: 1px solid #bbdefb;'>"
                        + "<b style='font-size:13px;'>" + j.getNom() + "</b><br>"
                        + "<div style='margin-top:5px; font-size:11px;'>"
                        + "<span style='color: #1976d2;'>🎯</span> Jalon  &nbsp;•&nbsp; 📅 " + j.getDate().format(f) + "<br>"
                        + "🏷 " + j.getType().getLibelle() + " &nbsp;•&nbsp; " + j.getStatut().getLibelle()
                        + (j.getDescription().isEmpty() ? "" : "<br><i style='color:#444;'>\"" + j.getDescription() + "\"</i>")
                        + "</div></div></html>");
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
        drawTasks(g2); drawJalons(g2); drawToday(g2); 
        drawStickyColumn(g2); // Dessiné en dernier pour rester au dessus
        drawLegend(g2);
    }

    private int getScrollX() {
        Container p = getParent();
        if (p instanceof JViewport) {
            return ((JViewport) p).getViewPosition().x;
        }
        return 0;
    }

    private void drawStickyColumn(Graphics2D g) {
        int sx = getScrollX();
        List<Tache> ts = getFilteredTasks();
        
        // Background vertical pour la zone de texte
        g.setColor(Theme.BG_SIDEBAR);
        g.fillRect(sx, 0, LBL_W, getHeight());
        
        // Bordure avec la timeline
        g.setColor(Theme.BORDER_LIGHT);
        g.fillRect(sx + LBL_W - 1, 0, 1, getHeight());

        // Header "TÂCHES"
        g.setColor(Theme.BG_TOPBAR);
        g.fillRect(sx, 0, LBL_W, HDR_H);
        g.setColor(Theme.BORDER);
        g.fillRect(sx, HDR_H - 1, LBL_W, 1);
        g.setFont(Theme.F_LABEL);
        g.setColor(Theme.TEXT_SECONDARY);
        g.drawString("TÂCHES", sx + 16, HDR_H / 2 + 4);
        
        // Noms des tâches
        for (int i = 0; i < ts.size(); i++) {
            Tache t = ts.get(i);
            int y = HDR_H + i * ROW_H;
            
            if (i == selected || i == hovered) {
                g.setColor(i == selected ? Theme.BG_SELECTED : Theme.BG_CARD_HOVER);
                g.fillRect(sx, y, LBL_W - 1, ROW_H);
            }
            
            Color pc = Theme.prioriteColor(t.getPriorite());
            g.setColor(pc);
            g.fillOval(sx + 10, y + ROW_H / 2 - 4, 8, 8);
            
            boolean isCritical = cheminCritique.contains(t.getId());
            g.setFont(Theme.font(Font.BOLD, 11));
            g.setColor(isCritical ? new Color(255, 80, 80) : Color.WHITE);
            g.drawString(cut(t.getNom(), 25), sx + 24, y + ROW_H / 2 + 2);
            
            g.setFont(Theme.F_TINY);
            g.setColor(Theme.TEXT_SECONDARY);
            g.drawString("\uD83D\uDC64 " + cut(t.getResponsable(), 22), sx + 24, y + ROW_H / 2 + 14);
        }
        
        if (!ts.isEmpty() && !projet.getJalons().isEmpty()) {
            int rowTop = HDR_H + ts.size() * ROW_H;
            g.setColor(Theme.BG_SIDEBAR);
            g.fillRect(sx, rowTop, LBL_W, JALON_ROW_H);
            g.setColor(Theme.BORDER_LIGHT);
            g.fillRect(sx + LBL_W - 1, rowTop, 1, JALON_ROW_H);
            g.setFont(Theme.F_LABEL);
            g.setColor(Theme.TEXT_SECONDARY);
            g.drawString("JALONS", sx + 14, rowTop + JALON_ROW_H / 2 + 4);
        }
    }

    private void drawBg(Graphics2D g){
        List<Tache> ts=getFilteredTasks();
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
            int x=LBL_W+i*zoom;
            boolean we=d.getDayOfWeek().getValue()>=6;
            boolean fom=d.getDayOfMonth()==1;
            boolean mon=d.getDayOfWeek().getValue()==1;
            if(we){ g.setColor(new Color(35,35,58,65)); g.fillRect(x,HDR_H,zoom,getHeight()-HDR_H); }
            if(fom){ g.setColor(new Color(70,70,108,130)); g.setStroke(new BasicStroke(1f)); g.drawLine(x,HDR_H/2,x,getHeight()); }
            else if(mon){ g.setColor(new Color(45,45,72,80)); g.setStroke(new BasicStroke(0.7f)); g.drawLine(x,HDR_H,x,getHeight()); }
        }
        g.setStroke(new BasicStroke(1f));
    }

    private void drawHeader(Graphics2D g){
        g.setColor(Theme.BG_TOPBAR); g.fillRect(0,0,getWidth(),HDR_H);
        g.setColor(Theme.BORDER);    g.fillRect(0,HDR_H-1,getWidth(),1);
        
        // On ne dessine ici que la partie timeline du header. 
        // La partie "TÂCHES" est maintenant gérée par drawStickyColumn.

        // Mois
        g.setFont(Theme.font(Font.BOLD,10));
        String curM=""; int mx=LBL_W;
        DateTimeFormatter mf=DateTimeFormatter.ofPattern("MMM yyyy");
        for(int i=0;i<=days;i++){
            LocalDate d=t0.plusDays(i); String m=d.format(mf).toUpperCase();
            if(!m.equals(curM)){
                int x=LBL_W+i*zoom;
                if(!curM.isEmpty()){ g.setColor(new Color(130,148,255)); ctr(g,curM,(mx+x)/2,13); }
                curM=m; mx=x;
            }
        }
        g.setColor(new Color(130,148,255)); ctr(g,curM,(mx+LBL_W+days*zoom)/2,13);

        // Jours
        g.setFont(Theme.font(Font.PLAIN,9));
        for(int i=0;i<days;i++){
            LocalDate d=t0.plusDays(i); int x=LBL_W+i*zoom;
            boolean we=d.getDayOfWeek().getValue()>=6;
            boolean show=zoom>=22||d.getDayOfMonth()==1||d.getDayOfMonth()==15;
            if(show){ g.setColor(we?Theme.TEXT_MUTED:new Color(90,85,120)); ctr(g,""+d.getDayOfMonth(),x+zoom/2,HDR_H-6); }
        }
    }

    private void drawTasks(Graphics2D g){
        List<Tache> ts=getFilteredTasks();
        for(int i=0;i<ts.size();i++) drawRow(g,ts.get(i),HDR_H+i*ROW_H,i==selected);
        g.setColor(Theme.BORDER_LIGHT); g.setStroke(new BasicStroke(1f));
        g.drawLine(LBL_W,HDR_H,LBL_W,HDR_H+ts.size()*ROW_H);
    }

    private void drawRow(Graphics2D g, Tache t, int y, boolean sel){
        Color sc=Theme.statutColor(t.getStatut());
        boolean isCritical = cheminCritique.contains(t.getId());
        
        // Note: La partie texte/nom est maintenant gérée par drawStickyColumn pour rester visible au scroll.

        // Barre Gantt
        int off=(int)ChronoUnit.DAYS.between(t0,t.getDateDebut());
        int bx=LBL_W+off*zoom;
        int bw=Math.max(zoom,t.getDureeJours()*zoom);
        int by=y+11, bh=ROW_H-22;

        // Ombre
        g.setColor(new Color(0,0,0,22)); g.fillRoundRect(bx+1,by+2,bw,bh,8,8);

        // Fond
        g.setColor(new Color(sc.getRed(),sc.getGreen(),sc.getBlue(),28)); g.fillRoundRect(bx,by,bw,bh,8,8);

        if (isCritical) {
            g.setColor(new Color(255, 60, 60, 18));
            g.fillRoundRect(bx, by, bw, bh, 8, 8);
        }

        // Progression (gradient)
        if(t.getProgression()>0){
            int pw=Math.max(8,(int)(bw*t.getProgression()/100.0));
            GradientPaint gp=new GradientPaint(bx,by,sc.brighter(),bx,by+bh,sc);
            g.setPaint(gp);
            Shape oldClip = g.getClip();
            g.clip(new RoundRectangle2D.Float(bx,by,pw,bh,8,8));
            g.fillRoundRect(bx,by,bw,bh,8,8);
            g.setClip(oldClip);
        }

        // Bordure
        if (isCritical) {
            g.setPaint(new Color(255, 60, 60));
            g.setStroke(new BasicStroke(2f));
        } else {
            g.setPaint(sc);
            g.setStroke(new BasicStroke(1.2f));
        }
        g.drawRoundRect(bx,by,bw,bh,8,8);
        g.setStroke(new BasicStroke(1f));

        // Badge CC
        if (isCritical) {
            g.setFont(Theme.font(Font.BOLD, 8));
            String badge = "CC";
            int bw2 = g.getFontMetrics().stringWidth(badge) + 6;
            g.setColor(new Color(255, 60, 60, 190));
            g.fillRoundRect(bx + bw - bw2 - 2, by - 1, bw2, 10, 4, 4);
            g.setColor(Color.WHITE);
            g.drawString(badge, bx + bw - bw2 + 1, by + 7);
        }

        // Label %
        if(bw>40){ g.setFont(Theme.font(Font.BOLD,9)); g.setColor(new Color(255,255,255,200)); ctr(g,t.getProgression()+"%",bx+bw/2,by+bh/2+4); }

        // Flèches dépendances
        for(int dep : t.getDependances()){
            projet.getTache(dep).ifPresent(from->{
                int fi=projet.getTaches().indexOf(from);
                if(fi>=0){
                    boolean depCritical = isCritical && cheminCritique.contains(from.getId());
                    int fend=(int)ChronoUnit.DAYS.between(t0,from.getDateFin());
                    int fx=LBL_W+(fend+1)*zoom;
                    int fy=HDR_H+fi*ROW_H+ROW_H/2;
                    int ty2=y+ROW_H/2;
                    Color arrowColor = depCritical ? new Color(255, 80, 80, 140) : new Color(140,130,190,65);
                    Color arrowHead = depCritical ? new Color(255, 80, 80, 200) : new Color(140,130,190,95);
                    g.setColor(arrowColor);
                    g.setStroke(depCritical ? new BasicStroke(1.4f) : new BasicStroke(1f,0,0,1.0f,new float[]{4,3},0));
                    g.drawLine(fx,fy,bx-5,fy); g.drawLine(bx-5,fy,bx-5,ty2);
                    g.setStroke(new BasicStroke(1.3f)); g.setColor(arrowHead);
                    g.fillPolygon(new int[]{bx-5,bx-10,bx-10},new int[]{ty2,ty2-4,ty2+4},3);
                    g.setStroke(new BasicStroke(1f));
                }
            });
        }
    }

    private void drawJalons(Graphics2D g) {
        List<gestion.model.Jalon> jalons = projet.getJalons();
        if (jalons.isEmpty()) return;
        int rowTop = HDR_H + getFilteredTasks().size() * ROW_H;
        g.setColor(new Color(28, 28, 50)); g.fillRect(0, rowTop, getWidth(), JALON_ROW_H);
        g.setColor(Theme.BORDER); g.fillRect(0, rowTop, getWidth(), 1);
        
        // Note: L'en-tête "JALONS" est gérée par drawStickyColumn.

        for (gestion.model.Jalon j : jalons) {
            long off = ChronoUnit.DAYS.between(t0, j.getDate());
            if (off < 0 || off >= days) continue;
            int jx = LBL_W + (int) off * zoom + zoom / 2;
            java.awt.Color jc = j.getStatut().getCouleur();
            g.setColor(new java.awt.Color(jc.getRed(), jc.getGreen(), jc.getBlue(), 35));
            g.setStroke(new BasicStroke(1f, 0, 0, 1.0f, new float[]{4, 4}, 0));
            g.drawLine(jx, HDR_H, jx, rowTop);
            g.setStroke(new BasicStroke(1f));
            drawDiamond(g, jx, rowTop + JALON_ROW_H / 2, 12, jc, j.getStatut());
            g.setFont(Theme.font(java.awt.Font.BOLD, 9)); g.setColor(Theme.TEXT_PRIMARY);
            String label = cut(j.getNom(), zoom/3 + 5);
            int lw = g.getFontMetrics().stringWidth(label);
            int lx = Math.max(LBL_W + 4, Math.min(getWidth() - lw - 4, jx - lw / 2));
            g.drawString(label, lx, rowTop + JALON_ROW_H / 2 - 14);
            g.setFont(Theme.font(java.awt.Font.PLAIN, 8)); g.setColor(Theme.TEXT_SECONDARY);
            String ds = j.getDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM"));
            int dw = g.getFontMetrics().stringWidth(ds);
            g.drawString(ds, jx - dw / 2, rowTop + JALON_ROW_H / 2 + 18);
        }
    }

    private void drawDiamond(Graphics2D g, int cx, int cy, int r, java.awt.Color color, gestion.model.Jalon.Statut statut) {
        int[] xs = {cx, cx + r, cx, cx - r}, ys = {cy - r, cy, cy + r, cy};
        g.setColor(new java.awt.Color(color.getRed(), color.getGreen(), color.getBlue(), 50));
        g.fillPolygon(xs, ys, 4);
        if (statut == gestion.model.Jalon.Statut.ATTEINT) {
            g.setPaint(new GradientPaint(cx, cy - r, color.brighter(), cx, cy + r, color));
            g.fillPolygon(xs, ys, 4);
            g.setColor(java.awt.Color.WHITE); g.fillOval(cx - 3, cy - 3, 6, 6);
        }
        g.setPaint(color); g.setStroke(new BasicStroke(1.8f)); g.drawPolygon(xs, ys, 4);
        g.setStroke(new BasicStroke(1f));
    }

    private void drawToday(Graphics2D g) {
        LocalDate now = LocalDate.now();

        List<Tache> ts = getFilteredTasks();
        int rowsH = ts.size() * ROW_H;
        int totalH = HDR_H + rowsH;

        // ── Calcul des positions clés ────────────────────────────────
        LocalDate startD = projet.getDateDebutReelle();
        LocalDate endD   = projet.getDateFinReelle();

        // Position X d'aujourd'hui (même hors des bornes affichées)
        int todayX = LBL_W + (int) ChronoUnit.DAYS.between(t0, now) * zoom + zoom / 2;

        // ═══════════════════════════════════════════════════════════
        // 1. ZONE TEMPS ÉCOULÉ (fond bleuté sur le passé du projet)
        // ═══════════════════════════════════════════════════════════
        if (startD != null && endD != null) {
            int startX = LBL_W + (int) ChronoUnit.DAYS.between(t0, startD) * zoom + zoom / 2;
            int endX   = LBL_W + (int) ChronoUnit.DAYS.between(t0, endD)   * zoom + zoom / 2;

            // Zone passée (entre début projet et aujourd'hui)
            int elapsedEndX = Math.max(startX, Math.min(endX, todayX));
            if (elapsedEndX > startX) {
                // Fond de la zone écoulée
                g.setColor(new Color(100, 130, 255, 14));
                g.fillRect(startX, HDR_H, elapsedEndX - startX, rowsH);
                // Ligne gauche de délimitation (début projet)
                g.setColor(new Color(100, 130, 255, 60));
                g.setStroke(new BasicStroke(1f));
                g.drawLine(startX, HDR_H, startX, totalH);
            }

            // Zone future (entre aujourd'hui et fin projet) — légèrement assombrie
            int futureStartX = Math.max(startX, todayX);
            if (futureStartX < endX) {
                g.setColor(new Color(0, 0, 0, 18));
                g.fillRect(futureStartX, HDR_H, endX - futureStartX, rowsH);
            }

            // ═══════════════════════════════════════════════════════
            // LIGNE DE DEADLINE (toujours affichée, style selon proximité)
            // ═══════════════════════════════════════════════════════
            long totalDays   = ChronoUnit.DAYS.between(startD, endD);
            long elapsedDays = Math.max(0, Math.min(totalDays, ChronoUnit.DAYS.between(startD, now)));
            int pct = totalDays > 0 ? (int) (elapsedDays * 100L / totalDays) : 0;

            long remainingDays = ChronoUnit.DAYS.between(now, endD);
            boolean isOverdue  = remainingDays < 0;
            boolean isNearEnd  = remainingDays >= 0 && remainingDays <= 7;
            boolean isCritical = remainingDays >= 0 && remainingDays <= 3;

            // Couleur de la barre de temps selon la proximité
            Color timeColor = isOverdue  ? Theme.RED
                            : isCritical ? Theme.RED
                            : isNearEnd  ? Theme.ORANGE
                            : Theme.GOLD;

            // Rail gris (durée totale)
            int yBar = HDR_H - 5;
            g.setColor(new Color(255, 255, 255, 20));
            g.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(startX, yBar, endX, yBar);

            // Portion écoulée
            if (elapsedEndX > startX) {
                g.setColor(timeColor);
                g.drawLine(startX, yBar, elapsedEndX, yBar);
            }
            g.setStroke(new BasicStroke(1f));

            // Pastilles aux extrémités
            g.setColor(timeColor);
            g.fillOval(startX - 4, yBar - 4, 8, 8);
            g.setColor(isOverdue || pct >= 100 ? timeColor : Theme.TEXT_MUTED);
            g.fillOval(endX - 4, yBar - 4, 8, 8);

            // Étiquette % écoulé sur la barre
            g.setFont(Theme.font(Font.BOLD, 8));
            String pctLabel = pct + "%";
            int pw = g.getFontMetrics().stringWidth(pctLabel);
            int labelX = Math.max(startX + 2, Math.min(endX - pw - 6, elapsedEndX - pw / 2 - 3));
            g.setColor(new Color(0, 0, 0, 160));
            g.fillRoundRect(labelX - 2, yBar - 11, pw + 8, 11, 4, 4);
            g.setColor(Color.WHITE);
            g.drawString(pctLabel, labelX + 2, yBar - 2);

            // ── Ligne verticale de deadline (TOUJOURS affichée) ──
            if (isOverdue || isCritical) {
                // Rouge plein — urgence maximale
                g.setColor(new Color(Theme.RED.getRed(), Theme.RED.getGreen(), Theme.RED.getBlue(), 35));
                g.fillRect(endX - 2, HDR_H, 4, rowsH);
                g.setColor(Theme.RED);
                g.setStroke(new BasicStroke(2f));
            } else if (isNearEnd) {
                // Orange pointillé — alerte
                g.setColor(Theme.ORANGE);
                g.setStroke(new BasicStroke(1.8f, 0, 0, 1f, new float[]{5, 3}, 0));
            } else {
                // Blanc subtil — deadline lointaine (toujours visible)
                g.setColor(new Color(180, 175, 220, 70));
                g.setStroke(new BasicStroke(1.2f, 0, 0, 1f, new float[]{4, 4}, 0));
            }
            g.drawLine(endX, HDR_H, endX, totalH);
            g.setStroke(new BasicStroke(1f));

            // Badge deadline (toujours affiché, texte adapté)
            String deadlineLabel;
            Color badgeColor;
            Color textColor;
            if (isOverdue) {
                deadlineLabel = "DÉPASSÉ +" + (-remainingDays) + "j";
                badgeColor    = Theme.RED;
                textColor     = Color.WHITE;
            } else if (isCritical) {
                deadlineLabel = "DEADLINE J-" + remainingDays;
                badgeColor    = Theme.RED;
                textColor     = Color.WHITE;
            } else if (isNearEnd) {
                deadlineLabel = "DEADLINE J-" + remainingDays;
                badgeColor    = Theme.ORANGE;
                textColor     = Color.WHITE;
            } else {
                deadlineLabel = "FIN J+" + remainingDays;
                badgeColor    = new Color(60, 60, 90, 180);
                textColor     = new Color(180, 175, 220);
            }
            int dlw = g.getFontMetrics().stringWidth(deadlineLabel);
            g.setColor(badgeColor);
            g.fillRoundRect(endX - dlw / 2 - 5, HDR_H + 4, dlw + 10, 13, 4, 4);
            g.setColor(textColor);
            ctr(g, deadlineLabel, endX, HDR_H + 13);

            // Petit triangle en bas de la ligne deadline (ancre visuelle)
            int triD = 5;
            int[] dxs = {endX, endX - triD, endX + triD};
            int[] dys = {totalH, totalH - triD * 2, totalH - triD * 2};
            g.setColor(isOverdue || isNearEnd ? timeColor : new Color(140, 135, 180, 100));
            g.fillPolygon(dxs, dys, 3);
        }

        // ═══════════════════════════════════════════════════════════
        // 3. AIGUILLE VERTICALE "AUJOURD'HUI" (toujours affichée)
        // ═══════════════════════════════════════════════════════════
        // Aiguille principale
        g.setColor(Theme.GOLD);
        g.setStroke(new BasicStroke(2f, 0, 0, 1f, new float[]{5, 3}, 0));
        g.drawLine(todayX, HDR_H, todayX, totalH);
        g.setStroke(new BasicStroke(1f));

        // Halo lumineux
        g.setColor(new Color(Theme.GOLD.getRed(), Theme.GOLD.getGreen(), Theme.GOLD.getBlue(), 18));
        g.fillRect(todayX - 3, HDR_H, 6, rowsH);

        // Pastille triangulaire en haut de l'aiguille
        int triW = 8;
        int[] txs = {todayX, todayX - triW, todayX + triW};
        int[] tys = {HDR_H + 18, HDR_H + 2, HDR_H + 2};
        g.setColor(Theme.GOLD);
        g.fillPolygon(txs, tys, 3);

        // Étiquette "AUJOURD'HUI"
        g.setFont(Theme.font(Font.BOLD, 8));
        String lbl = "AUJOURD'HUI";
        int lw = g.getFontMetrics().stringWidth(lbl);
        g.setColor(Theme.GOLD);
        g.fillRoundRect(todayX - lw / 2 - 5, HDR_H + 20, lw + 10, 13, 4, 4);
        g.setColor(new Color(18, 15, 0));
        ctr(g, lbl, todayX, HDR_H + 30);
    }

    private void drawLegend(Graphics2D g){
        if(projet==null) return;
        int sx = getScrollX();
        int y=getHeight()-30, x=sx + LBL_W+12;
        g.setFont(Theme.F_TINY);
        String[] legendL={"Terminé","En cours","En attente","Bloqué","Non commencé"};
        Color[] legendC={Theme.GREEN,Theme.ACCENT,Theme.ORANGE,Theme.RED,new Color(85,82,115)};
        for(int li=0;li<legendL.length;li++){
            Color c=legendC[li]; String lb=legendL[li];
            g.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),50)); g.fillRoundRect(x,y,12,10,4,4);
            g.setColor(c); g.drawRoundRect(x,y,12,10,4,4);
            g.setColor(Theme.TEXT_SECONDARY); g.drawString(lb,x+16,y+10);
            x+=g.getFontMetrics().stringWidth(lb)+24;
        }
    }

    private void drawEmpty(Graphics2D g){
        g.setColor(Theme.TEXT_MUTED); g.setFont(Theme.F_SUBTITLE);
        ctr(g,"Sélectionnez un projet",getWidth()/2,getHeight()/2-8);
    }

    private void ctr(Graphics2D g,String s,int cx,int y){ g.drawString(s,cx-g.getFontMetrics().stringWidth(s)/2,y); }
    private String cut(String s,int n){ return s.length()>n?s.substring(0,Math.max(1,n-1))+"…":s; }
}
