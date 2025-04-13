package model;

import java.io.*;
import java.util.List;
import java.util.ArrayList;

public class UserRegistrationCSVExporter {
    private static final String CSV_FILE_PATH = "accounts.csv";
    private static final String CSV_HEADER = "用户名,密码,手机号,邮箱,性别,地址,创建时间,账号状态,账户类型,金额\n";

    /**
     * 将账户列表保存到 CSV 文件。
     *
     * @param accountList 要保存的账户列表。
     * @param append      如果为 true，则追加写入文件；如果为 false，则覆盖写入文件。
     */
    public static void saveToCSV(List<AccountModel> accountList, boolean append) {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(CSV_FILE_PATH, append), "UTF-8"))) {
            if (!append) {
                // 如果是覆盖写入，则写入表头
                writer.append(CSV_HEADER);
            } else {
                // 如果是追加写入，检查文件是否为空，如果为空则写入表头（可选，取决于你的需求）
                File file = new File(CSV_FILE_PATH);
                if (file.length() == 0) {
                    writer.append(CSV_HEADER);
                }
            }

            // 写入账户数据
            for (AccountModel account : accountList) {
                writer.append(account.toCSV());
                writer.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<AccountModel> readFromCSV() {
        List<AccountModel> accounts = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(CSV_FILE_PATH), "UTF-8"))) {
            String line;
            // 读取并跳过列标题行
            reader.readLine();

            // 读取账户数据
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 10) { // 确保有 10 个字段（包含金额）
                    String username = parts[0];
                    String password = parts[1];
                    String phone = parts[2];
                    String email = parts[3];
                    String gender = parts[4];
                    String address = parts[5];
                    String creationTime = parts[6];
                    String accountStatus = parts[7];
                    String accountType = parts[8];
                    double balance = Double.parseDouble(parts[9]); // 解析金额

                    if (accountType.equals("Admin")) {
                        accounts.add(new AdminAccount(username, password, phone, email, gender, address, creationTime, accountStatus, accountType, balance));
                    } else {
                        accounts.add(new PersonalAccount(username, password, phone, email, gender, address, creationTime, accountStatus, accountType, balance));
                    }
                } else {
                    System.out.println("无效数据行: " + line + ". 期望 10 个字段，但找到 " + parts.length + " 个。");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.out.println("读取金额时发生错误，请检查 CSV 文件格式。");
            e.printStackTrace();
        }
        return accounts;
    }
}