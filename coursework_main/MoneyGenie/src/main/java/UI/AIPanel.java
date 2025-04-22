package UI;

// --- Keep existing UI/Util imports ---

import AccountModel.*;
import com.google.gson.Gson;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
// --- END ADDED IMPORTS ---


/**
 * AIPanel providing a chat interface, using the user's provided UI structure
 * and updated backend logic for the refactored transaction system.
 */
public class AIPanel extends JPanel {
    private JPanel chatArea;
    private JScrollPane scrollPane;
    private JTextField inputField;
    private JButton sendBtn;
    private static final Gson GSON = new Gson(); // Keep Gson

    // --- ADDED FORMATTER required by the backend logic ---
    // Define the date/time format used in TransactionModel's timestamp string
    // Ensure this matches the format stored (e.g., from TransactionService: yyyy/MM/dd HH:mm)
    private static final DateTimeFormatter TRANSACTION_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
    // --- END ADDED FORMATTER ---

    public AIPanel() {
        // --- User's Original UI Setup ---
        // 1) Theme & Layout
        // FlatLightLaf.setup(); // Keep or remove based on your original code's intention
        setLayout(new BorderLayout());
        setBackground(Color.white);

        // 2) Header: Gradient Purple -> Blue
        JPanel header = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(0x9C, 0x27, 0xB0), getWidth(), 0, new Color(0x00, 0x2F, 0xA7));
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

