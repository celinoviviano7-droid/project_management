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
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MainFrame extends JFrame {

    private static final String V_DASH = "dashboard";
    private static final String V_GANTT = "gantt";
    private static final String V_TABLE = "table";
    private static final String V_TEAM = "equipe";
    private static final String V_JALON = "jalons";

    private final ProjetService service = new ProjetService();
    private Projet selectedProjet;

    // ── Layout central ────────────────────────────────────────
    private CardLayout cardLayout;
    private JPanel mainContent;

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
        buildUI();
        loadProjets();
        if (!service.getProjets().isEmpty())
            projetList.setSelectedIndex(0);
        navigate(V_DASH);
    }

    // ─────────────────────────────────────────────────────────
    // Init & Layout
    // ─────────────────────────────────────────────────────────
    private void initFrame() {
        setTitle("GestionPro — Gestion de Projet Professionnelle");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1440, 880);
        setMinimumSize(new Dimension(1050, 680));
        setLocationRelativeTo(null);
        setIconImages(buildIcons());
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
    }

    private java.util.List<Image> buildIcons() {
        Image base = new ImageIcon(getClass().getResource("/img/Gemini_Generated_Image_ubpqkiubpqkiubpq.png")).getImage();
        java.util.List<Image> icons = new java.util.ArrayList<>();
        for (int size : new int[] { 16, 32, 64, 128, 256, 512 }) {
            BufferedImage scaled = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = scaled.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.drawImage(base, 0, 0, size, size, null);
            g2.dispose();
            icons.add(scaled);
        }
        return icons;
    }

    private void buildUI() {
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(Theme.BG_APP);
        add(buildTopBar(), BorderLayout.NORTH);
        add(buildSidebar(), BorderLayout.WEST);
        add(buildCenter(), BorderLayout.CENTER);
    }

    // ─────────────────────────────────────────────────────────
    // TOPBAR
    // ─────────────────────────────────────────────────────────
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Theme.BG_TOPBAR);
        bar.setBorder(new MatteBorder(0, 0, 1, 0, Theme.BORDER));
        bar.setPreferredSize(new Dimension(0, 52));

        // Gauche : logo + breadcrumb
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setBackground(Theme.BG_TOPBAR);

        JPanel logoZone = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        logoZone.setBackground(new Color(7, 7, 16));
        logoZone.setPreferredSize(new Dimension(218, 52));
        logoZone.setBorder(new MatteBorder(0, 0, 0, 1, Theme.BORDER));

        JLabel logo = new JLabel() {
            private Image logoImg = new ImageIcon(getClass().getResource("/img/Gemini_Generated_Image_ubpqkiubpqkiubpq.png")).getImage();

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.drawImage(logoImg, 0, 0, 40, 40, null);
                g2.setColor(Theme.TEXT_PRIMARY);
                g2.setFont(Theme.font(Font.BOLD, 18));
                g2.drawString("GestionPro", 48, 26);
                g2.dispose();
            }
        };
        logo.setPreferredSize(new Dimension(170, 40));
        logoZone.add(logo);
        left.add(logoZone);

        JPanel bread = new JPanel(new FlowLayout(FlowLayout.LEFT, Theme.GAP_MD, 0));
        bread.setBackground(Theme.BG_TOPBAR);
        lblBreadcrumb = new JLabel("Tableau de bord");
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
        sb.setPreferredSize(new Dimension(218, 0));
        sb.setBorder(new MatteBorder(0, 0, 0, 1, Theme.BORDER));

        // Navigation
        JPanel nav = new JPanel();
        nav.setBackground(Theme.BG_SIDEBAR);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(new EmptyBorder(Theme.GAP_SM, Theme.GAP_SM, Theme.GAP_SM, Theme.GAP_SM));
        nav.add(Box.createVerticalStrut(6));
        nav.add(navSection("NAVIGATION"));

        NavBtn d = new NavBtn("🏠", "Tableau de bord", V_DASH);
        NavBtn g = new NavBtn("📊", "Diagramme de Gantt", V_GANTT);
        NavBtn t = new NavBtn("📋", "Tableau des tâches", V_TABLE);
        NavBtn e = new NavBtn("👥", "Équipe", V_TEAM);
        NavBtn jn = new NavBtn("◆", "Jalons", V_JALON);

        for (NavBtn b : new NavBtn[] { d, g, t, e, jn }) {
            nav.add(b);
            nav.add(Box.createVerticalStrut(2));
        }
        activeNav = d;
        d.setActive(true);

        nav.add(Box.createVerticalStrut(Theme.GAP_MD));
        nav.add(navSection("PROJETS"));
        Widgets.FlatButton btnNew = new Widgets.FlatButton("＋  Nouveau projet", Theme.ACCENT);
        btnNew.setAlignmentX(LEFT_ALIGNMENT);
        btnNew.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        btnNew.addActionListener(e2 -> newProjet());
        nav.add(btnNew);
        nav.add(Box.createVerticalStrut(Theme.GAP_SM));
        lblCount = new JLabel("");
        lblCount.setFont(Theme.F_TINY);
        lblCount.setForeground(Theme.TEXT_MUTED);
        lblCount.setAlignmentX(LEFT_ALIGNMENT);
        nav.add(lblCount);
        nav.add(Box.createVerticalStrut(4));
        sb.add(nav, BorderLayout.NORTH);

        // Liste projets
        listModel = new DefaultListModel<>();
        projetList = new JList<>(listModel);
        projetList.setBackground(Theme.BG_SIDEBAR);
        projetList.setSelectionBackground(Theme.BG_SELECTED);
        projetList.setFixedCellHeight(68);
        projetList.setCellRenderer(new ProjetRenderer());
        projetList.addListSelectionListener(e2 -> {
            if (!e2.getValueIsAdjusting())
                onSelectProjet(projetList.getSelectedValue());
        });

        // Menu contextuel
        JPopupMenu menu = new JPopupMenu();
        menu.setBackground(Theme.BG_CARD);
        menu.setBorder(new LineBorder(Theme.BORDER));
        for (String[] item : new String[][] { { "📊 Voir le Gantt", V_GANTT }, { "👥 Gérer l'équipe", V_TEAM },
                { "◆ Jalons", V_JALON } }) {
            JMenuItem mi = new JMenuItem(item[0]);
            mi.setBackground(Theme.BG_CARD);
            mi.setForeground(Theme.TEXT_PRIMARY);
            mi.setFont(Theme.F_SMALL);
            String view = item[1];
            mi.addActionListener(e2 -> navigate(view));
            menu.add(mi);
        }
        menu.addSeparator();
        JMenuItem miDel = new JMenuItem("🗑  Supprimer le projet");
        miDel.setBackground(Theme.BG_CARD);
        miDel.setForeground(Theme.RED);
        miDel.setFont(Theme.F_SMALL);
        miDel.addActionListener(e2 -> deleteProjet());
        menu.add(miDel);
        projetList.setComponentPopupMenu(menu);

        JScrollPane sp = Widgets.scroll(projetList);
        sp.getViewport().setBackground(Theme.BG_SIDEBAR);
        sb.add(sp, BorderLayout.CENTER);
        sb.add(buildSideFooter(), BorderLayout.SOUTH);
        return sb;
    }

    private JPanel navSection(String title) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setAlignmentX(LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        JLabel l = new JLabel(title);
        l.setFont(Theme.F_LABEL);
        l.setForeground(Theme.TEXT_MUTED);
        l.setBorder(new EmptyBorder(0, 6, 4, 0));
        p.add(l, BorderLayout.WEST);
        return p;
    }

    private JPanel buildSideFooter() {
        JPanel f = new JPanel(new FlowLayout(FlowLayout.LEFT, Theme.GAP_SM, Theme.GAP_SM));
        f.setBackground(new Color(8, 8, 17));
        f.setBorder(new MatteBorder(1, 0, 0, 0, Theme.BORDER));
        Widgets.Avatar av = new Widgets.Avatar("AD", Theme.ACCENT, 32);
        JPanel info = new JPanel(new BorderLayout(0, 1));
        info.setOpaque(false);
        JLabel nm = new JLabel("Administrateur");
        nm.setFont(Theme.font(Font.BOLD, 11));
        nm.setForeground(Theme.TEXT_PRIMARY);
        JLabel rl = new JLabel("Chef de projet");
        rl.setFont(Theme.F_TINY);
        rl.setForeground(Theme.TEXT_MUTED);
        info.add(nm, BorderLayout.NORTH);
        info.add(rl, BorderLayout.SOUTH);
        f.add(av);
        f.add(info);
        return f;
    }

    // ─────────────────────────────────────────────────────────
    // CENTRE
    // ─────────────────────────────────────────────────────────
    private JPanel buildCenter() {
        cardLayout = new CardLayout();
        mainContent = new JPanel(cardLayout);
        mainContent.setBackground(Theme.BG_PANEL);

        dashboard = new DashboardPanel(service);
        dashboard.setOnOpenGantt(() -> navigate(V_GANTT));
        mainContent.add(dashboard, V_DASH);
        mainContent.add(buildGanttView(), V_GANTT);
        mainContent.add(buildTableView(), V_TABLE);

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

        // Toolbar
        JPanel tb = new JPanel(new BorderLayout());
        tb.setBackground(Theme.BG_CARD);
        tb.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, Theme.BORDER),
                new EmptyBorder(Theme.GAP_SM, Theme.GAP_MD, Theme.GAP_SM, Theme.GAP_MD)));
        JPanel tl = new JPanel(new FlowLayout(FlowLayout.LEFT, Theme.GAP_SM, 0));
        tl.setOpaque(false);
        Widgets.FlatButton add = new Widgets.FlatButton("＋  Nouvelle tâche", Theme.ACCENT);
        Widgets.FlatButton del = new Widgets.FlatButton("✕  Supprimer", Theme.RED);
        add.addActionListener(e -> newTache());
        del.addActionListener(e -> deleteTache());
        tl.add(add);
        tl.add(del);
        JPanel tr = new JPanel(new FlowLayout(FlowLayout.RIGHT, Theme.GAP_SM, 0));
        tr.setOpaque(false);
        JLabel hint = new JLabel("💡 Cliquez sur une barre pour éditer à droite");
        hint.setFont(Theme.F_TINY);
        hint.setForeground(Theme.TEXT_MUTED);
        tr.add(hint);
        tb.add(tl, BorderLayout.WEST);
        tb.add(tr, BorderLayout.EAST);

        gantt = new GanttPanel();
        gantt.setListener(t -> detail.setTache(t, selectedProjet));
        JScrollPane gs = Widgets.scroll(gantt);
        gs.getViewport().setBackground(Theme.BG_PANEL);

        detail = new TacheDetailPanel(() -> refreshAll());
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, gs, detail);
        split.setDividerLocation(1080);
        split.setDividerSize(1);
        split.setBackground(Theme.BORDER);
        split.setBorder(null);

        v.add(tb, BorderLayout.NORTH);
        v.add(split, BorderLayout.CENTER);
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
        Widgets.FlatButton add = new Widgets.FlatButton("＋  Nouvelle tâche", Theme.ACCENT);
        Widgets.FlatButton del = new Widgets.FlatButton("✕  Supprimer", Theme.RED);
        add.addActionListener(e -> newTache());
        del.addActionListener(e -> deleteTache());
        tl.add(add);
        tl.add(del);
        tb.add(tl, BorderLayout.WEST);
        v.add(tb, BorderLayout.NORTH);

        String[] cols = { "#", "Tâche", "Responsable", "Début", "Fin", "Durée", "Statut", "Priorité", "Progression" };
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
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

        JTableHeader th = tacheTable.getTableHeader();
        th.setBackground(Theme.BG_TOPBAR);
        th.setForeground(Theme.TEXT_SECONDARY);
        th.setFont(Theme.F_LABEL);
        th.setBorder(new MatteBorder(0, 0, 1, 0, Theme.BORDER));
        th.setReorderingAllowed(false);

        int[] widths = { 40, 200, 130, 90, 90, 65, 115, 90, 110 };
        for (int i = 0; i < widths.length; i++)
            tacheTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Renderer Progression
        tacheTable.getColumnModel().getColumn(8).setCellRenderer((table, val, sel, foc, row, col) -> {
            int pv = val instanceof Integer ? (Integer) val : 0;
            Color pc = pv == 100 ? Theme.GREEN : pv > 50 ? Theme.ACCENT : Theme.ORANGE;
            Widgets.ProgressBar pb = new Widgets.ProgressBar(pv, pc);
            pb.setBackground(sel ? Theme.BG_SELECTED : row % 2 == 0 ? Theme.BG_ROW_ODD : Theme.BG_ROW_EVEN);
            pb.setOpaque(true);
            return pb;
        });

        // Renderer Statut
        tacheTable.getColumnModel().getColumn(6).setCellRenderer((table, val, sel, foc, row, col) -> {
            String sv = val != null ? val.toString() : "";
            Color sc = Theme.TEXT_MUTED;
            for (Tache.Statut s : Tache.Statut.values())
                if (s.toString().replace("_", " ").equals(sv)) {
                    sc = Theme.statutColor(s);
                    break;
                }
            Widgets.Badge b = new Widgets.Badge(sv, sc);
            b.setBackground(sel ? Theme.BG_SELECTED : row % 2 == 0 ? Theme.BG_ROW_ODD : Theme.BG_ROW_EVEN);
            b.setOpaque(true);
            return b;
        });

        tacheTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && selectedProjet != null) {
                int r = tacheTable.getSelectedRow();
                if (r >= 0 && r < selectedProjet.getTaches().size())
                    detail.setTache(selectedProjet.getTaches().get(r), selectedProjet);
            }
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
        cardLayout.show(mainContent, view);
        lblBreadcrumb.setText(switch (view) {
            case V_DASH -> "Tableau de bord";
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
        listModel.clear();
        service.getProjets().forEach(listModel::addElement);
        lblCount.setText(service.getProjets().size() + " projet(s)");
    }

    private void onSelectProjet(Projet p) {
        selectedProjet = p;
        if (p == null) {
            clearTop();
            return;
        }
        gantt.setProjet(p);
        refreshTable();
        updateTop(p);
        equipe.setProjet(p);
        jalons.setProjet(p);
        dashboard.refresh();
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
        lblBreadcrumb.setText("Tableau de bord");
    }

    private void refreshTable() {
        if (selectedProjet == null || tableModel == null)
            return;
        tableModel.setRowCount(0);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (Tache t : selectedProjet.getTaches()) {
            tableModel.addRow(new Object[] { t.getId(), t.getNom(), t.getResponsable(),
                    t.getDateDebut().format(fmt), t.getDateFin().format(fmt), t.getDureeJours() + " j",
                    t.getStatut().toString().replace("_", " "), t.getPriorite().toString(), t.getProgression() });
        }
    }

    private void refreshAll() {
        if (selectedProjet != null) {
            gantt.setProjet(selectedProjet);
            refreshTable();
            updateTop(selectedProjet);
            loadProjets();
            dashboard.refresh();
        }
    }

    // ─────────────────────────────────────────────────────────
    // Actions
    // ─────────────────────────────────────────────────────────
    private void newProjet() {
        NouveauProjetDialog dlg = new NouveauProjetDialog(this);
        dlg.setVisible(true);
        Projet r = dlg.getResult();
        if (r != null) {
            service.ajouterProjet(r);
            loadProjets();
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
        NouveauTacheDialog dlg = new NouveauTacheDialog(this);
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
        private final String view;
        private boolean active;

        NavBtn(String icon, String label, String view) {
            this.view = view;
            setLayout(new FlowLayout(FlowLayout.LEFT, Theme.GAP_SM, 6));
            setOpaque(false);
            setAlignmentX(LEFT_ALIGNMENT);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            JLabel il = new JLabel(icon);
            il.setFont(Theme.font(Font.PLAIN, 13));
            JLabel ll = new JLabel(label);
            ll.setFont(Theme.F_SMALL);
            add(il);
            add(ll);
            setForeground(Theme.TEXT_SECONDARY);
            addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (activeNav != null)
                        activeNav.setActive(false);
                    setActive(true);
                    activeNav = NavBtn.this;
                    navigate(view);
                }

                public void mouseEntered(MouseEvent e) {
                    if (!active) {
                        setOpaque(true);
                        setBackground(Theme.BG_CARD_HOVER);
                        repaint();
                    }
                }

                public void mouseExited(MouseEvent e) {
                    if (!active) {
                        setOpaque(false);
                        repaint();
                    }
                }
            });
        }

        void setActive(boolean a) {
            active = a;
            setOpaque(a);
            setBackground(a ? Theme.BG_SELECTED : Theme.BG_SIDEBAR);
            setBorder(a ? new MatteBorder(0, 3, 0, 0, Theme.ACCENT) : new EmptyBorder(0, 3, 0, 0));
            for (Component c : getComponents()) {
                if (c instanceof JLabel)
                    c.setForeground(a ? Theme.TEXT_PRIMARY : Theme.TEXT_SECONDARY);
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (active || isOpaque()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), Theme.R_SM, Theme.R_SM);
                g2.dispose();
            }
            super.paintComponent(g);
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
            nm.setForeground(sel ? Color.WHITE : Theme.TEXT_PRIMARY);
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
