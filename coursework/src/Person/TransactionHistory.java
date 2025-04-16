<<<<<<< HEAD
package Person;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import model.UserSession;

public class TransactionHistory extends JDialog {

    public TransactionHistory(Dialog owner) {
        super(owner, "Transaction History", true);
        setSize(600, 400);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        String loggedInUsername = UserSession.getCurrentUsername();
        if (loggedInUsername == null) {
            JOptionPane.showMessageDialog(this, "Please log in first", "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }

        DefaultTableModel tableModel = new DefaultTableModel(new Object[]{"Operation", "Amount", "Target Account", "Time"}, 0);
        JTable transactionTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(transactionTable);

        List<String[]> transactions = loadTransactions(loggedInUsername);
        for (String[] transaction : transactions) {
            tableModel.addRow(transaction);
        }

        add(scrollPane, BorderLayout.CENTER);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> {
            setVisible(false);
            SwingUtilities.invokeLater(() -> {
                dispose();
            });
        });
        add(closeButton, BorderLayout.SOUTH);

        setVisible(true);
    }

    private List<String[]> loadTransactions(String username) {
        List<String[]> userTransactions = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("transactions.csv"))) {
            String line;
            // 读取表头（第一行），可以跳过或者用于设置更详细的列名
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 5 && data[0].equals(username)) {
                    // 只展示 "进行的操作", "转入或转出的金额", "转入或转出的目标账户", "时间" 这几列
                    userTransactions.add(new String[]{data[1], data[2], data[3], data[4]});
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to read transaction history file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        return userTransactions;
    }
=======
package Person;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import model.UserSession;

public class TransactionHistory extends JDialog {

    public TransactionHistory(Dialog owner) {
        super(owner, "Transaction History", true);
        setSize(600, 400);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        String loggedInUsername = UserSession.getCurrentUsername();
        if (loggedInUsername == null) {
            JOptionPane.showMessageDialog(this, "Please log in first", "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }

        DefaultTableModel tableModel = new DefaultTableModel(new Object[]{"Operation", "Amount", "Target Account", "Time"}, 0);
        JTable transactionTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(transactionTable);

        List<String[]> transactions = loadTransactions(loggedInUsername);
        for (String[] transaction : transactions) {
            tableModel.addRow(transaction);
        }

        add(scrollPane, BorderLayout.CENTER);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> {
            setVisible(false);
            SwingUtilities.invokeLater(() -> {
                dispose();
            });
        });
        add(closeButton, BorderLayout.SOUTH);

        setVisible(true);
    }

    private List<String[]> loadTransactions(String username) {
        List<String[]> userTransactions = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("transactions.csv"))) {
            String line;
            // 读取表头（第一行），可以跳过或者用于设置更详细的列名
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 5 && data[0].equals(username)) {
                    // 只展示 "进行的操作", "转入或转出的金额", "转入或转出的目标账户", "时间" 这几列
                    userTransactions.add(new String[]{data[1], data[2], data[3], data[4]});
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to read transaction history file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        return userTransactions;
    }
>>>>>>> 382f4a22ceb10164f9c36fd7cadf0016088cd827
}