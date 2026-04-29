package gestion.ui;

import gestion.model.*;
import gestion.service.ProjetService;
import gestion.util.*;

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
    private Runnable onOpenGantt;

    public DashboardPanel(ProjetService service) {
        this.service = service;
        setBackground(Theme.BG_PANEL);
        setLayout(new BorderLayout());
    }

    public void setOnOpenGantt(Runnable r){ onOpenGantt=r; }

    public void refresh(){
        removeAll();
        JPanel body=new JPanel();
        body.setBackground(Theme.BG_PANEL);
        body.setLayout(new BoxLayout(body,BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(Theme.GAP_LG,Theme.GAP_XL,Theme.GAP_LG,Theme.GAP_XL));

        // En-tête
        body.add(pageHeader());
        body.add(Box.createVerticalStrut(Theme.GAP_LG));

        // KPI cards
        body.add(kpiRow());
        body.add(Box.createVerticalStrut(Theme.GAP_LG));

        // Projets + Colonne droite
        JPanel row=new JPanel(new GridLayout(1,2,Theme.GAP_MD,0));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE,340));
        row.add(projetsList());
        row.add(rightCol());
        body.add(row);

        JScrollPane sp=Widgets.scroll(body);
        sp.getViewport().setBackground(Theme.BG_PANEL);
        add(sp,BorderLayout.CENTER);
        revalidate(); repaint();
    }

    private JPanel pageHeader(){
        JPanel h=new JPanel(new BorderLayout(0,4));
        h.setOpaque(false); h.setAlignmentX(LEFT_ALIGNMENT);
        h.setMaximumSize(new Dimension(Integer.MAX_VALUE,52));
        JLabel title=new JLabel("Tableau de bord");
        title.setFont(Theme.font(Font.BOLD,20)); title.setForeground(Theme.TEXT_PRIMARY);
        JLabel sub=new JLabel("Vue d'ensemble de tous vos projets — "+LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy")));
        sub.setFont(Theme.F_SMALL); sub.setForeground(Theme.TEXT_SECONDARY);
        h.add(title,BorderLayout.NORTH); h.add(sub,BorderLayout.SOUTH);
        return h;
    }

    private JPanel kpiRow(){
        List<Projet> ps=service.getProjets();
        int nTaches=ps.stream().mapToInt(p->p.getTaches().size()).sum();
        long done=ps.stream().flatMap(p->p.getTaches().stream()).filter(t->t.getStatut()==Tache.Statut.TERMINE).count();
        long actifs=ps.stream().flatMap(p->p.getTaches().stream()).filter(t->t.getStatut()==Tache.Statut.EN_COURS).count();
        long membres=ps.stream().flatMap(p->p.getMembres().stream()).map(Membre::getId).distinct().count();

        JPanel row=new JPanel(new GridLayout(1,4,Theme.GAP_MD,0));
        row.setOpaque(false); row.setAlignmentX(LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE,105));
        long jalons = ps.stream().mapToLong(p->p.getJalons().size()).sum();
        long jAtteints = ps.stream().flatMap(p->p.getJalons().stream())
            .filter(j->j.getStatut()==gestion.model.Jalon.Statut.ATTEINT).count();

        row.add(kpi("📁  Projets",      ""+ps.size(),  null,           Theme.ACCENT));
        row.add(kpi("📋  Tâches",       ""+nTaches,    null,           Theme.PURPLE));
        row.add(kpi("◆  Jalons",        ""+jalons,     jAtteints>0?"  ✓"+jAtteints:null, Theme.GOLD));
        row.add(kpi("👥  Membres",       ""+membres,    null,           Theme.CYAN));
        return row;
    }

    private JPanel kpi(String label, String val, String sub, Color color){
        Widgets.RoundPanel c=new Widgets.RoundPanel(Theme.R_MD,Theme.BORDER);
        c.setBackground(Theme.BG_CARD);
        c.setLayout(new BorderLayout(0,6));
        c.setBorder(new EmptyBorder(Theme.GAP_MD,Theme.GAP_MD,Theme.GAP_MD,Theme.GAP_MD));

        // Bande colorée top
        JPanel band=new JPanel(){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(color.getRed(),color.getGreen(),color.getBlue(),70));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),3,3); g2.dispose();
            }
        };
        band.setPreferredSize(new Dimension(0,3)); band.setOpaque(false);

        JLabel lbl=new JLabel(label); lbl.setFont(Theme.F_TINY); lbl.setForeground(Theme.TEXT_SECONDARY);

        JPanel vrow=new JPanel(new FlowLayout(FlowLayout.LEFT,3,0)); vrow.setOpaque(false);
        JLabel v=new JLabel(val); v.setFont(Theme.font(Font.BOLD,26)); v.setForeground(color);
        vrow.add(v);
        if(sub!=null){ JLabel s=new JLabel(sub); s.setFont(Theme.font(Font.PLAIN,13)); s.setForeground(Theme.TEXT_MUTED); vrow.add(s); }

        c.add(band,BorderLayout.NORTH);
        c.add(lbl,BorderLayout.CENTER);
        c.add(vrow,BorderLayout.SOUTH);
        return c;
    }

    private JPanel projetsList(){
        Widgets.RoundPanel card=new Widgets.RoundPanel(Theme.R_MD,Theme.BORDER);
        card.setBackground(Theme.BG_CARD);
        card.setLayout(new BorderLayout());

        JPanel hdr=new JPanel(new BorderLayout());
        hdr.setOpaque(false);
        hdr.setBorder(new EmptyBorder(Theme.GAP_MD,Theme.GAP_MD,Theme.GAP_SM,Theme.GAP_MD));
        JLabel t=new JLabel("Projets en cours");
        t.setFont(Theme.F_SUBTITLE); t.setForeground(Theme.TEXT_PRIMARY);
        hdr.add(t,BorderLayout.WEST);
        card.add(hdr,BorderLayout.NORTH);

        JPanel list=new JPanel();
        list.setLayout(new BoxLayout(list,BoxLayout.Y_AXIS));
        list.setOpaque(false);
        list.setBorder(new EmptyBorder(0,Theme.GAP_SM,Theme.GAP_SM,Theme.GAP_SM));

        List<Projet> ps=service.getProjets();
        if(ps.isEmpty()){
            JLabel e=new JLabel("Aucun projet — créez votre premier projet !");
            e.setForeground(Theme.TEXT_MUTED); e.setFont(Theme.F_SMALL);
            e.setBorder(new EmptyBorder(Theme.GAP_LG,Theme.GAP_SM,0,0));
            list.add(e);
        } else { for(Projet p:ps) list.add(projetRow(p)); }

        JScrollPane sp=Widgets.scroll(list);
        sp.getViewport().setBackground(Theme.BG_CARD);
        card.add(sp,BorderLayout.CENTER);
        return card;
    }

    private JPanel projetRow(Projet p){
        JPanel row=new JPanel(new BorderLayout(Theme.GAP_SM,0));
        row.setOpaque(false);
        row.setBorder(new CompoundBorder(
            new MatteBorder(0,0,1,0,Theme.BORDER),
            new EmptyBorder(10,Theme.GAP_SM,10,Theme.GAP_SM)));
        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Point statut
        Color dc=p.getProgressionGlobale()==100?Theme.GREEN:p.getProgressionGlobale()>50?Theme.ACCENT:Theme.ORANGE;
        JPanel dot=new JPanel(){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(dc); g2.fillOval(3,(getHeight()-8)/2,8,8); g2.dispose();
            }
        };
        dot.setPreferredSize(new Dimension(16,20)); dot.setOpaque(false);

        JPanel info=new JPanel(new BorderLayout(0,2)); info.setOpaque(false);
        JLabel nom=new JLabel(p.getNom()); nom.setFont(Theme.F_SUBTITLE); nom.setForeground(Theme.TEXT_PRIMARY);
        JLabel sub=new JLabel(p.getTaches().size()+" tâches  •  "+p.getMembres().size()+" membres  •  "+p.getResponsable());
        sub.setFont(Theme.F_TINY); sub.setForeground(Theme.TEXT_SECONDARY);
        info.add(nom,BorderLayout.NORTH); info.add(sub,BorderLayout.SOUTH);

        JPanel right=new JPanel(new BorderLayout(0,3)); right.setOpaque(false); right.setPreferredSize(new Dimension(90,40));
        JLabel pct=new JLabel(p.getProgressionGlobale()+"%"); pct.setFont(Theme.F_LABEL);
        pct.setForeground(Theme.TEXT_SECONDARY); pct.setHorizontalAlignment(SwingConstants.RIGHT);
        Widgets.ProgressBar pb=new Widgets.ProgressBar(p.getProgressionGlobale(),dc); pb.setShowLabel(false);
        right.add(pct,BorderLayout.NORTH); right.add(pb,BorderLayout.SOUTH);

        row.add(dot,BorderLayout.WEST); row.add(info,BorderLayout.CENTER); row.add(right,BorderLayout.EAST);

        row.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e){ if(onOpenGantt!=null) onOpenGantt.run(); }
            public void mouseEntered(MouseEvent e){ row.setOpaque(true); row.setBackground(Theme.BG_CARD_HOVER); }
            public void mouseExited(MouseEvent e){ row.setOpaque(false); row.repaint(); }
        });
        return row;
    }

    private JPanel rightCol(){
        JPanel col=new JPanel(); col.setLayout(new BoxLayout(col,BoxLayout.Y_AXIS)); col.setOpaque(false);
        col.add(donutCard());
        col.add(Box.createVerticalStrut(Theme.GAP_MD));
        col.add(urgentCard());
        return col;
    }

    private JPanel donutCard(){
        Widgets.RoundPanel c=new Widgets.RoundPanel(Theme.R_MD,Theme.BORDER);
        c.setBackground(Theme.BG_CARD); c.setLayout(new BorderLayout());
        c.setAlignmentX(LEFT_ALIGNMENT); c.setMaximumSize(new Dimension(Integer.MAX_VALUE,180));
        JPanel hdr=new JPanel(new BorderLayout()); hdr.setOpaque(false);
        hdr.setBorder(new EmptyBorder(Theme.GAP_MD,Theme.GAP_MD,0,Theme.GAP_MD));
        JLabel t=new JLabel("Progression globale"); t.setFont(Theme.F_SUBTITLE); t.setForeground(Theme.TEXT_PRIMARY);
        hdr.add(t,BorderLayout.WEST); c.add(hdr,BorderLayout.NORTH);

        int prog=service.getProjets().isEmpty()?0:(int)service.getProjets().stream().mapToInt(Projet::getProgressionGlobale).average().orElse(0);
        Color barC=prog==100?Theme.GREEN:prog>50?Theme.ACCENT:Theme.ORANGE;

        JPanel donut=new JPanel(){
            @Override protected void paintComponent(Graphics g){
                super.paintComponent(g); Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                int cx=getWidth()/2,cy=getHeight()/2,r=Math.min(cx,cy)-18;
                g2.setColor(new Color(40,40,68)); g2.setStroke(new BasicStroke(13,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND)); g2.drawOval(cx-r,cy-r,r*2,r*2);
                if(prog>0){ g2.setColor(barC); g2.drawArc(cx-r,cy-r,r*2,r*2,90,-(int)(3.6*prog)); }
                g2.setFont(Theme.font(Font.BOLD,22)); g2.setColor(Theme.TEXT_PRIMARY);
                String sv=prog+"%"; FontMetrics fm=g2.getFontMetrics();
                g2.drawString(sv,cx-fm.stringWidth(sv)/2,cy+fm.getAscent()/2-2);
                g2.setFont(Theme.F_TINY); g2.setColor(Theme.TEXT_SECONDARY);
                String subs="moy. projets"; fm=g2.getFontMetrics(); g2.drawString(subs,cx-fm.stringWidth(subs)/2,cy+15);
                g2.dispose();
            }
        };
        donut.setOpaque(false); donut.setPreferredSize(new Dimension(0,140));
        c.add(donut,BorderLayout.CENTER); return c;
    }

    private JPanel urgentCard(){
        Widgets.RoundPanel c=new Widgets.RoundPanel(Theme.R_MD,Theme.BORDER);
        c.setBackground(Theme.BG_CARD); c.setLayout(new BorderLayout());
        c.setAlignmentX(LEFT_ALIGNMENT);
        JPanel hdr=new JPanel(new BorderLayout()); hdr.setOpaque(false);
        hdr.setBorder(new EmptyBorder(Theme.GAP_MD,Theme.GAP_MD,Theme.GAP_SM,Theme.GAP_MD));
        JLabel t=new JLabel("⚠  Tâches critiques"); t.setFont(Theme.F_SUBTITLE); t.setForeground(Theme.TEXT_PRIMARY);
        hdr.add(t,BorderLayout.WEST); c.add(hdr,BorderLayout.NORTH);

        JPanel list=new JPanel(); list.setLayout(new BoxLayout(list,BoxLayout.Y_AXIS)); list.setOpaque(false);
        list.setBorder(new EmptyBorder(0,Theme.GAP_SM,Theme.GAP_SM,Theme.GAP_SM));

        List<Tache> crits=service.getProjets().stream()
            .flatMap(p->p.getTaches().stream())
            .filter(t2->t2.getPriorite()==Tache.Priorite.CRITIQUE&&t2.getStatut()!=Tache.Statut.TERMINE)
            .limit(5).collect(Collectors.toList());

        if(crits.isEmpty()){
            JLabel ok=new JLabel("✓  Aucune tâche critique en attente");
            ok.setForeground(Theme.GREEN); ok.setFont(Theme.F_SMALL);
            ok.setBorder(new EmptyBorder(Theme.GAP_SM,Theme.GAP_SM,Theme.GAP_SM,0));
            list.add(ok);
        } else {
            for(Tache t2:crits){
                JPanel row=new JPanel(new BorderLayout(6,0)); row.setOpaque(false);
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE,32));
                row.setBorder(new EmptyBorder(4,0,4,0));
                JLabel ln=new JLabel("⚡ "+t2.getNom()); ln.setFont(Theme.F_SMALL); ln.setForeground(Theme.TEXT_PRIMARY);
                Widgets.Badge b=new Widgets.Badge(t2.getStatut().toString().replace("_"," "),Theme.RED);
                row.add(ln,BorderLayout.CENTER); row.add(b,BorderLayout.EAST);
                list.add(row);
            }
        }
        c.add(list,BorderLayout.CENTER); return c;
    }

}
