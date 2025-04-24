## 月度年度账单查看
此功能旨在统计月度和年度用户的收入和支出，并通过饼状图进行直观的展示，可以让用户清晰地了解自己在某些方面支出或收入的占比，同时软件会进行相关总结，使数据更加清晰可见。
开发负责人：
郑宇宁：完成基本的功能实现和测试，实现月度和年度收入支出统计和饼状图可视化。
胡杨：进行相关功能的调整和优化。

# 核心功能

收入与支出数据录入：用户可以通过界面输入自己的收入和支出数据，程序会自动记录并保存这些信息，以便后续进行统计和分析。

月度与年度财务统计：系统会根据用户输入的收入和支出数据，自动计算出月度和年度的总收入、总支出，并生成财务报告。

数据可视化（饼状图展示）：通过Swing，系统生成饼状图来展示用户的收入与支出的比例，帮助用户直观了解财务分布。

财务分析与总结：基于收入与支出的统计数据，系统提供财务总结，包括余额计算、总支出各类别比例分析等，帮助用户做出财务决策

# 涉及的相关类

PersonalAccountModel.java、ChartController.java、IncomeExpenseChart.java

# 用户体验
简洁的输入界面：用户通过清晰、简洁的界面输入收入和支出数据，避免复杂的操作步骤，操作直观且方便。
即时反馈和统计结果：一旦用户输入数据并提交，系统会立即计算并展示月度和年度的统计结果，避免长时间等待，增强实时性和互动性。
可视化展示：通过饼状图等图形化展示，用户可以直观地看到收入和支出的比例，这种图形化界面比纯文本报告更易于理解，有助于用户迅速获得关键信息。
易于理解的财务总结：财务报告不仅仅是数字的罗列，还通过可视化和总结性语言帮助用户更好地理解自己的财务状况，从而做出更明智的决策。

## View monthly and annual bills
This function is designed to statistically analyze the income and expenditure of users on a monthly and annual basis, and present it intuitively through a pie chart, allowing users to clearly understand the proportion of their expenditure or income in certain aspects. Meanwhile, the software will conduct relevant summaries to make the data more visible.
Development Manager
Zheng Yuning: Complete the basic functional realization and testing, and achieve monthly and annual income and expenditure statistics as well as pie chart visualization.
Hu Yang: Make adjustments and optimizations to relevant functions.

# Core Functions

Income and expenditure data entry: Users can input their income and expenditure data through the interface. The program will automatically record and save this information for subsequent statistics and analysis.

Monthly and annual financial statistics: The system will automatically calculate the total monthly and annual income and total expenditure based on the income and expenditure data input by users, and generate financial reports.

Data visualization (pie chart display) : Through Swing, the system generates pie charts to show the ratio of users' income to expenditure, helping users intuitively understand the financial distribution.

Financial analysis and summary: Based on the statistics of income and expenditure, the system provides financial summaries, including balance calculation, proportion analysis of various categories of total expenditure, etc., to help users make financial decisions

# Related classes involved

PersonalAccountModel. Java, ChartController. Java, IncomeExpenseChart. Java

# User Experience
Simple input interface: Users can input income and expenditure data through a clear and concise interface, avoiding complex operation steps. The operation is intuitive and convenient.
Immediate feedback and statistical results: Once users input data and submit it, the system will immediately calculate and display the monthly and annual statistical results, avoiding long waiting times and enhancing real-time performance and interactivity.
Visual display: Through graphical displays such as pie charts, users can intuitively see the ratio of income to expenditure. This graphical interface is easier to understand than plain text reports and helps users quickly obtain key information.
Easy-to-understand financial summary: Financial reports are not merely a list of numbers; they also help users better understand their financial situation through visualization and summary language, thereby enabling them to make wiser decisions.
