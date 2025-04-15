package UI;

import model.AccountModel;
import model.UserRegistrationCSVExporter;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import Admin.AdminAccountQuery;
import Admin.AdminSelfInfo;
import Admin.ModifyCustomerInfoDialog;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class AdminUI extends JDialog {
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private JPanel userListPanel;
    private JTable table;
    private DefaultTableModel model;

    public AdminUI() {
        setTitle("Administrator Account Center");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        contentPanel = new JPanel();
        cardLayout = new CardLayout();
        contentPanel.setLayout(cardLayout);
        contentPanel.setBackground(new Color(30, 60, 120));

        JPanel dashboardPanel = createDashboardPanel();
        userListPanel = new JPanel(new BorderLayout());
        userListPanel.setBackground(new Color(30, 60, 120));

        contentPanel.add(dashboardPanel, "dashboard");
        contentPanel.add(userListPanel, "userList");

        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setPreferredSize(new Dimension(260, 0));
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(new Color(30, 60, 120));

        Dimension btnSize = new Dimension(240, 36);
        sidebarPanel.add(Box.createVerticalStrut(120)); // 顶部留空

        sidebarPanel.add(createSidebarButton("Administrator's Home Page", btnSize, () -> cardLayout.show(contentPanel, "dashboard")));
        sidebarPanel.add(Box.createVerticalStrut(10));
        sidebarPanel.add(createSidebarButton("Administrator Information", btnSize, () -> new AdminSelfInfo()));
        sidebarPanel.add(Box.createVerticalStrut(10));
        sidebarPanel.add(createSidebarButton("Modify Customer Information", btnSize, this::displayAdminVerificationForm));
        sidebarPanel.add(Box.createVerticalStrut(10));
        sidebarPanel.add(createSidebarButton("Customer Information Inquiry", btnSize, () -> new AdminAccountQuery()));
        sidebarPanel.add(Box.createVerticalStrut(10));
        sidebarPanel.add(createSidebarButton("Delete Customer Information", btnSize, this::showUserList));
        sidebarPanel.add(Box.createVerticalStrut(10));
        sidebarPanel.add(createSidebarButton("Import Transaction Records", btnSize, this::importTransactions));

        sidebarPanel.add(Box.createVerticalGlue());
        sidebarPanel.add(Box.createVerticalStrut(10));
        sidebarPanel.add(createSidebarButton("Log out", btnSize, this::dispose));
        sidebarPanel.add(Box.createVerticalStrut(20));

        add(sidebarPanel, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
        setVisible(true);

    }

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(null);
        panel.setBackground(new Color(30, 60, 120));

        JLabel title = new JLabel("Administrator Account Center");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setBounds(60, 30, 400, 40);
        panel.add(title);

        JLabel info = new JLabel("Welcome to the back office management system. Please select the function on the left side.");
        info.setForeground(Color.WHITE);
        info.setBounds(30, 80, 700, 30);
        panel.add(info);

        return panel;
    }

    private JButton createSidebarButton(String text, Dimension size, Runnable action) {
        JButton button = new JButton(text);
        button.setPreferredSize(size);
        button.setMaximumSize(size);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setHorizontalAlignment(SwingConstants.LEFT);  // 左对齐
        button.setIconTextGap(10);
        button.setBackground(new Color(20, 40, 80)); // 深蓝背景
        button.setForeground(Color.WHITE);           // 白色字体
        button.setBorderPainted(false);
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.addActionListener(e -> action.run());
        return button;
    }


    private void showUserList() {
        userListPanel.removeAll();

        model = new DefaultTableModel() {
            public Class<?> getColumnClass(int column) {
                return column == 0 ? Boolean.class : String.class;
            }

            public boolean isCellEditable(int row, int column) {
                return column == 0;
            }
        };

        table = new JTable(model);
        loadUserData();

        JButton deleteButton = new JButton("Delete Selected Users");
        deleteButton.addActionListener(e -> deleteSelectedUsers());

        JButton backButton = new JButton("Return");
        backButton.addActionListener(e -> cardLayout.show(contentPanel, "dashboard"));

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(deleteButton);
        buttonPanel.add(backButton);

        userListPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        userListPanel.add(buttonPanel, BorderLayout.SOUTH);
        cardLayout.show(contentPanel, "userList");
    }

    private void loadUserData() {
        model.setRowCount(0);
        model.setColumnCount(0);
        try (BufferedReader br = new BufferedReader(new FileReader("accounts.csv"))) {
            String line;
            boolean firstLine = true;
            List<String[]> userData = new ArrayList<>();

            while ((line = br.readLine()) != null) {
                String[] user = line.split(",");
                if (firstLine) {
                    model.addColumn("option");
                    for (String columnName : user) {
                        model.addColumn(columnName);
                    }
                    firstLine = false;
                } else {
                    userData.add(user);
                }
            }

            for (String[] user : userData) {
                Object[] rowData = new Object[user.length + 1];
                rowData[0] = false;
                for (int i = 0; i < user.length; i++) {
                    rowData[i + 1] = user[i];
                }
                model.addRow(rowData);
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load user data:" + e.getMessage());
        }
    }

    private void deleteSelectedUsers() {
        List<String[]> allUsers = new ArrayList<>();
        List<String[]> remainingUsers = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader("accounts.csv"))) {
            String line;
            while ((line = br.readLine()) != null) {
                allUsers.add(line.split(","));
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to read data:" + e.getMessage());
            return;
        }

        for (int i = 0; i < model.getRowCount(); i++) {
            boolean selected = (Boolean) model.getValueAt(i, 0);
            String username = (String) model.getValueAt(i, 1);
            if (!selected) {
                for (String[] user : allUsers) {
                    if (user[0].equals(username)) {
                        remainingUsers.add(user);
                        break;
                    }
                }
            }
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter("accounts.csv"))) {
            if (!allUsers.isEmpty()) {
                bw.write(String.join(",", allUsers.get(0)));
                bw.newLine();
            }
            for (String[] user : remainingUsers) {
                bw.write(String.join(",", user));
                bw.newLine();
            }
            JOptionPane.showMessageDialog(this, "Selected users have been successfully deleted");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Write failed:" + e.getMessage());
        }

        SwingUtilities.invokeLater(this::loadUserData);
    }

    private void importTransactions() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            importTransactionsFromCSV(fileChooser.getSelectedFile());
        }
    }

    private void importTransactionsFromCSV(File file) {
        List<String> newData = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields.length == 5) newData.add(line);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to read:" + e.getMessage());
            return;
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter("transactions.csv", true))) {
            for (String line : newData) {
                bw.newLine();
                bw.write(line);
            }
            JOptionPane.showMessageDialog(this, "Successful introduction " + newData.size() + " entries");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Write failed:" + e.getMessage());
        }
    }

    public void displayAdminVerificationForm() {
        JDialog dialog = new JDialog(this, "Administrator Validation", true);
        dialog.setSize(350, 150);
        dialog.setLocationRelativeTo(this);
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(new JLabel("Administrator user name:"));
        JTextField tfUser = new JTextField();
        panel.add(tfUser);
        panel.add(new JLabel("Administrator password."));
        JPasswordField pfPass = new JPasswordField();
        panel.add(pfPass);
        panel.add(new JLabel());
        JButton btn = new JButton("validate");
        panel.add(btn);

        dialog.add(panel);
        btn.addActionListener(e -> {
            String username = tfUser.getText();
            String password = new String(pfPass.getPassword());
            AccountModel account = getAccount(username, password);
            if (account != null) {
                dialog.dispose();
                String customer = JOptionPane.showInputDialog(this, "Please enter the customer's username to be changed.");
                if (customer != null && !customer.trim().isEmpty()) {
                    AccountModel target = getAccount(customer.trim(), "");
                    if (target != null) {
                        ModifyCustomerInfoDialog modifyDialog = new ModifyCustomerInfoDialog();
                        modifyDialog.setAccountInfo(target);
                        modifyDialog.setVisible(true);
                    } else {
                        JOptionPane.showMessageDialog(this, "The user could not be found!");
                    }
                }
            } else {
                JOptionPane.showMessageDialog(dialog, "The username or password is incorrect!");
            }
        });
        dialog.setVisible(true);
    }

    public static AccountModel getAccount(String username, String password) {
        for (AccountModel a : UserRegistrationCSVExporter.readFromCSV()) {
            if (a.getUsername().equals(username)) return a;
        }
        return null;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AdminUI::new);
    }
}
