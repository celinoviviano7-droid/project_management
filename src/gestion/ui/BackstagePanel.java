package gestion.ui;

import gestion.model.Projet;
import gestion.service.ProjetService;
import gestion.util.Theme;
import gestion.util.Toast;
import gestion.util.Widgets;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * BackstagePanel : Le Hub d'accueil de l'application (Architecture Backstage).
 * Permet de gérer ses fichiers de projet avant d'entrer dans l'espace de travail.
 */
public class BackstagePanel extends JPanel {

    private final ProjetService service;
    private final MainFrame mainFrame;
    private JPanel cards;
    private CardLayout cl;
    private Image bgImage;

    public BackstagePanel(ProjetService service, MainFrame mainFrame) {
        this.service = service;
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(Theme.BG_APP);
        
        loadBackgroundImage();
        buildUI();
    }

    private void loadBackgroundImage() {
        try {
            String path = "img/top-view-paint-can-containers-classic-blue-color.jpg";
            java.io.File file = new java.io.File(path);
            if (file.exists()) {
                bgImage = new ImageIcon(file.getAbsolutePath()).getImage();
            } else {
                java.net.URL url = getClass().getResource("/" + path);
                if (url != null) bgImage = new ImageIcon(url).getImage();
            }
        } catch (Exception e) {
            System.err.println("[Backstage] Erreur chargement fond: " + e.getMessage());
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (bgImage != null) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            
            // Dessiner l'image ajustée à la taille du panel
            g2.drawImage(bgImage, 0, 0, getWidth(), getHeight(), null);
            
            // Overlay sombre pour la lisibilité
            g2.setColor(new Color(11, 11, 20, 180));
            g2.fillRect(0, 0, getWidth(), getHeight());
            
            g2.dispose();
        }
    }

    private void buildUI() {
        // Sidebar du Hub
        add(buildHubSidebar(), BorderLayout.WEST);

        // Zone de contenu
        cl = new CardLayout();
        cards = new JPanel(cl);
        cards.setOpaque(false);

        cards.add(buildHomeView(), "HOME");
        cards.add(buildPlaceholderView("Paramètres", "Configurez vos préférences ici"), "SETTINGS");
        cards.add(buildPlaceholderView("Aide & Support", "Consultez la documentation ou contactez le support"), "HELP");

        add(cards, BorderLayout.CENTER);
    }

