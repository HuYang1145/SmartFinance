## Anomaly Detection and Alert Feature for Transactions

*   This feature aims to enhance account security and user awareness by automatically detecting potentially anomalous transaction patterns upon user login. If the system identifies suspicious activity based on predefined rules, it proactively alerts the user via warning messages.

*   **Development Lead:**
    *   **Zang Lu (臧璐):** Responsible for implementing the anomaly detection logic and related components.

*   **Core Functionality:**
    *   **Trigger Point:** Detection executes automatically within the `AccountManagementController` immediately after a successful user login.
    *   **Data Analysis:** The system analyzes the transaction history loaded into the user's `AccountModel`.
    *   **Rule Engine (`TransactionChecker.java`):** A dedicated class, `TransactionChecker`, contains the logic to evaluate transactions against several rules:
        *   **Absolute Large Amount:** Detects single transactions (income or expense) exceeding a predefined absolute threshold (e.g., ¥3000 CNY).
        *   **Relative Large Expense:** Identifies single expense transactions significantly higher than the user's average expense amount (e.g., over 3 times the average).
        *   **High-Frequency Large Expense:** Flags situations where multiple large expenses (above a specific threshold, e.g., ¥1000 CNY) occur on the same day (e.g., 4 or more times).
        *   **Monthly Spending Anomaly:** Detects single expense transactions significantly exceeding the user's typical monthly spending pattern (e.g., more than 200% of the calculated average monthly expense).
    *   **Alert Mechanism:**
        *   If the `TransactionChecker` detects one or more anomalies, the `AccountManagementController` receives a list specifying the types of anomalies found.
        *   For each detected anomaly, a user-friendly warning message (customized based on the specific rule triggered) is displayed to the user via a `JOptionPane` popup. This ensures immediate notification upon login.
        *   Warnings are displayed using `SwingUtilities.invokeLater` to ensure they pop up correctly even if the login window closes quickly.
        *   Warning messages include the user's account details (username, balance) for context and verification.

*   **Key Classes Involved:**
    *   `AccountManagementController.java`: Orchestrates the login process, initiates anomaly detection, and displays warning dialogs to the user.
    *   `TransactionChecker.java`: Encapsulates the rules and logic for identifying anomalous transactions, defining thresholds, anomaly type identifiers, and potentially calculating necessary averages (like average monthly expense).
    *   `AccountModel.java` (and subclasses): Holds user account details and the list of `TransactionModel` objects required for analysis.
    *   `TransactionModel.java`: Represents a single transaction record used by the detector.
    *   `TransactionService.java`: Responsible for loading transaction data from `transactions.csv`.
    *   `UserSession.java`: Provides access to the currently logged-in user's account information.

*   **Related Contributions by Zang Lu:**
    *   While the core detection logic resides in `TransactionChecker` and is invoked by `AccountManagementController`, work on the following components provides the necessary foundation and data support for this feature:
        *   `BudgetAdvisor.java` & `user_budget.csv`: Understanding normal budget patterns and user goals helps define what constitutes an "anomalous" deviation.
        *   `transactions.csv` Data Provision: Accurate transaction data is fundamental for the detection mechanism to function correctly.
        *   `PersonalUI.java` & `BudgetGoalDialog.java`: These UI components (potentially displaying budget status or allowing goal setting) form part of the user's financial management workflow where transaction awareness is crucial.

*   **User Experience:**
    *   Upon logging in, if any transaction patterns match the predefined rules, the user will see one or more pop-up warning messages briefly describing the nature of the anomaly detected (e.g., "Warning: A recent transaction amount was unusually large..."). This prompts the user to review their recent activity more closely. If no anomalies are found, the login process continues uninterrupted.

*   **Feature Benefits:**
    *   **Proactive Security:** Alerts users early to potentially unauthorized or unusual activity.
    *   **Spending Awareness:** Helps users monitor significant deviations from their typical spending habits.
    *   **Enhanced Trust:** Provides an additional layer of automated oversight for the user's financial data.

---

