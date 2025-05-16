package View.Administrator;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import Model.User;
import Model.UserSession;
import View.LoginAndMain.LoginComponents;
import static View.LoginAndMain.LoginComponents.showCustomMessage;

/**
 * Represents the administrator interface for managing user accounts and transactions.
 * Provides a dashboard, account management panel, and sidebar for navigation and actions.
 *
 * @author Group 19
 * @version 1.0
 */
public class AdminView extends JDialog {
    public final CardLayout cardLayout;
    public JPanel contentPanel;
    private final JPanel accountPanel;
    private JTable accountTable;
    private DefaultTableModel tableModel;
    private final JPanel sidebarPanel;
    private Consumer<String> modifyCustomerAction; // 添加成员变量

    /**
     * Constructs an AdminView dialog with specified actions for navigation and account management.
     *
     * @param dashboardAction         action to show the dashboard
     * @param modifyCustomerAction    action to query a customer for modification
     * @param customerInquiryAction   action to query customer information
     * @param deleteUsersAction       action to delete selected users
     * @param importAccountsAction    action to import customer accounts from a file
     * @param logoutAction            action to log out the administrator
     */
    public AdminView(Runnable dashboardAction, Consumer<String> modifyCustomerAction,
                    Consumer<List<User>> customerInquiryAction, Consumer<Set<String>> deleteUsersAction,
                    Consumer<File> importAccountsAction, Runnable logoutAction) {
        this.modifyCustomerAction = modifyCustomerAction; // 初始化成员变量
        setTitle("Administrator Account Center");
        setSize(1280, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        contentPanel = new JPanel();
        cardLayout = new CardLayout();
        contentPanel.setLayout(cardLayout);
        contentPanel.setBackground(new Color(30, 60, 120));

        JPanel dashboardPanel = createAdminInfoPanel();
        accountPanel = new JPanel(new BorderLayout());
        accountPanel.setBackground(new Color(245, 245, 245));
        setupAccountPanel(customerInquiryAction, deleteUsersAction);

        JPanel modifyPanel = createModifyPanel();
        modifyPanel.setBackground(new Color(245, 245, 245));

        contentPanel.add(dashboardPanel, "dashboard");
        contentPanel.add(accountPanel, "account");
        contentPanel.add(modifyPanel, "modify");

        sidebarPanel = new JPanel();
        sidebarPanel.setPreferredSize(new Dimension(260, 0));
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(new Color(30, 60, 120));

        Dimension btnSize = new Dimension(240, 36);
        sidebarPanel.add(Box.createVerticalStrut(120));

        sidebarPanel.add(createSidebarButton("Administrator Dashboard", btnSize, dashboardAction));
        sidebarPanel.add(Box.createVerticalStrut(10));
        sidebarPanel.add(createSidebarButton("Modify Customer Information", btnSize, () -> {
            cardLayout.show(contentPanel, "modify");
        }));
        sidebarPanel.add(Box.createVerticalStrut(10));
        sidebarPanel.add(createSidebarButton("Modify and Delete Customer Information", btnSize, () -> {
            customerInquiryAction.accept(null);
            cardLayout.show(contentPanel, "account");
        }));
        sidebarPanel.add(Box.createVerticalStrut(10));
        sidebarPanel.add(createSidebarButton("Import Customer Accounts", btnSize, () -> importAccounts(importAccountsAction)));
        sidebarPanel.add(Box.createVerticalGlue());
        sidebarPanel.add(Box.createVerticalStrut(10));
        sidebarPanel.add(createSidebarButton("Log Out", btnSize, logoutAction));
        sidebarPanel.add(Box.createVerticalStrut(20));

        add(sidebarPanel, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        cardLayout.show(contentPanel, "dashboard");

        setVisible(true);
    }

    /**
     * Creates a panel displaying the admin's personal information.
     *
     * @return the configured admin info panel
     */
    private JPanel createAdminInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 245, 245));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Administrator Personal Information");
        title.setForeground(new Color(30, 60, 120));
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setBorder(new EmptyBorder(10, 0, 20, 0));
        panel.add(title, BorderLayout.NORTH);

        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBackground(new Color(255, 255, 255));
        infoPanel.setBorder(BorderFactory.createLineBorder(new Color(30, 60, 120), 2));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        String loggedInUsername = UserSession.getCurrentUsername();
        if (loggedInUsername == null || loggedInUsername.isEmpty()) {
            LoginComponents.showCustomMessage(this, "Administrator login information not detected, please log in first", "Error", JOptionPane.ERROR_MESSAGE);
            return panel;
        }

