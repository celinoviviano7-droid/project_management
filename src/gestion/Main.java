package gestion;

import gestion.ui.MainFrame;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Antialiasing texte sur toutes les plateformes
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            JWindow splash = new JWindow();
            java.awt.Image img = new ImageIcon(Main.class.getResource("/img/Gemini_Generated_Image_ubpqkiubpqkiubpq.png")).getImage()
                                 .getScaledInstance(450, 450, java.awt.Image.SCALE_SMOOTH);
            JLabel label = new JLabel(new ImageIcon(img));
            splash.getContentPane().add(label);
            splash.pack();
            splash.setLocationRelativeTo(null);
            splash.setBackground(new java.awt.Color(0, 0, 0, 0));
            splash.setVisible(true);

            new SwingWorker<MainFrame, Void>() {
                @Override
                protected MainFrame doInBackground() throws Exception {
                    Thread.sleep(1500);
                    return new MainFrame();
                }

                @Override
                protected void done() {
                    try {
                        MainFrame frame = get();
                        splash.dispose();
                        frame.setVisible(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.execute();
        });
    }
}
