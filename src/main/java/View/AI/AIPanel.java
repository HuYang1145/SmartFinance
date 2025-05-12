/**
 * A panel for displaying an AI chat interface in the Smart Finance Application.
 * Provides a chat area for messages, an input field for user messages, and a send button.
 * Supports custom message bubbles with user and AI avatars, and handles scrolling and loading states.
 *
 * @author Group 19
 * @version 1.0
 */
package View.AI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import Model.ChatMessage;
import View.LoginAndMain.GradientComponents;

public class AIPanel extends JPanel {
    private JPanel chatArea;
    private JScrollPane scrollPane;
    private JTextField inputField;
    private JButton sendBtn;
    private AIViewListener listener;

    /**
     * Interface for handling send message events in the AI chat panel.
     */
    public interface AIViewListener {
        /**
         * Called when the user sends a message.
         *
         * @param message the message content sent by the user
         */
        void onSendMessage(String message);
    }

    /**
     * Constructs an AIPanel with a chat area, input field, and send button.
     * Initializes components and layouts for the AI chat interface.
     */
    public AIPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        initComponents();
        layoutComponents();
    }

    /**
     * Sets the listener for handling send message events.
     *
     * @param listener the AIViewListener to handle user messages
     */
    public void setListener(AIViewListener listener) {
        this.listener = listener;
    }

    /**
     * Adds a chat message to the chat area as a custom bubble.
     *
     * @param message the chat message to display
     */
    public void addMessage(ChatMessage message) {
        addCustomBubble(message.getContent(), message.isUserSent());
        scrollToBottom();
    }

    /**
     * Sets the loading state of the panel, enabling or disabling input and send button.
     *
     * @param isLoading true to set the panel to loading state, false otherwise
     */
    public void setLoadingState(boolean isLoading) {
        inputField.setEnabled(!isLoading);
        sendBtn.setEnabled(!isLoading);
        sendBtn.setText(isLoading ? "Processing..." : "Send");
    }

    /**
     * Displays an error message in a dialog.
     *
     * @param message the error message to display
     */
    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Initializes the components of the chat interface, including chat area, scroll pane, input field, and send button.
     */
    private void initComponents() {
        chatArea = new JPanel();
        chatArea.setLayout(new BoxLayout(chatArea, BoxLayout.Y_AXIS));
        chatArea.setBackground(Color.WHITE);
        chatArea.setAutoscrolls(true);

        scrollPane = new JScrollPane(chatArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        inputField = new JTextField();
        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        inputField.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1, true));

        sendBtn = new JButton("Send");
        sendBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        sendBtn.setForeground(Color.WHITE);
        sendBtn.setOpaque(false);
        sendBtn.setContentAreaFilled(false);
        sendBtn.setBorder(null);
        sendBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        sendBtn.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());
    }

    /**
     * Lays out the components of the chat interface, including header, chat area, and input bar.
     */
    private void layoutComponents() {
        JPanel header = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, Color.decode("#84ACC9"), getWidth(), 0, Color.decode("#A1DDA3"));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        header.setPreferredSize(new Dimension(0, 50));
        header.setLayout(new BorderLayout());
        JLabel headerLabel = new JLabel("AI Chat", SwingConstants.CENTER);
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        header.add(headerLabel, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(new EmptyBorder(10, 10, 10, 10));
        card.add(scrollPane, BorderLayout.CENTER);

        JPanel inputBar = new JPanel(new BorderLayout(8, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(0x84ACC9), 0, getHeight(), Color.decode("#A1DDA3"));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            }
        };
        inputBar.setBorder(new EmptyBorder(10, 10, 10, 10));
        inputBar.setOpaque(false);
        inputBar.setPreferredSize(new Dimension(0, 60));
        inputBar.add(inputField, BorderLayout.CENTER);
        inputBar.add(sendBtn, BorderLayout.EAST);
        card.add(inputBar, BorderLayout.SOUTH);

        add(card, BorderLayout.CENTER);
    }

    /**
     * Sends the user's input message to the listener and clears the input field.
     */
    private void sendMessage() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;
        if (listener != null) {
            listener.onSendMessage(text);
        }
        inputField.setText("");
    }

    /**
     * Adds a custom message bubble to the chat area with an avatar indicating user or AI.
     *
     * @param msg    the message content
     * @param isUser true if the message is from the user, false if from the AI
     */
    private void addCustomBubble(String msg, boolean isUser) {
        JPanel line = new JPanel();
        line.setLayout(new BoxLayout(line, BoxLayout.X_AXIS));
        line.setOpaque(false);
        line.setAlignmentY(Component.TOP_ALIGNMENT);

        JComponent avatar = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = isUser ? Color.decode("#A1DDA3") : Color.decode("#84ACC9");
                g2.setColor(c);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Microsoft YaHei", Font.BOLD, 12));
                FontMetrics fm = g2.getFontMetrics();
                String s = isUser ? "U" : "A";
                int sw = fm.stringWidth(s), sh = fm.getAscent();
                g2.drawString(s, (getWidth() - sw) / 2, (getHeight() + sh) / 2 - fm.getDescent());
                g2.dispose();
            }
        };
        Dimension avatarSize = new Dimension(30, 30);
        avatar.setPreferredSize(avatarSize);
        avatar.setMaximumSize(avatarSize);
        avatar.setMinimumSize(avatarSize);
        avatar.setAlignmentY(Component.TOP_ALIGNMENT);

        JTextArea ta = new JTextArea(msg);
        ta.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setEditable(false);
        ta.setOpaque(false);
        ta.setBorder(new EmptyBorder(8, 12, 8, 12));

        FontMetrics fm = ta.getFontMetrics(ta.getFont());
        int padding = 12;
        int maxBubbleW = (int) (getWidth() * 0.7);
        int textW = 0;
        for (String ln : msg.split("\n")) {
            textW = Math.max(textW, fm.stringWidth(ln));
        }
        int bubbleW = Math.min(textW + padding * 2, maxBubbleW);

        ta.setSize(bubbleW - padding * 2, Short.MAX_VALUE);
        Dimension taPref = ta.getPreferredSize();

        JPanel bubble = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = isUser ? Color.decode("#A1DDA3") : Color.decode("#84ACC9");
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }
        };
        bubble.setOpaque(false);
        bubble.add(ta, BorderLayout.CENTER);
        Dimension bubbleSize = new Dimension(bubbleW, taPref.height);
        bubble.setPreferredSize(bubbleSize);
        bubble.setMaximumSize(bubbleSize);
        bubble.setAlignmentY(Component.TOP_ALIGNMENT);

        if (isUser) {
            line.add(Box.createHorizontalGlue());
            line.add(bubble);
            line.add(Box.createRigidArea(new Dimension(6, 0)));
            line.add(avatar);
            line.setBorder(new EmptyBorder(0, 40, 0, 0));
        } else {
            line.add(avatar);
            line.add(Box.createRigidArea(new Dimension(6, 0)));
            line.add(bubble);
            line.add(Box.createHorizontalGlue());
            line.setBorder(new EmptyBorder(0, 0, 0, 40));
        }

        chatArea.add(Box.createVerticalStrut(4));
        chatArea.add(line);
        chatArea.revalidate();
        chatArea.repaint();
    }

    /**
     * Scrolls the chat area to the bottom to show the latest messages.
     */
     private void scrollToBottom() {
        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        GradientComponents.styleScrollBar(verticalScrollBar);
        SwingUtilities.invokeLater(() -> {
            verticalScrollBar.setValue(verticalScrollBar.getMaximum());
            SwingUtilities.invokeLater(() -> verticalScrollBar.setValue(verticalScrollBar.getMaximum()));
        });
    }
}