        try {
            String[][] adminInfo = new String[][]{
                {"Username:", "Password:", "Phone:", "Email:", "Gender:", "Address:", "Creation Time:", "Account Status:", "Account Type:", "Balance:"},
                {
                    UserSession.getCurrentAccount().getUsername(),
                    UserSession.getCurrentAccount().getPassword(),
                    UserSession.getCurrentAccount().getPhone(),
                    UserSession.getCurrentAccount().getEmail(),
                    UserSession.getCurrentAccount().getGender(),
                    UserSession.getCurrentAccount().getAddress(),
                    UserSession.getCurrentAccount().getCreationTime(),
                    UserSession.getCurrentAccount().getAccountStatus().toString(),
                    UserSession.getCurrentAccount().getAccountType(),
                    "%.2f".formatted(UserSession.getCurrentAccount().getBalance())
                }
            };

            String[] headers = adminInfo[0];
            String[] values = adminInfo[1];

            for (int i = 0; i < headers.length; i++) {
                JLabel nameLabel = new JLabel(headers[i]);
                nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
                nameLabel.setForeground(new Color(30, 60, 120));
                gbc.gridx = 0;
                gbc.gridy = i;
                gbc.weightx = 0.3;
                infoPanel.add(nameLabel, gbc);

                JLabel valueLabel = new JLabel(values[i]);
                valueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                valueLabel.setForeground(Color.BLACK);
                gbc.gridx = 1;
                gbc.weightx = 0.7;
                infoPanel.add(valueLabel, gbc);
            }
        } catch (Exception e) {
            LoginComponents.showCustomMessage(this, "Failed to load administrator information: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        panel.add(infoPanel, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Sets up the account management panel with a table and buttons for querying and deleting users.
     *
     * @param customerInquiryAction action to query customer information
     * @param deleteUsersAction     action to delete selected users
     */
    private void setupAccountPanel(Consumer<List<User>> customerInquiryAction, Consumer<Set<String>> deleteUsersAction) {
        String[] columnNames = {"Select", "Username", "Phone", "Email", "Gender", "Address", "Creation Time", "Account Status", "Account Type", "Balance"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int column) {
                return column == 0 ? Boolean.class : String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0;
            }
        };

        accountTable = new JTable(tableModel);
        accountTable.setFillsViewportHeight(true);
        accountTable.setBackground(Color.WHITE);
        accountTable.setGridColor(Color.LIGHT_GRAY);
        accountTable.getTableHeader().setBackground(new Color(230, 230, 230));
        accountTable.getTableHeader().setForeground(Color.BLACK);
        accountTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        accountTable.setRowHeight(25);
        accountTable.setSelectionBackground(new Color(180, 210, 255));
        accountTable.setSelectionForeground(Color.BLACK);

        TableColumnModel columnModel = accountTable.getColumnModel();
        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            if ("Creation Time".equals(tableModel.getColumnName(i))) {
                columnModel.getColumn(i).setPreferredWidth(150);
                break;
            }
        }

        JButton queryButton = new JButton("Query Registered Users");
        queryButton.addActionListener(e -> customerInquiryAction.accept(null));

        JButton deleteButton = new JButton("Delete Selected Users");
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setBackground(new Color(220, 53, 69));
        deleteButton.setPreferredSize(new Dimension(180, 30));
        deleteButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        deleteButton.addActionListener(e -> {
            Set<String> usernamesToDelete = new HashSet<>();
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                if ((Boolean) tableModel.getValueAt(i, 0)) {
                    String username = (String) tableModel.getValueAt(i, 1);
                    if (username != null && !username.trim().isEmpty()) {
                        usernamesToDelete.add(username.trim());
                    }
                }
            }
            deleteUsersAction.accept(usernamesToDelete);
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        buttonPanel.setBackground(accountPanel.getBackground());
        buttonPanel.add(queryButton);
        buttonPanel.add(deleteButton);

        JLabel listTitle = new JLabel("Account Management:");
        listTitle.setForeground(new Color(30, 60, 120));
        listTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        listTitle.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        accountPanel.add(listTitle, BorderLayout.NORTH);
        accountPanel.add(new JScrollPane(accountTable), BorderLayout.CENTER);
        accountPanel.add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Creates a styled sidebar button with hover effects and an associated action.
     *
     * @param text   the button text
     * @param size   the preferred size of the button
     * @param action the action to perform when the button is clicked
     * @return the configured JButton
     */
    private JButton createSidebarButton(String text, Dimension size, Runnable action) {
        JButton button = new JButton(text);
        button.setPreferredSize(size);
        button.setMaximumSize(size);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setIconTextGap(10);
        button.setBackground(new Color(50, 80, 140));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);

        Color originalColor = button.getBackground();
        Color hoverColor = new Color(70, 100, 160);

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(hoverColor);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(originalColor);
            }
        });

        button.addActionListener(e -> action.run());
        return button;
    }

    /**
     * Creates a panel for querying and modifying customer information.
     *
     * @return the configured modify panel
     */
    private JPanel createModifyPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Modify Customer Information");
        title.setForeground(new Color(30, 60, 120));
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setBorder(new EmptyBorder(10, 0, 20, 0));
        panel.add(title, BorderLayout.NORTH);

        JPanel queryPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        queryPanel.setBackground(new Color(245, 245, 245));
        JLabel usernameLabel = new JLabel("Enter Username:");
        usernameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JTextField usernameField = new JTextField(20);
        JButton queryButton = new JButton("Query");
        queryButton.setForeground(Color.WHITE);
        queryButton.setBackground(new Color(40, 167, 69));
        queryButton.setPreferredSize(new Dimension(100, 30));
        queryButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        queryButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            if (!username.isEmpty()) {
                modifyCustomerAction.accept(username);
            } else {
                LoginComponents.showCustomMessage(this, "Username cannot be empty.", "Input Error", JOptionPane.WARNING_MESSAGE);
            }
        });
        queryPanel.add(usernameLabel);
        queryPanel.add(usernameField);
        queryPanel.add(queryButton);
        panel.add(queryPanel, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Updates the modify panel with the provided user account information for editing.
     *
     * @param account the user account to display for modification
     * @param saveCallback the callback to handle saving changes
     */
    public void updateModifyPanel(User account, Consumer<User> saveCallback) {
        // 查找 modify 面板
        JPanel modifyPanel = null;
        Component[] components = contentPanel.getComponents();
        for (Component comp : components) {
            // "modify" panel is the one added with the name "modify"
            if (comp instanceof JPanel && comp.isVisible() && "Modify Customer Information".equals(((JPanel) comp).getBorder() instanceof EmptyBorder ? "" : ((JPanel) comp).getBorder().toString())) {
                modifyPanel = (JPanel) comp;
                break;
            }
        }
        // Fallback: get the panel by index (assuming order is dashboard, account, modify)
        if (modifyPanel == null && components.length >= 3) {
            modifyPanel = (JPanel) components[2];
        }
        if (modifyPanel == null) return;

        modifyPanel.removeAll();

        JLabel title = new JLabel("Modify Customer Information");
        title.setForeground(new Color(30, 60, 120));
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setBorder(new EmptyBorder(10, 0, 20, 0));
        modifyPanel.add(title, BorderLayout.NORTH);

        if (account == null) {
            JPanel queryPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
            queryPanel.setBackground(new Color(245, 245, 245));
            JLabel usernameLabel = new JLabel("Enter Username:");
            usernameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            JTextField usernameField = new JTextField(20);
            JButton queryButton = new JButton("Query");
            queryButton.setForeground(Color.WHITE);
            queryButton.setBackground(new Color(40, 167, 69));
            queryButton.setPreferredSize(new Dimension(100, 30));
            queryButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            queryButton.addActionListener(e -> {
                String username = usernameField.getText().trim();
                if (!username.isEmpty()) {
                    modifyCustomerAction.accept(username);
                } else {
                    LoginComponents.showCustomMessage(this, "Username cannot be empty.", "Input Error", JOptionPane.WARNING_MESSAGE);
                }
            });
            queryPanel.add(usernameLabel);
            queryPanel.add(usernameField);
            queryPanel.add(queryButton);
            modifyPanel.add(queryPanel, BorderLayout.CENTER);
            modifyPanel.revalidate();
            modifyPanel.repaint();
            return;
        }

        JPanel editPanel = new JPanel(new GridBagLayout());
        editPanel.setBackground(new Color(245, 245, 245));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        editPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        JTextField tfUsername = new JTextField(account.getUsername(), 20);
        tfUsername.setEditable(false);
        editPanel.add(tfUsername, gbc);

        gbc.gridx = 0; gbc.gridy++;
        editPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        JTextField tfPassword = new JTextField(account.getPassword(), 20);
        editPanel.add(tfPassword, gbc);

        gbc.gridx = 0; gbc.gridy++;
        editPanel.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1;
        JTextField tfPhone = new JTextField(account.getPhone(), 20);
        editPanel.add(tfPhone, gbc);

        gbc.gridx = 0; gbc.gridy++;
        editPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        JTextField tfEmail = new JTextField(account.getEmail(), 20);
        editPanel.add(tfEmail, gbc);

        gbc.gridx = 0; gbc.gridy++;
        editPanel.add(new JLabel("Gender:"), gbc);
        gbc.gridx = 1;
        JComboBox<String> cbGender = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        cbGender.setSelectedItem(account.getGender());
        editPanel.add(cbGender, gbc);

        gbc.gridx = 0; gbc.gridy++;
        editPanel.add(new JLabel("Address:"), gbc);
        gbc.gridx = 1;
        JTextField tfAddress = new JTextField(account.getAddress(), 20);
        editPanel.add(tfAddress, gbc);

        gbc.gridx = 0; gbc.gridy++;
        editPanel.add(new JLabel("Account Status:"), gbc);
        gbc.gridx = 1;
        JComboBox<String> cbAccountStatus = new JComboBox<>(new String[]{"ACTIVE", "FROZEN"});
        cbAccountStatus.setSelectedItem(account.getAccountStatus().toString());
        editPanel.add(cbAccountStatus, gbc);

        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton saveButton = new JButton("Save Changes");
        saveButton.setForeground(Color.WHITE);
        saveButton.setBackground(new Color(40, 167, 69));
        saveButton.setPreferredSize(new Dimension(150, 30));
        saveButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        saveButton.addActionListener(e -> {
            User updatedAccount = new User(
                    tfUsername.getText(),
                    tfPassword.getText(),
                    tfPhone.getText(),
                    tfEmail.getText(),
                    (String) cbGender.getSelectedItem(),
                    tfAddress.getText(),
                    account.getCreationTime(),
                    User.AccountStatus.valueOf((String) cbAccountStatus.getSelectedItem()),
                    account.getAccountType(),
                    account.getBalance()
            );
            saveCallback.accept(updatedAccount);
        });
        editPanel.add(saveButton, gbc);

        modifyPanel.add(editPanel, BorderLayout.CENTER);
        modifyPanel.revalidate();
        modifyPanel.repaint();
    }

    /**
     * Opens a file chooser to import customer accounts and invokes the provided action.
     *
     * @param importAccountsAction the action to perform with the selected file
     */
    private void importAccounts(Consumer<File> importAccountsAction) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select customer accounts file to import (10-column format)");
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (selectedFile != null) {
                importAccountsAction.accept(selectedFile);
            }
        }
    }

    /**
     * Updates the account table with the provided list of user accounts.
     *
     * @param accounts the list of user accounts to display
     */
    public void updateAccountTable(List<User> accounts) {
        tableModel.setRowCount(0);
        if (accounts == null || accounts.isEmpty()) {
            showCustomMessage(this, "No accounts found!", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        for (User account : accounts) {
            tableModel.addRow(new Object[]{
                    false,
                    account.getUsername(),
                    account.getPhone(),
                    account.getEmail(),
                    account.getGender(),
                    account.getAddress(),
                    account.getCreationTime(),
                    account.getAccountStatus().name(),
                    account.getAccountType(),
                    "%.2f".formatted(account.getBalance())
            });
        }
    }

    /**
     * Checks if the user list panel is currently visible.
     *
     * @return true if the account panel is visible, false otherwise
     */
    public boolean isUserListVisible() {
        for (Component comp : contentPanel.getComponents()) {
            if (comp.isVisible() && comp == accountPanel) {
                return true;
            }
        }
        return false;
    }
}