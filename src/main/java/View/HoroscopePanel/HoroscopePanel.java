//welcome.jpg：欢迎视图。 
// error_monster.png：错误占位。 
// electronics_star.png：电子产品星座。 
// foodie_star.png：美食星座。 
// shopper_star.png：购物星座。 
// rent_star.png：租金星座。 
// transport_star.png：交通星座。 
// travel_star.png：旅行星座。 
// entertainment_star.png：娱乐星座。 
// fitness_star.png：健身星座。 
// gift_star.png：礼物星座。 
// health_star.png：健康星座。 
// utilities_star.png：公用事业星座。 
// default_star.png：默认/神秘星座。 缺这几张图，图存放的位置是resources/icons/。










package View.HoroscopePanel;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

public class HoroscopePanel extends JPanel {
    private Controller.HoroscopeController controller;
    private JLabel titleLabel, welcomeImageLabel, reportImageLabel;
    private JTextArea descriptionArea;
    private JButton revealButton, refreshButton, backButton;
    private CardLayout contentCardLayout;
    private JPanel contentCardPanel;
    private static final String WELCOME_CARD = "Welcome";
    private static final String REPORT_CARD = "Report";

    public HoroscopePanel(String username) {
        try {
            this.controller = new Controller.HoroscopeController(username, this);
        } catch (IllegalArgumentException e) {
            this.controller = null; // Explicitly set to null
            setLayout(new BorderLayout());
            add(new JLabel("Error: " + e.getMessage(), SwingConstants.CENTER), BorderLayout.CENTER);
            setEnabled(false); // Disable panel interaction
            return;
        }
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(250, 250, 255));
        setBorder(new EmptyBorder(5, 5, 5, 5));
        initComponents();
        layoutComponents();
        contentCardLayout.show(contentCardPanel, WELCOME_CARD);
        if (controller != null) {
            controller.loadWelcomeImage();
        }
    }

    private void initComponents() {
        titleLabel = new JLabel(" ", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(80, 80, 150));
        titleLabel.setBorder(new EmptyBorder(5, 0, 10, 0));

        welcomeImageLabel = new JLabel();
        welcomeImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        welcomeImageLabel.setVerticalAlignment(SwingConstants.CENTER);

        reportImageLabel = new JLabel();
        reportImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        reportImageLabel.setVerticalAlignment(SwingConstants.CENTER);

        descriptionArea = new JTextArea(" ");
        descriptionArea.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        descriptionArea.setForeground(new Color(60, 60, 60));
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setEditable(false);
        descriptionArea.setOpaque(false);
        descriptionArea.setBorder(new EmptyBorder(5, 5, 5, 5));

        revealButton = new JButton("Reveal My Spending Star!");
        styleButton(revealButton, new Color(0x9C27B0), new Color(0x002FA7));
        revealButton.addActionListener(e -> {
            if (controller != null) {
                controller.onRevealClicked();
            }
        });

        refreshButton = new JButton("Check Stars Again");
        styleButton(refreshButton, new Color(0x007BFF), new Color(0x0056b3));
        refreshButton.addActionListener(e -> {
            if (controller != null) {
                controller.onRefreshClicked();
            }
        });

        backButton = new JButton("Back");
        styleButton(backButton, new Color(0x6c757d), new Color(0x5a6268));
        backButton.addActionListener(e -> {
            if (controller != null) {
                controller.onBackClicked();
            }
        });

        contentCardLayout = new CardLayout();
        contentCardPanel = new JPanel(contentCardLayout);
        contentCardPanel.setOpaque(false);
    }

    private void styleButton(JButton button, Color color1, Color color2) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 15));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(8, 20, 8, 20));
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        Color base1 = color1, base2 = color2, hover1 = color1.brighter(), hover2 = color2.brighter();
        button.setBackground(new Color(0, 0, 0, 0));
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = c.getWidth(), h = c.getHeight();
                GradientPaint gp = new GradientPaint(0, 0, button.getModel().isRollover() ? hover1 : base1, w, h, button.getModel().isRollover() ? hover2 : base2);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, w, h, 18, 18);
                g2.dispose();
                super.paint(g, c);
            }
        });
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.repaint();
            }
        });
    }

    private void layoutComponents() {
        JPanel welcomePanel = new JPanel(new BorderLayout(10, 10));
        welcomePanel.setOpaque(false);

        JPanel imageContainerWelcome = new JPanel(new BorderLayout());
        imageContainerWelcome.setOpaque(false);
        imageContainerWelcome.setBorder(new EmptyBorder(5, 0, 0, 0));
        imageContainerWelcome.add(welcomeImageLabel, BorderLayout.CENTER);

        JPanel controlsPanel = new JPanel();
        controlsPanel.setLayout(new BoxLayout(controlsPanel, BoxLayout.Y_AXIS));
        controlsPanel.setOpaque(false);
        controlsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel welcomeText = new JLabel("Ready to see your Spending Star?", SwingConstants.CENTER);
        welcomeText.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        welcomeText.setForeground(Color.DARK_GRAY);
        welcomeText.setAlignmentX(Component.CENTER_ALIGNMENT);
        welcomeText.setBorder(new EmptyBorder(5, 0, 10, 0));
        revealButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        controlsPanel.add(welcomeText);
        controlsPanel.add(revealButton);

        welcomePanel.add(imageContainerWelcome, BorderLayout.CENTER);
        welcomePanel.add(controlsPanel, BorderLayout.SOUTH);

        JPanel reportPanel = new JPanel(new GridBagLayout());
        reportPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.PAGE_START;
        gbc.insets = new Insets(5, 10, 0, 10);
        reportPanel.add(titleLabel, gbc);

        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(5, 5, 5, 5);
        reportPanel.add(reportImageLabel, gbc);

        JScrollPane descriptionScrollPane = new JScrollPane(descriptionArea);
        descriptionScrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 1, 0, Color.LIGHT_GRAY),
                new EmptyBorder(5, 5, 5, 5)));
        descriptionScrollPane.getViewport().setOpaque(false);
        descriptionScrollPane.setOpaque(false);
        descriptionScrollPane.setPreferredSize(new Dimension(100, 80));
        gbc.gridy = 2;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.PAGE_END;
        gbc.insets = new Insets(0, 10, 5, 10);
        reportPanel.add(descriptionScrollPane, gbc);

        JPanel reportButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        reportButtonPanel.setOpaque(false);
        reportButtonPanel.add(refreshButton);
        reportButtonPanel.add(backButton);
        gbc.gridy = 3;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.PAGE_END;
        gbc.insets = new Insets(5, 10, 5, 10);
        reportPanel.add(reportButtonPanel, gbc);

        contentCardPanel.add(welcomePanel, WELCOME_CARD);
        contentCardPanel.add(reportPanel, REPORT_CARD);
        add(contentCardPanel, BorderLayout.CENTER);
    }

    public void setWelcomeImage(ImageIcon icon) {
        welcomeImageLabel.setIcon(icon);
        welcomeImageLabel.setText(icon == null ? "<html><center style='color:red; padding:20px;'>Welcome Image Missing</center></html>" : null);
        welcomeImageLabel.revalidate();
        welcomeImageLabel.repaint();
    }

    public void setReportData(String title, String description, ImageIcon icon) {
        titleLabel.setText(title);
        descriptionArea.setText(description);
        descriptionArea.setCaretPosition(0);
        reportImageLabel.setIcon(icon);
        reportImageLabel.setText(icon == null ? "<html><center style='color:red; padding:20px;'>Report Image Missing</center></html>" : null);
        contentCardLayout.show(contentCardPanel, REPORT_CARD);
        revalidate();
        repaint();
    }

    public void setLoadingState(boolean isLoading) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> setLoadingState(isLoading));
            return;
        }
        revealButton.setEnabled(!isLoading);
        refreshButton.setEnabled(!isLoading);
        backButton.setEnabled(!isLoading);
        revealButton.setText(isLoading ? "Consulting Stars..." : "Reveal My Spending Star!");
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void showWelcomeCard() {
        contentCardLayout.show(contentCardPanel, WELCOME_CARD);
    }
}