    private JPanel buildHubSidebar() {
        JPanel sb = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(14, 14, 25, 200)); // BG_SIDEBAR avec alpha
                g.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };
        sb.setOpaque(false);
        sb.setPreferredSize(new Dimension(280, 0));
        sb.setLayout(new BoxLayout(sb, BoxLayout.Y_AXIS));
        sb.setBorder(new javax.swing.border.MatteBorder(0, 0, 0, 1, Theme.BORDER));

        // Logo (uniquement l'image, centrée et agrandie)
        JPanel logoZone = new JPanel();
        logoZone.setLayout(new BoxLayout(logoZone, BoxLayout.Y_AXIS));
        logoZone.setOpaque(false);
        logoZone.setBorder(new EmptyBorder(45, 0, 30, 0));
        logoZone.setAlignmentX(CENTER_ALIGNMENT);
        
        JLabel lblLogo;
        try {
            java.net.URL logoUrl = getClass().getResource("/img/Gemini_Generated_Image_ubpqkiubpqkiubpq.png");
            if (logoUrl != null) {
                Image img = new ImageIcon(logoUrl).getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                lblLogo = new JLabel(new ImageIcon(img));
            } else {
                FlatSVGIcon logoIcon = Widgets.svg("/resources/icons/folder.svg", 64, 64);
                logoIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Theme.CYAN));
                lblLogo = new JLabel(logoIcon);
            }
        } catch (Exception e) {
            lblLogo = new JLabel("TT");
            lblLogo.setFont(Theme.font(Font.BOLD, 28));
            lblLogo.setForeground(Theme.CYAN);
        }
        lblLogo.setAlignmentX(CENTER_ALIGNMENT);
        
        logoZone.add(lblLogo);
        sb.add(logoZone);

        sb.add(Box.createVerticalStrut(20));

        // Navigation
        sb.add(hubNavItem("Accueil", "/resources/icons/home.svg", "HOME"));
        sb.add(Box.createVerticalStrut(4));
        sb.add(hubNavItem("Paramètres", "/resources/icons/edit.svg", "SETTINGS"));
        sb.add(Box.createVerticalStrut(4));
        sb.add(hubNavItem("Aide", "/resources/icons/milestone.svg", "HELP"));

        sb.add(Box.createVerticalGlue());

        // Footer version
        JLabel ver = new JLabel("Version 3.0.0 — Backstage Edition");
        ver.setFont(Theme.F_TINY);
        ver.setForeground(Theme.TEXT_MUTED);
        ver.setBorder(new EmptyBorder(0, 25, 20, 0));
        sb.add(ver);

        return sb;
    }

    private JPanel hubNavItem(String text, String iconPath, String cardId) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        p.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        FlatSVGIcon icon = Widgets.svg(iconPath, 18, 18);
        icon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Theme.TEXT_SECONDARY));
        JLabel lbl = new JLabel(text, icon, SwingConstants.LEFT);
        lbl.setIconTextGap(15);
        lbl.setFont(Theme.F_BODY);
        lbl.setForeground(Theme.TEXT_SECONDARY);
        lbl.setBorder(new EmptyBorder(0, 25, 0, 0));

        p.add(lbl, BorderLayout.CENTER);

        p.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                cl.show(cards, cardId);
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                p.setOpaque(true);
                p.setBackground(Theme.BG_SELECTED);
                lbl.setForeground(Color.WHITE);
                p.repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                p.setOpaque(false);
                lbl.setForeground(Theme.TEXT_SECONDARY);
                p.repaint();
            }
        });

        return p;
    }

    private JPanel buildHomeView() {
        JPanel home = new JPanel();
        home.setOpaque(false);
        home.setLayout(new BoxLayout(home, BoxLayout.Y_AXIS));
        home.setBorder(new EmptyBorder(60, 60, 60, 60));

        // Titre
        JLabel title = new JLabel("Bienvenue sur Tantagna tetikasa");
        title.setFont(Theme.font(Font.BOLD, 36));
        title.setForeground(Theme.TEXT_PRIMARY);
        home.add(title);

        home.add(Box.createVerticalStrut(10));
        JLabel sub = new JLabel("Que souhaitez-vous faire aujourd'hui ?");
        sub.setFont(Theme.font(Font.PLAIN, 18));
        sub.setForeground(Theme.TEXT_SECONDARY);
        home.add(sub);

        home.add(Box.createVerticalStrut(50));

        // Actions Rapides
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        actions.setOpaque(false);
        actions.setAlignmentX(LEFT_ALIGNMENT);

        actions.add(actionCard("Nouveau Projet", "Démarrer un projet de zéro", Theme.ACCENT, "/resources/icons/plus.svg", () -> {
            // Ici on pourrait ouvrir NouveauProjetDialog, mais MainFrame sait déjà le faire.
            // On va juste forcer l'entrée dans le workspace et déclencher l'action.
            mainFrame.openProjet(null); // Mode "vide" ou direct
            // Alternative: mainFrame.triggerNewProjet()
        }));

        actions.add(actionCard("Ouvrir", "Parcourir vos fichiers locaux", Theme.PURPLE, "/resources/icons/folder.svg", () -> {
            Toast.info("Fonctionnalité d'importation bientôt disponible");
        }));

        home.add(actions);

        home.add(Box.createVerticalStrut(60));

        // Récents
        JLabel lblRecent = new JLabel("RÉCENTS");
        lblRecent.setFont(Theme.F_LABEL);
        lblRecent.setForeground(Theme.TEXT_MUTED);
        home.add(lblRecent);
        home.add(Box.createVerticalStrut(15));

        JPanel recentGrid = new JPanel(new GridLayout(0, 2, 15, 15));
        recentGrid.setOpaque(false);
        recentGrid.setAlignmentX(LEFT_ALIGNMENT);
        recentGrid.setMaximumSize(new Dimension(900, 300));

        List<Projet> list = service.getProjets();
        for (int i = 0; i < Math.min(list.size(), 4); i++) {
            recentGrid.add(recentProjectCard(list.get(i)));
        }

        home.add(recentGrid);
        return home;
    }

    private JPanel actionCard(String title, String desc, Color color, String iconPath, Runnable action) {
        Widgets.RoundPanel card = new Widgets.RoundPanel(Theme.R_MD, Theme.BORDER);
        card.setBackground(Theme.BG_CARD);
        card.setLayout(new BorderLayout());
        card.setPreferredSize(new Dimension(280, 140));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel content = new JPanel(new BorderLayout(Theme.GAP_MD, 0));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(20, 20, 20, 20));

        FlatSVGIcon icon = Widgets.svg(iconPath, 32, 32);
        icon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> color));
        JLabel lblIcon = new JLabel(icon);
        content.add(lblIcon, BorderLayout.WEST);

        JPanel info = new JPanel(new GridLayout(2, 1, 0, 2));
        info.setOpaque(false);
        JLabel lblT = new JLabel(title);
        lblT.setFont(Theme.font(Font.BOLD, 16));
        lblT.setForeground(Theme.TEXT_PRIMARY);
        JLabel lblD = new JLabel("<html>" + desc + "</html>");
        lblD.setFont(Theme.F_SMALL);
        lblD.setForeground(Theme.TEXT_MUTED);
        info.add(lblT);
        info.add(lblD);

        content.add(info, BorderLayout.CENTER);
        card.add(content, BorderLayout.CENTER);

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { action.run(); }
            @Override
            public void mouseEntered(MouseEvent e) { card.setBackground(Theme.BG_CARD_HOVER); }
            @Override
            public void mouseExited(MouseEvent e) { card.setBackground(Theme.BG_CARD); }
        });

        return card;
    }

    private JPanel recentProjectCard(Projet p) {
        Widgets.RoundPanel card = new Widgets.RoundPanel(Theme.R_SM, Theme.BORDER);
        card.setBackground(Theme.BG_CARD);
        card.setLayout(new BorderLayout());
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel content = new JPanel(new BorderLayout(15, 0));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(12, 15, 12, 15));

        FlatSVGIcon icon = Widgets.svg("/resources/icons/folder.svg", 20, 20);
        icon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Theme.CYAN));
        content.add(new JLabel(icon), BorderLayout.WEST);

        JPanel info = new JPanel(new BorderLayout());
        info.setOpaque(false);
        JLabel lblNom = new JLabel(p.getNom());
        lblNom.setFont(Theme.font(Font.BOLD, 14));
        lblNom.setForeground(Theme.TEXT_PRIMARY);
        JLabel lblDet = new JLabel(p.getTaches().size() + " tâches • Chef: " + p.getResponsable());
        lblDet.setFont(Theme.F_TINY);
        lblDet.setForeground(Theme.TEXT_MUTED);
        info.add(lblNom, BorderLayout.NORTH);
        info.add(lblDet, BorderLayout.SOUTH);
        content.add(info, BorderLayout.CENTER);

        Widgets.Badge b = new Widgets.Badge(p.getProgressionGlobale() + "%", Theme.GREEN);
        content.add(b, BorderLayout.EAST);

        card.add(content, BorderLayout.CENTER);

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { mainFrame.openProjet(p); }
            @Override
            public void mouseEntered(MouseEvent e) { card.setBackground(Theme.BG_CARD_HOVER); }
            @Override
            public void mouseExited(MouseEvent e) { card.setBackground(Theme.BG_CARD); }
        });

        return card;
    }

    private JPanel buildPlaceholderView(String title, String desc) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);

        JLabel lblT = new JLabel(title);
        lblT.setFont(Theme.font(Font.BOLD, 24));
        lblT.setForeground(Theme.TEXT_PRIMARY);
        lblT.setAlignmentX(CENTER_ALIGNMENT);

        JLabel lblD = new JLabel(desc);
        lblD.setFont(Theme.F_BODY);
        lblD.setForeground(Theme.TEXT_SECONDARY);
        lblD.setAlignmentX(CENTER_ALIGNMENT);

        inner.add(lblT);
        inner.add(Box.createVerticalStrut(10));
        inner.add(lblD);

        p.add(inner);
        return p;
    }

    public void refresh() {
        // Optionnel: rafraichir la liste des récents si nécessaire
        // Pour l'instant on reconstruit partiellement ou on laisse tel quel car chargé au démarrage
    }
}
