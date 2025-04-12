package views;

import controllers.StaffController;
import models.Staff;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class LoginPage extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private Map<String, String> credentials = new HashMap<>();
    private StaffController staffController = new StaffController();

    public LoginPage() {
      setTitle("Login - School Management System");
      setSize(850, 500);
      setLocationRelativeTo(null);
      setDefaultCloseOperation(EXIT_ON_CLOSE);
      setLayout(new BorderLayout());
  
      loadCredentials();
  
      JPanel background = new JPanel() {
        private Image backgroundImage = new ImageIcon("src/resources/MRCBackground.png").getImage();
    
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
      };
      background.setLayout(new GridBagLayout());
    
  
      JPanel formPanel = new JPanel();
      formPanel.setOpaque(false);
      formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
      formPanel.setPreferredSize(new Dimension(400, 300));

      ImageIcon logoIcon = new ImageIcon("src/resources/MRClogo.png");
      Image scaledLogo = logoIcon.getImage().getScaledInstance(150, 100, Image.SCALE_SMOOTH); 
      JLabel logoLabel = new JLabel(new ImageIcon(scaledLogo));
      logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
      formPanel.add(logoLabel);

  
      JLabel title = new JLabel("SCHOOL MANAGEMENT SYSTEM", SwingConstants.CENTER);
      title.setAlignmentX(Component.CENTER_ALIGNMENT);
      title.setFont(new Font("Arial", Font.BOLD, 22));
      title.setForeground(Color.WHITE);
  
      JLabel subtitle = new JLabel("LOGIN PORTAL", SwingConstants.CENTER);
      subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
      subtitle.setFont(new Font("Arial", Font.ITALIC, 16));
      subtitle.setForeground(Color.WHITE);
      subtitle.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
  
      JPanel fieldsPanel = new JPanel(new GridLayout(2, 2, 10, 10));
      fieldsPanel.setOpaque(false);
      JLabel userLabel = new JLabel("Username:");
      JLabel passLabel = new JLabel("Password:");
      userLabel.setForeground(Color.WHITE);
      passLabel.setForeground(Color.WHITE);
      usernameField = new JTextField();
      passwordField = new JPasswordField();
  
      fieldsPanel.add(userLabel);
      fieldsPanel.add(usernameField);
      fieldsPanel.add(passLabel);
      fieldsPanel.add(passwordField);
  
      JPanel buttonPanel = new JPanel(new FlowLayout());
      buttonPanel.setOpaque(false);
      JButton loginBtn = createButton("LOGIN", this::handleLogin);
      JButton addUserBtn = createButton("ADD USER", this::handleAddUser);
      JButton cancelBtn = createButton("CANCEL", e -> System.exit(0));
      buttonPanel.add(loginBtn);
      buttonPanel.add(addUserBtn);
      buttonPanel.add(cancelBtn);
  
      formPanel.add(title);
      formPanel.add(subtitle);
      formPanel.add(fieldsPanel);
      formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
      formPanel.add(buttonPanel);
  
      background.add(formPanel);
      add(background, BorderLayout.CENTER);
  
      setVisible(true);
  }
  

    private JButton createButton(String text, java.awt.event.ActionListener action) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(100, 30));
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBackground(new Color(0, 120, 215));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.addActionListener(action);
        return button;
    }

    private void handleLogin(ActionEvent e) {
        String id = usernameField.getText().trim().toUpperCase();
        String pass = new String(passwordField.getPassword()).trim();

        if (!credentials.containsKey(id)) {
            JOptionPane.showMessageDialog(this, "User not found or password not set.");
            return;
        }

        if (!credentials.get(id).equals(pass)) {
            JOptionPane.showMessageDialog(this, "Incorrect password.");
            return;
        }

        dispose();
        MainMenu menu = new MainMenu();
        menu.setVisible(true);

    }

    private void handleAddUser(ActionEvent e) {
        String id = usernameField.getText().trim().toUpperCase();
        String pass = new String(passwordField.getPassword()).trim();

        if (id.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both ID and password.");
            return;
        }

        Staff staff = staffController.getStaffByID(id);
        if (staff == null) {
            JOptionPane.showMessageDialog(this, "Staff ID not found in records.");
            return;
        }

        credentials.put(id, pass);
        saveCredentials();
        JOptionPane.showMessageDialog(this, "User added successfully.");
    }

    private void loadCredentials() {
        File file = new File("database/passwords.txt");
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 2);
                if (parts.length == 2) {
                    credentials.put(parts[0].toUpperCase(), parts[1]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveCredentials() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("database/passwords.txt"))) {
            for (Map.Entry<String, String> entry : credentials.entrySet()) {
                writer.println(entry.getKey() + "," + entry.getValue());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginPage::new);
    }
}
