package gestion;

import gestion.ui.MainFrame;
import gestion.ui.SplashScreen;
import javax.swing.*;
import com.formdev.flatlaf.FlatDarkLaf;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Main {
    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Chargement de l'image dans le thread main
        final BufferedImage splashBg = loadSplashBackground();

        SwingUtilities.invokeLater(() -> {
            try {
                FlatDarkLaf.setup();
            } catch (Throwable t) {
                System.err.println("[Main] Warning: Impossible de charger FlatLaf: " + t.getMessage());
            }

            SplashScreen splash = new SplashScreen(splashBg);
            splash.setVisible(true);

            // Le SwingWorker simule les étapes PUIS crée le MainFrame
            // dans done(), mais on met le splash en "attente active"
            // pendant la création du MainFrame.
            new SwingWorker<Void, int[]>() {
                @Override
                protected Void doInBackground() throws Exception {
                    String[] steps = {
                        "Chargement des modules...",
                        "Initialisation du thème...",
                        "Préparation de l'interface..."
                    };
                    int[] progStart = {0, 30, 65};
                    int[] progEnd   = {30, 65, 90};

                    for (int i = 0; i < steps.length; i++) {
                        // Animation fluide de la barre pour chaque étape
                        int from = progStart[i];
                        int to   = progEnd[i];
                        int fps = 30;
                        int sleepMs = 1000 / fps; // 1 seconde par étape

                        for (int frame = 0; frame <= fps; frame++) {
                            int val = from + (to - from) * frame / fps;
                            publish(new int[]{val, i});
                            Thread.sleep(sleepMs);
                        }
                    }
                    return null;
                }

                @Override
                protected void process(java.util.List<int[]> chunks) {
                    int[] latest = chunks.get(chunks.size() - 1);
                    int progress = latest[0];
                    int step = latest[1];
                    String[] steps = {
                        "Chargement des modules...",
                        "Initialisation du thème...",
                        "Préparation de l'interface..."
                    };
                    splash.setStatus(steps[step], progress);
                }

                @Override
                protected void done() {
                    // Afficher 90% et le message final
                    splash.setStatus("Lancement de l'application...", 90);

                    // Utiliser un Timer pour laisser l'EDT peindre "90%"
                    // PUIS créer le MainFrame dans un second temps.
                    // Pendant la création (lourde) du MainFrame, le splash reste visible.
                    new Timer(100, evt -> {
                        ((Timer) evt.getSource()).stop();

                        // Créer le MainFrame (3-4 secondes, bloque l'EDT)
                        // Le splash est DÉJÀ peint avec "Lancement..." et 90%,
                        // donc l'utilisateur voit quelque chose de stable.
                        MainFrame frame = new MainFrame();

                        // Transition
                        splash.setStatus("Prêt !", 100);
                        
                        // Petit délai pour montrer "Prêt !" avant de fermer
                        new Timer(300, evt2 -> {
                            ((Timer) evt2.getSource()).stop();
                            splash.dispose();
                            frame.setVisible(true);
                        }).start();
                    }).start();
                }
            }.execute();
        });
    }

    private static BufferedImage loadSplashBackground() {
        try {
            Image img = new ImageIcon("img/retro-computer-desk-arrangement.jpg").getImage();
            MediaTracker tracker = new MediaTracker(new Canvas());
            tracker.addImage(img, 0);
            tracker.waitForID(0);

            if (tracker.isErrorID(0)) {
                System.err.println("[Main] Image de fond non trouvée");
                return null;
            }

            BufferedImage result = new BufferedImage(500, 320, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = result.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(img, 0, 0, 500, 320, null);
            g.dispose();
            return result;
        } catch (Exception e) {
            System.err.println("[Main] Erreur fond splash: " + e.getMessage());
            return null;
        }
    }
}
