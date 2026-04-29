package gestion.ui;

import gestion.model.*;
import gestion.util.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class EquipePanel extends JPanel {

    private Projet projet;
    private Runnable onUpdate;
    private Membre selected;

    private JPanel cardsPanel;
    private JLabel lblInfo;
    private JPanel detailPanel;

    public EquipePanel(Runnable onUpdate) {
        this.onUpdate = onUpdate;
        setBackground(Theme.BG_PANEL);
        setLayout(new BorderLayout());
        build();
    }

    public void setProjet(Projet p) { this.projet = p; selected = null; refresh(); }

    private void build() {
        add(buildToolbar(), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildCardsArea(), buildDetailArea());
        split.setDividerLocation(620);
        split.setDividerSize(1);
        split.setBackground(Theme.BORDER);
        split.setBorder(null);
        add(split, BorderLayout.CENTER);
    }

    // ── Toolbar ───────────────────────────────────────────────
    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Theme.BG_CARD);
        bar.setBorder(new CompoundBorder(new MatteBorder(0,0,1,0,Theme.BORDER), new EmptyBorder(Theme.GAP_SM,Theme.GAP_MD,Theme.GAP_SM,Theme.GAP_MD)));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, Theme.GAP_SM, 0));
        left.setOpaque(false);

        Widgets.FlatButton btnAdd    = new Widgets.FlatButton("＋  Ajouter un membre", Theme.ACCENT);
        Widgets.FlatButton btnAssign = new Widgets.FlatButton("🔗  Assigner des tâches", new Color(40,110,65));
        Widgets.FlatButton btnEdit   = new Widgets.FlatButton("✏  Modifier", new Color(55,75,140));
        Widgets.FlatButton btnDel    = new Widgets.FlatButton("✕  Retirer", Theme.RED);

        btnAdd.addActionListener(e    -> addMembre());
        btnAssign.addActionListener(e -> assignTaches());
        btnEdit.addActionListener(e   -> editMembre());
        btnDel.addActionListener(e    -> deleteMembre());

        lblInfo = new JLabel(""); lblInfo.setFont(Theme.F_TINY); lblInfo.setForeground(Theme.TEXT_MUTED);

        left.add(btnAdd); left.add(btnAssign); left.add(btnEdit); left.add(btnDel);
        left.add(Box.createHorizontalStrut(Theme.GAP_SM)); left.add(lblInfo);
        bar.add(left, BorderLayout.WEST);
        return bar;
    }

    // ── Zone cartes ───────────────────────────────────────────
    private JScrollPane buildCardsArea() {
        cardsPanel = new JPanel(new WrapLayout(FlowLayout.LEFT,12,12));
        cardsPanel.setBackground(Theme.BG_PANEL);
        cardsPanel.setBorder(new EmptyBorder(Theme.GAP_LG,Theme.GAP_MD,Theme.GAP_LG,Theme.GAP_MD));
        JScrollPane sp = Widgets.scroll(cardsPanel);
        sp.getViewport().setBackground(Theme.BG_PANEL);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        return sp;
    }

    // ── Panneau détail ────────────────────────────────────────
    private JScrollPane buildDetailArea() {
        detailPanel = new JPanel();
        detailPanel.setBackground(new Color(18,18,32));
        detailPanel.setLayout(new BoxLayout(detailPanel, BoxLayout.Y_AXIS));
        detailPanel.setBorder(new EmptyBorder(Theme.GAP_LG,Theme.GAP_MD,Theme.GAP_LG,Theme.GAP_MD));
        showDetailPlaceholder();
        JScrollPane sp = Widgets.scroll(detailPanel);
        sp.setBorder(new MatteBorder(0,1,0,0,Theme.BORDER));
        sp.getViewport().setBackground(new Color(18,18,32));
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        return sp;
    }

    // ── Refresh ───────────────────────────────────────────────
    public void refresh() {
        cardsPanel.removeAll();
        if (projet == null) {
            addEmpty(cardsPanel, "Sélectionnez un projet pour voir l'équipe");
        } else {
            List<Membre> ms = projet.getMembres();
            long dispo = ms.stream().filter(m->m.getDisponibilite()==Membre.Disponibilite.DISPONIBLE).count();
            lblInfo.setText(ms.size()+" membre(s)  •  "+dispo+" disponible(s)");
            if (ms.isEmpty()) addEmpty(cardsPanel, "Aucun membre — cliquez « Ajouter un membre »");
            else ms.forEach(m -> cardsPanel.add(buildCard(m)));
        }
        cardsPanel.revalidate(); cardsPanel.repaint();

        if (selected!=null && projet!=null && projet.getMembres().contains(selected)) showDetail(selected);
        else showDetailPlaceholder();
    }

    // ── Carte membre ─────────────────────────────────────────
    private JPanel buildCard(Membre m) {
        boolean sel = m == selected;
        Widgets.RoundPanel card = new Widgets.RoundPanel(Theme.R_MD, sel ? Theme.ACCENT : Theme.BORDER);
        card.setBackground(sel ? Theme.BG_SELECTED : Theme.BG_CARD);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(Theme.GAP_MD,Theme.GAP_MD,Theme.GAP_MD,Theme.GAP_MD));
        card.setPreferredSize(new Dimension(170,190));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Avatar
        JPanel avWrap = new JPanel(new FlowLayout(FlowLayout.CENTER,0,0)); avWrap.setOpaque(false);
        Widgets.Avatar av = new Widgets.Avatar(m.getInitiales(), m.getAvatarColor(), 58);
        av.setDot(m.getDisponibilite().getCouleur());
        avWrap.add(av); card.add(avWrap);
        card.add(Box.createVerticalStrut(Theme.GAP_SM));

        // Infos
        JLabel nom = centeredLabel(m.getNomComplet(), Theme.font(Font.BOLD,11), Theme.TEXT_PRIMARY);
        JLabel role = centeredLabel(m.getRole().getIcone()+"  "+m.getRole().getLibelle(), Theme.F_TINY, Theme.TEXT_SECONDARY);
        int nt = m.getTachesAssignees().size();
        JLabel taches = centeredLabel(nt+" tâche"+(nt!=1?"s":""), Theme.font(Font.BOLD,10), nt>0?Theme.ACCENT:Theme.TEXT_MUTED);
        Widgets.Badge dispoBadge = new Widgets.Badge(m.getDisponibilite().getLibelle(), m.getDisponibilite().getCouleur());
        dispoBadge.setAlignmentX(CENTER_ALIGNMENT);

        card.add(nom); card.add(Box.createVerticalStrut(2));
        card.add(role); card.add(Box.createVerticalStrut(6));
        card.add(taches); card.add(Box.createVerticalStrut(4));
        card.add(dispoBadge);

        card.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e){ selected=m; refresh(); }
            public void mouseEntered(MouseEvent e){ if(m!=selected){ card.setBackground(Theme.BG_CARD_HOVER); card.repaint(); } }
            public void mouseExited(MouseEvent e){ if(m!=selected){ card.setBackground(Theme.BG_CARD); card.repaint(); } }
        });
        return card;
    }

    // ── Détail membre ─────────────────────────────────────────
    private void showDetailPlaceholder() {
        detailPanel.removeAll();
        JLabel l = new JLabel("<html><center>Cliquez sur un<br>membre pour voir<br>son profil</center></html>");
        l.setForeground(Theme.TEXT_MUTED); l.setFont(Theme.F_SMALL); l.setAlignmentX(CENTER_ALIGNMENT);
        detailPanel.add(Box.createVerticalGlue()); detailPanel.add(l); detailPanel.add(Box.createVerticalGlue());
        detailPanel.revalidate(); detailPanel.repaint();
    }

    private void showDetail(Membre m) {
        detailPanel.removeAll();

        // Avatar large
        JPanel avWrap = new JPanel(new FlowLayout(FlowLayout.CENTER)); avWrap.setOpaque(false); avWrap.setAlignmentX(CENTER_ALIGNMENT);
        Widgets.Avatar av = new Widgets.Avatar(m.getInitiales(), m.getAvatarColor(), 72);
        av.setDot(m.getDisponibilite().getCouleur()); avWrap.add(av);
        detailPanel.add(avWrap);
        detailPanel.add(Box.createVerticalStrut(Theme.GAP_SM));
        detailPanel.add(centeredLabel(m.getNomComplet(), Theme.font(Font.BOLD,14), Theme.TEXT_PRIMARY));
        detailPanel.add(centeredLabel(m.getRole().toString(), Theme.F_SMALL, Theme.TEXT_SECONDARY));
        detailPanel.add(Box.createVerticalStrut(Theme.GAP_MD));

        Widgets.FlatButton btnEdit = new Widgets.FlatButton("✏  Modifier le profil", new Color(55,75,140));
        btnEdit.setAlignmentX(CENTER_ALIGNMENT); btnEdit.setMaximumSize(new Dimension(Integer.MAX_VALUE,32));
        btnEdit.addActionListener(e -> editMembre());
        detailPanel.add(btnEdit);
        detailPanel.add(Box.createVerticalStrut(Theme.GAP_MD));

        sec("📬  Contact");
        infoRow("Email",  m.getEmail().isEmpty() ? "—" : m.getEmail());
        infoRow("Tél.",   m.getTelephone().isEmpty() ? "—" : m.getTelephone());
        detailPanel.add(Box.createVerticalStrut(Theme.GAP_SM));

        sec("🟢  Disponibilité");
        JLabel dispo = new JLabel("●  "+m.getDisponibilite().getLibelle());
        dispo.setFont(Theme.font(Font.BOLD,12)); dispo.setForeground(m.getDisponibilite().getCouleur());
        dispo.setAlignmentX(LEFT_ALIGNMENT); detailPanel.add(dispo);
        detailPanel.add(Box.createVerticalStrut(Theme.GAP_SM));

        sec("⭐  Compétences");
        JLabel comp = new JLabel("<html><div style='width:190px'>"+(m.getCompetences().isEmpty()?"<i>Non renseignées</i>":m.getCompetences())+"</div></html>");
        comp.setFont(Theme.F_SMALL); comp.setForeground(Theme.TEXT_SECONDARY); comp.setAlignmentX(LEFT_ALIGNMENT);
        detailPanel.add(comp);
        detailPanel.add(Box.createVerticalStrut(Theme.GAP_SM));

        sec("📌  Tâches assignées ("+m.getTachesAssignees().size()+")");
        if (m.getTachesAssignees().isEmpty()) {
            JLabel none = new JLabel("Aucune tâche assignée"); none.setFont(Theme.F_SMALL);
            none.setForeground(Theme.TEXT_MUTED); none.setAlignmentX(LEFT_ALIGNMENT); detailPanel.add(none);
        } else {
            for (int tid : m.getTachesAssignees()) {
                if (projet!=null) projet.getTache(tid).ifPresent(t->{
                    JPanel row = new JPanel(new BorderLayout(6,0)); row.setOpaque(false);
                    row.setAlignmentX(LEFT_ALIGNMENT); row.setMaximumSize(new Dimension(Integer.MAX_VALUE,24));
                    JLabel dot = new JLabel("●"); dot.setForeground(Theme.statutColor(t.getStatut())); dot.setFont(Theme.font(Font.PLAIN,10));
                    JLabel tn  = new JLabel(t.getNom().length()>22?t.getNom().substring(0,20)+"…":t.getNom());
                    tn.setFont(Theme.F_SMALL); tn.setForeground(Theme.TEXT_PRIMARY);
                    JLabel pct = new JLabel(t.getProgression()+"%"); pct.setFont(Theme.font(Font.BOLD,10)); pct.setForeground(Theme.ACCENT);
                    row.add(dot,BorderLayout.WEST); row.add(tn,BorderLayout.CENTER); row.add(pct,BorderLayout.EAST);
                    detailPanel.add(row); detailPanel.add(Box.createVerticalStrut(3));
                });
            }
        }

        detailPanel.revalidate(); detailPanel.repaint();
    }

    // ── Actions ───────────────────────────────────────────────
    private void addMembre() {
        if (projet==null) { Toast.warn("Sélectionnez un projet d'abord"); return; }
        MembreFormDialog dlg = new MembreFormDialog(SwingUtilities.getWindowAncestor(this), null);
        dlg.setVisible(true);
        Membre r = dlg.getResult();
        if (r!=null) { projet.ajouterMembre(r); selected=r; refresh(); if(onUpdate!=null) onUpdate.run(); Toast.ok("Membre "+r.getNomComplet()+" ajouté"); }
    }

    private void editMembre() {
        if (selected==null) { Toast.warn("Sélectionnez un membre à modifier"); return; }
        MembreFormDialog dlg = new MembreFormDialog(SwingUtilities.getWindowAncestor(this), selected);
        dlg.setVisible(true);
        if (dlg.getResult()!=null) { refresh(); if(onUpdate!=null) onUpdate.run(); Toast.ok("Profil mis à jour"); }
    }

    private void assignTaches() {
        if (projet==null) { Toast.warn("Sélectionnez un projet d'abord"); return; }
        if (selected==null) { Toast.warn("Sélectionnez un membre d'abord"); return; }
        if (projet.getTaches().isEmpty()) { Toast.info("Aucune tâche dans ce projet"); return; }
        AssignTachesDialog dlg = new AssignTachesDialog(SwingUtilities.getWindowAncestor(this), selected, projet);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) { refresh(); if(onUpdate!=null) onUpdate.run(); Toast.ok("Assignations mises à jour"); }
    }

    private void deleteMembre() {
        if (selected==null) { Toast.warn("Sélectionnez un membre à retirer"); return; }
        int ok = JOptionPane.showConfirmDialog(this,"Retirer « "+selected.getNomComplet()+" » de l'équipe ?","Confirmer",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
        if (ok==JOptionPane.YES_OPTION) {
            String nom = selected.getNomComplet();
            projet.supprimerMembre(selected.getId()); selected=null; refresh();
            if(onUpdate!=null) onUpdate.run(); Toast.info(nom+" retiré de l'équipe");
        }
    }

    // ── Helpers ───────────────────────────────────────────────
    private JLabel centeredLabel(String t, Font f, Color c) {
        JLabel l = new JLabel(t, SwingConstants.CENTER); l.setFont(f); l.setForeground(c);
        l.setAlignmentX(CENTER_ALIGNMENT); l.setMaximumSize(new Dimension(Integer.MAX_VALUE,20)); return l;
    }

    private void sec(String text) {
        JLabel l = new JLabel(text.toUpperCase()); l.setFont(Theme.F_LABEL); l.setForeground(Theme.TEXT_MUTED);
        l.setAlignmentX(LEFT_ALIGNMENT);
        l.setBorder(new CompoundBorder(new MatteBorder(0,0,1,0,Theme.BORDER),new EmptyBorder(0,0,4,0)));
        l.setMaximumSize(new Dimension(Integer.MAX_VALUE,20));
        detailPanel.add(l); detailPanel.add(Box.createVerticalStrut(4));
    }

    private void infoRow(String k, String v) {
        JPanel row = new JPanel(new BorderLayout(8,0)); row.setOpaque(false); row.setAlignmentX(LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE,20));
        JLabel kl = new JLabel(k); kl.setFont(Theme.F_LABEL); kl.setForeground(Theme.TEXT_MUTED); kl.setPreferredSize(new Dimension(42,18));
        JLabel vl = new JLabel(v); vl.setFont(Theme.F_SMALL); vl.setForeground(Theme.TEXT_PRIMARY);
        row.add(kl,BorderLayout.WEST); row.add(vl,BorderLayout.CENTER);
        detailPanel.add(row); detailPanel.add(Box.createVerticalStrut(3));
    }

    private void addEmpty(JPanel p, String msg) {
        JLabel l = new JLabel(msg); l.setForeground(Theme.TEXT_MUTED); l.setFont(Theme.F_SMALL);
        l.setBorder(new EmptyBorder(Theme.GAP_XL,0,0,0));
        p.add(l);
    }

    // ── WrapLayout ────────────────────────────────────────────
    static class WrapLayout extends FlowLayout {
        WrapLayout(int align,int h,int v){super(align,h,v);}
        @Override public Dimension preferredLayoutSize(Container t){ return layout(t,true); }
        @Override public Dimension minimumLayoutSize(Container t)  { return layout(t,false); }
        private Dimension layout(Container t,boolean pref){
            synchronized(t.getTreeLock()){
                int tw=t.getSize().width; if(tw==0) tw=Integer.MAX_VALUE;
                Insets ins=t.getInsets();
                int max=tw-(ins.left+ins.right+getHgap()*2);
                Dimension dim=new Dimension(0,0);
                int rw=0,rh=0;
                for(int i=0;i<t.getComponentCount();i++){
                    Component c=t.getComponent(i); if(!c.isVisible()) continue;
                    Dimension d=pref?c.getPreferredSize():c.getMinimumSize();
                    if(rw+d.width>max){ dim.width=Math.max(dim.width,rw); dim.height+=rh+getVgap(); rw=0;rh=0; }
                    rw+=d.width+getHgap(); rh=Math.max(rh,d.height);
                }
                dim.width=Math.max(dim.width,rw);
                dim.height+=rh+ins.top+ins.bottom+getVgap()*2;
                return dim;
            }
        }
    }
}
