#模型下载说明

本项目需要以下两个模型文件，因体积过大未纳入 GitHub 仓库，请手动下载并放入指定目录：

链接：https://pan.baidu.com/s/1tFIxDmDdK33rMuXv5c6IHQ 
提取码：bz18

下载后请解压放入项目路径：src/ai_model/  
请确保该文件夹下包含 model.safetensors、config.json、tokenizer_config.json 等文件。

# Folder Structure

The workspace contains two folders by default, where:

- `src`: the folder to maintain sources
- `lib`: the folder to maintain dependencies

Meanwhile, the compiled output files will be generated in the `bin` folder by default.

## 模块详细说明

### `Admin` 包 (管理员模块)

* **`AdminAccountQuery.java`**:
    * 提供管理员查询所有注册用户账户信息的界面 (`JFrame`)。
    * 使用 `JTable` 展示数据，表格内容不可直接编辑。
    * 从 `accounts.csv` 读取并显示用户列表。
* **`AdminModifyService.java`**:
    * 处理管理员修改操作的后台服务逻辑（非界面）。
    * 提供方法：获取账户信息、修改账户状态、更新客户详细信息。
    * 与 `UserRegistrationCSVExporter` 交互以读写 `accounts.csv`。
* **`ModifyCustomerInfoDialog.java`**:
    * 提供管理员修改指定客户信息的弹出对话框 (`JDialog`)。
    * 包含客户信息输入字段和管理员密码验证字段。
    * 验证管理员密码后，调用 `AdminModifyService` 执行更新。
    * 提供 `setAccountInfo` 方法用于预填充客户信息。
* **`AdminSelfInfo.java`** :
    * 用于显示当前登录管理员自身的账户信息。

### `Main` 包 (主程序入口与资源)

* **`App.java`**:
    * 应用程序的启动类 (`JFrame`)。
    * 显示初始界面，包含背景、Logo 和 "Log in" / "Register" 按钮。
    * 点击按钮会打开 `UI.AccountManagementUI`。
* **`background.png`, `log_img.png`**:
    * `App.java` 界面所需的图片资源。

### `model` 包 (数据模型与核心逻辑)

* **`AccountModel.java`**:
    * 所有账户类型的抽象基类，定义通用属性（用户名、密码、余额、状态等）。
    * 提供 `getter/setter` 和 `toCSV` 方法用于数据转换。
* **`AccountStatus.java`**:
    * 定义账户状态的枚举 (`enum`)，如 `ACTIVE`, `FROZEN` 等。
* **`AccountValidator.java`**:
    * 提供账户相关数据验证的工具方法（如 `isEmpty`）。
* **`AdminAccount.java`**:
    * 继承 `AccountModel`，代表管理员类型的账户。
* **`PersonalAccount.java`**:
    * 继承 `AccountModel`，代表个人用户类型的账户。
* **`UserRegistrationCSVExporter.java`**:
    * 负责账户数据与 `accounts.csv` 文件之间的读写操作（持久化）。
    * 处理 CSV 格式、文件编码 (UTF-8)、追加/覆盖写入。
* **`UserSession.java`**:
    * 管理当前登录用户的会话状态（存储当前用户名）。
    * 提供静态方法来获取、设置和清除当前用户名。

### `Person` 包 (个人用户功能模块)

* **`DepositDialog.java`**:
    * 提供存款操作的弹出对话框 (`JDialog`)。
    * 验证密码后，更新 `accounts.csv` 中的余额，并在 `transactions.csv` 中记录存款。
* **`TransactionHistoryDialog.java`**:
    * 提供查看当前用户交易记录的弹出对话框 (`JDialog`)。
    * 从 `transactions.csv` 读取并筛选当前用户的记录，用 `JTable` 显示。
* **`transferAccounts.java`**:
    * 提供转账操作的弹出对话框 (`JDialog`)。
    * 验证转出方密码和余额，查找收款方。
    * 更新双方在 `accounts.csv` 中的余额，并在 `transactions.csv` 中记录转账（两条记录）。
* **`ViewBalanceDialog.java`**:
    * 提供查看当前用户账户余额的简单弹出对话框 (`JDialog`)。
* **`ViewPersonalInfo.java`**:
    * 提供查看当前用户详细个人信息的弹出对话框 (`JDialog`)。
* **`WithdrawalDialog.java`**:
    * 提供取款操作的弹出对话框 (`JDialog`)。
    * 验证密码和余额后，更新 `accounts.csv` 中的余额，并在 `transactions.csv` 中记录取款。

### `UI` 包 (通用UI组件与主界面框架)

* **`AccountManagementUI.java`**:
    * 提供统一的登录和注册界面 (`JDialog`)。
    * 根据模式（登录/注册）显示不同表单。
    * 处理用户输入验证、用户名存在性检查、账户创建和登录逻辑。
    * 登录成功后，根据账户类型打开 `AdminUI` 或 `PersonalUI`，并将用户名存入 `UserSession`。
* **`AdminUI.java`**:
    * 管理员登录后的主界面框架 (`JDialog`)。
    * 包含左侧功能导航栏和右侧内容显示区 (`CardLayout`)。
    * 提供用户查询、修改、删除、交易导入等功能的入口。
    * 直接处理部分文件操作（如删除用户、导入交易）。
* **`PersonalUI.java`**:
    * 个人用户登录后的主界面框架 (`JDialog`)。
    * 包含左侧功能导航栏（带图标）和右侧内容显示区 (`CardLayout`)。
    * 提供查看余额、存款、转账、取款、交易记录、查看个人信息等功能的入口。
    * *注意：当前部分按钮仅切换卡片布局，需要进一步开发以调用 `Person` 包中的相应功能对话框。*
* **`icons/` 文件夹**:
    * 存放界面按钮所需的图标资源。

## 数据存储

* **`accounts.csv`**:
    * 存储所有用户（包括管理员和个人用户）的账户详细信息。
    * 格式：`用户名,密码,手机号,邮箱,性别,地址,创建时间,账号状态,账户类型,金额`
    * 由 `model.UserRegistrationCSVExporter` 类负责读写。
* **`transactions.csv`**:
    * 存储所有的金融交易记录（存款、取款、转账）。
    * 格式：`用户名,操作类型,金额,目标账户,时间`
    * 由 `Person` 包中的各个操作对话框（`DepositDialog`, `WithdrawalDialog`, `transferAccounts`）追加写入。
    * 可由 `AdminUI` 导入外部交易记录。

---
