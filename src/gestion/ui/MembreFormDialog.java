package gestion.ui;

import gestion.model.Membre;
import gestion.util.Theme;
import gestion.util.Widgets;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class MembreFormDialog extends JDialog {

    private final Membre editMembre;
    private Membre result;
    private Color avatarColor;

    private JTextField tfPrenom, tfNom, tfEmail, tfTel, tfComp;
    private JComboBox<Membre.Role>          cbRole;
    private JComboBox<Membre.Disponibilite> cbDispo;
    private JPanel avatarPreview;

    public MembreFormDialog(Window parent, Membre membre) {
        super(parent, membre == null ? "Ajouter un membre" : "Modifier — " + membre.getNomComplet(),
              ModalityType.APPLICATION_MODAL);
        this.editMembre  = membre;
        this.avatarColor = membre != null ? membre.getAvatarColor() : Theme.ACCENT;
        setPreferredSize(new Dimension(480, 560));
        build();
        pack();
        setLocationRelativeTo(null);
        if (membre != null) prefill(membre);
    }

    private void build() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG_PANEL);

        // ── Header ──────────────────────────────────────────
        JPanel hdr = new JPanel(new BorderLayout(Theme.GAP_MD, 0));
        hdr.setBackground(Theme.BG_TOPBAR);
        hdr.setBorder(new CompoundBorder(new MatteBorder(0,0,1,0,Theme.BORDER), new EmptyBorder(14,18,14,18)));

        avatarPreview = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(avatarColor.getRed(),avatarColor.getGreen(),avatarColor.getBlue(),50));
                g2.fillOval(0,0,50,50);
                g2.setColor(avatarColor); g2.setFont(Theme.font(Font.BOLD,18));
                FontMetrics fm = g2.getFontMetrics();
                String s = initPreview();
                g2.drawString(s,(50-fm.stringWidth(s))/2,(50+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        avatarPreview.setPreferredSize(new Dimension(50,50)); avatarPreview.setOpaque(false);

        JPanel inf = new JPanel(new BorderLayout(0,3)); inf.setOpaque(false);
        JLabel t = new JLabel(editMembre==null ? "Nouveau membre" : "Modifier le profil");
        t.setFont(Theme.F_SUBTITLE); t.setForeground(Theme.TEXT_PRIMARY);
        JLabel s = new JLabel(editMembre==null ? "Remplissez les informations du membre" : editMembre.getNomComplet());
        s.setFont(Theme.F_TINY); s.setForeground(Theme.TEXT_SECONDARY);
        inf.add(t, BorderLayout.NORTH); inf.add(s, BorderLayout.SOUTH);
        hdr.add(avatarPreview, BorderLayout.WEST); hdr.add(inf, BorderLayout.CENTER);
        root.add(hdr, BorderLayout.NORTH);

        // ── Corps ───────────────────────────────────────────
        JPanel body = new JPanel();
        body.setBackground(Theme.BG_PANEL);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(Theme.GAP_LG, Theme.GAP_LG, Theme.GAP_MD, Theme.GAP_LG));

        secLabel(body, "Identité", "team.svg");
        JPanel nameRow = new JPanel(new GridLayout(1,2,8,0)); nameRow.setOpaque(false);
        nameRow.setAlignmentX(LEFT_ALIGNMENT); nameRow.setMaximumSize(new Dimension(Integer.MAX_VALUE,56));
        tfPrenom = tf(); tfNom = tf();
        nameRow.add(lf("Prénom *", tfPrenom)); nameRow.add(lf("Nom *", tfNom));
        body.add(nameRow); body.add(Box.createVerticalStrut(Theme.GAP_SM));

        tfEmail = addF(body, "Email *", "prenom.nom@email.com");
        tfTel   = addF(body, "Téléphone", "+261 34 XX XXX XX");
        body.add(Box.createVerticalStrut(Theme.GAP_SM));

        secLabel(body, "Rôle & Disponibilité", "info.svg");
        JPanel rdRow = new JPanel(new GridLayout(1,2,8,0)); rdRow.setOpaque(false);
        rdRow.setAlignmentX(LEFT_ALIGNMENT); rdRow.setMaximumSize(new Dimension(Integer.MAX_VALUE,58));
        cbRole  = new JComboBox<>(Membre.Role.values());          Theme.applyCombo(cbRole);
        cbDispo = new JComboBox<>(Membre.Disponibilite.values()); Theme.applyCombo(cbDispo);
        rdRow.add(lf("Rôle *", cbRole)); rdRow.add(lf("Disponibilité", cbDispo));
        body.add(rdRow); body.add(Box.createVerticalStrut(Theme.GAP_SM));

        tfComp = addF(body, "Compétences", "Ex : Java, React, SQL — séparées par virgules");
        body.add(Box.createVerticalStrut(Theme.GAP_SM));

        secLabel(body, "Couleur de l'avatar", "edit.svg");
        body.add(buildPalette());

        JScrollPane sp = Widgets.scroll(body); sp.getViewport().setBackground(Theme.BG_PANEL);
        root.add(sp, BorderLayout.CENTER);

        // ── Footer ──────────────────────────────────────────
        JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        foot.setBackground(Theme.BG_TOPBAR);
        foot.setBorder(new MatteBorder(1,0,0,0,Theme.BORDER));
        Widgets.FlatButton cancel = new Widgets.FlatButton("Annuler", new Color(48,48,72));
        cancel.addActionListener(e -> dispose());
        String saveLabel = editMembre==null ? "Ajouter" : "Enregistrer";
        Widgets.FlatButton save = new Widgets.FlatButton(saveLabel, Theme.ACCENT);
        FlatSVGIcon iconS = Widgets.svg("/resources/icons/check.svg", 14, 14);
        iconS.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
        save.setIcon(iconS); save.setIconTextGap(6);
        save.addActionListener(e -> handleSave());
        foot.add(cancel); foot.add(save);
        root.add(foot, BorderLayout.SOUTH);

        setContentPane(root);

        // Mise à jour preview avatar au changement prénom/nom
        KeyAdapter ka = new KeyAdapter(){ public void keyReleased(KeyEvent e){ avatarPreview.repaint(); }};
        tfPrenom.addKeyListener(ka); tfNom.addKeyListener(ka);
    }

    private JPanel buildPalette() {
        Color[] palette = {
            new Color(99,120,255), new Color(52,199,120), new Color(255,120,80),
            new Color(200,80,200), new Color(56,189,220), new Color(255,180,40),
            new Color(120,200,80), new Color(218,80,100), new Color(150,100,220),
            new Color(40,180,180), new Color(255,100,140), new Color(80,160,255)
        };
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT,6,4));
        p.setOpaque(false); p.setAlignmentX(LEFT_ALIGNMENT);
        for (Color c : palette) {
            JPanel sw = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D)g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(c); g2.fillOval(2,2,getWidth()-4,getHeight()-4);
                    if (c.equals(avatarColor)) {
                        g2.setColor(Color.WHITE); g2.setStroke(new BasicStroke(2));
                        g2.drawOval(2,2,getWidth()-5,getHeight()-5);
                    }
                    g2.dispose();
                }
            };
            sw.setPreferredSize(new Dimension(28,28)); sw.setOpaque(false);
            sw.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            sw.addMouseListener(new MouseAdapter(){
                public void mouseClicked(MouseEvent e){ avatarColor=c; avatarPreview.repaint(); p.repaint(); }
            });
            p.add(sw);
        }
        return p;
    }

    private void prefill(Membre m) {
        tfPrenom.setText(m.getPrenom()); tfPrenom.setForeground(Theme.TEXT_PRIMARY);
        tfNom.setText(m.getNom());       tfNom.setForeground(Theme.TEXT_PRIMARY);
        tfEmail.setText(m.getEmail());   tfEmail.setForeground(Theme.TEXT_PRIMARY);
        tfTel.setText(m.getTelephone()); tfTel.setForeground(Theme.TEXT_PRIMARY);
        tfComp.setText(m.getCompetences()); tfComp.setForeground(Theme.TEXT_PRIMARY);
        cbRole.setSelectedItem(m.getRole());
        cbDispo.setSelectedItem(m.getDisponibilite());
    }

    private void handleSave() {
        String prenom = tfPrenom.getText().trim();
        String nom    = tfNom.getText().trim();
        String email  = tfEmail.getText().trim();
        if (prenom.isEmpty()||nom.isEmpty()||email.isEmpty()) {
            JOptionPane.showMessageDialog(this,"Prénom, Nom et Email sont obligatoires.","Champs requis",JOptionPane.WARNING_MESSAGE);
            return;
        }
        Membre.Role role = (Membre.Role) cbRole.getSelectedItem();
        if (editMembre == null) {
            result = new Membre(prenom, nom, email, role);
        } else {
            editMembre.setPrenom(prenom); editMembre.setNom(nom);
            editMembre.setEmail(email);   editMembre.setRole(role);
            result = editMembre;
        }
        result.setTelephone(tfTel.getText().trim());
        result.setCompetences(tfComp.getText().trim());
        result.setDisponibilite((Membre.Disponibilite) cbDispo.getSelectedItem());
        result.setAvatarColor(avatarColor);
        dispose();
    }

    private String initPreview() {
        String p = tfPrenom!=null?tfPrenom.getText():"";
        String n = tfNom!=null?tfNom.getText():"";
        String i1 = p.isEmpty()?"?":(p.trim().isEmpty()?"?":String.valueOf(p.trim().charAt(0)).toUpperCase());
        String i2 = n.trim().isEmpty()?""  :String.valueOf(n.trim().charAt(0)).toUpperCase();
        return i1+i2;
    }

    private JTextField tf() { JTextField t = new JTextField(); Theme.applyTextField(t); return t; }

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

    private JPanel lf(String label, JComponent comp) {
        JPanel pan = new JPanel(new BorderLayout(0,3)); pan.setOpaque(false);
        pan.add(lbl(label), BorderLayout.NORTH); pan.add(comp, BorderLayout.CENTER);
        return pan;
    }

    private JLabel lbl(String t) {
        JLabel l = new JLabel(t); l.setFont(Theme.F_LABEL); l.setForeground(Theme.TEXT_SECONDARY); return l;
    }

    private void secLabel(JPanel p, String text, String iconName) {
        JLabel l = new JLabel(text.toUpperCase());
        l.setFont(Theme.font(Font.BOLD, 10)); l.setForeground(Theme.TEXT_MUTED);
        
        if (iconName != null) {
            FlatSVGIcon icon = new FlatSVGIcon("resources/icons/" + iconName, 12, 12);
            icon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Theme.TEXT_MUTED));
            l.setIcon(icon);
            l.setIconTextGap(6);
        }
        
        l.setAlignmentX(LEFT_ALIGNMENT);
        l.setBorder(new CompoundBorder(new MatteBorder(0,0,1,0,Theme.BORDER),new EmptyBorder(4,0,4,0)));
        l.setMaximumSize(new Dimension(Integer.MAX_VALUE,24));
        p.add(l); p.add(Box.createVerticalStrut(Theme.GAP_SM));
    }

    public Membre getResult() { return result; }
}