        // 3) Main Card Panel
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.white);
        card.setBorder(new EmptyBorder(10, 10, 10, 10));
        add(card, BorderLayout.CENTER);

        // 4) Chat Area
        chatArea = new JPanel();
        chatArea.setLayout(new BoxLayout(chatArea, BoxLayout.Y_AXIS));
        chatArea.setBackground(Color.white);
        chatArea.setAutoscrolls(true);

        scrollPane = new JScrollPane(
                chatArea,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        // Remove border from scrollpane if desired, or keep original
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        card.add(scrollPane, BorderLayout.CENTER);

        // 5) Input Bar: Gradient with rounded corners
        JPanel inputBar = new JPanel(new BorderLayout(8, 0)) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(0x9C, 0x27, 0xB0, 200), 0, getHeight(), new Color(0x00, 0x2F, 0xA7, 200));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            }
        };
        inputBar.setBorder(new EmptyBorder(10, 10, 10, 10));
        inputBar.setOpaque(false);
        inputBar.setPreferredSize(new Dimension(0, 60));
        card.add(inputBar, BorderLayout.SOUTH);

        // 5.1) Input Field
        inputField = new JTextField();
        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        // Use the border style from your original code
        inputField.setBorder(BorderFactory.createLineBorder(Color.white, 1, true));
        inputBar.add(inputField, BorderLayout.CENTER);

        // 5.2) Send Button
        sendBtn = new JButton("Send");
        sendBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        sendBtn.setForeground(Color.white);
        // Use the styling from your original code
        sendBtn.setOpaque(false);
        sendBtn.setContentAreaFilled(false);
        sendBtn.setBorder(null); // Or your original border
        sendBtn.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Add cursor if desired
        inputBar.add(sendBtn, BorderLayout.EAST);

        // 6) Event Binding (Remains the same)
        sendBtn.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());

        // Add initial greeting bubble (Optional)
        // addWelcomeMessage(); // Uncomment if you want a welcome message
        // --- End Original UI Setup ---
    }

     // Optional welcome message function
    // private void addWelcomeMessage() {
    //     SwingUtilities.invokeLater(() -> {
    //         addCustomBubble("Hello! How can I help?", false);
    //     });
    // }


    // --- sendMessage method (Uses SwingWorker, calls updated predictReply) ---
    // This method's logic should remain as updated previously
    private void sendMessage() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;
        addCustomBubble(text, true); // Uses your original addCustomBubble
        inputField.setText("");

        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return predictReply(text); // Calls updated backend logic
            }

            @Override
            protected void done() {
                try {
                    String reply = get();
                    addCustomBubble(reply, false); // Uses your original addCustomBubble
                } catch (Exception e) {
                    e.printStackTrace();
                    addCustomBubble("Sorry, an error occurred while processing.", false);
                }
            }
        };
        worker.execute();
    }

    // --- predictReply method (UPDATED backend logic) ---
    // This method should remain as updated previously to handle new transaction model
    private String predictReply(String userText) {
        // 1. 先把用户输入去掉首尾空格
        userText = userText == null ? "" : userText.trim();
        if (userText.isEmpty()) {
            return "Please say something first.";
        }

        try {
            // 2. 构造本地可执行文件的路径
            //    假设当前工作目录 (user.dir) 下有 dist/predict/predict.exe
            String baseDir = System.getProperty("user.dir");
            String exePath = baseDir
                    + File.separator + "dist"
                    + File.separator + "predict"
                    + File.separator + "predict.exe";

            // 3. 调用本地 predict.exe 进行推理
            ProcessBuilder pb = new ProcessBuilder(exePath, userText);
            pb.redirectErrorStream(true);
            System.out.println("DEBUG: AIPanel - Executing local exe: " + pb.command());
            Process proc = pb.start();

            // 4. 读取 predict.exe 输出的 JSON
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(proc.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }
            }
            int exitCode = proc.waitFor();
            System.out.println("DEBUG: AIPanel - predict.exe exit code: " + exitCode);
            if (exitCode != 0 || output.length() == 0) {
                return "Error: AI failed to understand.";
            }

            // 5. 解析 JSON 到 IntentResult
            IntentResult intentResult;
            try {
                intentResult = GSON.fromJson(output.toString(), IntentResult.class);
            } catch (com.google.gson.JsonSyntaxException e) {
                System.err.println("ERROR: JSON parse failed → " + output);
                return "Error: couldn't parse AI response.";
            }
            String intent = intentResult.getIntent() != null
                    ? intentResult.getIntent()
                    : "Unknown";
            System.out.println("DEBUG: AIPanel - Detected intent: " + intent);

            // 6. 根据意图执行对应业务
            AccountModel acct = UserSession.getCurrentAccount();
            if (acct == null) {
                return "Please log in first.";
            }

            switch (intent) {
                case "QueryBalance":
                    return "Your balance is: ¥" + String.format("%.2f", acct.getBalance());

                case "QuerySuggestion":
                    String summary = buildTransactionSummary(acct);
                    if (summary.isEmpty()) {
                        return "No transactions loaded.";
                    }
                    try {
                        String suggestions = DeepSeekFileProcessor.callDeepSeekApi(
                                summary,
                                "Analyze these transactions and give 2-3 brief, actionable suggestions."
                        );
                        return "Suggestions:\n" + suggestions;
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        return "Sorry, couldn't fetch suggestions.";
                    }

                case "QuerySpendTime":
                    double spent = getCurrentMonthExpenseFromModel(acct);
                    return "You spent ¥" + String.format("%.2f", spent) + " this month.";

                case "Unknown":
                default:
                    return "I can tell you your balance, spending this month, or give suggestions.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Sorry, an unexpected error occurred.";
        }
    }


    // --- buildTransactionSummary method (UPDATED backend logic) ---
    // Needs access to the updated TransactionModel structure
    public static String buildTransactionSummary(AccountModel account) {
        if (account == null || account.getTransactions() == null || account.getTransactions().isEmpty()) return "";
        StringBuilder sb = new StringBuilder("Operation,Amount,Time,Merchant/Payee,Type\n"); // Header for context
        List<TransactionModel> transactions = account.getTransactions();
        for (TransactionModel tx : transactions) {
             String merchant = tx.getMerchant() != null ? tx.getMerchant() : "";
             String type = tx.getType() != null ? tx.getType() : "";
            sb.append(tx.getOperation()).append(",")
              .append(String.format("%.2f", tx.getAmount())).append(",")
              .append(tx.getTimestamp()).append(",")
              .append(escapeForSummary(merchant)).append(",")
              .append(escapeForSummary(type))
              .append("\n");
        }
        return sb.toString();
    }

    // --- escapeForSummary helper (Needed by buildTransactionSummary) ---
     private static String escapeForSummary(String field) {
        // Simple escape for comma within field for the summary string
        if (field.contains(",")) return "\"" + field.replace("\"", "\"\"") + "\"";
        return field;
    }

    // --- getCurrentMonthExpenseFromModel method (UPDATED backend logic) ---
    // Needs access to updated TransactionModel structure and TRANSACTION_TIME_FORMATTER
    public static double getCurrentMonthExpenseFromModel(AccountModel account) {
         if (account == null || account.getTransactions() == null) return 0.0;
        double totalExpense = 0.0;
        java.time.LocalDate now = java.time.LocalDate.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();
        List<TransactionModel> transactions = account.getTransactions();
        for (TransactionModel tx : transactions) {
            if ("Expense".equalsIgnoreCase(tx.getOperation())) {
                try {
                    // Use the formatter defined at the class level
                    LocalDateTime dt = LocalDateTime.parse(tx.getTimestamp(), TRANSACTION_TIME_FORMATTER);
                    if (dt.getYear() == currentYear && dt.getMonthValue() == currentMonth) {
                        totalExpense += tx.getAmount();
                    }
                } catch (DateTimeParseException e) {
                    System.err.println("AIPanel (Expense Calc - User UI): Date parse error: " + tx.getTimestamp());
                } catch (Exception e) {
                     System.err.println("AIPanel (Expense Calc - User UI): Generic error: " + tx);
                }
            }
        }
        return totalExpense;
    }


    // --- addCustomBubble method (Your Original UI Style) ---
    // This method paints the bubbles as defined in the code you provided
    private void addCustomBubble(String msg, boolean isUser) {
        SwingUtilities.invokeLater(() -> {
            // --- A) 整行容器 ---
            JPanel line = new JPanel();
            line.setLayout(new BoxLayout(line, BoxLayout.X_AXIS));
            line.setOpaque(false);
            line.setAlignmentY(Component.TOP_ALIGNMENT);

            // --- B) 头像圆形 ---
            JComponent avatar = new JComponent() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    Color c = isUser ? new Color(0x9C27B0) : new Color(0x002FA7);
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

            // --- C) 文本区 & 计算 ---
            JTextArea ta = new JTextArea(msg);
            ta.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
            ta.setLineWrap(true);
            ta.setWrapStyleWord(true);
            ta.setEditable(false);
            ta.setOpaque(false);
            ta.setBorder(new EmptyBorder(8, 12, 8, 12));

            FontMetrics fm = ta.getFontMetrics(ta.getFont());
            int padding = 12;
            int maxBubbleW = (int)(getWidth() * 0.7); // 最多占 70% 宽度
            int textW = 0;
            for (String ln : msg.split("\n")) {
                textW = Math.max(textW, fm.stringWidth(ln));
            }
            int bubbleW = Math.min(textW + padding * 2, maxBubbleW);

            ta.setSize(bubbleW - padding * 2, Short.MAX_VALUE);
            Dimension taPref = ta.getPreferredSize();

            // --- D) 气泡面板 ---
            JPanel bubble = new JPanel(new BorderLayout()) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    Color bg = isUser ? new Color(0xE1BEE7) : new Color(0xE3F2FD);
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

            // --- E) 组装：用户右对齐、AI 左对齐 ---
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

            // --- F) 加到 chatArea 并滚动到底部 ---
            chatArea.add(Box.createVerticalStrut(4));
            chatArea.add(line);
            chatArea.revalidate();
            chatArea.repaint();
            SwingUtilities.invokeLater(() -> {
                JScrollBar bar = scrollPane.getVerticalScrollBar();
                bar.setValue(bar.getMaximum());
            });
        });
    }


    // --- scrollToBottom helper (Remains the same) ---
     private void scrollToBottom() {
          JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
          SwingUtilities.invokeLater(() -> {
              verticalScrollBar.setValue(verticalScrollBar.getMaximum());
               SwingUtilities.invokeLater(() -> {
                    verticalScrollBar.setValue(verticalScrollBar.getMaximum());
              });
          });
     }

} // End of AIPanel class