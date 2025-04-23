package View;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import AccountModel.AccountModel;

public class ViewPersonalInfo extends JDialog {

    public ViewPersonalInfo(Dialog owner, AccountModel userAccount) {
        super(owner, "Personal Information", true);
        setSize(400, 300);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        if (userAccount == null) {
            JOptionPane.showMessageDialog(this, "User information not found", "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }

        setLayout(new BorderLayout());
        JPanel infoPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        addFields(infoPanel, userAccount);
        add(infoPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton returnButton = new JButton("Return");
        returnButton.addActionListener(e -> {
            setVisible(false);
            SwingUtilities.invokeLater(this::dispose);
        });
        buttonPanel.add(returnButton);
        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void addFields(JPanel panel, AccountModel userAccount) {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("Username", userAccount.getUsername());
        fields.put("Phone Number", userAccount.getPhone());
        fields.put("Email", userAccount.getEmail());
        fields.put("Gender", userAccount.getGender());
        fields.put("Address", userAccount.getAddress());
        fields.put("Creation Time", userAccount.getCreationTime());
        fields.put("Account Status", userAccount.getAccountStatus().name());
        fields.put("Account Type", userAccount.getAccountType());
        fields.put("Balance", String.format("%.2f", userAccount.getBalance()));

        for (Map.Entry<String, String> entry : fields.entrySet()) {
            panel.add(new JLabel(entry.getKey() + ":"));
            panel.add(new JLabel(entry.getValue() != null ? entry.getValue() : ""));
        }
    }
}