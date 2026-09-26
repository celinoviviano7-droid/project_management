package gestion.ui;

import gestion.model.*;
import gestion.util.*;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Panneau de gestion des jalons d'un projet
 */
public class JalonsPanel extends JPanel {

    private Projet  projet;
    private Runnable onUpdate;
    private Jalon   selected;
    private String  searchQuery = "";

    // ── UI ────────────────────────────────────────────────────
    private JPanel listPanel;
    private JLabel lblInfo;

    // Détail droit
    private JPanel detailPanel;

    public JalonsPanel(Runnable onUpdate) {
        this.onUpdate = onUpdate;
        setBackground(Theme.BG_PANEL);
        setLayout(new BorderLayout());
        build();
    }

    public void setProjet(Projet p) { projet = p; selected = null; refresh(); }

    private void build() {
        add(buildToolbar(), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
            buildListArea(), buildDetailArea());
        split.setDividerLocation(850);
        split.setResizeWeight(0.7);
        split.setDividerSize(1);
        split.setBackground(Theme.BORDER);
        split.setBorder(null);
        add(split, BorderLayout.CENTER);
    }

    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Theme.BG_CARD);
        bar.setBorder(new CompoundBorder(
            new MatteBorder(0,0,1,0,Theme.BORDER),
            new EmptyBorder(Theme.GAP_SM,Theme.GAP_MD,Theme.GAP_SM,Theme.GAP_MD)));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, Theme.GAP_SM, 0));
        left.setOpaque(false);

        JTextField searchField = Widgets.searchField("Rechercher un jalon...", q -> {
            searchQuery = q.toLowerCase();
            refresh();
        });
        left.add(searchField);
        left.add(Box.createHorizontalStrut(Theme.GAP_SM));

        Widgets.FlatButton btnAdd  = new Widgets.FlatButton("Nouveau jalon", Theme.GOLD);
        FlatSVGIcon iconM = Widgets.svg("/resources/icons/milestone.svg");
        iconM.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Theme.CYAN));
        btnAdd.setIcon(iconM);
        btnAdd.setForeground(new Color(18,15,0));
        btnAdd.addActionListener(e -> addJalon());

        Widgets.FlatButton btnEdit = new Widgets.FlatButton("Modifier", new Color(55,75,140));
        FlatSVGIcon iconE = Widgets.svg("/resources/icons/edit.svg");
        iconE.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Theme.CYAN));
        btnEdit.setIcon(iconE);
        btnEdit.addActionListener(e -> editJalon());

        Widgets.FlatButton btnDel  = new Widgets.FlatButton("Supprimer", Theme.RED);
        FlatSVGIcon iconD = Widgets.svg("/resources/icons/trash.svg");
        iconD.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Theme.CYAN));
        btnDel.setIcon(iconD);
        btnDel.addActionListener(e -> deleteJalon());

        lblInfo = new JLabel("");
        lblInfo.setFont(Theme.F_TINY); lblInfo.setForeground(Theme.TEXT_MUTED);

        left.add(btnAdd); left.add(btnEdit); left.add(btnDel);
        left.add(Box.createHorizontalStrut(Theme.GAP_SM)); left.add(lblInfo);
        bar.add(left, BorderLayout.WEST);

        JLabel hint = new JLabel("Un jalon est un événement ponctuel — livraison, validation, décision clé");
        FlatSVGIcon iconH = Widgets.svg("/resources/icons/info.svg", 12, 12);
        iconH.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Theme.TEXT_MUTED));
        hint.setIcon(iconH); hint.setIconTextGap(6);
        hint.setFont(Theme.F_TINY); hint.setForeground(Theme.TEXT_MUTED);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT,0,0)); right.setOpaque(false);
        right.add(hint);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JScrollPane buildListArea() {
        listPanel = new JPanel();
        listPanel.setBackground(Theme.BG_PANEL);
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBorder(new EmptyBorder(Theme.GAP_MD, Theme.GAP_MD, Theme.GAP_MD, Theme.GAP_MD));

        JScrollPane sp = Widgets.scroll(listPanel);
        sp.getViewport().setBackground(Theme.BG_PANEL);
        return sp;
    }

    private JScrollPane buildDetailArea() {
        detailPanel = new JPanel();
        detailPanel.setBackground(Theme.BG_PANEL);
        detailPanel.setLayout(new BoxLayout(detailPanel, BoxLayout.Y_AXIS));
        detailPanel.setBorder(new EmptyBorder(Theme.GAP_LG, Theme.GAP_MD, Theme.GAP_LG, Theme.GAP_MD));

        showDetailPlaceholder();

        JScrollPane sp = Widgets.scroll(detailPanel);
        sp.setBorder(new MatteBorder(0,1,0,0,Theme.BORDER));
        sp.getViewport().setBackground(Theme.BG_PANEL);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        return sp;
    }

    public void refresh() {
        listPanel.removeAll();
        if (projet == null) {
            addEmpty("Sélectionnez un projet pour voir ses jalons");
        } else {
            List<Jalon> jalons = projet.getJalons();
            long atteints = jalons.stream().filter(j->j.getStatut()==Jalon.Statut.ATTEINT).count();
            long manques  = jalons.stream().filter(j->j.getStatut()==Jalon.Statut.MANQUE).count();
            lblInfo.setText(jalons.size()+" jalon(s)  •  "+atteints+" atteint(s)");
            if (manques > 0) {
                lblInfo.setText(lblInfo.getText() + "  •  " + manques + " manqué(s)");
                lblInfo.setForeground(Theme.RED);
            } else {
                lblInfo.setForeground(Theme.TEXT_MUTED);
            }

            if (jalons.isEmpty()) {
                addEmpty("Aucun jalon — cliquez « Nouveau jalon »");
            } else {
                List<Jalon> filtered = jalons;
                if (!searchQuery.isEmpty()) {
                    filtered = jalons.stream()
                        .filter(j -> j.getNom().toLowerCase().contains(searchQuery)
                                || j.getType().getLibelle().toLowerCase().contains(searchQuery))
                        .collect(Collectors.toList());
                }

                if (filtered.isEmpty() && !searchQuery.isEmpty()) {
                    addEmpty("Aucun jalon ne correspond à votre recherche");
                } else if (filtered.isEmpty()) {
                    addEmpty("Aucun jalon — cliquez « Nouveau jalon »");
                } else {
                    List<Jalon> passes = filtered.stream()
                        .filter(j -> j.getDate().isBefore(LocalDate.now()))
                        .sorted(Comparator.comparing(Jalon::getDate).reversed())
                        .collect(Collectors.toList());
                    List<Jalon> avenir = filtered.stream()
                        .filter(j -> !j.getDate().isBefore(LocalDate.now()))
                        .sorted(Comparator.comparing(Jalon::getDate))
                        .collect(Collectors.toList());

                    if (!avenir.isEmpty()) {
                        listPanel.add(groupHeader("À venir", avenir.size()));
                        for (Jalon j : avenir) listPanel.add(buildJalonRow(j));
                        listPanel.add(Box.createVerticalStrut(Theme.GAP_MD));
                    }
                    if (!passes.isEmpty()) {
                        listPanel.add(groupHeader("Passés", passes.size()));
                        for (Jalon j : passes) listPanel.add(buildJalonRow(j));
                    }
                }
            }
        }
        listPanel.revalidate(); listPanel.repaint();

        if (selected != null && projet != null && projet.getJalons().contains(selected))
            showDetail(selected);
        else
            showDetailPlaceholder();
    }

    private JPanel buildJalonRow(Jalon j) {
        boolean sel = j == selected;
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy");
        boolean passee = j.estPassee();

        JPanel row = new JPanel(new BorderLayout(Theme.GAP_MD, 0));
        row.setBackground(sel ? Theme.BG_SELECTED : Theme.BG_CARD);
        row.setBorder(new CompoundBorder(
            new MatteBorder(0,0,1,0,Theme.BORDER),
            new EmptyBorder(12, Theme.GAP_MD, 12, Theme.GAP_MD)
        ));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel diamond = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = j.getStatut().getCouleur();
                int cx=getWidth()/2, cy=getHeight()/2, r=14;
                int[] xs={cx,cx+r,cx,cx-r}, ys={cy-r,cy,cy+r,cy};
                g2.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),45)); g2.fillPolygon(xs,ys,4);
                if(j.getStatut()==Jalon.Statut.ATTEINT){ g2.setColor(c); g2.fillPolygon(xs,ys,4); }
                g2.setColor(c); g2.setStroke(new BasicStroke(1.8f)); g2.drawPolygon(xs,ys,4);
                if(j.getStatut()==Jalon.Statut.ATTEINT){ g2.setColor(Color.WHITE); g2.fillOval(cx-3,cy-3,6,6); }
                g2.setFont(Theme.font(Font.PLAIN,10)); g2.setColor(c);
                String ic=j.getType().getIcone();
                FontMetrics fm=g2.getFontMetrics();
                g2.drawString(ic,cx-fm.stringWidth(ic)/2,cy+fm.getAscent()/2-2);
                g2.dispose();
            }
        };
        diamond.setPreferredSize(new Dimension(36,44)); diamond.setOpaque(false);

        JPanel info = new JPanel(new BorderLayout(0, 3)); info.setOpaque(false);
        JLabel nom = new JLabel(j.getNom());
        nom.setFont(Theme.font(Font.BOLD, 12));
        nom.setForeground(sel ? Color.WHITE : passee&&j.getStatut()!=Jalon.Statut.ATTEINT
            ? Theme.TEXT_SECONDARY : Theme.TEXT_PRIMARY);

        JPanel subRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0)); subRow.setOpaque(false);
        JLabel dateL = new JLabel("📅 " + j.getDate().format(fmt));
        dateL.setFont(Theme.F_TINY); dateL.setForeground(Theme.TEXT_SECONDARY);
        Widgets.Badge badge = new Widgets.Badge(j.getStatut().getLibelle(), j.getStatut().getCouleur());
        Widgets.Badge typeBadge = new Widgets.Badge(j.getType().getLibelle(), Theme.GOLD);
        subRow.add(dateL); subRow.add(badge); subRow.add(typeBadge);

        info.add(nom, BorderLayout.NORTH); info.add(subRow, BorderLayout.SOUTH);

        long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), j.getDate());
        String dist = daysLeft == 0 ? "Aujourd'hui !"
            : daysLeft > 0 ? "Dans "+daysLeft+" j"
            : "Il y a "+Math.abs(daysLeft)+" j";
        Color distColor = daysLeft == 0 ? Theme.GOLD
            : daysLeft > 0 && daysLeft <= 7 ? Theme.ORANGE
            : j.getStatut()==Jalon.Statut.ATTEINT ? Theme.GREEN
            : passee ? Theme.RED : Theme.TEXT_MUTED;
        JLabel distLbl = new JLabel(dist);
        distLbl.setFont(Theme.font(Font.BOLD, 10));
        distLbl.setForeground(distColor);
        distLbl.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(diamond, BorderLayout.WEST);
        row.add(info,    BorderLayout.CENTER);
        row.add(distLbl, BorderLayout.EAST);

        row.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e) { selected = j; refresh(); }
            public void mouseEntered(MouseEvent e) { if(j!=selected){ row.setBackground(Theme.BG_CARD_HOVER); } }
            public void mouseExited(MouseEvent e) { if(j!=selected){ row.setBackground(Theme.BG_CARD); } }
        });
        return row;
    }

    private void showDetailPlaceholder() {
        detailPanel.removeAll();
        JLabel l = new JLabel("<html><center>Cliquez sur un jalon<br>pour voir ses détails</center></html>");
        l.setForeground(Theme.TEXT_MUTED); l.setFont(Theme.F_SMALL);
        l.setAlignmentX(CENTER_ALIGNMENT);
        detailPanel.add(Box.createVerticalGlue());
        detailPanel.add(l);
        detailPanel.add(Box.createVerticalGlue());
        detailPanel.revalidate(); detailPanel.repaint();
    }

    private void showDetail(Jalon j) {
        detailPanel.removeAll();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMMM yyyy");

        JPanel bigDiamond = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = j.getStatut().getCouleur();
                int cx=getWidth()/2, cy=getHeight()/2, r=28;
                int[] xs={cx,cx+r,cx,cx-r}, ys={cy-r,cy,cy+r,cy};
                g2.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),40)); g2.fillPolygon(xs,ys,4);
                if(j.getStatut()==Jalon.Statut.ATTEINT){
                    GradientPaint gp=new GradientPaint(cx,cy-r,c.brighter(),cx,cy+r,c);
                    g2.setPaint(gp); g2.fillPolygon(xs,ys,4);
                }
                g2.setColor(c); g2.setStroke(new BasicStroke(2.2f)); g2.drawPolygon(xs,ys,4);
                if(j.getStatut()==Jalon.Statut.ATTEINT){ g2.setColor(Color.WHITE); g2.fillOval(cx-5,cy-5,10,10); }
                g2.setFont(Theme.font(Font.PLAIN,16));
                String ic=j.getType().getIcone(); FontMetrics fm=g2.getFontMetrics();
                g2.setColor(c.brighter()); g2.drawString(ic,cx-fm.stringWidth(ic)/2,cy+fm.getAscent()/2-2);
                g2.dispose();
            }
        };
        bigDiamond.setPreferredSize(new Dimension(70,70)); bigDiamond.setOpaque(false);
        bigDiamond.setAlignmentX(CENTER_ALIGNMENT);
        JPanel dw = new JPanel(new FlowLayout(FlowLayout.CENTER)); dw.setOpaque(false); dw.setAlignmentX(CENTER_ALIGNMENT);
        dw.add(bigDiamond);
        detailPanel.add(dw);
        detailPanel.add(Box.createVerticalStrut(Theme.GAP_SM));

        cLabel(j.getNom(), Theme.font(Font.BOLD,14), Theme.TEXT_PRIMARY);
        JPanel badges = new JPanel(new FlowLayout(FlowLayout.CENTER,4,0)); badges.setOpaque(false); badges.setAlignmentX(CENTER_ALIGNMENT);
        badges.add(new Widgets.Badge(j.getStatut().getLibelle(), j.getStatut().getCouleur()));
        badges.add(new Widgets.Badge(j.getType().getLibelle(), Theme.GOLD));
        detailPanel.add(badges);
        detailPanel.add(Box.createVerticalStrut(Theme.GAP_MD));

        Widgets.FlatButton btnEdit = new Widgets.FlatButton("Modifier ce jalon", new Color(55,75,140));
        FlatSVGIcon iconE2 = Widgets.svg("/resources/icons/edit.svg");
        iconE2.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Theme.CYAN));
        btnEdit.setIcon(iconE2);
        btnEdit.setAlignmentX(CENTER_ALIGNMENT); btnEdit.setMaximumSize(new Dimension(Integer.MAX_VALUE,32));
        btnEdit.addActionListener(e -> editJalon());
        detailPanel.add(btnEdit);
        detailPanel.add(Box.createVerticalStrut(Theme.GAP_LG));

        sec("Date");
        infoRow("Date",   j.getDate().format(fmt));
        long dl = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), j.getDate());
        String dist = dl==0?"Aujourd'hui !":dl>0?"Dans "+dl+" jours":"Il y a "+Math.abs(dl)+" jours";
        infoRow("Délai",  dist);
        detailPanel.add(Box.createVerticalStrut(Theme.GAP_SM));

        sec("Classification");
        infoRow("Type",   j.getType().getIcone()+"  "+j.getType().getLibelle());
        infoRow("Statut", j.getStatut().getLibelle());
        detailPanel.add(Box.createVerticalStrut(Theme.GAP_SM));

        if (j.getTacheLieeId() > 0 && projet != null) {
            sec("Tâche liée");
            projet.getTache(j.getTacheLieeId()).ifPresent(t -> {
                JPanel trow = new JPanel(new BorderLayout(6,0)); trow.setOpaque(false);
                trow.setAlignmentX(LEFT_ALIGNMENT); trow.setMaximumSize(new Dimension(Integer.MAX_VALUE,22));
                JLabel dot = new JLabel("●"); dot.setForeground(Theme.statutColor(t.getStatut())); dot.setFont(Theme.font(Font.PLAIN,10));
                JLabel tn  = new JLabel(t.getNom()); tn.setFont(Theme.F_SMALL); tn.setForeground(Theme.TEXT_PRIMARY);
                JLabel pct = new JLabel(t.getProgression()+"%"); pct.setFont(Theme.font(Font.BOLD,10)); pct.setForeground(Theme.ACCENT);
                trow.add(dot,BorderLayout.WEST); trow.add(tn,BorderLayout.CENTER); trow.add(pct,BorderLayout.EAST);
                detailPanel.add(trow);
            });
            detailPanel.add(Box.createVerticalStrut(Theme.GAP_SM));
        }

        if (!j.getDescription().isEmpty()) {
            sec("Description");
            JLabel desc = new JLabel("<html><div style='width:210px'>"+j.getDescription()+"</div></html>");
            desc.setFont(Theme.F_SMALL); desc.setForeground(Theme.TEXT_SECONDARY);
            desc.setAlignmentX(LEFT_ALIGNMENT);
            detailPanel.add(desc);
        }

        detailPanel.revalidate(); detailPanel.repaint();
    }

    private void addJalon() {
        if (projet==null) { Toast.warn("Sélectionnez un projet d'abord"); return; }
        Frame owner = (Frame)SwingUtilities.getWindowAncestor(this);
        JalonFormDialog dlg = new JalonFormDialog(owner, null, projet);
        dlg.setVisible(true);
        Jalon r = dlg.getResult();
        if (r!=null) {
            projet.ajouterJalon(r);
            selected = r;
            refresh();
            if (onUpdate!=null) onUpdate.run();
            Toast.ok("Jalon « "+r.getNom()+" » créé");
        }
    }

    private void editJalon() {
        if (selected==null) { Toast.warn("Sélectionnez un jalon à modifier"); return; }
        Frame owner = (Frame)SwingUtilities.getWindowAncestor(this);
        JalonFormDialog dlg = new JalonFormDialog(owner, selected, projet);
        dlg.setVisible(true);
        if (dlg.getResult()!=null) {
            refresh();
            if (onUpdate!=null) onUpdate.run();
            Toast.ok("Jalon mis à jour");
        }
    }

    private void deleteJalon() {
        if (selected==null) { Toast.warn("Sélectionnez un jalon à supprimer"); return; }
        int ok = JOptionPane.showConfirmDialog(this,
            "Supprimer le jalon « "+selected.getNom()+" » ?",
            "Confirmer", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok==JOptionPane.YES_OPTION) {
            String nom = selected.getNom();
            projet.supprimerJalon(selected.getId());
            selected = null;
            refresh();
            if (onUpdate!=null) onUpdate.run();
            Toast.info("Jalon « "+nom+" » supprimé");
        }
    }

    private JPanel groupHeader(String title, int count) {
        JPanel p = new JPanel(new BorderLayout(8,0)); p.setOpaque(false);
        p.setAlignmentX(LEFT_ALIGNMENT); p.setMaximumSize(new Dimension(Integer.MAX_VALUE,26));
        p.setBorder(new EmptyBorder(4,0,6,0));
        JLabel l = new JLabel(title.toUpperCase());
        l.setFont(Theme.F_LABEL); l.setForeground(Theme.TEXT_MUTED);
        Widgets.Badge badge = new Widgets.Badge(""+count, Theme.TEXT_MUTED);
        p.add(l,BorderLayout.WEST); p.add(badge,BorderLayout.EAST);
        return p;
    }

    private void addEmpty(String msg) {
        JLabel l = new JLabel(msg); l.setForeground(Theme.TEXT_MUTED); l.setFont(Theme.F_SMALL);
        l.setBorder(new EmptyBorder(Theme.GAP_XL,0,0,0)); listPanel.add(l);
    }

    private void cLabel(String text, Font f, Color c) {
        JLabel l = new JLabel(text, SwingConstants.CENTER); l.setFont(f); l.setForeground(c);
        l.setAlignmentX(CENTER_ALIGNMENT); l.setMaximumSize(new Dimension(Integer.MAX_VALUE,22));
        detailPanel.add(l);
    }

    private void sec(String text) {
        JLabel l = new JLabel(text.toUpperCase()); l.setFont(Theme.F_LABEL); l.setForeground(Theme.TEXT_MUTED);
        l.setAlignmentX(LEFT_ALIGNMENT);
        l.setBorder(new CompoundBorder(new MatteBorder(0,0,1,0,Theme.BORDER),new EmptyBorder(0,0,4,0)));
        l.setMaximumSize(new Dimension(Integer.MAX_VALUE,20));
        detailPanel.add(l); detailPanel.add(Box.createVerticalStrut(4));
    }

    private void infoRow(String k, String v) {
        JPanel row = new JPanel(new BorderLayout(8,0)); row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT); row.setMaximumSize(new Dimension(Integer.MAX_VALUE,20));
        JLabel kl = new JLabel(k); kl.setFont(Theme.F_LABEL); kl.setForeground(Theme.TEXT_MUTED);
        kl.setPreferredSize(new Dimension(50,18));
        JLabel vl = new JLabel(v); vl.setFont(Theme.F_SMALL); vl.setForeground(Theme.TEXT_PRIMARY);
        row.add(kl,BorderLayout.WEST); row.add(vl,BorderLayout.CENTER);
        detailPanel.add(row); detailPanel.add(Box.createVerticalStrut(3));
    }
}
