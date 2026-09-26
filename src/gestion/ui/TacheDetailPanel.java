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
import java.time.format.DateTimeParseException;

public class TacheDetailPanel extends JPanel {

    private Tache tache;
    private Projet projet;
    private final Runnable onUpdate;

    private JLabel lblNom, lblSub, lblCriticalBadge;
    private JTextField tfNom;
    private JComboBox<String> cbResp;
    private Widgets.DatePicker tfDebut, tfFin;
    private JComboBox<Tache.Statut> cbStatut;
    private JComboBox<Tache.Priorite> cbPriorite;
    private JSlider slider;
    private JLabel lblPct;
    private JTextArea taDesc;
    private JLabel lblDuree;
    private JPanel depPanel;
    private java.util.List<JCheckBox> depChecks = new java.util.ArrayList<>();
    private java.util.Set<Integer> cheminCritique = java.util.Collections.emptySet();

    public TacheDetailPanel(Runnable onUpdate) {
        this.onUpdate = onUpdate;
        setBackground(Theme.BG_SIDEBAR);
        setPreferredSize(new Dimension(300, 0));
        setMinimumSize(new Dimension(280, 0));
        setBorder(new MatteBorder(0, 1, 0, 0, Theme.BORDER));
        setLayout(new BorderLayout());
        build();
        empty();
    }

