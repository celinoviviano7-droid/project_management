package gestion.ui;

import gestion.model.*;
import gestion.util.Theme;
import gestion.util.Widgets;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class AssignTachesDialog extends JDialog {

    private final Membre membre;
    private final Projet projet;
    private boolean confirmed;
    private final Map<Integer,JCheckBox> boxes = new LinkedHashMap<>();

    public AssignTachesDialog(Window parent, Membre membre, Projet projet) {
        super(parent, "Assigner des tâches — "+membre.getNomComplet(), ModalityType.APPLICATION_MODAL);
        this.membre = membre; this.projet = projet;
        setPreferredSize(new Dimension(540, 540));
        build(); pack();
        setLocationRelativeTo(null);
    }

    private void build() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG_PANEL);

        // ── Header ──────────────────────────────────────────
        JPanel hdr = new JPanel(new BorderLayout(Theme.GAP_SM,0));
        hdr.setBackground(Theme.BG_TOPBAR);
        hdr.setBorder(new CompoundBorder(new MatteBorder(0,0,1,0,Theme.BORDER),new EmptyBorder(14,18,14,18)));

        Widgets.Avatar av = new Widgets.Avatar(membre.getInitiales(), membre.getAvatarColor(), 46);
        JPanel inf = new JPanel(new BorderLayout(0,3)); inf.setOpaque(false);
        JLabel n = new JLabel(membre.getNomComplet()); n.setFont(Theme.F_SUBTITLE); n.setForeground(Theme.TEXT_PRIMARY);
        JLabel r = new JLabel(membre.getRole()+"  •  "+membre.getDisponibilite()); r.setFont(Theme.F_TINY); r.setForeground(Theme.TEXT_SECONDARY);
        JLabel inst = new JLabel("Cochez les tâches à assigner à ce membre"); inst.setFont(Theme.F_TINY); inst.setForeground(new Color(150,130,210));
        inf.add(n, BorderLayout.NORTH); inf.add(r, BorderLayout.CENTER); inf.add(inst, BorderLayout.SOUTH);
        hdr.add(av, BorderLayout.WEST); hdr.add(inf, BorderLayout.CENTER);
        root.add(hdr, BorderLayout.NORTH);

        // ── Liste tâches ─────────────────────────────────────
        JPanel list = new JPanel();
        list.setBackground(Theme.BG_PANEL);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBorder(new EmptyBorder(Theme.GAP_SM, Theme.GAP_MD, Theme.GAP_SM, Theme.GAP_MD));

        // Stats
        long assigned = projet.getTaches().stream().filter(t -> membre.getTachesAssignees().contains(t.getId())).count();
        JPanel stats = new JPanel(new FlowLayout(FlowLayout.LEFT,14,0));
        stats.setOpaque(false); stats.setAlignmentX(LEFT_ALIGNMENT);
        addStat(stats, ""+projet.getTaches().size(), "tâches", Theme.TEXT_PRIMARY);
        addStat(stats, ""+assigned,                  "assignées", Theme.ACCENT);
        list.add(stats);
        list.add(Box.createVerticalStrut(Theme.GAP_SM));

        // Groupes
        List<Tache> actives  = projet.getTaches().stream().filter(t->t.getStatut()!=Tache.Statut.TERMINE).collect(java.util.stream.Collectors.toList());
        List<Tache> termines = projet.getTaches().stream().filter(t->t.getStatut()==Tache.Statut.TERMINE).collect(java.util.stream.Collectors.toList());

        if (!actives.isEmpty()) {
            list.add(groupLabel("En cours & À faire"));
            for (Tache t : actives) list.add(taskRow(t));
        }
        if (!termines.isEmpty()) {
            list.add(Box.createVerticalStrut(Theme.GAP_SM));
            list.add(groupLabel("Terminées"));
            for (Tache t : termines) list.add(taskRow(t));
        }

        JScrollPane sp = Widgets.scroll(list); sp.getViewport().setBackground(Theme.BG_PANEL);
        root.add(sp, BorderLayout.CENTER);

        // ── Footer ──────────────────────────────────────────
        JPanel foot = new JPanel(new BorderLayout());
        foot.setBackground(Theme.BG_TOPBAR);
        foot.setBorder(new MatteBorder(1,0,0,0,Theme.BORDER));

        JPanel quickBtns = new JPanel(new FlowLayout(FlowLayout.LEFT,8,10));
        quickBtns.setBackground(Theme.BG_TOPBAR);
        JButton all  = smallBtn("Tout sélectionner");  all.addActionListener(e->boxes.values().forEach(b->b.setSelected(true)));
        JButton none = smallBtn("Tout désélectionner"); none.addActionListener(e->boxes.values().forEach(b->b.setSelected(false)));
        quickBtns.add(all); quickBtns.add(none);
        foot.add(quickBtns, BorderLayout.WEST);

        JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,10));
        rightBtns.setBackground(Theme.BG_TOPBAR);
        Widgets.FlatButton cancel = new Widgets.FlatButton("Annuler", new Color(48,48,72));
        cancel.addActionListener(e->dispose());
        Widgets.FlatButton confirm = new Widgets.FlatButton("Confirmer", Theme.ACCENT);
        FlatSVGIcon iconC = Widgets.svg("/resources/icons/check.svg", 14, 14);
        iconC.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
        confirm.setIcon(iconC); confirm.setIconTextGap(6);
        confirm.addActionListener(e->handleConfirm());
        rightBtns.add(cancel); rightBtns.add(confirm);
        foot.add(rightBtns, BorderLayout.EAST);
        root.add(foot, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JPanel taskRow(Tache t) {
        boolean checked = membre.getTachesAssignees().contains(t.getId());
        JPanel row = new JPanel(new BorderLayout(8,0));
        row.setBackground(checked ? new Color(30,40,80) : Theme.BG_CARD);
        row.setBorder(new CompoundBorder(new MatteBorder(0,0,1,0,Theme.BORDER),new EmptyBorder(8,10,8,10)));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE,52));
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JCheckBox cb = new JCheckBox(); cb.setSelected(checked); cb.setOpaque(false); cb.setFocusPainted(false);
        boxes.put(t.getId(), cb);

        Color sc = Theme.statutColor(t.getStatut());
        JLabel dot = new JLabel("●"); dot.setForeground(sc); dot.setFont(Theme.font(Font.PLAIN,11));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT,4,0)); left.setOpaque(false);
        left.add(cb); left.add(dot);

        JPanel info = new JPanel(new BorderLayout(0,2)); info.setOpaque(false);
        JLabel nm = new JLabel(t.getNom()); nm.setFont(Theme.F_SUBTITLE); nm.setForeground(Theme.TEXT_PRIMARY);
        JLabel si = new JLabel(t.getStatut().toString().replace("_"," ")+"  •  "+t.getProgression()+"%  •  "+t.getDureeJours()+" jours");
        si.setFont(Theme.F_TINY); si.setForeground(Theme.TEXT_SECONDARY);
        info.add(nm, BorderLayout.NORTH); info.add(si, BorderLayout.SOUTH);

        Color pc = Theme.prioriteColor(t.getPriorite());
        Widgets.Badge prio = new Widgets.Badge(t.getPriorite().toString(), pc);

        row.add(left, BorderLayout.WEST); row.add(info, BorderLayout.CENTER); row.add(prio, BorderLayout.EAST);

        MouseAdapter ma = new MouseAdapter(){
            public void mouseClicked(MouseEvent e){ cb.setSelected(!cb.isSelected()); row.setBackground(cb.isSelected()?new Color(30,40,80):Theme.BG_CARD); }
            public void mouseEntered(MouseEvent e){ if(!cb.isSelected()) row.setBackground(Theme.BG_CARD_HOVER); }
            public void mouseExited(MouseEvent e){ row.setBackground(cb.isSelected()?new Color(30,40,80):Theme.BG_CARD); }
        };
        row.addMouseListener(ma);
        return row;
    }

    private void handleConfirm() {
        for (Map.Entry<Integer,JCheckBox> e : boxes.entrySet()) {
            int id = e.getKey();
            if (e.getValue().isSelected()) {
                membre.assignerTache(id);
                projet.getTache(id).ifPresent(t->t.setResponsable(membre.getNomComplet()));
            } else {
                membre.retirerTache(id);
            }
        }
        confirmed = true;
        dispose();
    }

    private JPanel groupLabel(String text) {
        JPanel p = new JPanel(new BorderLayout()); p.setOpaque(false); p.setAlignmentX(LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE,22));
        JLabel l = new JLabel(text.toUpperCase()); l.setFont(Theme.F_LABEL); l.setForeground(Theme.TEXT_MUTED);
        l.setBorder(new EmptyBorder(4,0,4,0)); p.add(l, BorderLayout.WEST); return p;
    }

    private void addStat(JPanel p, String val, String lbl, Color c) {
        JPanel s = new JPanel(new FlowLayout(FlowLayout.LEFT,3,0)); s.setOpaque(false);
        JLabel v = new JLabel(val); v.setFont(Theme.font(Font.BOLD,16)); v.setForeground(c);
        JLabel l = new JLabel(lbl); l.setFont(Theme.F_TINY); l.setForeground(Theme.TEXT_SECONDARY);
        s.add(v); s.add(l); p.add(s);
    }

    private JButton smallBtn(String text) {
        JButton b = new JButton(text); b.setBackground(new Color(38,38,62));
        b.setForeground(Theme.TEXT_SECONDARY); b.setFont(Theme.F_TINY);
        b.setBorder(new EmptyBorder(4,10,4,10)); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); return b;
    }

    public boolean isConfirmed() { return confirmed; }
}
