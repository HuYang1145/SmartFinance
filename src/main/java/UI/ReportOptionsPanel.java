package UI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class ReportOptionsPanel extends JPanel {

    private JButton viewReportButton;
    private JButton changeCycleButton;
    private JButton backButton;
    private PersonalUI parentUI; // Reference to the main UI to switch cards

    public ReportOptionsPanel(PersonalUI parent) {
        this.parentUI = parent;
        initComponents();
    }

    private void initComponents() {
        setBackground(new Color(245, 245, 245));
        setLayout(new GridBagLayout()); // Use GridBagLayout for more control

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Padding around components
        gbc.gridwidth = GridBagConstraints.REMAINDER; // Span across the full width
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

        // Title
        JLabel titleLabel = new JLabel("Periodic financial reporting", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(30, 60, 120));
        gbc.weighty = 0.1; // Give some space at the top
        add(titleLabel, gbc);

        // Button Panel
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 20, 0)); // 1 row, 2 columns for the main buttons
        buttonPanel.setOpaque(false); // Make panel transparent

        viewReportButton = UIUtils.createStyledButton("View the report", Color.WHITE, new Color(30, 60, 120), new Dimension(180, 40), new Font("Segoe UI", Font.PLAIN, 14));
        changeCycleButton = UIUtils.createStyledButton("Change reporting cycle", Color.WHITE, new Color(30, 60, 120), new Dimension(180, 40), new Font("Segoe UI", Font.PLAIN, 14));

        buttonPanel.add(viewReportButton);
        buttonPanel.add(changeCycleButton);

        gbc.weighty = 0.8; // Give more space to the button panel
        gbc.fill = GridBagConstraints.NONE; // Don't stretch buttons horizontally
        gbc.anchor = GridBagConstraints.CENTER;
        add(buttonPanel, gbc);

        // Back Button
        backButton = UIUtils.createStyledButton("Back to Home page", new Color(100, 100, 100), Color.LIGHT_GRAY, new Dimension(180, 35), new Font("Segoe UI", Font.PLAIN, 14));
        gbc.weighty = 0.1; // Space at the bottom
        gbc.anchor = GridBagConstraints.PAGE_END; // Anchor to bottom
        add(backButton, gbc);

        // --- Action Listeners ---
        viewReportButton.addActionListener(e -> parentUI.showReportView());
        changeCycleButton.addActionListener(e -> parentUI.showCycleSettings());
        backButton.addActionListener(e -> parentUI.showIndividualCenter());
    }
}