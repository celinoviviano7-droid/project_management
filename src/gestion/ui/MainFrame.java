package gestion.ui;

import gestion.model.*;
import gestion.service.ProjetService;
import gestion.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import com.formdev.flatlaf.extras.FlatSVGIcon;

public class MainFrame extends JFrame {

    private static final String V_DASH = "dashboard";
    private static final String V_GANTT = "gantt";
    private static final String V_TABLE = "table";
    private static final String V_TEAM = "equipe";
    private static final String V_JALON = "jalons";

    private static final String ROOT_BACKSTAGE = "BACKSTAGE";
    private static final String ROOT_WORKSPACE = "WORKSPACE";
    private boolean isRefreshingList = false;
    private boolean sidebarCollapsed = true;
    private JDialog detailDialog;
    private String searchQueryTaches = "";
    private JPanel sidebarPanel, navSectionProjets, footPanel, userInfoPanel;
    private JLabel lblLogoText;
    private List<NavBtn> navButtons = new ArrayList<>();
    private JButton btnToggle;
    private Widgets.FlatButton btnNew;
    private Projet selectedProjet;
    private ProjetService service = new ProjetService();

    // ── Root Layout ──────────────────────────────────────────
    private CardLayout rootLayout;
    private JPanel rootPanel;
    private BackstagePanel backstage;
    private JPanel workspacePanel;

    // ── Layout central ────────────────────────────────────────
    private CardLayout cardLayout, taskCardLayout;
    private JPanel mainContent, taskContent;

    // ── Panels ────────────────────────────────────────────────
    private DashboardPanel dashboard;
    private GanttPanel gantt;
    private TacheDetailPanel detail;
    private EquipePanel equipe;
    private JalonsPanel jalons;

    // ── Table tâches ─────────────────────────────────────────
    private JTable tacheTable;
    private DefaultTableModel tableModel;

    // ── Sidebar ───────────────────────────────────────────────
    private DefaultListModel<Projet> listModel;
    private JList<Projet> projetList;
    private JLabel lblCount;

    // ── Topbar ────────────────────────────────────────────────
    private JLabel lblBreadcrumb;
    private JProgressBar topBar;
    private JLabel lblPct, lblMeta;

    // ── Nav ───────────────────────────────────────────────────
    private NavBtn activeNav;

    public MainFrame() {
        Toast.init(this);
        initFrame();
        
        // 1. Initialiser le Hub Backstage
        backstage = new BackstagePanel(service, this);
        
        // 2. Initialiser l'Espace de Travail
        workspacePanel = buildWorkspaceUI();
        
        // 3. Root Switcher
        rootLayout = new CardLayout();
        rootPanel = new JPanel(rootLayout);
        rootPanel.add(backstage, ROOT_BACKSTAGE);
        rootPanel.add(workspacePanel, ROOT_WORKSPACE);
        
        setLayout(new BorderLayout());
        add(rootPanel, BorderLayout.CENTER);

        loadProjets();
        showBackstage();
    }

    public void showBackstage() {
        rootLayout.show(rootPanel, ROOT_BACKSTAGE);
        backstage.refresh();
        setTitle("Tantagna tetikasa - Accueil");
    }

    public void openProjet(Projet p) {
        if (p == null) {
            newProjet();
            return;
        }
        onSelectProjet(p);
        rootLayout.show(rootPanel, ROOT_WORKSPACE);
        navigate(V_DASH);
        setTitle("Tantagna tetikasa - " + p.getNom());
    }

