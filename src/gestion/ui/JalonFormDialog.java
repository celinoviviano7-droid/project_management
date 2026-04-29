package gestion.ui;

import gestion.model.Jalon;
import gestion.model.Projet;
import gestion.model.Tache;
import gestion.util.Theme;
import gestion.util.Widgets;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Dialogue de création / modification d'un jalon
 */
public class JalonFormDialog extends JDialog {

    private final Jalon editJalon;
    private final Projet projet;
    private Jalon result;

    private JTextField              tfNom, tfDate, tfDesc;
    private JComboBox<Jalon.Type>   cbType;
    private JComboBox<Jalon.Statut> cbStatut;
    private JComboBox<String>       cbTache;   // tâche liée (optionnelle)
    private JPanel                  previewDiamond;

    public JalonFormDialog(Frame parent, Jalon jalon, Projet projet) {
        super(parent, jalon == null ? "Nouveau jalon" : "Modifier le jalon", true);
        this.editJalon = jalon;
        this.projet    = projet;
        setPreferredSize(new Dimension(460, 480));
        build();
        pack();
        setLocationRelativeTo(parent);
        if (jalon != null) prefill(jalon);
        updatePreview();
    }

    private void build() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG_PANEL);

        // ── Header ──────────────────────────────────────────
        JPanel hdr = new JPanel(new BorderLayout(Theme.GAP_MD, 0));
        hdr.setBackground(Theme.BG_TOPBAR);
        hdr.setBorder(new CompoundBorder(
            new MatteBorder(0,0,1,0,Theme.BORDER),
            new EmptyBorder(14,18,14,18)));

        // Aperçu losange
        previewDiamond = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Jalon.Statut st = cbStatut!=null ? (Jalon.Statut)cbStatut.getSelectedItem() : Jalon.Statut.PREVU;
                Color c = st != null ? st.getCouleur() : Theme.GOLD;
                int cx=getWidth()/2, cy=getHeight()/2, r=18;
                int[] xs={cx,cx+r,cx,cx-r}, ys={cy-r,cy,cy+r,cy};
                g2.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),50)); g2.fillPolygon(xs,ys,4);
                if(st==Jalon.Statut.ATTEINT){
                    g2.setColor(c); g2.fillPolygon(xs,ys,4);
                    g2.setColor(Color.WHITE); g2.fillOval(cx-4,cy-4,8,8);
                }
                g2.setColor(c); g2.setStroke(new BasicStroke(2f)); g2.drawPolygon(xs,ys,4);
                g2.dispose();
            }
        };
        previewDiamond.setPreferredSize(new Dimension(50,50));
        previewDiamond.setOpaque(false);

        JPanel info = new JPanel(new BorderLayout(0,3)); info.setOpaque(false);
        JLabel t = new JLabel(editJalon==null ? "Nouveau jalon" : "Modifier — "+editJalon.getNom());
        t.setFont(Theme.F_SUBTITLE); t.setForeground(Theme.TEXT_PRIMARY);
        JLabel s = new JLabel("Un jalon marque un point de contrôle clé dans le projet");
        s.setFont(Theme.F_TINY); s.setForeground(Theme.TEXT_SECONDARY);
        info.add(t,BorderLayout.NORTH); info.add(s,BorderLayout.SOUTH);
        hdr.add(previewDiamond, BorderLayout.WEST); hdr.add(info, BorderLayout.CENTER);
        root.add(hdr, BorderLayout.NORTH);

        // ── Corps ────────────────────────────────────────────
        JPanel body = new JPanel();
        body.setBackground(Theme.BG_PANEL);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(Theme.GAP_LG, Theme.GAP_LG, Theme.GAP_MD, Theme.GAP_LG));

        secLabel(body, "📌  Identité");
        tfNom = addF(body, "Nom du jalon *", "Ex : Livraison version 1.0");

        // Date
        JPanel drow = new JPanel(new BorderLayout(0,4)); drow.setOpaque(false);
        drow.setAlignmentX(LEFT_ALIGNMENT); drow.setMaximumSize(new Dimension(Integer.MAX_VALUE,58));
        drow.add(lbl("Date du jalon *  (dd/MM/yyyy)"), BorderLayout.NORTH);
        tfDate = new JTextField(LocalDate.now().plusDays(30).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        Theme.applyTextField(tfDate);
        drow.add(tfDate, BorderLayout.CENTER);
        body.add(drow); body.add(Box.createVerticalStrut(Theme.GAP_SM));

        // Type + Statut en ligne
        JPanel tsRow = new JPanel(new GridLayout(1,2,8,0)); tsRow.setOpaque(false);
        tsRow.setAlignmentX(LEFT_ALIGNMENT); tsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE,58));
        cbType   = new JComboBox<>(Jalon.Type.values());   Theme.applyCombo(cbType);
        cbStatut = new JComboBox<>(Jalon.Statut.values()); Theme.applyCombo(cbStatut);
        cbStatut.addActionListener(e -> updatePreview());
        tsRow.add(lf("Type de jalon", cbType));
        tsRow.add(lf("Statut", cbStatut));
        body.add(tsRow); body.add(Box.createVerticalStrut(Theme.GAP_MD));

        secLabel(body, "🔗  Liens");
        // Tâche liée
        JPanel trow = new JPanel(new BorderLayout(0,4)); trow.setOpaque(false);
        trow.setAlignmentX(LEFT_ALIGNMENT); trow.setMaximumSize(new Dimension(Integer.MAX_VALUE,58));
        trow.add(lbl("Tâche liée (optionnelle)"), BorderLayout.NORTH);
        String[] tacheItems = buildTacheItems();
        cbTache = new JComboBox<>(tacheItems); Theme.applyCombo(cbTache);
        trow.add(cbTache, BorderLayout.CENTER);
        body.add(trow); body.add(Box.createVerticalStrut(Theme.GAP_MD));

        secLabel(body, "📝  Description");
        JPanel descRow = new JPanel(new BorderLayout(0,4)); descRow.setOpaque(false);
        descRow.setAlignmentX(LEFT_ALIGNMENT); descRow.setMaximumSize(new Dimension(Integer.MAX_VALUE,85));
        JTextArea ta = new JTextArea(3,20); Theme.applyTextArea(ta);
        tfDesc = new JTextField(); // reused as proxy - we'll use the textarea directly below
        JScrollPane dsp = new JScrollPane(ta); dsp.setBorder(new LineBorder(Theme.BORDER));
        descRow.add(dsp, BorderLayout.CENTER);
        body.add(descRow);
        // Store textarea ref via name trick
        ta.setName("desc");
        // Find textarea later in handleSave via container search
        body.putClientProperty("descArea", ta);

        JScrollPane sp = Widgets.scroll(body); sp.getViewport().setBackground(Theme.BG_PANEL);
        root.add(sp, BorderLayout.CENTER);

        // ── Footer ──────────────────────────────────────────
        JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,12));
        foot.setBackground(Theme.BG_TOPBAR);
        foot.setBorder(new MatteBorder(1,0,0,0,Theme.BORDER));
        Widgets.FlatButton cancel = new Widgets.FlatButton("Annuler", new Color(48,48,72));
        cancel.addActionListener(e -> dispose());
        String lbl2 = editJalon==null ? "✓  Créer le jalon" : "✓  Enregistrer";
        Widgets.FlatButton save = new Widgets.FlatButton(lbl2, Theme.GOLD);
        save.setForeground(new Color(20,18,0));
        save.addActionListener(e -> handleSave(body));
        foot.add(cancel); foot.add(save);
        root.add(foot, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void handleSave(JPanel body) {
        String nom = tfNom.getText().trim();
        if (nom.isEmpty()) {
            JOptionPane.showMessageDialog(this,"Le nom du jalon est obligatoire.","Champ requis",JOptionPane.WARNING_MESSAGE);
            return;
        }
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate date;
        try {
            date = LocalDate.parse(tfDate.getText().trim(), fmt);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this,"Format de date invalide — utilisez dd/MM/yyyy.","Erreur",JOptionPane.ERROR_MESSAGE);
            return;
        }

        Jalon.Type   type   = (Jalon.Type)   cbType.getSelectedItem();
        Jalon.Statut statut = (Jalon.Statut) cbStatut.getSelectedItem();

        // Retrieve textarea
        JTextArea ta = (JTextArea) body.getClientProperty("descArea");
        String desc = ta != null ? ta.getText() : "";

        if (editJalon == null) {
            result = new Jalon(nom, date, type);
        } else {
            editJalon.setNom(nom); editJalon.setDate(date); editJalon.setType(type);
            result = editJalon;
        }
        result.setStatut(statut);
        result.setDescription(desc);

        // Tâche liée
        int sel = cbTache.getSelectedIndex();
        if (sel > 0 && projet != null) {
            Tache linked = projet.getTaches().get(sel - 1);
            result.setTacheLieeId(linked.getId());
        } else {
            result.setTacheLieeId(-1);
        }
        dispose();
    }

    private void prefill(Jalon j) {
        tfNom.setText(j.getNom()); tfNom.setForeground(Theme.TEXT_PRIMARY);
        tfDate.setText(j.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        cbType.setSelectedItem(j.getType());
        cbStatut.setSelectedItem(j.getStatut());
        // tâche liée
        if (j.getTacheLieeId() > 0 && projet != null) {
            for (int i=0; i<projet.getTaches().size(); i++) {
                if (projet.getTaches().get(i).getId() == j.getTacheLieeId()) {
                    cbTache.setSelectedIndex(i+1); break;
                }
            }
        }
    }

    private void updatePreview() {
        if (previewDiamond != null) previewDiamond.repaint();
    }

    private String[] buildTacheItems() {
        if (projet == null || projet.getTaches().isEmpty()) return new String[]{"— Aucune tâche —"};
        String[] items = new String[projet.getTaches().size() + 1];
        items[0] = "— Aucune tâche liée —";
        for (int i=0; i<projet.getTaches().size(); i++) {
            Tache t = projet.getTaches().get(i);
            items[i+1] = "["+t.getId()+"] "+t.getNom();
        }
        return items;
    }

    // ── Helpers ──────────────────────────────────────────────
    private JTextField addF(JPanel p, String label, String placeholder) {
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

    private void secLabel(JPanel p, String text) {
        JLabel l = new JLabel(text.toUpperCase()); l.setFont(Theme.F_LABEL); l.setForeground(Theme.TEXT_MUTED);
        l.setAlignmentX(LEFT_ALIGNMENT);
        l.setBorder(new CompoundBorder(new MatteBorder(0,0,1,0,Theme.BORDER),new EmptyBorder(0,0,4,0)));
        l.setMaximumSize(new Dimension(Integer.MAX_VALUE,20));
        p.add(l); p.add(Box.createVerticalStrut(Theme.GAP_SM));
    }

    private JPanel lf(String label, JComponent comp) {
        JPanel pan = new JPanel(new BorderLayout(0,3)); pan.setOpaque(false);
        pan.add(lbl(label), BorderLayout.NORTH); pan.add(comp, BorderLayout.CENTER); return pan;
    }

    private JLabel lbl(String text) {
        JLabel l = new JLabel(text); l.setFont(Theme.F_LABEL); l.setForeground(Theme.TEXT_SECONDARY); return l;
    }

    public Jalon getResult() { return result; }
}
