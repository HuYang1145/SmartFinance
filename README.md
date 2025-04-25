# Folder Structure
## 程序运行指令
在命令行输入java -jar target/group19-1.0.0-shaded.jar运行程序
## Program operation instructions
Run the program by typing java -jar target/group19-1.0.0-shaded.jar in the command line
## AI模型文件
通过网盘分享的文件：dist.zip
链接: https://pan.baidu.com/s/1M3x8FVq7p5cE0sdAx2x9UA 提取码: pgpp
下载解压后放入coursework目录下，与src同级。
## AI Model File
Shared file via cloud disk: dist.zip
Link: https://pan.baidu.com/s/1M3x8FVq7p5cE0sdAx2x9UA
Extraction code: pgpp
After downloading and decompressing, place it in the coursework directory at the same level as src.
---



## 任务分配
## Task Allocation

* **陈静怡**：
* 实现金融建议功能，该功能能够读取用户过去收支情况给出智能定制预算建议，并有多种建议模式系统智能选择。
    * 创立`user_budget.csv`，以记录用户自定义预算金额。
    * 编写`BudgetGoalDialog.java`，为个人用户提供管理预算目标的界面。
    * 编写`BudgetAdvisor.java`，包含预算建议的核心业务逻辑，读取`transactions.csv`，建立分配预算模式。
* `PersonalUI.java`部分功能建立及更改，增加了Financial Suggestion部分的相关界面。
* `transactions.csv`部分数据提供，完善用户金融情况更加丰富。

* **Chen Jingyi**:
* Implement the financial advice function, which can read the user's past income and expenditure to provide intelligent customized budget suggestions, and the system can intelligently select from multiple suggestion modes.
* Create `user_budget.csv` to record the user-defined budget amounts.
* Write `BudgetGoalDialog.java` to provide an interface for individual users to manage their budget goals.
* Write `BudgetAdvisor.java`, which contains the core business logic of budget advice, reads `transactions.csv`, and establishes budget allocation modes.
* Establish and modify some functions of `PersonalUI.java`, adding the relevant interface for the Financial Suggestion section.
* Provide some data for `transactions.csv` to enrich the user's financial situation.* Write `BudgetAdvisor.java`, which contains the core business logic of budget advice, reads `transactions.csv`, and establishes budget allocation modes.



* **罗雅琦**：
* 训练专属于此项目的ai 对话用户意图识别模型自己编写预测脚本，部署为exe 文件
    * 将此模型与`deepseek API`结合使用，共同服务于ai 对话功能
    * 编写 `IntentResult.java`,`AIPanel.java`实现ai 对话功能，该功能可以根据用户输入识别用户意图，调用不同功能或调用deepseek api 回答用户问题。
* 编写 `AdminUI`， `AccountManagementUI`， `PersonalUI`， `Deposit`， `Pay`， `Withdrawal`， `ViewBalance`部分功能建立及更改，编写主要ui ，添加多种图片与图标美化用户交互界面。
* 提供UI需要的图标与图片

* **Luo Yaqi**：
* Develop a prediction script for the AI dialogue user intent recognition model specifically for this project and deploy it as an executable file.
    * Integrate this model with the `deepseek API` to jointly serve the AI dialogue function.
    * Implement the AI dialogue function by writing `IntentResult.java` and `AIPanel.java`. This function can recognize user intents based on user input, call different functions or invoke the `deepseek API` to answer user questions.
* Establish and modify the functions of `AdminUI`, `AccountManagementUI`, `PersonalUI`, `Deposit`, `Pay`, `Withdrawal`, and `ViewBalance`, and write the main UI. Add various images and icons to beautify the user interaction interface.
* Provide the icons and images needed for the UI.



* **范耘豪**：
* 开发了生成周期性报告的新功能。该功能包括：
    * 允许用户设置自定义报告周期（以天为单位）。
    * 使用折线图清晰显示选定周期内的收入和支出金额。
    * 创建了 `ReportCycleSettingsPanel.java`, `ReportOptionsPanel.java`, `ReportViewPanel.java` 文件，用于实现报告的设置和视图界面。
    * 创建了 `TransactionService.java` 文件，用于处理交易相关服务。
    * 创建了 `IncomeDialog.java` 和 `ExpenseDialog.java` 文件，用于处理收入和支出的对话框界面。