    // ─────────────────────────────────────────────────────────
    // Init & Layout
    // ─────────────────────────────────────────────────────────
    private void initFrame() {
        setTitle("Gestion de Projet Professionnelle");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1440, 880);
        setMinimumSize(new Dimension(1050, 680));
        setLocationRelativeTo(null);
        setIconImages(buildIcons());
    }

    private java.util.List<Image> buildIcons() {
        java.util.List<Image> icons = new java.util.ArrayList<>();
        try {
            java.net.URL resource = getClass().getResource("/img/Gemini_Generated_Image_ubpqkiubpqkiubpq.png");
            if (resource == null) {
                java.io.File file = new java.io.File("img/Gemini_Generated_Image_ubpqkiubpqkiubpq.png");
                if (file.exists()) {
                    resource = file.toURI().toURL();
                }
            }
            if (resource != null) {
                Image base = new ImageIcon(resource).getImage();
                for (int size : new int[] { 16, 32, 64, 128, 256, 512 }) {
                    BufferedImage scaled = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2 = scaled.createGraphics();
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.drawImage(base, 0, 0, size, size, null);
                    g2.dispose();
                    icons.add(scaled);
                }
            }
        } catch (Exception e) {
            System.err.println("[MainFrame] Erreur chargement icônes : " + e.getMessage());
        }
        return icons;
    }

    private JPanel buildWorkspaceUI() {
        JPanel wp = new JPanel(new BorderLayout(0, 0));
        wp.setBackground(Theme.BG_APP);
        wp.add(buildTopBar(), BorderLayout.NORTH);
        wp.add(buildSidebar(), BorderLayout.WEST);
        wp.add(buildCenter(), BorderLayout.CENTER);
        return wp;
    }

    // ─────────────────────────────────────────────────────────
    // TOPBAR
    // ─────────────────────────────────────────────────────────
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Theme.BG_TOPBAR);
        bar.setBorder(new MatteBorder(0, 0, 1, 0, Theme.BORDER));
        bar.setPreferredSize(new Dimension(0, 52));

        // Gauche : Bouton Retour + Breadcrumb
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setBackground(Theme.BG_TOPBAR);
        
        left.add(Box.createHorizontalStrut(Theme.GAP_MD));

        // Bouton Retour au Hub
        FlatSVGIcon homeIcon = Widgets.svg("/resources/icons/home.svg", 16, 16);
        homeIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Theme.TEXT_SECONDARY));
        Widgets.FlatButton btnHome = Widgets.FlatButton.icon(homeIcon);
        btnHome.setToolTipText("Retour à l'accueil");
        btnHome.addActionListener(e -> showBackstage());
        left.add(btnHome);
        left.add(Box.createHorizontalStrut(Theme.GAP_SM));

        JPanel bread = new JPanel(new FlowLayout(FlowLayout.LEFT, Theme.GAP_MD, 0));
        bread.setBackground(Theme.BG_TOPBAR);
        lblBreadcrumb = new JLabel("Accueil");
        lblBreadcrumb.setFont(Theme.F_SMALL);
        lblBreadcrumb.setForeground(Theme.TEXT_SECONDARY);
        bread.add(lblBreadcrumb);
        left.add(bread);
        bar.add(left, BorderLayout.WEST);

        // Droite : progression + stats
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, Theme.GAP_SM, 10));
        right.setBackground(Theme.BG_TOPBAR);
        right.setBorder(new EmptyBorder(0, 0, 0, Theme.GAP_MD));

        lblMeta = new JLabel("");
        lblMeta.setFont(Theme.F_TINY);
        lblMeta.setForeground(Theme.TEXT_SECONDARY);
        right.add(lblMeta);

        JLabel pl = new JLabel("Progression :");
        pl.setFont(Theme.F_TINY);
        pl.setForeground(Theme.TEXT_SECONDARY);
        right.add(pl);

        topBar = new JProgressBar(0, 100);
        topBar.setPreferredSize(new Dimension(120, 8));
        topBar.setForeground(Theme.GREEN);
        topBar.setBackground(new Color(36, 36, 60));
        topBar.setBorderPainted(false);
        right.add(topBar);

        lblPct = new JLabel("0%");
        lblPct.setFont(Theme.font(Font.BOLD, 11));
        lblPct.setForeground(Theme.GREEN);
        right.add(lblPct);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    // ─────────────────────────────────────────────────────────
    // SIDEBAR
    // ─────────────────────────────────────────────────────────
    private JPanel buildSidebar() {
        JPanel sb = new JPanel(new BorderLayout());
        sb.setBackground(Theme.BG_SIDEBAR);
        sb.setPreferredSize(new Dimension(60, 0));
        sb.setBorder(new MatteBorder(0, 0, 0, 1, Theme.BORDER));

        // Navigation
        JPanel nav = new JPanel();
        nav.setBackground(Theme.BG_SIDEBAR);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(new EmptyBorder(Theme.GAP_SM, Theme.GAP_SM, Theme.GAP_SM, Theme.GAP_SM));
        nav.add(Box.createVerticalStrut(10));

        NavBtn d = new NavBtn(Widgets.svg("/resources/icons/dashboard.svg"), "Tableau de bord", V_DASH);
        NavBtn g = new NavBtn(Widgets.svg("/resources/icons/gantt.svg"), "Diagramme de Gantt", V_GANTT);
        NavBtn t = new NavBtn(Widgets.svg("/resources/icons/table.svg"), "Tâches", V_TABLE);
        NavBtn e = new NavBtn(Widgets.svg("/resources/icons/team.svg"), "Équipe", V_TEAM);
        NavBtn jn = new NavBtn(Widgets.svg("/resources/icons/milestone.svg"), "Jalons", V_JALON);

        navButtons = Arrays.asList(d, g, t, e, jn);
        for (NavBtn b : navButtons) {
            b.setCollapsed(true);
            nav.add(b);
            nav.add(Box.createVerticalStrut(2));
        }
        activeNav = d;
        d.setActive(true);

        sidebarPanel = sb;

        // Container pour Logo + Nav
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);

        // Logo Zone (Haut de la sidebar)
        JPanel logoZone = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 15));
        logoZone.setOpaque(false);
        try {
            java.net.URL logoUrl = getClass().getResource("/img/Gemini_Generated_Image_ubpqkiubpqkiubpq.png");
            if (logoUrl != null) {
                Image img = new ImageIcon(logoUrl).getImage().getScaledInstance(42, 42, Image.SCALE_SMOOTH);
                logoZone.add(new JLabel(new ImageIcon(img)));
            }
        } catch (Exception ex) {}
        topContainer.add(logoZone);
        topContainer.add(nav);

        sb.add(topContainer, BorderLayout.NORTH);


        // Initialiser les modèles vides (nécessaire pour loadProjets)
        listModel = new DefaultListModel<>();
        projetList = new JList<>(listModel);
        projetList.addListSelectionListener(e2 -> {
            if (!e2.getValueIsAdjusting() && !isRefreshingList)
                onSelectProjet(projetList.getSelectedValue());
        });

        footPanel = buildSideFooter();
        // Ajuster l'avatar pour le mode replié initial
        userInfoPanel.setVisible(false);
        FlowLayout fl = (FlowLayout) footPanel.getLayout();
        fl.setAlignment(FlowLayout.CENTER);
        fl.setHgap(0);

        sb.add(footPanel, BorderLayout.SOUTH);
        return sb;
    }

    private JPanel navSection(String title) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setAlignmentX(LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        JLabel l = new JLabel(title);
        l.setFont(Theme.F_LABEL);
        l.setForeground(Color.WHITE);
        l.setBorder(new EmptyBorder(0, 6, 4, 0));
        p.add(l, BorderLayout.WEST);
        return p;
    }

    private JPanel buildSideFooter() {
        JPanel f = new JPanel(new FlowLayout(FlowLayout.LEFT, Theme.GAP_SM, Theme.GAP_SM));
        f.setBackground(new Color(8, 8, 17));
        f.setBorder(new MatteBorder(1, 0, 0, 0, Theme.BORDER));
        
        Widgets.Avatar av = new Widgets.Avatar("AD", Theme.ACCENT, 32);
        
        userInfoPanel = new JPanel(new BorderLayout(0, 1));
        userInfoPanel.setOpaque(false);
        JLabel nm = new JLabel("Administrateur");
        nm.setFont(Theme.font(Font.BOLD, 11));
        nm.setForeground(Theme.TEXT_PRIMARY);
        JLabel rl = new JLabel("Chef de projet");
        rl.setFont(Theme.F_TINY);
        rl.setForeground(Theme.TEXT_MUTED);
        userInfoPanel.add(nm, BorderLayout.NORTH);
        userInfoPanel.add(rl, BorderLayout.SOUTH);
        
        f.add(av);
        f.add(userInfoPanel);
        return f;
    }

    private void toggleSidebar() {
        sidebarCollapsed = !sidebarCollapsed;
        int width = sidebarCollapsed ? 60 : 218;
        sidebarPanel.setPreferredSize(new Dimension(width, 0));
        
        // On masque les infos texte mais on garde le footer (pour l'avatar)
        userInfoPanel.setVisible(!sidebarCollapsed);
        
        // Ajuster l'alignement pour centrer l'avatar quand c'est replié
        FlowLayout fl = (FlowLayout) footPanel.getLayout();
        if (sidebarCollapsed) {
            fl.setAlignment(FlowLayout.CENTER);
            fl.setHgap(0);
        } else {
            fl.setAlignment(FlowLayout.LEFT);
            fl.setHgap(Theme.GAP_SM);
        }
        
        for (NavBtn b : navButtons) b.setCollapsed(sidebarCollapsed);
        btnToggle.repaint();
        revalidate();
        repaint();
    }


    private void toggleTaskDetail() {
        detailDialog.setVisible(!detailDialog.isVisible());
    }

    // ─────────────────────────────────────────────────────────
    // CENTRE
    // ─────────────────────────────────────────────────────────
    private JPanel buildCenter() {
        cardLayout = new CardLayout();
        mainContent = new JPanel(cardLayout);
        mainContent.setBackground(Theme.BG_PANEL);

        // Initialiser le panneau de détail en tant que fenêtre flottante
        detail = new TacheDetailPanel(() -> refreshAll());
        detailDialog = new JDialog(this, "Détails de la tâche", false);
        detailDialog.setLayout(new BorderLayout());
        detailDialog.add(detail);
        detailDialog.setSize(420, 800);
        detailDialog.setLocation(1050, 100); // Position par défaut à droite du centre
        
        // Créer le module des tâches (Gantt + Tableau partageant le détail)
        taskCardLayout = new CardLayout();
        taskContent = new JPanel(taskCardLayout);
        taskContent.setOpaque(true);
        taskContent.setBackground(Theme.BG_PANEL);
        taskContent.add(buildGanttView(), V_GANTT);
        taskContent.add(buildTableView(), V_TABLE);

        dashboard = new DashboardPanel(service);
        dashboard.setOnOpenGantt(() -> navigate(V_GANTT));
        mainContent.add(dashboard, V_DASH);
        mainContent.add(taskContent, "task_module"); 

        equipe = new EquipePanel(() -> {
            refreshAll();
            loadProjets();
        });
        mainContent.add(equipe, V_TEAM);

        jalons = new JalonsPanel(() -> {
            refreshAll();
            loadProjets();
        });
        mainContent.add(jalons, V_JALON);
        return mainContent;
    }

    private JPanel buildGanttView() {
        JPanel v = new JPanel(new BorderLayout());
        v.setBackground(Theme.BG_PANEL);
        v.setOpaque(true);

        // Toolbar
        JPanel tb = new JPanel(new BorderLayout());
        tb.setBackground(Theme.BG_CARD);
        tb.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, Theme.BORDER),
                new EmptyBorder(Theme.GAP_SM, Theme.GAP_MD, Theme.GAP_SM, Theme.GAP_MD)));
        JPanel tl = new JPanel(new FlowLayout(FlowLayout.LEFT, Theme.GAP_SM, 0));
        tl.setOpaque(false);
        Widgets.FlatButton add = new Widgets.FlatButton("Nouvelle tâche", Theme.ACCENT);
        FlatSVGIcon iconAdd = Widgets.svg("/resources/icons/plus.svg");
        iconAdd.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Theme.CYAN));
        add.setIcon(iconAdd);
        Widgets.FlatButton del = new Widgets.FlatButton("Supprimer", Theme.RED);
        FlatSVGIcon iconDel = Widgets.svg("/resources/icons/trash.svg");
        iconDel.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Theme.CYAN));
        del.setIcon(iconDel);
        add.addActionListener(e -> newTache());
        del.addActionListener(e -> deleteTache());

        JTextField searchField = Widgets.searchField("Rechercher une tâche...", q -> {
            searchQueryTaches = q.toLowerCase();
            refreshAll();
        });
        tl.add(searchField);
        tl.add(Box.createHorizontalStrut(Theme.GAP_SM));

        tl.add(add);
        tl.add(del);

        // Bouton replier détail
        FlatSVGIcon iconDetail = Widgets.svg("/resources/icons/edit.svg");
        iconDetail.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Theme.CYAN));
        Widgets.FlatButton btnDet = new Widgets.FlatButton("Détails", Theme.BG_CARD);
        btnDet.setIcon(iconDetail);
        btnDet.addActionListener(e -> toggleTaskDetail());
        tl.add(Box.createHorizontalStrut(Theme.GAP_SM));
        tl.add(btnDet);
        JPanel tr = new JPanel(new FlowLayout(FlowLayout.RIGHT, Theme.GAP_SM, 0));
        tr.setOpaque(false);
        JLabel hint = new JLabel("💡 Ctrl+Molette pour Zoom");
        hint.setFont(Theme.F_TINY);
        hint.setForeground(Theme.TEXT_MUTED);
        tr.add(hint);
        tr.add(Box.createHorizontalStrut(Theme.GAP_MD));

        Widgets.FlatButton zIn = Widgets.FlatButton.outline(" + ", Theme.TEXT_SECONDARY);
        zIn.addActionListener(e -> gantt.zoomIn());
        Widgets.FlatButton zOut = Widgets.FlatButton.outline(" - ", Theme.TEXT_SECONDARY);
        zOut.addActionListener(e -> gantt.zoomOut());
        Widgets.FlatButton zRes = Widgets.FlatButton.outline(" 100% ", Theme.TEXT_SECONDARY);
        zRes.addActionListener(e -> gantt.resetZoom());

        tr.add(zOut);
        tr.add(zIn);
        tr.add(zRes);
        tb.add(tl, BorderLayout.WEST);
        tb.add(tr, BorderLayout.EAST);

        gantt = new GanttPanel();
        gantt.setListener((t, p) -> {
            detail.setTache(t, p);
            if (t != null) detailDialog.setVisible(true);
        });
        JScrollPane gs = Widgets.scroll(gantt);
        gs.getViewport().setBackground(Theme.BG_PANEL);

        v.add(tb, BorderLayout.NORTH);
        v.add(gs, BorderLayout.CENTER);
        
        return v;
    }

    private JPanel buildTableView() {
        JPanel v = new JPanel(new BorderLayout());
        v.setBackground(Theme.BG_PANEL);

        JPanel tb = new JPanel(new BorderLayout());
        tb.setBackground(Theme.BG_CARD);
        tb.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, Theme.BORDER),
                new EmptyBorder(Theme.GAP_SM, Theme.GAP_MD, Theme.GAP_SM, Theme.GAP_MD)));
        JPanel tl = new JPanel(new FlowLayout(FlowLayout.LEFT, Theme.GAP_SM, 0));
        tl.setOpaque(false);
        Widgets.FlatButton add = new Widgets.FlatButton("Nouvelle tâche", Theme.ACCENT);
        FlatSVGIcon iconAdd2 = Widgets.svg("/resources/icons/plus.svg");
        iconAdd2.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Theme.CYAN));
        add.setIcon(iconAdd2);
        Widgets.FlatButton del = new Widgets.FlatButton("Supprimer", Theme.RED);
        FlatSVGIcon iconDel2 = Widgets.svg("/resources/icons/trash.svg");
        iconDel2.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Theme.CYAN));
        del.setIcon(iconDel2);
        add.addActionListener(e -> newTache());
        del.addActionListener(e -> deleteTache());

        // Bouton replier détail
        FlatSVGIcon iconDetail2 = Widgets.svg("/resources/icons/edit.svg");
        iconDetail2.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Theme.CYAN));
        Widgets.FlatButton btnDet2 = new Widgets.FlatButton("Détails", Theme.BG_CARD);
        btnDet2.setIcon(iconDetail2);
        btnDet2.addActionListener(e -> toggleTaskDetail());

        JTextField searchField = Widgets.searchField("Rechercher une tâche...", q -> {
            searchQueryTaches = q.toLowerCase();
            refreshAll();
        });
        tl.add(searchField);
        tl.add(Box.createHorizontalStrut(Theme.GAP_SM));

        tl.add(add);
        tl.add(del);
        tl.add(Box.createHorizontalStrut(Theme.GAP_SM));
        tl.add(btnDet2);
        tb.add(tl, BorderLayout.WEST);
        v.add(tb, BorderLayout.NORTH);

        String[] cols = {"ID", "Tâche", "Début", "Fin", "Durée", "Marge", "Prédécesseurs", "Successeurs", "Prog.", "Statut", "Resp."};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                if (c == 0 || c == 4 || c == 5 || c == 8) return Integer.class;
                return String.class;
            }
        };

        tacheTable = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                c.setBackground(
                        isRowSelected(row) ? Theme.BG_SELECTED : row % 2 == 0 ? Theme.BG_ROW_ODD : Theme.BG_ROW_EVEN);
                c.setForeground(Theme.TEXT_PRIMARY);
                return c;
            }
        };
        tacheTable.setBackground(Theme.BG_ROW_ODD);
        tacheTable.setForeground(Theme.TEXT_PRIMARY);
        tacheTable.setSelectionBackground(Theme.BG_SELECTED);
        tacheTable.setGridColor(Theme.BORDER);
        tacheTable.setRowHeight(36);
        tacheTable.setFont(Theme.F_SMALL);
        tacheTable.setShowGrid(false);
        tacheTable.setIntercellSpacing(new Dimension(0, 1));
        tacheTable.setFillsViewportHeight(true);
        tacheTable.setAutoCreateRowSorter(true);

        JTableHeader th = tacheTable.getTableHeader();
        th.setBackground(Theme.BG_TOPBAR);
        th.setForeground(Theme.TEXT_SECONDARY);
        th.setFont(Theme.F_LABEL);
        th.setBorder(new MatteBorder(0, 0, 1, 0, Theme.BORDER));
        th.setReorderingAllowed(false);

        int[] widths = { 40, 200, 85, 85, 65, 65, 130, 130, 80, 100, 90 };
        for (int i = 0; i < widths.length; i++)
            tacheTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Renderer Date (2, 3)
        TableCellRenderer dateRenderer = (table, val, sel, foc, r, c) -> {
            JLabel l = new JLabel(val != null ? val.toString() : ""); 
            l.setFont(Theme.F_SMALL); l.setForeground(Theme.TEXT_PRIMARY);
            l.setOpaque(true); l.setBackground(sel ? Theme.BG_SELECTED : r % 2 == 0 ? Theme.BG_ROW_ODD : Theme.BG_ROW_EVEN);
            l.setBorder(new EmptyBorder(0,5,0,5));
            return l;
        };
        tacheTable.getColumnModel().getColumn(2).setCellRenderer(dateRenderer);
        tacheTable.getColumnModel().getColumn(3).setCellRenderer(dateRenderer);

        // Renderer Durée et Marge (4, 5)
        TableCellRenderer centerRenderer = (table, val, sel, foc, row, col) -> {
            JLabel l = new JLabel(val + " j");
            l.setFont(Theme.F_SMALL); l.setForeground(Theme.TEXT_PRIMARY);
            l.setHorizontalAlignment(SwingConstants.CENTER);
            l.setOpaque(true); l.setBackground(sel ? Theme.BG_SELECTED : row % 2 == 0 ? Theme.BG_ROW_ODD : Theme.BG_ROW_EVEN);
            return l;
        };
        tacheTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        tacheTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);

        // Renderer Progression (8)
        tacheTable.getColumnModel().getColumn(8).setCellRenderer((table, val, sel, foc, row, col) -> {
            int pv = val instanceof Integer ? (Integer) val : 0;
            Color pc = pv == 100 ? Theme.GREEN : pv > 50 ? Theme.ACCENT : Theme.ORANGE;
            Widgets.ProgressBar pb = new Widgets.ProgressBar(pv, pc);
            pb.setBackground(sel ? Theme.BG_SELECTED : row % 2 == 0 ? Theme.BG_ROW_ODD : Theme.BG_ROW_EVEN);
            pb.setOpaque(true);
            return pb;
        });

        // Renderer Statut (9)
        tacheTable.getColumnModel().getColumn(9).setCellRenderer((table, val, sel, foc, row, col) -> {
            Tache.Statut s = val instanceof Tache.Statut ? (Tache.Statut) val : Tache.Statut.NON_COMMENCE;
            String sv = s.toString().replace("_", " ");
            Widgets.Badge b = new Widgets.Badge(sv, Theme.statutColor(s));
            b.setBackground(sel ? Theme.BG_SELECTED : row % 2 == 0 ? Theme.BG_ROW_ODD : Theme.BG_ROW_EVEN);
            b.setOpaque(true);
            return b;
        });

        tacheTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && selectedProjet != null) {
                int r = tacheTable.getSelectedRow();
                    if (r >= 0) {
                        try {
                            int id = (int) tacheTable.getValueAt(r, 0);
                            Tache t = findTache(id);
                            if (t != null) {
                                detail.setTache(t, selectedProjet);
                                detailDialog.setVisible(true);
                            }
                        } catch(Exception ex) {}
                    }          }
        });

        JScrollPane sp = Widgets.scroll(tacheTable);
        sp.getViewport().setBackground(Theme.BG_ROW_ODD);
        v.add(sp, BorderLayout.CENTER);
        return v;
    }

    // ─────────────────────────────────────────────────────────
    // Navigation & Sélection
    // ─────────────────────────────────────────────────────────
    private void navigate(String view) {
        if (view.equals(V_GANTT) || view.equals(V_TABLE)) {
            cardLayout.show(mainContent, "task_module");
            taskCardLayout.show(taskContent, view);
        } else {
            cardLayout.show(mainContent, view);
        }
        mainContent.revalidate();
        mainContent.repaint();
        lblBreadcrumb.setText(switch (view) {
            case V_DASH -> "Accueil";
            case V_GANTT -> (selectedProjet != null ? selectedProjet.getNom() + "  /  " : "") + "Diagramme de Gantt";
            case V_TABLE -> (selectedProjet != null ? selectedProjet.getNom() + "  /  " : "") + "Tâches";
            case V_TEAM -> (selectedProjet != null ? selectedProjet.getNom() + "  /  " : "") + "Équipe";
            case V_JALON -> (selectedProjet != null ? selectedProjet.getNom() + "  /  " : "") + "Jalons";
            default -> view;
        });
        if (view.equals(V_DASH))
            dashboard.refresh();
    }

    private void loadProjets() {
        isRefreshingList = true;
        Projet current = selectedProjet;
        listModel.clear();
        service.getProjets().forEach(listModel::addElement);
        if (current != null) {
            for (int i = 0; i < listModel.getSize(); i++) {
                if (listModel.get(i).getId() == current.getId()) {
                    projetList.setSelectedIndex(i);
                    break;
                }
            }
        }
        isRefreshingList = false;
    }

    private Tache findTache(int id) {
        if (selectedProjet == null) return null;
        for (Tache t : selectedProjet.getTaches()) {
            if (t.getId() == id) return t;
        }
        return null;
    }

    private void onSelectProjet(Projet p) {
        selectedProjet = p;
        if (p == null) {
            clearTop();
            return;
        }
        refreshAll();
    }

    private void updateTop(Projet p) {
        int prog = p.getProgressionGlobale();
        topBar.setValue(prog);
        lblPct.setText(prog + "%");
        Color c = prog == 100 ? Theme.GREEN : prog > 50 ? Theme.ACCENT : Theme.ORANGE;
        topBar.setForeground(c);
        lblPct.setForeground(c);
        long done = p.getTaches().stream().filter(t -> t.getStatut() == Tache.Statut.TERMINE).count();
        lblMeta.setText(p.getNom() + "  •  " + done + "/" + p.getTaches().size() + " tâches  •  " + p.getJalons().size()
                + " jalons  •  " + p.getMembres().size() + " membres   ");
        lblBreadcrumb.setText(p.getNom());
    }

    private void clearTop() {
        topBar.setValue(0);
        lblPct.setText("0%");
        lblMeta.setText("");
        lblBreadcrumb.setText("Accueil");
    }

    private void refreshTable() {
        if (selectedProjet == null || tableModel == null) return;
        tableModel.setRowCount(0);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        java.util.Map<Integer, Long> marges = ProjetService.calculerMarges(selectedProjet);

        for (Tache t : selectedProjet.getTaches()) {
            if (!searchQueryTaches.isEmpty() && !t.getNom().toLowerCase().contains(searchQueryTaches)) continue;
            
            // Prédécesseurs
            String pre = "";
            java.util.List<Integer> deps = t.getDependances();
            if (!deps.isEmpty()) {
                java.util.List<String> names = new java.util.ArrayList<>();
                for (Integer id : deps) {
                    Tache other = findTache(id);
                    if (other != null) names.add(other.getNom());
                }
                pre = String.join(", ", names);
            }

            // Successeurs
            String succ = "";
            java.util.List<String> succNames = new java.util.ArrayList<>();
            for (Tache other : selectedProjet.getTaches()) {
                if (other.getDependances().contains(t.getId())) {
                    succNames.add(other.getNom());
                }
            }
            if (!succNames.isEmpty()) {
                succ = String.join(", ", succNames);
            }

            long margeVal = marges.getOrDefault(t.getId(), 0L);

            tableModel.addRow(new Object[] { 
                t.getId(), 
                t.getNom(), 
                t.getDateDebut().format(fmt), 
                t.getDateFin().format(fmt), 
                (int)t.getDureeJours(),
                (int)margeVal,
                pre,
                succ,
                t.getProgression(),
                t.getStatut(), 
                t.getResponsable() != null ? t.getResponsable() : "-"
            });
        }
    }

    private void refreshAll() {
        if (selectedProjet != null) {
            java.util.Set<Integer> cp = ProjetService.calculerCheminCritique(selectedProjet);
            gantt.setCriticalPath(cp);
            detail.setCriticalPath(cp);
            gantt.setSearchQuery(searchQueryTaches);
            gantt.setProjet(selectedProjet);
            equipe.setProjet(selectedProjet);
            jalons.setProjet(selectedProjet);
            dashboard.setProjet(selectedProjet);
            refreshTable();
            updateTop(selectedProjet);
            loadProjets();
            dashboard.refresh();
        }
    }

    // ─────────────────────────────────────────────────────────
    // Actions
    // ─────────────────────────────────────────────────────────
    public void newProjet() {
        NouveauProjetDialog dlg = new NouveauProjetDialog(this);
        dlg.setVisible(true);
        Projet r = dlg.getResult();
        if (r != null) {
            service.ajouterProjet(r);
            loadProjets();
            rootLayout.show(rootPanel, ROOT_WORKSPACE);
            projetList.setSelectedIndex(listModel.getSize() - 1);
            Toast.ok("Projet « " + r.getNom() + " » créé");
            navigate(V_GANTT);
        }
    }

    private void newTache() {
        if (selectedProjet == null) {
            Toast.warn("Sélectionnez un projet d'abord");
            return;
        }
        NouveauTacheDialog dlg = new NouveauTacheDialog(this, selectedProjet.getTaches(), selectedProjet.getMembres());
        dlg.setVisible(true);
        Tache r = dlg.getResult();
        if (r != null) {
            selectedProjet.ajouterTache(r);
            refreshAll();
            Toast.ok("Tâche « " + r.getNom() + " » ajoutée");
        }
    }

    private void deleteProjet() {
        if (selectedProjet == null)
            return;
        int ok = JOptionPane.showConfirmDialog(this,
                "Supprimer « " + selectedProjet.getNom() + " » ?\nCette action est irréversible.", "Confirmer",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) {
            String nom = selectedProjet.getNom();
            service.supprimerProjet(selectedProjet.getId());
            selectedProjet = null;
            loadProjets();
            if (!service.getProjets().isEmpty())
                projetList.setSelectedIndex(0);
            else {
                clearTop();
                navigate(V_DASH);
            }
            Toast.info("Projet « " + nom + " » supprimé");
        }
    }

    private void deleteTache() {
        if (selectedProjet == null)
            return;
        int r = tacheTable.getSelectedRow();
        if (r < 0 || r >= selectedProjet.getTaches().size()) {
            Toast.warn("Sélectionnez une tâche dans le tableau");
            return;
        }
        Tache t = selectedProjet.getTaches().get(r);
        int ok = JOptionPane.showConfirmDialog(this, "Supprimer la tâche « " + t.getNom() + " » ?", "Confirmer",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) {
            String nm = t.getNom();
            selectedProjet.supprimerTache(t.getId());
            refreshAll();
            Toast.info("Tâche « " + nm + " » supprimée");
        }
    }

    // ─────────────────────────────────────────────────────────
    // Composants internes
    // ─────────────────────────────────────────────────────────
    private class NavBtn extends JPanel {
        private final FlatSVGIcon icon;
        private final String text, view;
        private boolean active = false;
        private boolean collapsed = false;
        private final JLabel lblText;

        public NavBtn(FlatSVGIcon icon, String text, String view) {
            this.icon = icon;
            this.text = text;
            this.view = view;
            
            setLayout(new BorderLayout(12, 0));
            setBackground(Theme.BG_SIDEBAR);
            setBorder(new EmptyBorder(8, 12, 8, 12));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            icon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> active ? Theme.CYAN : Theme.TEXT_MUTED));
            JLabel lblIcon = new JLabel(icon);
            add(lblIcon, BorderLayout.WEST);

            lblText = new JLabel(text);
            lblText.setFont(Theme.F_SMALL);
            lblText.setForeground(Theme.TEXT_MUTED);
            add(lblText, BorderLayout.CENTER);

            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { if (!active) setBackground(Theme.BG_CARD_HOVER); }
                public void mouseExited(MouseEvent e) { if (!active) setBackground(Theme.BG_SIDEBAR); }
                public void mouseClicked(MouseEvent e) { navigate(view); setActive(true); }
            });
        }

        public void setActive(boolean v) {
            this.active = v;
            if (active) {
                if (activeNav != null && activeNav != this)
                    activeNav.setActive(false);
                activeNav = this;
                setBackground(Theme.BG_SELECTED);
                lblText.setForeground(Theme.CYAN);
                icon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Theme.CYAN));
            } else {
                setBackground(Theme.BG_SIDEBAR);
                lblText.setForeground(Theme.TEXT_MUTED);
                icon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Theme.TEXT_MUTED));
            }
            repaint();
        }

        public void setCollapsed(boolean v) {
            this.collapsed = v;
            lblText.setVisible(!v);
            setBorder(new EmptyBorder(8, v ? 18 : 12, 8, v ? 16 : 12));
            setToolTipText(v ? text : null);
            revalidate();
        }
    }

    private class ProjetRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int idx, boolean sel, boolean foc) {
            Projet p = (Projet) value;
            JPanel cell = new JPanel(new BorderLayout(0, 4));
            cell.setOpaque(true);
            cell.setBackground(sel ? Theme.BG_SELECTED : Theme.BG_SIDEBAR);
            cell.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, Theme.BORDER),
                    new EmptyBorder(8, Theme.GAP_MD, 8, Theme.GAP_SM)));

            String name = p.getNom().length() > 20 ? p.getNom().substring(0, 18) + "…" : p.getNom();
            JLabel nm = new JLabel(name);
            nm.setFont(Theme.font(Font.BOLD, 11));
            nm.setForeground(Color.WHITE);
            JLabel info = new JLabel(p.getTaches().size() + " tâches  •  " + p.getMembres().size() + " membres");
            info.setFont(Theme.F_TINY);
            info.setForeground(Theme.TEXT_SECONDARY);

            int prog = p.getProgressionGlobale();
            Color bc = prog == 100 ? Theme.GREEN : prog > 50 ? Theme.ACCENT : Theme.ORANGE;
            Widgets.ProgressBar pb = new Widgets.ProgressBar(prog, bc);
            pb.setShowLabel(false);
            pb.setPreferredSize(new Dimension(0, 14));
            pb.setOpaque(false);

            cell.add(nm, BorderLayout.NORTH);
            cell.add(info, BorderLayout.CENTER);
            cell.add(pb, BorderLayout.SOUTH);
            return cell;
        }
    }
}
