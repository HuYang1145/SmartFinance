package UI;

import javax.swing.*;
import java.awt.*;
import javax.swing.Timer;

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
            
            // 模拟回复
            Timer timer = new Timer(500, evt -> {
                aiChatArea.append("AI Assistant: " + "This is a simulated response based on your query about '" 
                    + text.substring(0, Math.min(text.length(), 15)) + "...'.\n\n");
                ((Timer)evt.getSource()).stop();
            });
            timer.setRepeats(false);
            timer.start();
            
            aiInputField.setText("");
            aiChatArea.setCaretPosition(aiChatArea.getDocument().getLength());
        }
    }
}