* 原型对齐修改：更新了几个关键组件以符合提供的原型设计。这些修改包括：
    * 改进和调整了个人用户界面 (`PersonalUI.java`)。
    * 优化了登录界面。
    * 修改了提款功能。
    * 确保了应用程序的外观和核心交互与原型规范一致。
* 更新了管理员模块 (`AdminUI.java`) 中读取 `transactions.csv` 文件的方法，以确保能正确解析文件修改后格式的数据。修改了 `AdminUI.java` 和 `transactions.csv` 文件。
* 在 `main` 分支中执行了常规的代码集成和维护任务。这项持续的工作确保了发顺利正常进行。

* **Fan Yunhao**:
* Developed a new feature for generating periodic reports. This feature includes:
    * Allowing users to set custom report periods (in days).
    * Using line charts to clearly display the income and expenditure amounts within the selected period.
    * Created `ReportCycleSettingsPanel.java`, `ReportOptionsPanel.java`, and `ReportViewPanel.java` files to implement the settings and view interfaces for the reports.
    * Created `TransactionService.java` file to handle transaction-related services.
    * Created `IncomeDialog.java` and `ExpenseDialog.java` files to handle the dialog interfaces for income and expenses.
* Prototype alignment modification: Updated several key components to conform to the provided prototype design. These modifications include:
    * Improving and adjusting the personal user interface (`PersonalUI.java`).
    * Optimizing the login interface.
    * Modifying the withdrawal function.
    * Ensuring that the appearance and core interactions of the application are consistent with the prototype specifications.
* Updated the method for reading the `transactions.csv` file in the administrator module (`AdminUI.java`) to ensure that it can correctly parse the data in the modified format. Modified `AdminUI.java` and `transactions.csv` files.
* Performed regular code integration and maintenance tasks in the `main` branch. This ongoing work ensures the smooth and normal progress of the release.



* **胡杨**：
* 写管理员系统的所有功能，写用户个人信息查询，转入转出转账，查看余额
    * `PersonalCenterPanel.java`个人中心资金账户数据的直观显示以及数据的统计整理
    * `PersonalMainPlane.java`，主界面
    * `IncomeExpenseChart.java`提供多个种类的账单查询选择
    * 所有与`Admin`的java代码：管理员系统

* **Hu Yang**:
* Describe all the functions of the administrator system, including user personal information query, transfer-in, transfer-out and transfer, and balance viewing.
    * `PersonalCenterPanel.java` provides intuitive display of personal center fund account data and data statistics and organization.
    * `PersonalMainPlane.java`, main interface.
    * `IncomeExpenseChart.java` offers multiple types of bill query options.
    * All Java codes related to `Admin`: administrator system.



* **臧璐**：
* 实现检测异常交易功能，如用户的以往交易有风险，登录时会增加一道非阻塞性的风险提示，帮助用户及时关注大额资金变动。
    * 创立`TransactionChecker.java`，定义检查逻辑。
    * 编写`AccountManagementUI.java`，在登录时触发检查并显示提醒。
    * 创立`TransactionModel.java`，定义交易数据结构。
    * 编写`AccountModel.java`，将交易与账户关联。
* 编写 `transactions.csv` 作为异常交易数据源，编写`UserRegistrationCSVExporter.java` 来读取账户信息。

* **Zang Lu**:
* Implement the function for detecting abnormal transactions. If there is a risk in the user's previous transactions, a non-blocking risk prompt will be added during login to help users promptly notice any significant changes in their funds.
    * Create `TransactionChecker.java` to define the checking logic.
    * Write `AccountManagementUI.java` to trigger the check and display the reminder when logging in.
    * Create `TransactionModel.java` to define the data structure for transactions.
    * Write `AccountModel.java` to associate transactions with accounts.
* Write `transactions.csv` as the data source for abnormal transactions and write `UserRegistrationCSVExporter.java` to read account information.



* **郑宇宁**：
* 实现账单分类功能，该功能能够查看账户的支出分类，并用饼状图展示
    * 编写`IncomeExpenseChart.java`，生成支出的分类。
* `PersonalUI.java`部分功能建立，增加了部分相关界面。
* `transaction.csv`，`accounts.csv`的部分完善。

* **Zheng Yuning**:
* Achieve the bill classification function, which enables viewing the expenditure classification of the account and presenting it through a pie chart.
    * Write `IncomeExpenseChart.java` to generate the classification of expenditures.
* Complete the functions in `PersonalUI.java`, adding some related interfaces.
* Improve the parts of `transaction.csv` and `accounts.csv`.
