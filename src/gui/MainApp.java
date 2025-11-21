package gui;

import javax.swing.*;

public class MainApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Buat CinemaApp
            CinemaApp app = new CinemaApp();
            app.setVisible(false);

            // Buat LoadingScreen, setelah selesai panggil app
            LoadingScreen loadingScreen = new LoadingScreen(() -> {
                app.setVisible(true);
                // Jika langsung mau ke MovieSelectionPage:
                app.showPage(new LoginPage(app));
            });

            loadingScreen.startLoading();
        });
    }
}
