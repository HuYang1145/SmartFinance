package UI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;


public class AIPanel extends JPanel {
    private JTextArea aiChatArea;
    private JTextField aiInputField;

    public AIPanel() {
        setLayout(new BorderLayout(0, 10));
        setBackground(new Color(245, 245, 245));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initializeComponents();
    }

    private void initializeComponents() {
        // 标题
        JLabel titleLabel = new JLabel("AI Financial Assistant Q&A");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(30, 60, 120));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        add(titleLabel, BorderLayout.NORTH);

        // 聊天显示区域
        aiChatArea = new JTextArea();
        aiChatArea.setEditable(false);
        aiChatArea.setLineWrap(true);
        aiChatArea.setWrapStyleWord(true);
        aiChatArea.setForeground(Color.BLACK);
        aiChatArea.setBackground(Color.WHITE);
        aiChatArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        aiChatArea.setMargin(new Insets(5, 5, 5, 5));

        JScrollPane aiScrollPane = new JScrollPane(aiChatArea);
        aiScrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        add(aiScrollPane, BorderLayout.CENTER);

        // 底部输入区域
        JPanel aiInputPanel = new JPanel(new BorderLayout(10, 0));
        aiInputPanel.setBackground(new Color(245, 245, 245));
        aiInputPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        aiInputField = new JTextField();
        aiInputField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        aiInputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        aiInputPanel.add(aiInputField, BorderLayout.CENTER);

        JButton sendButton = new JButton("Send");
        sendButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        sendButton.setBackground(new Color(30, 60, 120));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        sendButton.setPreferredSize(new Dimension(80, 30));
        aiInputPanel.add(sendButton, BorderLayout.EAST);

        add(aiInputPanel, BorderLayout.SOUTH);

        // 发送按钮点击事件
        sendButton.addActionListener(e -> sendMessage());
        aiInputField.addActionListener(e -> sendMessage());
    }

    private void sendMessage() {
        String text = aiInputField.getText().trim();
        if (!text.isEmpty()) {
            aiChatArea.append("You: " + text + "\n\n");

            try {
                File exeFile = new File("coursework/dist/predict/predict.exe");
                String exePath = exeFile.getCanonicalPath();
                ProcessBuilder pb = new ProcessBuilder(exePath, text);

                pb.redirectErrorStream(true);
                Process process = pb.start();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line).append("\n");
                }

                aiChatArea.append("AI Assistant:\n" + response.toString() + "\n");

            } catch (Exception ex) {
                aiChatArea.append("AI Assistant: Error calling AI model.\n");
                ex.printStackTrace();
            }


            aiInputField.setText("");
            aiChatArea.setCaretPosition(aiChatArea.getDocument().getLength());
        }
    }
}