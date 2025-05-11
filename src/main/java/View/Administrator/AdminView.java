/**
 * Represents the administrator interface for managing user accounts and transactions.
 * Provides a dashboard, account management panel, and sidebar for navigation and actions.
 *
 * @author Group 19
 * @version 1.0
 */
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
import java.awt.GridLayout;
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
import javax.swing.JPasswordField;
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

public class AdminView extends JDialog {
    public final CardLayout cardLayout;
    public JPanel contentPanel;
    private final JPanel accountPanel;
    private JTable accountTable;
    private DefaultTableModel tableModel;
    private final JPanel sidebarPanel;

    /**
     * Constructs an AdminView dialog with specified actions for navigation and account management.
     *
     * @param dashboardAction         action to show the dashboard
     * @param adminInfoAction         action to display admin information
     * @param modifyCustomerAction    action to modify customer information
     * @param customerInquiryAction   action to query customer information
     * @param deleteUsersAction       action to delete selected users
     * @param importAccountsAction    action to import customer accounts from a file
     * @param importTransactionsAction action to import transaction records from a file
     * @param logoutAction            action to log out the administrator
     */
    public AdminView(Runnable dashboardAction, Runnable adminInfoAction, Consumer<String[]> modifyCustomerAction,
                    Consumer<List<User>> customerInquiryAction, Consumer<Set<String>> deleteUsersAction,
                    Consumer<File> importAccountsAction, Consumer<File> importTransactionsAction, Runnable logoutAction) {
        setTitle("Administrator Account Center");
        setSize(1280, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        contentPanel = new JPanel();
        cardLayout = new CardLayout();
        contentPanel.setLayout(cardLayout);
        contentPanel.setBackground(new Color(30, 60, 120));

        JPanel dashboardPanel = createDashboardPanel();
        accountPanel = new JPanel(new BorderLayout());
        accountPanel.setBackground(new Color(245, 245, 245));
        setupAccountPanel(customerInquiryAction, deleteUsersAction);

        contentPanel.add(dashboardPanel, "dashboard");
        contentPanel.add(accountPanel, "account");

        sidebarPanel = new JPanel();
        sidebarPanel.setPreferredSize(new Dimension(260, 0));
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(new Color(30, 60, 120));

        Dimension btnSize = new Dimension(240, 36);
        sidebarPanel.add(Box.createVerticalStrut(120));

        sidebarPanel.add(createSidebarButton("Administrator Dashboard", btnSize, dashboardAction));
        sidebarPanel.add(Box.createVerticalStrut(10));
        sidebarPanel.add(createSidebarButton("Administrator Information", btnSize, adminInfoAction));
        sidebarPanel.add(Box.createVerticalStrut(10));
        sidebarPanel.add(createSidebarButton("Modify Customer Information", btnSize, () -> showAdminVerificationDialog(modifyCustomerAction)));
        sidebarPanel.add(Box.createVerticalStrut(10));
        sidebarPanel.add(createSidebarButton("Customer Information Inquiry", btnSize, () -> {
            customerInquiryAction.accept(null);
            cardLayout.show(contentPanel, "account");
        }));
        sidebarPanel.add(Box.createVerticalStrut(10));
        sidebarPanel.add(createSidebarButton("Delete Customer Information", btnSize, () -> {
            cardLayout.show(contentPanel, "account");
        }));
        sidebarPanel.add(Box.createVerticalStrut(10));
        sidebarPanel.add(createSidebarButton("Import Customer Accounts", btnSize, () -> importAccounts(importAccountsAction)));
        sidebarPanel.add(Box.createVerticalStrut(10));
        sidebarPanel.add(createSidebarButton("Import Transaction Records", btnSize, () -> importTransactions(importTransactionsAction)));
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
     * Creates the dashboard panel with a welcome message.
     *
     * @return the configured dashboard panel
     */
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(null);
        panel.setBackground(new Color(245, 245, 245));

        JLabel title = new JLabel("Administrator Account Center");
        title.setForeground(new Color(30, 60, 120));
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setBounds(40, 40, 500, 40);
        panel.add(title);

        JLabel info = new JLabel("Welcome to the backend management system. Please select a function from the left.");
        info.setForeground(Color.DARK_GRAY);
        info.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        info.setBounds(40, 100, 600, 30);
        panel.add(info);

        return panel;
    }

    /**
     * Sets up the account management panel with a table and buttons for querying and deleting users.
     *
     * @param customerInquiryAction action to query customer information
     * @param deleteUsersAction    action to delete selected users
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
     * Displays a dialog for administrator verification before modifying customer information.
     *
     * @param modifyCustomerAction the action to perform with the provided credentials
     */
    private void showAdminVerificationDialog(Consumer<String[]> modifyCustomerAction) {
        JDialog dialog = new JDialog(this, "Administrator Verification", true);
        dialog.setSize(350, 180);
        dialog.setLocationRelativeTo(this);
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        panel.add(new JLabel("Admin Username:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        JTextField tfUser = new JTextField();
        panel.add(tfUser, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        panel.add(new JLabel("Admin Password:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        JPasswordField pfPass = new JPasswordField();
        panel.add(pfPass, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton btn = new JButton("Verify");
        panel.add(btn, gbc);

        dialog.add(panel);

        btn.addActionListener(e -> {
            String username = tfUser.getText();
            String password = new String(pfPass.getPassword());
            modifyCustomerAction.accept(new String[]{username, password});
            dialog.dispose();
        });

        dialog.setVisible(true);
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
     * Opens a file chooser to import transaction records and invokes the provided action.
     *
     * @param importTransactionsAction the action to perform with the selected file
     */
    private void importTransactions(Consumer<File> importTransactionsAction) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select transaction records file to import (5-column format)");
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (selectedFile != null) {
                importTransactionsAction.accept(selectedFile);
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

    /**
     * Dialog for displaying administrator personal information.
     */
    public class AdminInfoDialog extends JDialog {
        /**
         * Constructs an AdminInfoDialog to display the current administrator's details.
         */
        public AdminInfoDialog() {
            super(AdminView.this, "Administrator Personal Information", true);
            setSize(400, 300);
            setLocationRelativeTo(AdminView.this);
            setLayout(new GridLayout(0, 2));

            String loggedInUsername = UserSession.getCurrentUsername();

            if (loggedInUsername == null || loggedInUsername.isEmpty()) {
                LoginComponents.showCustomMessage(this, "Administrator login information not detected, please log in first", "Error", JOptionPane.ERROR_MESSAGE);
                dispose();
                return;
            }

            try {
                String[][] adminInfo = new String[][]{{"Username:", "Password:", "Phone:", "Email:", "Gender:", "Address:", "Creation Time:", "Account Status:", "Account Type:", "Balance:"},
                        {UserSession.getCurrentAccount().getUsername(), UserSession.getCurrentAccount().getPassword(), UserSession.getCurrentAccount().getPhone(), UserSession.getCurrentAccount().getEmail(), UserSession.getCurrentAccount().getGender(), UserSession.getCurrentAccount().getAddress(), UserSession.getCurrentAccount().getCreationTime(), UserSession.getCurrentAccount().getAccountStatus().toString(), UserSession.getCurrentAccount().getAccountType(), "%.2f".formatted(UserSession.getCurrentAccount().getBalance())}};

                String[] headers = adminInfo[0];
                String[] values = adminInfo[1];

                for (int i = 0; i < headers.length; i++) {
                    JLabel nameLabel = new JLabel(headers[i]);
                    JLabel valueLabel = new JLabel(values[i]);
                    add(nameLabel);
                    add(valueLabel);
                }
            } catch (Exception e) {
                LoginComponents.showCustomMessage(this, "Failed to load administrator information: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                dispose();
                return;
            }

            setVisible(true);
        }
    }

    /**
     * Dialog for modifying customer account information.
     */
    public class ModifyCustomerDialog extends JDialog {
        private JTextField tfUsername, tfPhone, tfEmail, tfAddress;
        private JPasswordField pfPassword;
        private JComboBox<String> cbGender, cbAccountStatus;
        private JPasswordField pfAdminPassword;
        private Consumer<ModifyCustomerDialog> confirmCallback;

        /**
         * Constructs a ModifyCustomerDialog for editing a user's account details.
         *
         * @param account         the user account to modify
         * @param confirmCallback the callback to handle confirmation
         */
        public ModifyCustomerDialog(User account, Consumer<ModifyCustomerDialog> confirmCallback) {
            super(AdminView.this, "Modify Customer Information", true);
            this.confirmCallback = confirmCallback;
            setSize(400, 500);
            setLocationRelativeTo(AdminView.this);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);

            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            gbc.gridx = 0; gbc.gridy = 0;
            panel.add(new JLabel("Username:"), gbc);
            gbc.gridx = 1;
            tfUsername = new JTextField(20);
            tfUsername.setEditable(false);
            panel.add(tfUsername, gbc);

            gbc.gridx = 0; gbc.gridy = 1;
            panel.add(new JLabel("Password:"), gbc);
            gbc.gridx = 1;
            pfPassword = new JPasswordField(20);
            panel.add(pfPassword, gbc);

            gbc.gridx = 0; gbc.gridy = 2;
            panel.add(new JLabel("Phone:"), gbc);
            gbc.gridx = 1;
            tfPhone = new JTextField(20);
            panel.add(tfPhone, gbc);

            gbc.gridx = 0; gbc.gridy = 3;
            panel.add(new JLabel("Email:"), gbc);
            gbc.gridx = 1;
            tfEmail = new JTextField(20);
            panel.add(tfEmail, gbc);

            gbc.gridx = 0; gbc.gridy = 4;
            panel.add(new JLabel("Gender:"), gbc);
            gbc.gridx = 1;
            cbGender = new JComboBox<>(new String[]{"Male", "Female", "Other"});
            panel.add(cbGender, gbc);

            gbc.gridx = 0; gbc.gridy = 5;
            panel.add(new JLabel("Address:"), gbc);
            gbc.gridx = 1;
            tfAddress = new JTextField(20);
            panel.add(tfAddress, gbc);

            gbc.gridx = 0; gbc.gridy = 6;
            panel.add(new JLabel("Account Status:"), gbc);
            gbc.gridx = 1;
            cbAccountStatus = new JComboBox<>(new String[]{"ACTIVE", "FROZEN"});
            panel.add(cbAccountStatus, gbc);

            gbc.gridx = 0; gbc.gridy = 7;
            panel.add(new JLabel("Admin Password:"), gbc);
            gbc.gridx = 1;
            pfAdminPassword = new JPasswordField(20);
            panel.add(pfAdminPassword, gbc);

            gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 2;
            JButton btnConfirm = new JButton("Confirm");
            btnConfirm.addActionListener(e -> confirmCallback.accept(this));
            panel.add(btnConfirm, gbc);

            add(panel);

            setAccountInfo(account);
            setVisible(true);
        }

        /**
         * Gets the username from the dialog.
         *
         * @return the username
         */
        public String getUsername() {
            return tfUsername.getText();
        }

        /**
         * Gets the password from the dialog.
         *
         * @return the password
         */
        public String getPassword() {
            return new String(pfPassword.getPassword());
        }

        /**
         * Gets the phone number from the dialog.
         *
         * @return the phone number
         */
        public String getPhone() {
            return tfPhone.getText();
        }

        /**
         * Gets the email from the dialog.
         *
         * @return the email
         */
        public String getEmail() {
            return tfEmail.getText();
        }

        /**
         * Gets the selected gender from the dialog.
         *
         * @return the gender
         */
        public String getGender() {
            return (String) cbGender.getSelectedItem();
        }

        /**
         * Gets the address from the dialog.
         *
         * @return the address
         */
        public String getAddress() {
            return tfAddress.getText();
        }

        /**
         * Gets the selected account status from the dialog.
         *
         * @return the account status
         */
        public String getAccountStatus() {
            return (String) cbAccountStatus.getSelectedItem();
        }

        /**
         * Gets the admin password from the dialog.
         *
         * @return the admin password
         */
        public String getAdminPassword() {
            return new String(pfAdminPassword.getPassword());
        }

        /**
         * Sets the dialog fields with the provided account information.
         *
         * @param account the user account to display
         */
        public void setAccountInfo(User account) {
            tfUsername.setText(account.getUsername());
            pfPassword.setText(account.getPassword());
            tfPhone.setText(account.getPhone());
            tfEmail.setText(account.getEmail());
            cbGender.setSelectedItem(account.getGender());
            tfAddress.setText(account.getAddress());
            cbAccountStatus.setSelectedItem(account.getAccountStatus().toString());
        }

        /**
         * Closes the dialog.
         */
        public void close() {
            dispose();
        }
    }

    /**
     * Displays the admin information dialog.
     */
    public void showAdminInfoDialog() {
        new AdminInfoDialog();
    }

    /**
     * Displays the modify customer dialog for the specified account.
     *
     * @param account         the user account to modify
     * @param confirmCallback the callback to handle confirmation
     */
    public void showModifyCustomerDialog(User account, Consumer<ModifyCustomerDialog> confirmCallback) {
        new ModifyCustomerDialog(account, confirmCallback);
    }
}