package gestion.ui;

import gestion.model.*;
import gestion.service.ProjetService;
import gestion.util.*;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class DashboardPanel extends JPanel {

    private final ProjetService service;
    private Projet selectedProjet;
    private Runnable onOpenGantt;

    public DashboardPanel(ProjetService service) {
        this.service = service;
        setBackground(Theme.BG_PANEL);
        setLayout(new BorderLayout());
    }

    public void setProjet(Projet p) {
        this.selectedProjet = p;
    }

    public void setOnOpenGantt(Runnable r) {
        onOpenGantt = r;
    }

    public void refresh() {
        removeAll();
        JPanel body = new JPanel();
        body.setBackground(Theme.BG_PANEL);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(8, Theme.GAP_XL, Theme.GAP_LG, Theme.GAP_XL));

        // En-tête
        body.add(pageHeader());
        body.add(Box.createVerticalStrut(Theme.GAP_LG));

        // KPI cards
        body.add(kpiRow());
        body.add(Box.createVerticalStrut(Theme.GAP_LG));

        // Projets + Colonne droite (Adaptive: side-by-side or stacked)
        JPanel projets = projetsList();
        JPanel right = rightCol();

        Widgets.AdaptivePanel adaptiveRow = new Widgets.AdaptivePanel(900,
                new BoxLayout(null, BoxLayout.Y_AXIS), // Small: vertical (null parent for layout manager)
                new GridLayout(1, 2, Theme.GAP_MD, 0) // Large: horizontal
        ) {
            @Override
            public void setLayout(LayoutManager mgr) {
                super.setLayout(mgr);
                // Adjust alignment for BoxLayout if needed
                if (mgr instanceof BoxLayout) {
                    projets.setAlignmentX(LEFT_ALIGNMENT);
                    right.setAlignmentX(LEFT_ALIGNMENT);
                }
            }
        };
        adaptiveRow.setAlignmentX(LEFT_ALIGNMENT);
        adaptiveRow.add(projets);
        adaptiveRow.add(right);
        body.add(adaptiveRow);

        JScrollPane sp = Widgets.scroll(body);
        sp.getViewport().setBackground(Theme.BG_PANEL);
        add(sp, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private JPanel pageHeader() {
        JPanel h = new JPanel(new BorderLayout(0, 4));
        h.setOpaque(false);
        h.setAlignmentX(LEFT_ALIGNMENT);
        h.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        int hour = java.time.LocalTime.now().getHour();
        String greeting = (hour < 5 || hour >= 18) ? "Bonsoir" : "Bonjour";
        String projectName = (selectedProjet != null) ? selectedProjet.getNom() : "aucun projet";

        JLabel title = new JLabel(greeting + ", voici le suivi de : " + projectName);
        title.setFont(Theme.font(Font.BOLD, 24));
        title.setForeground(Theme.TEXT_PRIMARY);

        JLabel sub = new JLabel(
                "Dernière mise à jour : " + LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy")));
        sub.setFont(Theme.F_SMALL);
        sub.setForeground(Theme.TEXT_SECONDARY);

        h.add(title, BorderLayout.NORTH);
        h.add(sub, BorderLayout.SOUTH);
        return h;
    }

    private JPanel heroBanner() {
        Widgets.RoundPanel banner = new Widgets.RoundPanel(Theme.R_MD, Theme.BORDER);
        banner.setBackground(Theme.BG_CARD);
        banner.setLayout(new BorderLayout());
        banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        banner.setPreferredSize(new Dimension(0, 220));
        banner.setAlignmentX(LEFT_ALIGNMENT);

        JLabel imgLbl = new JLabel() {
            private Image heroImg = loadHeroImage();

            private Image loadHeroImage() {
                try {
                    java.net.URL resource = getClass().getResource("/img/Gemini_Generated_Image_zasupizasupizasu.png");
                    if (resource == null) {
                        java.io.File file = new java.io.File("img/Gemini_Generated_Image_zasupizasupizasu.png");
                        if (file.exists()) {
                            resource = file.toURI().toURL();
                        }
                    }
                    if (resource != null) {
                        return new ImageIcon(resource).getImage();
                    }
                } catch (Exception e) {
                    System.err.println("[DashboardPanel] Erreur chargement image hero: " + e.getMessage());
                }
                return null;
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                // Draw image with "cover" behavior
                int w = getWidth(), h = getHeight();
                if (heroImg != null) {
                    double iw = heroImg.getWidth(null), ih = heroImg.getHeight(null);
                    double scale = Math.max((double) w / iw, (double) h / ih);
                    int nw = (int) (iw * scale), nh = (int) (ih * scale);
                    g2.drawImage(heroImg, (w - nw) / 2, (h - nh) / 2, nw, nh, null);
                } else {
                    g2.setPaint(new GradientPaint(0, 0, Theme.BG_CARD, w, h, Theme.BG_CARD_HOVER));
                    g2.fillRect(0, 0, w, h);
                }

                // Subtle gradient overlay for text readability if needed
                GradientPaint gp = new GradientPaint(0, 0, new Color(0, 0, 0, 100), w / 2, 0, new Color(0, 0, 0, 0));
                g2.setPaint(gp);
                g2.fillRect(0, 0, w, h);

                // Text overlay
                g2.setColor(Color.WHITE);
                g2.setFont(Theme.font(Font.BOLD, 22));
                // g2.drawString("Gérez vos projets avec efficacité", 30, h/2 + 5);
                // g2.setFont(Theme.font(Font.PLAIN, 14));
                // g2.setColor(new Color(255,255,255,200));
                // g2.drawString("Suivez vos tâches, collaborez et atteignez vos objectifs.",
                // 30, h/2 + 30);

                g2.dispose();
            }
        };
        banner.add(imgLbl, BorderLayout.CENTER);
        return banner;
    }

    private JPanel kpiRow() {
        if (selectedProjet == null)
            return new JPanel();

        int nTaches = selectedProjet.getTaches().size();
        long jalons = selectedProjet.getJalons().size();
        long jAtteints = selectedProjet.getJalons().stream()
                .filter(j -> j.getStatut() == gestion.model.Jalon.Statut.ATTEINT).count();
        long membres = selectedProjet.getMembres().size();
        int prog = selectedProjet.getProgressionGlobale();

        // Use WrapLayout for the KPI cards so they flow to next line on small windows
        JPanel row = new JPanel(new Widgets.WrapLayout(FlowLayout.LEFT, Theme.GAP_MD, Theme.GAP_MD));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);

        FlatSVGIcon iF = Widgets.svg("/resources/icons/dashboard.svg", 20, 20);
        iF.setColorFilter(new FlatSVGIcon.ColorFilter(c2 -> Theme.CYAN));
        row.add(kpi("Progression", prog + "%", null, Theme.ACCENT, iF));

        FlatSVGIcon iT = Widgets.svg("/resources/icons/table.svg", 20, 20);
        iT.setColorFilter(new FlatSVGIcon.ColorFilter(c2 -> Theme.CYAN));
        row.add(kpi("Tâches", "" + nTaches, null, Theme.PURPLE, iT));

        FlatSVGIcon iM = Widgets.svg("/resources/icons/milestone.svg", 20, 20);
        iM.setColorFilter(new FlatSVGIcon.ColorFilter(c2 -> Theme.CYAN));
        row.add(kpi("Jalons", "" + jalons, jAtteints > 0 ? "  ✓" + jAtteints : null, Theme.GOLD, iM));

        FlatSVGIcon iU = Widgets.svg("/resources/icons/team.svg", 20, 20);
        iU.setColorFilter(new FlatSVGIcon.ColorFilter(c2 -> Theme.CYAN));
        row.add(kpi("Équipe", "" + membres, "membres", Theme.CYAN, iU));
        return row;
    }

    private JPanel kpi(String label, String val, String sub, Color color, Icon icon) {
        Widgets.RoundPanel c = new Widgets.RoundPanel(Theme.R_MD, Theme.BORDER);
        c.setBackground(Theme.BG_CARD);
        c.setLayout(new BorderLayout(Theme.GAP_MD, 4));
        c.setBorder(new EmptyBorder(14, 14, 14, 14));
        c.setPreferredSize(new Dimension(210, 110));
        c.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Bande colorée top (plus fluide)
        JPanel band = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 90));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                g2.dispose();
            }
        };
        band.setPreferredSize(new Dimension(0, 4));
        band.setOpaque(false);

        JPanel content = new JPanel(new BorderLayout(Theme.GAP_SM, 0));
        content.setOpaque(false);
        JLabel iconLbl = new JLabel(icon);
        iconLbl.setForeground(color);
        content.add(iconLbl, BorderLayout.WEST);

        JPanel txt = new JPanel(new BorderLayout(0, 0));
        txt.setOpaque(false);
        JLabel lbl = new JLabel(label.toUpperCase());
        lbl.setFont(Theme.font(Font.BOLD, 10));
        lbl.setForeground(Theme.TEXT_MUTED);
        txt.add(lbl, BorderLayout.CENTER);
        content.add(txt, BorderLayout.CENTER);

        JPanel vrow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        vrow.setOpaque(false);
        JLabel v = new JLabel(val);
        v.setFont(Theme.font(Font.BOLD, 32));
        v.setForeground(Theme.TEXT_PRIMARY);
        vrow.add(v);
        if (sub != null) {
            JLabel s = new JLabel(sub);
            s.setFont(Theme.font(Font.BOLD, 12));
            s.setForeground(Theme.GREEN);
            vrow.add(s);
        }

        c.add(band, BorderLayout.NORTH);
        c.add(content, BorderLayout.CENTER);
        c.add(vrow, BorderLayout.SOUTH);

        c.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                c.setBackground(Theme.BG_CARD_HOVER);
                c.repaint();
            }

            public void mouseExited(MouseEvent e) {
                c.setBackground(Theme.BG_CARD);
                c.repaint();
            }
        });

        return c;
    }

    private JPanel projetsList() {
        Widgets.RoundPanel card = new Widgets.RoundPanel(Theme.R_MD, Theme.BORDER);
        card.setBackground(Theme.BG_CARD);
        card.setLayout(new BorderLayout());

        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false);
        hdr.setBorder(new EmptyBorder(Theme.GAP_MD, Theme.GAP_MD, Theme.GAP_SM, Theme.GAP_MD));
        JLabel t = new JLabel("Détails du projet");
        t.setFont(Theme.F_SUBTITLE);
        t.setForeground(Theme.TEXT_PRIMARY);
        hdr.add(t, BorderLayout.WEST);
        card.add(hdr, BorderLayout.NORTH);

        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setOpaque(false);
        list.setBorder(new EmptyBorder(0, Theme.GAP_SM, Theme.GAP_SM, Theme.GAP_SM));

        if (selectedProjet == null) {
            JLabel e = new JLabel("Aucun projet sélectionné.");
            e.setForeground(Theme.TEXT_MUTED);
            e.setFont(Theme.F_SMALL);
            e.setBorder(new EmptyBorder(Theme.GAP_LG, Theme.GAP_SM, 0, 0));
            list.add(e);
        } else {
            list.add(projetRow(selectedProjet));
            list.add(Box.createVerticalStrut(Theme.GAP_MD));

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String startStr = selectedProjet.getDateDebutReelle() != null ? selectedProjet.getDateDebutReelle().format(dtf) : "Non définie";
            String endStr = selectedProjet.getDateFinReelle() != null ? selectedProjet.getDateFinReelle().format(dtf) : "Non définie";

            JLabel lblDates = new JLabel("<html><b>Dates du projet :</b> " + startStr + " au " + endStr + "</html>");
            lblDates.setFont(Theme.F_SMALL);
            lblDates.setForeground(Theme.TEXT_SECONDARY);
            lblDates.setBorder(new EmptyBorder(5, 10, 5, 10));
            list.add(lblDates);

            if (selectedProjet.getDescription() != null && !selectedProjet.getDescription().isEmpty()) {
                JLabel lblD = new JLabel(
                        "<html><b>Description :</b><br>" + selectedProjet.getDescription() + "</html>");
                lblD.setFont(Theme.F_SMALL);
                lblD.setForeground(Theme.TEXT_SECONDARY);
                lblD.setBorder(new EmptyBorder(5, 10, 10, 10));
                list.add(lblD);
            }
        }

        JScrollPane sp = Widgets.scroll(list);
        sp.getViewport().setBackground(Theme.BG_CARD);
        card.add(sp, BorderLayout.CENTER);
        return card;
    }

    private JPanel projetRow(Projet p) {
        JPanel row = new JPanel(new BorderLayout(Theme.GAP_SM, 0));
        row.setOpaque(false);
        row.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, Theme.BORDER),
                new EmptyBorder(10, Theme.GAP_SM, 10, Theme.GAP_SM)));
        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Point statut
        Color dc = p.getProgressionGlobale() == 100 ? Theme.GREEN
                : p.getProgressionGlobale() > 50 ? Theme.ACCENT : Theme.ORANGE;
        JPanel dot = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(dc);
                g2.fillOval(3, (getHeight() - 8) / 2, 8, 8);
                g2.dispose();
            }
        };
        dot.setPreferredSize(new Dimension(16, 20));
        dot.setOpaque(false);

        JPanel info = new JPanel(new BorderLayout(0, 2));
        info.setOpaque(false);
        JLabel nom = new JLabel(p.getNom());
        nom.setFont(Theme.F_SUBTITLE);
        nom.setForeground(Theme.TEXT_PRIMARY);
        JLabel sub = new JLabel(
                p.getTaches().size() + " tâches  •  " + p.getMembres().size() + " membres  •  " + p.getResponsable());
        sub.setFont(Theme.F_TINY);
        sub.setForeground(Theme.TEXT_SECONDARY);
        info.add(nom, BorderLayout.NORTH);
        info.add(sub, BorderLayout.SOUTH);

        JPanel right = new JPanel(new BorderLayout(0, 2));
        right.setOpaque(false);
        right.setPreferredSize(new Dimension(100, 40));
        JLabel pct = new JLabel(p.getProgressionGlobale() + "%");
        pct.setFont(Theme.font(Font.BOLD, 12));
        pct.setForeground(p.getProgressionGlobale() == 100 ? Theme.GREEN : Theme.TEXT_SECONDARY);
        pct.setHorizontalAlignment(SwingConstants.RIGHT);
        Widgets.ProgressBar pb = new Widgets.ProgressBar(p.getProgressionGlobale(), dc);
        pb.setShowLabel(false);
        right.add(pct, BorderLayout.NORTH);
        right.add(pb, BorderLayout.SOUTH);

        row.add(dot, BorderLayout.WEST);
        row.add(info, BorderLayout.CENTER);
        row.add(right, BorderLayout.EAST);

        row.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (onOpenGantt != null)
                    onOpenGantt.run();
            }

            public void mouseEntered(MouseEvent e) {
                row.setOpaque(true);
                row.setBackground(Theme.BG_CARD_HOVER);
            }

            public void mouseExited(MouseEvent e) {
                row.setOpaque(false);
                row.repaint();
            }
        });
        return row;
    }

    private JPanel rightCol() {
        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setOpaque(false);
        col.add(donutCard());
        col.add(Box.createVerticalStrut(Theme.GAP_MD));
        col.add(urgentCard());
        return col;
    }

    private JPanel donutCard() {
        Widgets.RoundPanel c = new Widgets.RoundPanel(Theme.R_MD, Theme.BORDER);
        c.setBackground(Theme.BG_CARD);
        c.setLayout(new BorderLayout());
        c.setAlignmentX(LEFT_ALIGNMENT);
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false);
        hdr.setBorder(new EmptyBorder(Theme.GAP_MD, Theme.GAP_MD, 0, Theme.GAP_MD));
        JLabel t = new JLabel("Progression globale");
        t.setFont(Theme.F_SUBTITLE);
        t.setForeground(Theme.TEXT_PRIMARY);
        hdr.add(t, BorderLayout.WEST);
        c.add(hdr, BorderLayout.NORTH);

        int prog = (selectedProjet != null) ? selectedProjet.getProgressionGlobale() : 0;
        Color barC = prog == 100 ? Theme.GREEN : prog > 50 ? Theme.ACCENT : Theme.ORANGE;

        JPanel donut = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int cx = getWidth() / 2, cy = getHeight() / 2, r = Math.min(cx, cy) - 22;

                // Track
                g2.setColor(new Color(40, 40, 68));
                g2.setStroke(new BasicStroke(16, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawOval(cx - r, cy - r, r * 2, r * 2);

                if (prog > 0) {
                    g2.setStroke(new BasicStroke(16, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.setColor(barC);
                    g2.drawArc(cx - r, cy - r, r * 2, r * 2, 90, -(int) (3.6 * prog));
                }

                // Text
                g2.setFont(Theme.font(Font.BOLD, 24));
                g2.setColor(Theme.TEXT_PRIMARY);
                String sv = prog + "%";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(sv, cx - fm.stringWidth(sv) / 2, cy + fm.getAscent() / 2 - 5);

                g2.setFont(Theme.font(Font.BOLD, 9));
                g2.setColor(Theme.TEXT_MUTED);
                String subs = "PROGRESSION";
                fm = g2.getFontMetrics();
                g2.drawString(subs, cx - fm.stringWidth(subs) / 2, cy + 18);
                g2.dispose();
            }
        };
        donut.setOpaque(false);
        donut.setPreferredSize(new Dimension(0, 160));
        c.add(donut, BorderLayout.CENTER);
        return c;
    }

    private JPanel urgentCard() {
        Widgets.RoundPanel c = new Widgets.RoundPanel(Theme.R_MD, Theme.BORDER);
        c.setBackground(Theme.BG_CARD);
        c.setLayout(new BorderLayout());
        c.setAlignmentX(LEFT_ALIGNMENT);
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false);
        hdr.setBorder(new EmptyBorder(Theme.GAP_MD, Theme.GAP_MD, Theme.GAP_SM, Theme.GAP_MD));
        JLabel t = new JLabel("Tâches critiques");
        t.setFont(Theme.F_SUBTITLE);
        t.setForeground(Theme.TEXT_PRIMARY);
        FlatSVGIcon iconA = new FlatSVGIcon("resources/icons/alert.svg");
        iconA.setColorFilter(new FlatSVGIcon.ColorFilter(c2 -> Theme.CYAN));
        t.setIcon(iconA);
        hdr.add(t, BorderLayout.WEST);
        c.add(hdr, BorderLayout.NORTH);

        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setOpaque(false);
        list.setBorder(new EmptyBorder(0, Theme.GAP_SM, Theme.GAP_SM, Theme.GAP_SM));

        if (selectedProjet == null)
            return new JPanel();

        List<Tache> crits = new java.util.ArrayList<>();
        if (selectedProjet != null) {
            java.util.Set<Integer> cp = ProjetService.calculerCheminCritique(selectedProjet);
            crits = selectedProjet.getTaches().stream()
                    .filter(t2 -> cp.contains(t2.getId()) && t2.getStatut() != Tache.Statut.TERMINE)
                    .limit(5).collect(Collectors.toList());
            
            // Fallback aux priorités CRITIQUE si aucune tâche du chemin critique n'est active
            if (crits.isEmpty()) {
                crits = selectedProjet.getTaches().stream()
                        .filter(t2 -> t2.getPriorite() == Tache.Priorite.CRITIQUE && t2.getStatut() != Tache.Statut.TERMINE)
                        .limit(5).collect(Collectors.toList());
            }
        }

        if (crits.isEmpty()) {
            JLabel ok = new JLabel("Aucune tâche critique en attente");
            FlatSVGIcon iconC = new FlatSVGIcon("resources/icons/check.svg");
            iconC.setColorFilter(new FlatSVGIcon.ColorFilter(c2 -> Theme.CYAN));
            ok.setIcon(iconC);
            ok.setForeground(Theme.GREEN);
            ok.setFont(Theme.F_SMALL);
            ok.setBorder(new EmptyBorder(Theme.GAP_SM, Theme.GAP_SM, Theme.GAP_SM, 0));
            list.add(ok);
        } else {
            for (Tache t2 : crits) {
                JPanel row = new JPanel(new BorderLayout(8, 0));
                row.setOpaque(false);
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
                row.setBorder(new EmptyBorder(6, 0, 6, 0));

                JLabel ln = new JLabel(t2.getNom());
                ln.setFont(Theme.font(Font.BOLD, 12));
                ln.setForeground(Theme.TEXT_PRIMARY);
                FlatSVGIcon iconA2 = new FlatSVGIcon("resources/icons/alert.svg", 14, 14);
                iconA2.setColorFilter(new FlatSVGIcon.ColorFilter(c2 -> Theme.RED));
                ln.setIcon(iconA2);

                Widgets.Badge b = new Widgets.Badge(t2.getStatut().toString().replace("_", " "), Theme.RED);
                row.add(ln, BorderLayout.CENTER);
                row.add(b, BorderLayout.EAST);
                list.add(row);
                list.add(new JSeparator(SwingConstants.HORIZONTAL) {
                    @Override
                    public void paintComponent(Graphics g) {
                        g.setColor(Theme.BORDER);
                        g.drawLine(0, 0, getWidth(), 0);
                    }
                });
            }
        }
        c.add(list, BorderLayout.CENTER);
        return c;
    }

}
