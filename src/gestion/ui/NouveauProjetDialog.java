package gestion.ui;

import gestion.model.Projet;
import gestion.util.Theme;
import gestion.util.Widgets;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class NouveauProjetDialog extends JDialog {

    private JTextField tfNom, tfResp, tfDebut, tfFin;
    private JTextArea  taDesc;
    private Projet result;

    public NouveauProjetDialog(Frame parent) {
        super(parent, "Nouveau projet", true);
        setPreferredSize(new Dimension(460, 430));
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
        JLabel title = new JLabel("📁  NOUVEAU PROJET");
        title.setFont(Theme.F_SUBTITLE); title.setForeground(Theme.TEXT_PRIMARY);
        JLabel sub   = new JLabel("Créez un nouveau projet et son équipe");
        sub.setFont(Theme.F_TINY); sub.setForeground(Theme.TEXT_SECONDARY);
        JPanel ti = new JPanel(new BorderLayout(0,3)); ti.setOpaque(false);
        ti.add(title, BorderLayout.NORTH); ti.add(sub, BorderLayout.SOUTH);
        hdr.add(ti, BorderLayout.CENTER);
        root.add(hdr, BorderLayout.NORTH);

        // ── Formulaire ──────────────────────────────────────
        JPanel form = new JPanel();
        form.setBackground(Theme.BG_PANEL);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(Theme.GAP_LG, Theme.GAP_LG, Theme.GAP_MD, Theme.GAP_LG));

        tfNom  = addField(form, "Nom du projet *",   "Ex : Refonte Site Web");
        tfResp = addField(form, "Chef de projet *",  "Ex : Alice Martin");

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate now = LocalDate.now();
        tfDebut = addFieldVal(form, "Date de début *  (dd/MM/yyyy)", now.format(fmt));
        tfFin   = addFieldVal(form, "Date de fin *    (dd/MM/yyyy)", now.plusDays(90).format(fmt));

        JPanel drow = new JPanel(new BorderLayout(0,4)); drow.setOpaque(false);
        drow.setAlignmentX(LEFT_ALIGNMENT); drow.setMaximumSize(new Dimension(Integer.MAX_VALUE,100));
        drow.add(lbl("Description (optionnelle)"), BorderLayout.NORTH);
        taDesc = new JTextArea(3,20); Theme.applyTextArea(taDesc);
        JScrollPane dsp = new JScrollPane(taDesc); dsp.setBorder(new LineBorder(Theme.BORDER));
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
        Widgets.FlatButton create = new Widgets.FlatButton("✓  Créer le projet", Theme.ACCENT);
        create.addActionListener(e -> handleCreate());
        foot.add(cancel); foot.add(create);
        root.add(foot, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void handleCreate() {
        String nom  = tfNom.getText().trim();
        String resp = tfResp.getText().trim();
        if (nom.isEmpty() || resp.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Le nom et le chef de projet sont obligatoires.", "Champs requis", JOptionPane.WARNING_MESSAGE);
            return;
        }
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        try {
            LocalDate d = LocalDate.parse(tfDebut.getText().trim(), fmt);
            LocalDate f = LocalDate.parse(tfFin.getText().trim(), fmt);
            result = new Projet(nom, taDesc.getText(), resp, d, f);
            dispose();
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Format de date invalide — utilisez dd/MM/yyyy.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JTextField addField(JPanel p, String label, String placeholder) {
        JPanel row = new JPanel(new BorderLayout(0,4)); row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT); row.setMaximumSize(new Dimension(Integer.MAX_VALUE,58));
        row.add(lbl(label), BorderLayout.NORTH);
        JTextField tf = new JTextField(); Theme.applyTextField(tf);
        tf.setForeground(Theme.TEXT_MUTED); tf.setText(placeholder);
        tf.addFocusListener(new FocusAdapter(){
            boolean first=true;
            public void focusGained(FocusEvent e){ if(first){tf.setText("");tf.setForeground(Theme.TEXT_PRIMARY);first=false;} }
        });
        row.add(tf, BorderLayout.CENTER);
        p.add(row); p.add(Box.createVerticalStrut(Theme.GAP_SM));
        return tf;
    }

    private JTextField addFieldVal(JPanel p, String label, String val) {
        JPanel row = new JPanel(new BorderLayout(0,4)); row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT); row.setMaximumSize(new Dimension(Integer.MAX_VALUE,58));
        row.add(lbl(label), BorderLayout.NORTH);
        JTextField tf = new JTextField(val); Theme.applyTextField(tf);
        row.add(tf, BorderLayout.CENTER);
        p.add(row); p.add(Box.createVerticalStrut(Theme.GAP_SM));
        return tf;
    }

    private JLabel lbl(String text) {
        JLabel l = new JLabel(text); l.setFont(Theme.F_LABEL); l.setForeground(Theme.TEXT_SECONDARY); return l;
    }

    public Projet getResult() { return result; }
}