## 异常交易检测与提醒功能

*   此功能旨在通过在用户登录时自动检测潜在的异常交易模式，来增强账户安全性并提高用户意识。如果系统根据预定义的规则识别出可疑活动，它将主动通过警告消息提醒用户。

*   **开发负责人:**
    *   **臧璐**: 负责实现异常交易检测逻辑及相关组件。

*   **核心功能:**
    *   **触发时机:** 在用户成功登录后，检测会在 `AccountManagementController` 中立即自动执行。
    *   **数据分析:** 系统分析加载到用户 `AccountModel` 中的交易历史记录。
    *   **规则引擎 (`TransactionChecker.java`):** 一个专门的类 `TransactionChecker` 包含了根据多项规则评估交易的逻辑：
        *   **绝对大额:** 检测单笔交易（收入或支出）是否超过预定义的绝对阈值（例如：人民币 3000 元）。
        *   **相对大额支出:** 识别出显著高于用户平均支出金额（例如：超过平均值的 3 倍）的单笔支出。
        *   **高频大额支出:** 标记在同一天内发生多次大额支出（超过特定阈值，例如：人民币 1000 元）的情况（例如：达到或超过 4 次）。
        *   **月度消费异常:** 检测显著超出用户常规月度支出模式的单笔支出交易（例如：超过计算出的月平均支出的 200%）。
    *   **提醒机制:**
        *   如果 `TransactionChecker` 检测到一个或多个异常情况，`AccountManagementController` 会收到一个包含具体检测到的异常类型的列表。
        *   针对每种检测到的异常，系统会使用 `JOptionPane` 弹窗向用户显示一个用户友好的警告消息（根据触发的具体规则定制）。这确保用户在登录时能立即得到通知。
        *   警告信息通过 `SwingUtilities.invokeLater` 显示，以确保即使登录窗口迅速关闭，它们也能正确弹出。
        *   警告消息包括用户的账户信息（用户名、账户余额），以便用户能够确认是否需要采取措施。

*   **涉及的关键类:**
    *   `AccountManagementController.java`: 协调登录过程，启动异常检测，并向用户显示警告对话框。
    *   `TransactionChecker.java`: 封装了用于识别异常交易的规则和逻辑，定义了阈值、异常类型标识符，并可能包含计算所需平均值（如月平均支出）的方法。
    *   `AccountModel.java` (及其子类): 持有用户账户详情和进行分析所需的 `TransactionModel` 对象列表。
    *   `TransactionModel.java`: 代表由检测器使用的单条交易记录。
    *   `TransactionService.java`: 负责从 `transactions.csv` 加载交易数据。
    *   `UserSession.java`: 提供当前登录用户的账户信息。

*   **臧璐的相关贡献:**
    *   虽然核心检测逻辑位于 `TransactionChecker` 中并由 `AccountManagementController` 调用，但在以下组件上的工作为此功能提供了必要的基础和数据支持：
        *   `BudgetAdvisor.java` & `user_budget.csv`: 理解正常的预算模式和用户目标有助于定义什么构成“异常”偏差。
        *   `transactions.csv` 数据提供: 提供准确的交易数据是检测机制正常运作的基础。
        *   `PersonalUI.java` & `BudgetGoalDialog.java`: 这些UI组件（可能用于显示预算状态或允许设置目标）构成了用户财务管理工作流程的一部分，其中交易意识至关重要。

*   **用户体验:**
    *   用户登录时，如果系统检测到任何符合预定义规则的交易模式，用户将看到一个或多个弹窗警告消息，简要描述检测到的异常性质（例如：“警告：最近有一笔交易金额异常大…”）。这将提示用户更仔细地检查他们最近的活动。如果未发现异常，登录过程将不受干扰地继续。

*   **功能优势:**
    *   **主动安全:** 及早提醒用户潜在的未授权或异常活动。
    *   **消费意识:** 帮助用户监控与其典型消费习惯的显著偏差。
    *   **增强信任:** 为用户的财务数据提供额外的自动化监督层。