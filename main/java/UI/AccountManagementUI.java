package UI;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.*;

public class AccountManagementUI extends JFrame {
    private JTextField usernameField, phoneField, emailField, addressField;
    private JPasswordField passwordField;
    private JComboBox<String> genderComboBox, accountTypeComboBox;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private AccountManagementController controller;
    

    public AccountManagementUI(AccountManagementController controller) {
        this.controller = controller;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Smart Finance - Welcome");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Main container with light blue background
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(230, 240, 250)); // #E6F0FA
        add(mainPanel);

        // Sidebar (Left)
        JPanel sidebar = createSidebar();
        mainPanel.add(sidebar, BorderLayout.WEST);

        // Content Panel (Right) with CardLayout
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(new Color(230, 240, 250)); // #E6F0FA
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // Initialize content panels
        initializeContentPanels();

        setVisible(true);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(214, 230, 245)); // #D6E6F5
        sidebar.setPreferredSize(new Dimension(200, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(180, 200, 220)));

        // Sidebar title
        JLabel titleLabel = new JLabel("Smart Finance");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(0, 120, 215)); // #0078D7
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        sidebar.add(titleLabel);

        // Menu options
        String[] options = {"Welcome", "Login", "Register"};
        for (String option : options) {
            JLabel menuItem = createMenuItem(option);
            sidebar.add(menuItem);
            sidebar.add(Box.createRigidArea(new Dimension(0, 10))); // Spacing
        }

        return sidebar;
    }

    private JLabel createMenuItem(String text) {
        JLabel menuItem = new JLabel(text);
        menuItem.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        menuItem.setForeground(new Color(50, 50, 50));
        menuItem.setAlignmentX(Component.CENTER_ALIGNMENT);
        menuItem.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        menuItem.setOpaque(true);
        menuItem.setBackground(new Color(214, 230, 245)); // #D6E6F5

        // Hover and click effects
        menuItem.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                menuItem.setBackground(new Color(200, 220, 240)); // Lighter blue on hover
                menuItem.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                menuItem.setBackground(new Color(214, 230, 245)); // #D6E6F5
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                cardLayout.show(contentPanel, text);
                for (Component comp : menuItem.getParent().getComponents()) {
                    if (comp instanceof JLabel && comp != menuItem) {
                        comp.setBackground(new Color(214, 230, 245)); // #D6E6F5
                    }
                }
                menuItem.setBackground(new Color(180, 200, 220)); // Selected state
            }
        });

        return menuItem;
    }

    private void initializeContentPanels() {
        // Welcome Panel
        JPanel welcomePanel = createWelcomePanel();
        contentPanel.add(welcomePanel, "Welcome");

        // Login Panel
        JPanel loginPanel = createLoginPanel();
        contentPanel.add(loginPanel, "Login");

        // Register Panel
        JPanel registerPanel = createRegisterPanel();
        contentPanel.add(registerPanel, "Register");

        // Show Welcome panel by default
        cardLayout.show(contentPanel, "Welcome");
    }

    private JPanel createWelcomePanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(230, 240, 250)); // #E6F0FA
        panel.setLayout(new BorderLayout());

        // Card-like container
        JPanel card = new JPanel();
        card.setBackground(new Color(230, 240, 250)); // #E6F0FA
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        card.setLayout(null);
        card.setBorder(BorderFactory.createLineBorder(new Color(180, 200, 220), 1, true));

        // Title
        JLabel titleLabel = new JLabel("User Account");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(0, 120, 215)); // #0078D7
        titleLabel.setBounds(80, 20, 200, 30);
        card.add(titleLabel);

        // Placeholder for image
        JLabel imagePlaceholder = new JLabel();
        imagePlaceholder.setBounds(80, 60, 300, 250);
        imagePlaceholder.setBorder(BorderFactory.createLineBorder(new Color(180, 200, 220)));
        ImageIcon rawIcon = new ImageIcon(getClass().getResource("/UI/icons/log_img.png"));
        Image scaled = rawIcon.getImage().getScaledInstance(300, 250, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaled);

        imagePlaceholder.setIcon(scaledIcon);

        card.add(imagePlaceholder);

        // Buttons
        JButton loginButton = new JButton("Log in");
        styleRoundedButton(loginButton);
        loginButton.setBounds(80, 320, 180, 40);
        loginButton.addActionListener(e -> cardLayout.show(contentPanel, "Login"));
        card.add(loginButton);

        JButton registerButton = new JButton("Sign in");
        styleRoundedButton(registerButton);
        registerButton.setBounds(80, 380, 180, 40);
        registerButton.addActionListener(e -> cardLayout.show(contentPanel, "Register"));
        card.add(registerButton);

        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(230, 240, 250)); // #E6F0FA
        panel.setLayout(new BorderLayout());
    
        // Card-like container
        JPanel card = new JPanel();
        card.setBackground(new Color(230, 240, 250)); // #E6F0FA
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        card.setLayout(null);
        card.setBorder(BorderFactory.createLineBorder(new Color(180, 200, 220), 1, true));
    
        // Common Font and Layout variables
        Font font = new Font("Segoe UI", Font.PLAIN, 16);
        int labelX = 30, fieldX = 130, width = 200, height = 30;
        int y = 30, gap = 50;
    
        // Username
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(font);
        usernameLabel.setForeground(new Color(50, 50, 50));
        usernameLabel.setBounds(labelX, y, 100, height);
        card.add(usernameLabel);
        JTextField usernameField = new JTextField(); // Local variable
        usernameField.setFont(font);
        usernameField.setBounds(fieldX, y, width, height);
        card.add(usernameField);
    
        y += gap;
        // Password
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(font);
        passwordLabel.setForeground(new Color(50, 50, 50));
        passwordLabel.setBounds(labelX, y, 100, height);
        card.add(passwordLabel);
        JPasswordField passwordField = new JPasswordField(); // Local variable
        passwordField.setFont(font);
        passwordField.setBounds(fieldX, y, width, height);
        card.add(passwordField);
    
        y += gap;
        // Login Button
        JButton loginButton = new JButton("Log in");
        styleRoundedButton(loginButton);
        loginButton.setBounds(80, y, 200, 35);
        loginButton.addActionListener(e -> controller.handleLogin(usernameField.getText(), new String(passwordField.getPassword()), this));
        card.add(loginButton);
    
        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(230, 240, 250)); // #E6F0FA
        panel.setLayout(new BorderLayout());
    
        // Card-like container
        JPanel card = new JPanel();
        card.setBackground(new Color(230, 240, 250)); // #E6F0FA
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        card.setLayout(null);
        card.setBorder(BorderFactory.createLineBorder(new Color(180, 200, 220), 1, true));
    
        // Common Font and Layout variables
        Font font = new Font("Segoe UI", Font.PLAIN, 16);
        int labelX = 30, fieldX = 130, width = 200, height = 30;
        int y = 20, gap = 40;
    
        // Labels and Fields
        JLabel[] labels = {
                new JLabel("Username:"), new JLabel("Password:"), new JLabel("Phone Number:"),
                new JLabel("Email:"), new JLabel("Gender:"), new JLabel("Address:")
        };
        Component[] fields = {
                new JTextField(), new JPasswordField(),
                new JTextField(), new JTextField(),
                new JComboBox<>(new String[]{"Male", "Female"}),
                new JTextField()
        };
    
        for (int i = 0; i < labels.length; i++) {
            labels[i].setFont(font);
            labels[i].setForeground(new Color(50, 50, 50));
            labels[i].setBounds(labelX, y, 100, height);
            card.add(labels[i]);
            fields[i].setFont(font);
            fields[i].setBounds(fieldX, y, width, height);
            card.add(fields[i]);
            y += gap;
        }
    
        // Account Type Selection
        JLabel accountTypeLabel = new JLabel("Account Type:");
        accountTypeLabel.setFont(font);
        accountTypeLabel.setForeground(new Color(50, 50, 50));
        accountTypeLabel.setBounds(labelX, y, 100, height);
        card.add(accountTypeLabel);
        JComboBox<String> accountTypeComboBox = new JComboBox<>(new String[]{"Personal Account", "Admin Account"});
        accountTypeComboBox.setFont(font);
        accountTypeComboBox.setBounds(fieldX, y, width, height);
        card.add(accountTypeComboBox);
    
        y += gap + 10;
        // Register Button
        JButton createButton = new JButton("Register");
        styleRoundedButton(createButton);
        createButton.setBounds(80, y, 200, 35);
        createButton.addActionListener(e -> {
            String type = accountTypeComboBox.getSelectedItem().toString();
            controller.handleRegister(
                ((JTextField) fields[0]).getText(),
                new String(((JPasswordField) fields[1]).getPassword()),
                ((JTextField) fields[2]).getText(),
                ((JTextField) fields[3]).getText(),
                ((JComboBox<String>) fields[4]).getSelectedItem().toString(),
                ((JTextField) fields[5]).getText(),
                type.equals("Admin Account") ? "Admin" : "personal",
                this
            );
        });
        card.add(createButton);
    
        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    private void styleRoundedButton(JButton button) {
        button.setBackground(new Color(0, 120, 215)); // #0078D7
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        button.setBorder(BorderFactory.createLineBorder(new Color(0, 120, 215)));
        button.setOpaque(true);
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(0, 100, 180)); // Darker blue on hover
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(0, 120, 215)); // #0078D7
            }
        });
    }

    public void showMessage(String message, String title, int messageType) {
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }

    public void switchToLoginPanel() {
        cardLayout.show(contentPanel, "Login");
    }

    public void closeWindow() {
        SwingUtilities.invokeLater(this::dispose);
    }
}