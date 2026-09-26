package gestion.ui;

import gestion.model.Membre;
import gestion.model.Tache;
import gestion.util.Theme;
import gestion.util.Widgets;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class NouveauTacheDialog extends JDialog {

    private JTextField tfNom;
    private JComboBox<String> cbResp;
    private Widgets.DatePicker tfDebut, tfFin;
    private JComboBox<Tache.Priorite> cbPrio;
    private JTextArea taDesc;
    private List<JCheckBox> depChecks = new ArrayList<>();
    private Tache result;
    private List<Tache> tachesExistantes;
    private List<Membre> membres;

    public NouveauTacheDialog(Frame parent, List<Tache> tachesExistantes, List<Membre> membres) {
        super(parent, "Nouvelle tâche", true);
        this.tachesExistantes = tachesExistantes;
        this.membres = membres;
        setPreferredSize(new Dimension(460, 600));
        build();
        pack();
        setLocationRelativeTo(parent);
    }

    private void build() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG_PANEL);

        // ── Header ──────────────────────────────────────────
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setBackground(Theme.BG_TOPBAR);
        hdr.setBorder(new CompoundBorder(
            new MatteBorder(0,0,1,0,Theme.BORDER),
            new EmptyBorder(16,20,16,20)));
        JLabel title = new JLabel("NOUVELLE TÂCHE");
        FlatSVGIcon iconT = Widgets.svg("/resources/icons/plus.svg", 16, 16);
        iconT.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Theme.CYAN));
        title.setIcon(iconT); title.setIconTextGap(8);
        title.setFont(Theme.F_SUBTITLE); title.setForeground(Theme.TEXT_PRIMARY);
        JLabel sub = new JLabel("Remplissez les informations de la tâche");
        sub.setFont(Theme.F_TINY); sub.setForeground(Theme.TEXT_SECONDARY);
        JPanel tinfo = new JPanel(new BorderLayout(0,3)); tinfo.setOpaque(false);
        tinfo.add(title, BorderLayout.NORTH); tinfo.add(sub, BorderLayout.SOUTH);
        hdr.add(tinfo, BorderLayout.CENTER);
        root.add(hdr, BorderLayout.NORTH);

        // ── Formulaire ──────────────────────────────────────
        JPanel form = new JPanel();
        form.setBackground(Theme.BG_PANEL);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(Theme.GAP_LG, Theme.GAP_LG, Theme.GAP_MD, Theme.GAP_LG));

        tfNom  = addField(form, "Nom de la tâche *", "Ex : Développement module authentification");
        
        // Responsable (Combo)
        JPanel rrow = new JPanel(new BorderLayout(0,4)); rrow.setOpaque(false);
        rrow.setAlignmentX(LEFT_ALIGNMENT); rrow.setMaximumSize(new Dimension(Integer.MAX_VALUE,56));
        rrow.add(lbl("Responsable"), BorderLayout.NORTH);
        List<String> noms = new ArrayList<>();
        if (membres != null) {
            for (Membre m : membres) noms.add(m.getNom());
        }
        if (noms.isEmpty()) noms.add("Aucun membre défini");
        cbResp = new JComboBox<>(noms.toArray(new String[0]));
        Theme.applyCombo(cbResp);
        rrow.add(cbResp, BorderLayout.CENTER);
        form.add(rrow); form.add(Box.createVerticalStrut(Theme.GAP_SM));

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate now = LocalDate.now();
        tfDebut = addDatePicker(form, "Date de début *", now);
        tfFin   = addDatePicker(form, "Date de fin *",   now.plusDays(14));

        // Priorité
        JPanel prow = new JPanel(new BorderLayout(0,4)); prow.setOpaque(false);
        prow.setAlignmentX(LEFT_ALIGNMENT); prow.setMaximumSize(new Dimension(Integer.MAX_VALUE,56));
        JLabel lp = lbl("Priorité"); prow.add(lp, BorderLayout.NORTH);
        cbPrio = new JComboBox<>(Tache.Priorite.values());
        cbPrio.setSelectedItem(Tache.Priorite.NORMALE);
        Theme.applyCombo(cbPrio);
        prow.add(cbPrio, BorderLayout.CENTER);
        form.add(prow); form.add(Box.createVerticalStrut(Theme.GAP_SM));

        // Dépendances
        if (!tachesExistantes.isEmpty()) {
            form.add(lbl("Dépendances"));
            for (Tache task : tachesExistantes) {
                JCheckBox cb = new JCheckBox(task.getNom());
                cb.setOpaque(false); cb.setForeground(Theme.TEXT_PRIMARY);
                depChecks.add(cb);
                form.add(cb);
            }
            form.add(Box.createVerticalStrut(Theme.GAP_SM));
        }

        // Description
        JPanel drow = new JPanel(new BorderLayout(0,4)); drow.setOpaque(false);
        drow.setAlignmentX(LEFT_ALIGNMENT); drow.setMaximumSize(new Dimension(Integer.MAX_VALUE,100));
        drow.add(lbl("Description (optionnelle)"), BorderLayout.NORTH);
        taDesc = new JTextArea(3, 20); Theme.applyTextArea(taDesc);
        JScrollPane dsp = new JScrollPane(taDesc);
        dsp.setBorder(new LineBorder(Theme.BORDER));
        drow.add(dsp, BorderLayout.CENTER);
        form.add(drow);

        JScrollPane fsp = Widgets.scroll(form);
        fsp.getViewport().setBackground(Theme.BG_PANEL);
        root.add(fsp, BorderLayout.CENTER);

        // ── Footer ──────────────────────────────────────────
        JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        foot.setBackground(Theme.BG_TOPBAR);
        foot.setBorder(new MatteBorder(1,0,0,0,Theme.BORDER));
        Widgets.FlatButton cancel = new Widgets.FlatButton("Annuler", new Color(48,48,72));
        cancel.addActionListener(e -> dispose());
        Widgets.FlatButton create = new Widgets.FlatButton("Créer la tâche", Theme.ACCENT);
        FlatSVGIcon iconC = Widgets.svg("/resources/icons/check.svg", 14, 14);
        iconC.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
        create.setIcon(iconC); create.setIconTextGap(6);
        create.addActionListener(e -> handleCreate());
        foot.add(cancel); foot.add(create);
        root.add(foot, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void handleCreate() {
        String nom  = tfNom.getText().trim();
        String resp = (String) cbResp.getSelectedItem();
        if (nom.isEmpty() || resp == null || resp.equals("Aucun membre défini")) {
            JOptionPane.showMessageDialog(this, "Le nom et un responsable valide sont obligatoires.", "Champs requis", JOptionPane.WARNING_MESSAGE);
            return;
        }
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        try {
            LocalDate d = LocalDate.parse(tfDebut.getText().trim(), fmt);
            LocalDate f = LocalDate.parse(tfFin.getText().trim(), fmt);
            if (f.isBefore(d)) { JOptionPane.showMessageDialog(this, "La date de fin doit être après la date de début.", "Date invalide", JOptionPane.WARNING_MESSAGE); return; }
            result = new Tache(nom, d, f, resp);
            result.setPriorite((Tache.Priorite) cbPrio.getSelectedItem());
            result.setDescription(taDesc.getText());
            for (int i = 0; i < depChecks.size(); i++) {
                if (depChecks.get(i).isSelected()) result.addDependance(tachesExistantes.get(i).getId());
            }
            dispose();
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Format de date invalide — utilisez dd/MM/yyyy.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JTextField addField(JPanel p, String label, String placeholder) {
        JPanel row = new JPanel(new BorderLayout(0,4)); row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT); row.setMaximumSize(new Dimension(Integer.MAX_VALUE,58));
        row.add(lbl(label), BorderLayout.NORTH);
        JTextField tf = new JTextField();
        Theme.applyTextField(tf);
        tf.setForeground(Theme.TEXT_MUTED); tf.setText(placeholder);
        tf.addFocusListener(new FocusAdapter(){
            boolean first=true;
            public void focusGained(FocusEvent e){ if(first){tf.setText("");tf.setForeground(Theme.TEXT_PRIMARY);first=false;} }
        });
        row.add(tf, BorderLayout.CENTER);
        p.add(row); p.add(Box.createVerticalStrut(Theme.GAP_SM));
        return tf;
    }

    private Widgets.DatePicker addDatePicker(JPanel p, String label, LocalDate val) {
        JPanel row = new JPanel(new BorderLayout(0,4)); row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT); row.setMaximumSize(new Dimension(Integer.MAX_VALUE,58));
        row.add(lbl(label), BorderLayout.NORTH);
        Widgets.DatePicker dp = new Widgets.DatePicker(val);
        row.add(dp, BorderLayout.CENTER);
        p.add(row); p.add(Box.createVerticalStrut(Theme.GAP_SM));
        return dp;
    }

    private JLabel lbl(String text) {
        JLabel l = new JLabel(text); l.setFont(Theme.F_LABEL); l.setForeground(Theme.TEXT_SECONDARY); return l;
    }

    public Tache getResult() { return result; }
}
