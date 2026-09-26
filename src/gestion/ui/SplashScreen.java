package gestion.ui;

import gestion.util.Theme;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

/**
 * Splash Screen ultra-robuste : Fusionne tout le design (fond + logo + nom)
 * dans une seule image pour éviter tout scintillement ou superposition erronée.
 */
public class SplashScreen extends JWindow {

    private final JLabel lblStatus;
    private final JProgressBar progressBar;
    private final BufferedImage finalRender;

    public SplashScreen(BufferedImage background) {
        setSize(500, 320);
        setLocationRelativeTo(null);
        setBackground(new Color(0, 0, 0, 0));

        // 1. FUSION DU DESIGN : On crée une image finale qui contient TOUT
        this.finalRender = createFinalRender(background);

        // 2. PANEL UNIQUE : Affiche l'image fusionnée
        JPanel content = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Forme arrondie
                RoundRectangle2D shape = new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), Theme.R_LG,
                        Theme.R_LG);
                g2.setClip(shape);

                if (finalRender != null) {
                    g2.drawImage(finalRender, 0, 0, null);
                } else {
                    g2.setColor(Theme.BG_APP);
                    g2.fill(shape);
                }

                g2.dispose();
            }
        };
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(0, 40, 30, 40));

        // 3. SEULS LES COMPOSANTS MOBILES (Progress/Status) sont ajoutés par-dessus
        JPanel bottom = new JPanel(new BorderLayout(0, 8));
        bottom.setOpaque(false);

        lblStatus = new JLabel("Initialisation...");
        lblStatus.setFont(Theme.F_TINY);
        lblStatus.setForeground(new Color(200, 200, 220));
        lblStatus.setHorizontalAlignment(SwingConstants.CENTER);

        progressBar = new JProgressBar(0, 100);
        progressBar.setPreferredSize(new Dimension(0, 5));
        progressBar.setForeground(Theme.CYAN);
        progressBar.setBackground(new Color(255, 255, 255, 40));
        progressBar.setBorderPainted(false);

        bottom.add(lblStatus, BorderLayout.NORTH);
        bottom.add(progressBar, BorderLayout.SOUTH);

        content.add(bottom, BorderLayout.SOUTH);

        setContentPane(content);
        setShape(new RoundRectangle2D.Double(0, 0, 500, 320, Theme.R_LG, Theme.R_LG));
    }

    /**
     * Fusionne l'image de fond, l'overlay, le logo et le texte en une seule image.
     */
    private BufferedImage createFinalRender(BufferedImage bg) {
        BufferedImage render = new BufferedImage(500, 320, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = render.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // a. Dessiner le fond
        if (bg != null) {
            g.drawImage(bg, 0, 0, 500, 320, null);
            // b. Appliquer l'overlay sombre
            g.setColor(new Color(11, 11, 20, 170));
            g.fillRect(0, 0, 500, 320);
        }

        // c. Dessiner la ligne d'accent
        g.setColor(Theme.CYAN);
        g.fillRect(0, 0, 500, 4);

        // d. Dessiner le LOGO au centre (chargement sécurisé et synchrone)
        try {
            String path = "img/Gemini_Generated_Image_ubpqkiubpqkiubpq.png";
            Image logoImg = null;
            java.io.File file = new java.io.File(path);

            if (file.exists()) {
                logoImg = new ImageIcon(file.getAbsolutePath()).getImage();
            } else {
                java.net.URL url = getClass().getResource("/" + path);
                if (url != null)
                    logoImg = new ImageIcon(url).getImage();
            }

            if (logoImg != null) {
                // Attendre que le logo soit prêt
                MediaTracker mt = new MediaTracker(new Canvas());
                mt.addImage(logoImg, 0);
                mt.waitForID(0);

                // Dessiner le logo redimensionné proprement
                int lw = 90;
                int lh = 90;
                g.drawImage(logoImg, (500 - lw) / 2, 80, lw, lh, null);
            }
        } catch (Exception e) {
            System.err.println("[SplashScreen] Erreur logo: " + e.getMessage());
        }

        // e. Dessiner le NOM au centre (Simple et élégant)
        String text = "Tantagna tetikasa";
        g.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 38));
        g.setColor(Color.WHITE);
        FontMetrics fm = g.getFontMetrics();
        int tx = (500 - fm.stringWidth(text)) / 2;
        g.drawString(text, tx, 215);

        g.dispose();
        return render;
    }

    public void setStatus(String status, int progress) {
        lblStatus.setText(status);
        progressBar.setValue(progress);
        // Force le rafraîchissement des seuls éléments qui bougent
        lblStatus.paintImmediately(lblStatus.getVisibleRect());
        progressBar.paintImmediately(progressBar.getVisibleRect());
    }
}
