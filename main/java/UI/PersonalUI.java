package UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class PersonalUI extends JFrame {
    private JPanel contentPanel;
    private CardLayout cardLayout;

    public PersonalUI() {
        // Set up the JFrame
        setTitle("Smart Finance - Personal Dashboard");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Main container with white background
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        add(mainPanel);

        // Sidebar (Left)
        JPanel sidebar = createSidebar();
        mainPanel.add(sidebar, BorderLayout.WEST);

        // Content Panel (Right) with CardLayout
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(Color.WHITE);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // Initialize content panels for each option
        initializeContentPanels();

        // Set visible
        setVisible(true);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(245, 245, 245)); // Light gray for contrast
        sidebar.setPreferredSize(new Dimension(200, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(200, 200, 200)));

        // Sidebar title
        JLabel titleLabel = new JLabel("Smart Finance");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(0, 120, 215));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        sidebar.add(titleLabel);

        // Menu options
        String[] options = {"Account Management", "Transaction System", "Bill Statistics", "Personal Center", "Budget Management","AI Assistant"};
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
        menuItem.setBackground(new Color(245, 245, 245));

        // Hover and click effects
        menuItem.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                menuItem.setBackground(new Color(230, 230, 230));
                menuItem.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                menuItem.setBackground(new Color(245, 245, 245));
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                // Switch to the corresponding content panel
                cardLayout.show(contentPanel, text);
                // Highlight selected item
                for (Component comp : menuItem.getParent().getComponents()) {
                    if (comp instanceof JLabel && comp != menuItem) {
                        comp.setBackground(new Color(245, 245, 245));
                    }
                }
                menuItem.setBackground(new Color(200, 200, 200));
            }
        });

        return menuItem;
    }

    private void initializeContentPanels() {
        String[] panelNames = {"Account Management", "Transaction System", "Bill Statistics", "Personal Center", "Budget Management", "AI Assistant"};
        for (String name : panelNames) {
            JPanel panel;
            if (name.equals("AI Assistant")) {
                panel = new AIPanel();
            } else {
                panel = new JPanel();
                panel.setBackground(Color.WHITE);
                panel.setLayout(new BorderLayout());

                JPanel card = new JPanel();
                card.setBackground(Color.WHITE);
                card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                card.setLayout(new BorderLayout());
                card.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true));

                JLabel contentLabel = new JLabel("Welcome to " + name, SwingConstants.CENTER);
                contentLabel.setFont(new Font("Segoe UI", Font.PLAIN, 24));
                contentLabel.setForeground(new Color(50, 50, 50));
                card.add(contentLabel, BorderLayout.CENTER);

                panel.add(card, BorderLayout.CENTER);
            }

            contentPanel.add(panel, name);
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(PersonalUI::new);
    }
}