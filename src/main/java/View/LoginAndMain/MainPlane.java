package View.LoginAndMain;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentListener;

import Controller.BillController;
import Controller.MainPanelController;
import Controller.PersonCenterController;
import Model.UserSession;
import View.LoginAndMain.LoginRoundedInputField.*;

public class MainPlane extends JFrame {
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private String username;
    private final List<NavItemPanel> navItems = new ArrayList<>();
    private final PersonCenterController personCenterController;
    private final BillController billController;
    private MainPanelController contentPanelManager;
    private JPanel sidebar;

    public MainPlane(PersonCenterController personCenterController, BillController billController) {
        this.personCenterController = personCenterController;
        this.billController = billController;
        this.username = UserSession.getCurrentUsername();
        if (username == null || username.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "No user logged in. Please log in first.", "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }
        System.out.println("MainPlane initialized with username: " + username);
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Smart Finance - Personal Dashboard");
        setSize(1920, 1080);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        add(mainPanel);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(Color.WHITE);
        System.out.println("Initialized cardLayout=" + cardLayout + ", contentPanel=" + contentPanel);

        contentPanelManager = new MainPanelController(username, contentPanel, personCenterController, billController, cardLayout);
        contentPanelManager.initializeContentPanels();

        sidebar = createSidebar();
        mainPanel.add(sidebar, BorderLayout.WEST);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        contentPanelManager.showPanel("Personal Center");

        for (NavItemPanel item : navItems) {
            if (item.getName().equals("Personal Center")) {
                item.setSelected(true);
            }
        }

        setVisible(true);
    }

    private JPanel createSidebar() {
        JPanel sb = new JPanel();
        sb.setLayout(new BoxLayout(sb, BoxLayout.Y_AXIS));
        sb.setBackground(Color.WHITE);
        sb.setPreferredSize(new Dimension(200, 0));
        System.out.println("Creating sidebar with " + navItems.size() + " items");

        NavItemPanel.GradientLabel logo = new NavItemPanel.GradientLabel("Smart Finance");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        logo.setBorder(new EmptyBorder(20, 0, 10, 0));
        logo.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        sb.add(logo);

        NavItemPanel welcomePanel = new NavItemPanel("Welcome, " + username, navItems, null, null, null);
        welcomePanel.setIconText("U");
        welcomePanel.setCursor(Cursor.getDefaultCursor());
        welcomePanel.setBackground(null);
        navItems.add(welcomePanel);
        sb.add(welcomePanel);
        sb.add(Box.createRigidArea(new Dimension(0, 8)));

        JTextField search = new JTextField();
        styleSearchField(search, "Search...");
        sb.add(search);
        sb.add(Box.createRigidArea(new Dimension(0, 16)));

        String[] options = {
                "Personal Center",
                "Transaction System",
                "Bill Statistics",
                "Budget Management",
                "AI Assistant",
                "Spending Star Whispers"
        };

        for (String opt : options) {
            NavItemPanel item = new NavItemPanel(opt, navItems, cardLayout, contentPanel, contentPanelManager);
            navItems.add(item);
            sb.add(item);
            sb.add(Box.createRigidArea(new Dimension(0, 8)));
            System.out.println("Added NavItem: " + opt + ", bounds: " + item.getBounds());
        }

        sb.add(Box.createVerticalGlue());

        NavItemPanel logout = new NavItemPanel("Logout", navItems, null, null, null);
        logout.setIconText("U");
        logout.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.out.println("Logout button clicked");
                int choice = JOptionPane.showConfirmDialog(
                        MainPlane.this,
                        "Are you sure you want to log out?",
                        "Logout Confirmation",
                        JOptionPane.YES_NO_OPTION
                );
                if (choice == JOptionPane.YES_OPTION) {
                    UserSession.clearSession();
                    dispose();
                }
            }
        });
        navItems.add(logout);
        sb.add(logout);
        sb.add(Box.createRigidArea(new Dimension(0, 20)));

        search.getDocument().addDocumentListener(new DocumentListener() {
            private void update() {
                String text = search.getText().trim().toLowerCase();
                if (text.equals("search...")) {
                    text = "";
                }
                final String searchText = text;
                for (NavItemPanel it : navItems) {
                    if (it.getName().equals("Welcome, " + username) || it.getName().equals("Logout")) {
                        it.setVisible(true);
                    } else {
                        boolean isVisible = it.getName().toLowerCase().contains(searchText);
                        System.out.println("NavItem: " + it.getName() + ", Visible: " + isVisible + ", SearchText: " + searchText);
                        it.setVisible(isVisible);
                    }
                }
                sb.revalidate();
                sb.repaint();
            }

            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                update();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                update();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                update();
            }
        });

        welcomePanel.setSelected(true);

        sb.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.out.println("Sidebar panel clicked at: " + e.getPoint());
            }
        });

        return sb;
    }

    private void styleSearchField(JTextField tf, String placeholder) {
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setBackground(new Color(240, 240, 240));
        tf.setBorder(BorderFactory.createCompoundBorder(
                new RoundBorder(17, new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        tf.setText(placeholder);
        tf.setForeground(Color.GRAY);
        tf.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (tf.getText().equals(placeholder)) {
                    tf.setText("");
                    tf.setForeground(Color.DARK_GRAY);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (tf.getText().isEmpty()) {
                    tf.setText(placeholder);
                    tf.setForeground(Color.GRAY);
                }
            }
        });
    }
}