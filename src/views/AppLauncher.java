package views;

import javax.swing.SwingUtilities;

public class AppLauncher {
  public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginPage::new);
    }
}
