package UI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ReportCycleSettingsPanel extends JPanel {

    private JTextField pastPeriodField;
    private JTextField newPeriodField;
    private JButton saveButton;
    private JButton backButton;
    private PersonalUI parentUI;

    public ReportCycleSettingsPanel(PersonalUI parent) {
        this.parentUI = parent;
        initComponents();
        loadCurrentCycle(); // Load the current cycle when the panel is created
    }

    private void initComponents() {
        setBackground(new Color(245, 245, 245));
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50)); // Add padding

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 10, 15, 10); // Padding
        gbc.anchor = GridBagConstraints.WEST; // Align components to the left

        // Past Period
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel pastLabel = new JLabel("Past period:");
        pastLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        add(pastLabel, gbc);

        gbc.gridx = 1;
        pastPeriodField = new JTextField(5); // Smaller field size
        pastPeriodField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        pastPeriodField.setEditable(false); // Not editable
        pastPeriodField.setBackground(Color.LIGHT_GRAY); // Indicate non-editable
        add(pastPeriodField, gbc);

        gbc.gridx = 2;
        JLabel pastDaysLabel = new JLabel("days");
        pastDaysLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        add(pastDaysLabel, gbc);

        // New Period
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel newLabel = new JLabel("New period:");
        newLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        add(newLabel, gbc);

        gbc.gridx = 1;
        newPeriodField = new JTextField(5);
        newPeriodField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        add(newPeriodField, gbc);

        gbc.gridx = 2;
        JLabel newDaysLabel = new JLabel("days");
        newDaysLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        add(newDaysLabel, gbc);

        // Buttons Panel
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3; // Span across 3 columns
        gbc.anchor = GridBagConstraints.CENTER; // Center the buttons
        gbc.insets = new Insets(30, 10, 10, 10); // More top margin for buttons

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setOpaque(false);

        saveButton = UIUtils.createStyledButton("Save", Color.WHITE, new Color(30, 60, 120), new Dimension(120, 35), new Font("Segoe UI", Font.BOLD, 14));
        backButton = UIUtils.createStyledButton("Back to Home page", new Color(100, 100, 100), Color.LIGHT_GRAY, new Dimension(180, 35), new Font("Segoe UI", Font.PLAIN, 14));

        buttonPanel.add(saveButton);
        buttonPanel.add(backButton);
        add(buttonPanel, gbc);


        // --- Action Listeners ---
        saveButton.addActionListener(e -> saveNewCycle());
        backButton.addActionListener(e -> parentUI.showIndividualCenter());
    }

    private void loadCurrentCycle() {
        // Get the cycle duration from PersonalUI (which holds the session's value)
        pastPeriodField.setText(String.valueOf(parentUI.getReportCycleDays()));
        newPeriodField.setText(""); // Clear the input field
    }

    private void saveNewCycle() {
        String newCycleText = newPeriodField.getText().trim();
        try {
            int newCycleDays = Integer.parseInt(newCycleText);
            if (newCycleDays > 0) {
                parentUI.setReportCycleDays(newCycleDays); // Update the value in PersonalUI for this session
                pastPeriodField.setText(String.valueOf(newCycleDays)); // Update the display
                newPeriodField.setText(""); // Clear input
                JOptionPane.showMessageDialog(this, "Reporting cycle updated to " + newCycleDays + " days for this session.", "Success", JOptionPane.INFORMATION_MESSAGE);
                // No actual file saving happens here as per requirement
            } else {
                JOptionPane.showMessageDialog(this, "Please enter a positive number for the cycle duration.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number for the cycle duration.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Call this method when the panel becomes visible to refresh the 'Past period' display
    @Override
    public void setVisible(boolean aFlag) {
        super.setVisible(aFlag);
        if (aFlag) {
            loadCurrentCycle();
        }
    }
}