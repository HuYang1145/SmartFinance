package UI;

import Model.AccountModel;
import Model.DeepSeekFileProcessor;
import Model.IntentResult;
import Model.UserSession;
import com.formdev.flatlaf.FlatLightLaf;
import com.google.gson.Gson;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class AIPanel extends JPanel {
    private JPanel chatArea;
    private JScrollPane scrollPane;
    private JTextField inputField;
    private JButton sendBtn;

    public AIPanel() {
        // 1) 主题 & 布局
        FlatLightLaf.setup();
        setLayout(new BorderLayout());
        setBackground(Color.white);

        // 2) 顶部栏：亮紫 (#9C27B0) → 克莱因蓝 (#002FA7)
        JPanel header = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(0x9C, 0x27, 0xB0),
                        getWidth(), 0, new Color(0x00, 0x2F, 0xA7)
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        header.setPreferredSize(new Dimension(0, 50));
        header.setLayout(new BorderLayout());
        JLabel headerLabel = new JLabel("AI Chat", SwingConstants.CENTER);
        headerLabel.setForeground(Color.white);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        header.add(headerLabel, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        // 3) 主体卡片
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.white);
        card.setBorder(new EmptyBorder(10, 10, 10, 10));
        add(card, BorderLayout.CENTER);

        // 4) 聊天区
        chatArea = new JPanel();
        chatArea.setLayout(new BoxLayout(chatArea, BoxLayout.Y_AXIS));
        chatArea.setBackground(Color.white);
        chatArea.setAutoscrolls(true);

        scrollPane = new JScrollPane(
                chatArea,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        card.add(scrollPane, BorderLayout.CENTER);

        // 5) 底部输入栏：同样渐变，圆角
        JPanel inputBar = new JPanel(new BorderLayout(8, 0)) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(0x9C, 0x27, 0xB0, 200),
                        0, getHeight(), new Color(0x00, 0x2F, 0xA7, 200)
                );
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            }
        };
        inputBar.setBorder(new EmptyBorder(10, 10, 10, 10));
        inputBar.setOpaque(false);
        inputBar.setPreferredSize(new Dimension(0, 60));
        card.add(inputBar, BorderLayout.SOUTH);

        // 5.1) 输入框
        inputField = new JTextField();
        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        inputField.setBorder(BorderFactory.createLineBorder(Color.white, 1, true));
        inputBar.add(inputField, BorderLayout.CENTER);

        // 5.2) 发送按钮
        sendBtn = new JButton("Send");
        sendBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        sendBtn.setForeground(Color.white);
        sendBtn.setOpaque(false);
        sendBtn.setContentAreaFilled(false);
        sendBtn.setBorder(null);
        inputBar.add(sendBtn, BorderLayout.EAST);

        // 6) 事件绑定
        sendBtn.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());
    }

    private void sendMessage() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;

        addCustomBubble(text, true);
        inputField.setText("");

        SwingUtilities.invokeLater(() -> {
            String reply = predictReply(text);
            addCustomBubble(reply, false);
        });
    }

    private String predictReply(String userText) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "D:/python/envs/nlp/python.exe",
                    "dict/predict.py",
                    userText
            );
            pb.redirectErrorStream(true);
            Process p = pb.start();
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String ln;
            while ((ln = r.readLine()) != null) sb.append(ln);
            IntentResult res = new Gson().fromJson(sb.toString(), IntentResult.class);
            String intent = res.getIntent();
            if (intent == null) intent = "Unknown";

            AccountModel acct = UserSession.getCurrentAccount();
            String summary = buildTransactionSummary(acct);
            if ("QueryBalance".equals(intent)) {
                return "Your balance is: " + acct.getBalance();
            } else if ("QuerySuggestion".equals(intent)) {

                return DeepSeekFileProcessor.callDeepSeekApi(
                        summary,
                        "Please analyse the transaction records and give concise English suggestions."
                );
            } else if("QuerySpendTime".equals(intent)){
                return  "You spent " +getCurrentMonthExpense(summary)+" this month.";
            }
            return "Predicted: " + intent;
        } catch (Exception ex) {
            ex.printStackTrace();
            return "[Error]";
        }
    }

    public static String buildTransactionSummary(AccountModel account) {
        StringBuilder sb = new StringBuilder();
        for (var tx : account.getTransactions()) {
            sb.append(tx.getTimestamp())
                    .append(", ")
                    .append(tx.getType())
                    .append(", ")
                    .append(tx.getAmount())
                    .append(", ")
                    .append(tx.getDescription())
                    .append("\n");
        }
        return sb.toString();
    }
    public static double getCurrentMonthExpense(String summary) {
        double totalExpense = 0.0;

        java.time.LocalDate now = java.time.LocalDate.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();

        for (String line : summary.split("\n")) {
            String[] parts = line.split(",\\s*");
            if (parts.length <4) continue;

            try {
                String operation = parts[1].trim(); // 保险起见 trim 一下
                double amount = Double.parseDouble(parts[2].trim());
                String timestampStr = parts[0].trim();

                java.time.LocalDateTime dt = java.time.LocalDateTime.parse(
                        timestampStr,
                        java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")
                );

                if (dt.getYear() == currentYear && dt.getMonthValue() == currentMonth) {
                    if (operation.equalsIgnoreCase("Transfer Out") || operation.equalsIgnoreCase("Withdrawal")) {
                        totalExpense += Math.abs(amount); // 加正值
                    }
                }
            } catch (Exception e) {
                e.printStackTrace(); // 可视化调试
            }
        }

        return totalExpense;
    }



    private void addCustomBubble(String msg, boolean isUser) {
        // A) 整行容器，水平 BoxLayout，顶对齐
        JPanel line = new JPanel();
        line.setLayout(new BoxLayout(line, BoxLayout.X_AXIS));
        line.setOpaque(false);
        line.setAlignmentY(Component.TOP_ALIGNMENT);

        // B) 头像
        JComponent avatar = new JComponent() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = isUser ? new Color(0x9C27B0) : new Color(0x002FA7);
                g2.setColor(c);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(Color.white);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                FontMetrics fm = g2.getFontMetrics();
                String s = isUser ? "U" : "AI";
                int sw = fm.stringWidth(s), sh = fm.getAscent();
                g2.drawString(s, (getWidth()-sw)/2, (getHeight()+sh)/2-2);
            }
        };
        avatar.setPreferredSize(new Dimension(30, 30));
        avatar.setMaximumSize(avatar.getPreferredSize());
        avatar.setAlignmentY(Component.TOP_ALIGNMENT);

        // C) 文本区 & 动态测量
        JTextArea ta = new JTextArea(msg);
        ta.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setOpaque(false);
        ta.setBorder(new EmptyBorder(8, 12, 8, 12));
        FontMetrics fm = ta.getFontMetrics(ta.getFont());
        int padding = 12, maxW = 300, textW = 0;
        for (String ln : msg.split("\n")) textW = Math.max(textW, fm.stringWidth(ln));
        int bubbleW = Math.min(textW + padding*2, maxW);
        int avgW = fm.charWidth('口');
        ta.setColumns(Math.max(1, (bubbleW - padding*2)/avgW));
        ta.setSize(bubbleW, Short.MAX_VALUE);
        Dimension d = ta.getPreferredSize();

        // D) 气泡面板 & 圆角背景
        JPanel bubble = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = isUser ? new Color(0xE1BEE7) : new Color(0xE3F2FD);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            }
        };
        bubble.setOpaque(false);
        bubble.add(ta, BorderLayout.CENTER);
        bubble.setPreferredSize(new Dimension(bubbleW, d.height));
        bubble.setMaximumSize(bubble.getPreferredSize());
        bubble.setMinimumSize(bubble.getPreferredSize());
        bubble.setAlignmentY(Component.TOP_ALIGNMENT);

        // E) 组装：用户靠右、AI 靠左＋Glue
        if (isUser) {
            line.add(Box.createHorizontalGlue());
            line.add(bubble);
            line.add(Box.createRigidArea(new Dimension(6, 0)));
            line.add(avatar);
        } else {
            line.add(avatar);
            line.add(Box.createRigidArea(new Dimension(6, 0)));
            line.add(bubble);
            line.add(Box.createHorizontalGlue());
        }

        // F) 加入 chatArea & 自动滚到底
        chatArea.add(Box.createVerticalStrut(4));
        chatArea.add(line);
        chatArea.revalidate();
        SwingUtilities.invokeLater(() ->
                scrollPane.getVerticalScrollBar()
                        .setValue(scrollPane.getVerticalScrollBar().getMaximum())
        );
    }
}
