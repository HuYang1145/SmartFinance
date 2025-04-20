package Admin;

import Model.UserSession;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.*;

public class AdminSelfInfo extends JFrame {

    public AdminSelfInfo() {
        setTitle("Admin Personal Information");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new GridLayout(0, 2)); // 使用 GridLayout，名称和值各占一列

        String loggedInUsername = UserSession.getCurrentUsername(); // 从 UserSession 获取当前登录用户名

        if (loggedInUsername == null || loggedInUsername.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Admin login information not detected, please log in first", "Error", JOptionPane.ERROR_MESSAGE);
            return; // 如果没有用户名，直接返回，不再继续加载数据
        }

        try (BufferedReader br = new BufferedReader(new FileReader("accounts.csv"))) {
            String headerLine = br.readLine(); // 读取第一行作为表头（名称）
            if (headerLine == null) {
                JOptionPane.showMessageDialog(this, "accounts.csv file is empty or header is missing", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String[] headers = headerLine.split(","); // 表头字段

            String line;
            boolean adminFound = false;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length != headers.length) { // 数据行字段数量校验
                    System.err.println("Data row field count does not match header, skipping row: " + line);
                    continue;
                }

                // 假设 accounts.csv 文件中第一列是用户名 (username)
                if (values[0].equals(loggedInUsername)) { // 使用从 UserSession 获取的用户名进行匹配
                    adminFound = true;

                    // 找到匹配的管理员信息，开始展示
                    for (int i = 0; i < headers.length; i++) {
                        JLabel nameLabel = new JLabel(headers[i] + ":"); // 创建名称标签，显示表头作为名称
                        JLabel valueLabel = new JLabel(values[i]);         // 创建值标签，显示对应的数据值

                        add(nameLabel); // 添加名称标签到窗口
                        add(valueLabel); // 添加值标签到窗口
                    }
                    break; // 找到匹配的管理员信息后，就可以停止循环了
                }
            }

            if (!adminFound) {
                JOptionPane.showMessageDialog(this, "Admin information for username " + loggedInUsername + " not found in accounts.csv file", "Information", JOptionPane.WARNING_MESSAGE);
            }

        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to read accounts.csv file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AdminSelfInfo());
    }
}