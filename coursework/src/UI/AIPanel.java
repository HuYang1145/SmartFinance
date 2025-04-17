package UI;

import javax.swing.*;
import java.awt.*;
import ai_model.AIModel;
import java.util.concurrent.ExecutionException;

public class AIPanel extends JPanel {
    private JTextArea aiChatArea;
    private JTextField aiInputField;
    private AIModel aiModel;
    
    public AIPanel() {
        setLayout(new BorderLayout(0, 10));
        setBackground(new Color(245, 245, 245));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Initialize AI model
        initializeAIModel();
        
        // Initialize UI components
        initializeComponents();
    }
    
    private void initializeAIModel() {
        // Use default Python path
        aiModel = new AIModel();
        
        // Optional: Set custom Python path if needed
        String pythonPath = "D:/python/python.exe";
        if (pythonPath != null && !pythonPath.isEmpty()) {
            aiModel.setPythonPath(pythonPath);
        }
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
        //
        aiChatArea.setFont(new Font("SimSun", Font.PLAIN, 14));
        aiChatArea.setMargin(new Insets(5, 5, 5, 5));
        
        JScrollPane aiScrollPane = new JScrollPane(aiChatArea);
        aiScrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        add(aiScrollPane, BorderLayout.CENTER);
        
        // 底部输入区域
        JPanel aiInputPanel = new JPanel(new BorderLayout(10, 0));
        aiInputPanel.setBackground(new Color(245, 245, 245));
        aiInputPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        aiInputField = new JTextField();
        aiInputField.setFont(new Font("SimSun", Font.PLAIN, 14));
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
            // Display user message
            aiChatArea.append("You: " + text + "\n\n");
            
            // Display "typing" indicator
            aiChatArea.append("AI Assistant: Thinking...\n");
            aiChatArea.setCaretPosition(aiChatArea.getDocument().getLength());
            
            // Clear input field
            aiInputField.setText("");
            
            // Use SwingWorker to prevent UI freezing
            SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
                @Override
                protected String doInBackground() throws Exception {
                    // Call the AI model
                    return aiModel.getResponse(text);
                }
                
                @Override
                protected void done() {
                    try {
                        // Remove "typing" indicator (remove the last line)
                        String currentText = aiChatArea.getText();
                        int lastLineStart = currentText.lastIndexOf("AI Assistant: Thinking...");
                        if (lastLineStart >= 0) {
                            aiChatArea.replaceRange("", lastLineStart, currentText.length());
                        }
                        
                        // Display AI response
                        String response = get();
                        aiChatArea.append("AI Assistant:\n" + response + "\n\n");
                    } catch (InterruptedException | ExecutionException e) {
                        aiChatArea.append("AI Assistant: Error processing your request.\n\n");
                    }
                    
                    // Scroll to bottom
                    aiChatArea.setCaretPosition(aiChatArea.getDocument().getLength());
                }
            };
            
            worker.execute();
        }
    }
}