    private void build() {
        // Header
        JPanel hdr = new JPanel(new BorderLayout(Theme.GAP_SM, 0));
        hdr.setBackground(Theme.BG_CARD);
        hdr.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, Theme.BORDER),
                new EmptyBorder(Theme.GAP_MD, Theme.GAP_MD, Theme.GAP_MD, Theme.GAP_MD)));

        JPanel av = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(Theme.ACCENT.getRed(), Theme.ACCENT.getGreen(), Theme.ACCENT.getBlue(), 40));
                g2.fillOval(0, 0, 40, 40);
                g2.setColor(Theme.ACCENT);
                g2.setFont(Theme.font(Font.BOLD, 16));
                FontMetrics fm = g2.getFontMetrics();
                String s = "T";
                g2.drawString(s, (40 - fm.stringWidth(s)) / 2, (40 + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        av.setPreferredSize(new Dimension(40, 40));
        av.setOpaque(false);

        JPanel inf = new JPanel(new BorderLayout(0, 3));
        inf.setOpaque(false);
        lblNom = new JLabel("Aucune tâche");
        lblNom.setFont(Theme.F_SUBTITLE);
        lblNom.setForeground(Color.WHITE);

        JPanel subLine = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        subLine.setOpaque(false);
        lblSub = new JLabel("Cliquez sur le Gantt");
        lblSub.setFont(Theme.F_TINY);
        lblSub.setForeground(Color.WHITE);
        
        lblCriticalBadge = new JLabel(" CC ");
        lblCriticalBadge.setFont(Theme.font(Font.BOLD, 9));
        lblCriticalBadge.setForeground(Color.WHITE);
        lblCriticalBadge.setBackground(new Color(255, 60, 60));
        lblCriticalBadge.setOpaque(true);
        lblCriticalBadge.setVisible(false);
        
        subLine.add(lblSub);
        subLine.add(lblCriticalBadge);

        inf.add(lblNom, BorderLayout.CENTER);
        inf.add(subLine, BorderLayout.SOUTH);
        hdr.add(av, BorderLayout.WEST);
        hdr.add(inf, BorderLayout.CENTER);
        add(hdr, BorderLayout.NORTH);

        // Body
        JPanel body = new JPanel();
        body.setBackground(Theme.BG_SIDEBAR);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(Theme.GAP_MD, Theme.GAP_MD, Theme.GAP_LG, Theme.GAP_MD));

        sec(body, "Informations", "edit.svg");
        tfNom = tf();
        addLF(body, "Nom", tfNom);
        
        cbResp = new JComboBox<>();
        Theme.applyCombo(cbResp);
        addLF(body, "Responsable", cbResp);

        JPanel drow = new JPanel(new GridLayout(1, 2, 6, 0));
        drow.setOpaque(false);
        drow.setAlignmentX(LEFT_ALIGNMENT);
        drow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        tfDebut = new Widgets.DatePicker();
        tfFin = new Widgets.DatePicker();
        drow.add(lf("Début", tfDebut));
        drow.add(lf("Fin", tfFin));
        body.add(drow);
        body.add(Box.createVerticalStrut(4));
        lblDuree = new JLabel("—");
        lblDuree.setFont(Theme.F_TINY);
        lblDuree.setForeground(Color.WHITE);
        lblDuree.setAlignmentX(LEFT_ALIGNMENT);
        body.add(lblDuree);
        body.add(Box.createVerticalStrut(Theme.GAP_MD));

        sec(body, "Statut & Priorité", "info.svg");
        cbStatut = combo(Tache.Statut.values());
        cbPriorite = combo(Tache.Priorite.values());
        JPanel sprow = new JPanel(new GridLayout(1, 2, 6, 0));
        sprow.setOpaque(false);
        sprow.setAlignmentX(LEFT_ALIGNMENT);
        sprow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        sprow.add(lf("Statut", cbStatut));
        sprow.add(lf("Priorité", cbPriorite));
        body.add(sprow);
        body.add(Box.createVerticalStrut(Theme.GAP_MD));

        sec(body, "Antériorités", "link.svg");
        depPanel = new JPanel();
        depPanel.setOpaque(false);
        depPanel.setLayout(new BoxLayout(depPanel, BoxLayout.Y_AXIS));
        depPanel.setAlignmentX(LEFT_ALIGNMENT);
        JScrollPane depSp = new JScrollPane(depPanel);
        depSp.setBorder(new LineBorder(Theme.BORDER));
        depSp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        depSp.setAlignmentX(LEFT_ALIGNMENT);
        depSp.getViewport().setBackground(Theme.BG_SIDEBAR);
        body.add(depSp);
        body.add(Box.createVerticalStrut(Theme.GAP_MD));

        sec(body, "Progression", "gantt.svg");
        JPanel prog = new JPanel(new BorderLayout(6, 0));
        prog.setOpaque(false);
        prog.setAlignmentX(LEFT_ALIGNMENT);
        prog.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        slider = new JSlider(0, 100, 0);
        slider.setBackground(Theme.BG_SIDEBAR);
        slider.setPaintTicks(true);
        slider.setMajorTickSpacing(25);
        slider.setFont(Theme.font(Font.PLAIN, 8));
        lblPct = new JLabel("0%");
        lblPct.setFont(Theme.font(Font.BOLD, 13));
        lblPct.setForeground(Theme.ACCENT);
        lblPct.setPreferredSize(new Dimension(36, 20));
        lblPct.setHorizontalAlignment(SwingConstants.RIGHT);
        slider.addChangeListener(e -> {
            int v = slider.getValue();
            lblPct.setText(v + "%");
            lblPct.setForeground(v == 100 ? Theme.GREEN : v > 50 ? Theme.ACCENT : Theme.ORANGE);
        });
        prog.add(slider, BorderLayout.CENTER);
        prog.add(lblPct, BorderLayout.EAST);
        body.add(prog);
        body.add(Box.createVerticalStrut(Theme.GAP_MD));

        sec(body, "Description", "edit.svg");
        taDesc = new JTextArea(4, 20);
        Theme.applyTextArea(taDesc);
        JScrollPane dsp = new JScrollPane(taDesc);
        dsp.setBorder(new LineBorder(Theme.BORDER));
        dsp.setAlignmentX(LEFT_ALIGNMENT);
        dsp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        body.add(dsp);
        body.add(Box.createVerticalStrut(Theme.GAP_MD));

        Widgets.FlatButton save = new Widgets.FlatButton("Enregistrer", Theme.ACCENT);
        FlatSVGIcon iconS = Widgets.svg("/resources/icons/check.svg");
        iconS.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Theme.CYAN));
        save.setIcon(iconS);
        save.setAlignmentX(LEFT_ALIGNMENT);
        save.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        save.addActionListener(e -> saveChanges());
        Widgets.FlatButton reset = Widgets.FlatButton.outline("Annuler", Theme.TEXT_SECONDARY);
        reset.setAlignmentX(LEFT_ALIGNMENT);
        reset.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        reset.addActionListener(e -> {
            if (tache != null)
                setTache(tache, projet);
        });
        body.add(save);
        body.add(Box.createVerticalStrut(Theme.GAP_SM));
        body.add(reset);

        JScrollPane sp = Widgets.scroll(body);
        sp.getViewport().setBackground(Theme.BG_SIDEBAR);
        add(sp, BorderLayout.CENTER);
    }

    public void setCriticalPath(java.util.Set<Integer> cp) {
        this.cheminCritique = cp != null ? cp : java.util.Collections.emptySet();
        if (tache != null) {
            lblCriticalBadge.setVisible(cheminCritique.contains(tache.getId()));
        }
    }

    public void setTache(Tache t, Projet p) {
        this.tache = t;
        this.projet = p;
        if (t == null) {
            empty();
            return;
        }
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        lblNom.setText(t.getNom().length() > 22 ? t.getNom().substring(0, 20) + "…" : t.getNom());
        lblSub.setText(t.getStatut().toString().replace("_", " ") + "  •  " + t.getProgression() + "%");
        lblCriticalBadge.setVisible(cheminCritique.contains(t.getId()));
        
        set(tfNom, t.getNom());
        
        // Responsables
        cbResp.removeAllItems();
        if (p != null) {
            for (gestion.model.Membre m : p.getMembres()) cbResp.addItem(m.getNom());
        }
        if (cbResp.getItemCount() == 0) cbResp.addItem(t.getResponsable()); // Garder l'actuel si pas de membres
        cbResp.setSelectedItem(t.getResponsable());

        tfDebut.setText(t.getDateDebut().format(fmt));
        tfFin.setText(t.getDateFin().format(fmt));
        lblDuree.setText(t.getDureeJours() + " jours");
        cbStatut.setSelectedItem(t.getStatut());
        cbPriorite.setSelectedItem(t.getPriorite());
        slider.setValue(t.getProgression());
        lblPct.setText(t.getProgression() + "%");
        taDesc.setText(t.getDescription());
        
        // Dépendances
        depPanel.removeAll();
        depChecks.clear();
        if (projet != null) {
            for (Tache other : projet.getTaches()) {
                if (other.getId() == t.getId()) continue;
                JCheckBox cb = new JCheckBox(other.getNom());
                cb.setOpaque(false);
                cb.setForeground(Theme.TEXT_PRIMARY);
                cb.setFont(Theme.F_TINY);
                cb.setSelected(t.getDependances().contains(other.getId()));
                cb.putClientProperty("tacheId", other.getId());
                depChecks.add(cb);
                depPanel.add(cb);
            }
        }
        
        en(true);
        revalidate();
        repaint();
    }

    private void empty() {
        lblNom.setText("Aucune tâche sélectionnée");
        lblSub.setText("Cliquez sur une barre dans le Gantt");
        lblCriticalBadge.setVisible(false);
        set(tfNom, "");
        cbResp.removeAllItems();
        tfDebut.setText("");
        tfFin.setText("");
        lblDuree.setText("—");
        slider.setValue(0);
        lblPct.setText("0%");
        taDesc.setText("");
        depPanel.removeAll();
        en(false);
    }

    private void saveChanges() {
        if (tache == null)
            return;
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        try {
            tache.setNom(tfNom.getText().trim());
            tache.setResponsable((String) cbResp.getSelectedItem());
            tache.setDateDebut(LocalDate.parse(tfDebut.getText().trim(), fmt));
            tache.setDateFin(LocalDate.parse(tfFin.getText().trim(), fmt));
            tache.setStatut((Tache.Statut) cbStatut.getSelectedItem());
            tache.setPriorite((Tache.Priorite) cbPriorite.getSelectedItem());
            tache.setProgression(slider.getValue());
            tache.setDescription(taDesc.getText());
            
            // Sauvegarder les dépendances
            tache.getDependances().clear();
            for (JCheckBox cb : depChecks) {
                if (cb.isSelected()) {
                    tache.addDependance((Integer) cb.getClientProperty("tacheId"));
                }
            }
            
            lblDuree.setText(tache.getDureeJours() + " jours");
            lblNom.setText(tache.getNom().length() > 22 ? tache.getNom().substring(0, 20) + "…" : tache.getNom());
            lblSub.setText(tache.getStatut().toString().replace("_", " ") + "  •  " + tache.getProgression() + "%");
            if (onUpdate != null)
                onUpdate.run();
            Toast.ok("Tâche « " + tache.getNom() + " » sauvegardée");
        } catch (DateTimeParseException ex) {
            Toast.err("Format date invalide — utilisez dd/MM/yyyy");
        }
    }

    // ── Helpers ──────────────────────────────────────────────
    private JTextField tf() {
        JTextField t = new JTextField();
        Theme.applyTextField(t);
        return t;
    }

    private void set(JTextField tf, String v) {
        tf.setText(v);
        tf.setForeground(Theme.TEXT_PRIMARY);
    }

    private void en(boolean b) {
        for (JComponent c : new JComponent[] { tfNom, cbResp, tfDebut, tfFin, cbStatut, cbPriorite, slider, taDesc })
            c.setEnabled(b);
        for (JCheckBox cb : depChecks) cb.setEnabled(b);
    }

    private <T> JComboBox<T> combo(T[] items) {
        JComboBox<T> cb = new JComboBox<>(items);
        Theme.applyCombo(cb);
        return cb;
    }

    private void sec(JPanel p, String title, String iconName) {
        JLabel l = new JLabel(title.toUpperCase());
        l.setFont(Theme.font(Font.BOLD, 10));
        l.setForeground(Theme.CYAN);

        if (iconName != null) {
            FlatSVGIcon icon = Widgets.svg("/resources/icons/" + iconName, 12, 12);
            icon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Theme.CYAN));
            l.setIcon(icon);
            l.setIconTextGap(6);
        }

        l.setAlignmentX(LEFT_ALIGNMENT);
        l.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, Theme.BORDER), new EmptyBorder(4, 0, 4, 0)));
        l.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        p.add(l);
        p.add(Box.createVerticalStrut(Theme.GAP_SM));
    }

    private void addLF(JPanel p, String label, JComponent comp) {
        p.add(lf(label, comp));
        p.add(Box.createVerticalStrut(Theme.GAP_SM));
    }

    private JPanel lf(String label, JComponent comp) {
        JPanel pan = new JPanel(new BorderLayout(0, 3));
        pan.setOpaque(false);
        pan.setAlignmentX(LEFT_ALIGNMENT);
        pan.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
        JLabel l = new JLabel(label);
        l.setFont(Theme.F_LABEL);
        l.setForeground(Color.WHITE);
        pan.add(l, BorderLayout.NORTH);
        pan.add(comp, BorderLayout.CENTER);
        return pan;
    }
}
