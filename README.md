好的，遵照您的要求，我将把您提供的中文和英文内容分别整理成完整的 README.md 文件格式。

Markdown

# [项目名称]

## 作者

胡杨

## 项目描述

本项目是一个基于 Java Swing 构建的桌面财务管理系统，包含个人用户和管理员用户两类角色。个人用户可以记录收支、查看账单及进行数据分析，管理员用户则负责用户账户的管理。数据主要通过 CSV 文件进行持久化存储。

## 功能特性

### 管理员系统功能

* 用户账户查询
* 用户账户修改
* 删除用户
* 账户数据导入
* 交易数据导入

### 个人用户功能

* 记录收支
* 转账 (转入/转出)
* 查看当前余额
* 查看历史交易明细
* 查看财务统计数据
    * 按选定时间范围（年、月）查看总收入和总支出。
    * 查看特定年份收支随时间变化的趋势图。
    * 查看按类别划分的收支构成分析。
    * 深入查看某年某月的详细财务报告。

### 特定 UI 组件角色

* `PersonalMainPlane.java`: 项目的**主界面**，个人用户登录后的主要操作窗口。
* `PersonalCenterPanel.java`: 主界面中用于**直观显示个人资金账户数据**并进行数据统计整理的面板。
* `IncomeExpenseChart.java` (或重构后的 View/Controller): 负责提供多种类的**账单查询**和**统计图表**展示。

## 已解决的用户故事：个人财务数据查看与分析系列

### 概览

通过提供历史交易明细、多维度统计概览、趋势图表和分类分析功能，我们解决了个人用户难以全面掌握自身财务状况、分析消费习惯以及进行有效预算和规划的问题。

### 用户故事

#### 用户故事 1：查看历史交易明细

**作为**一名个人用户，
**我希望**能够方便地**浏览我的所有历史交易记录的完整列表**，包括每一笔交易的日期、时间、类型（收入/支出）、具体操作、金额、商户/来源、类别、备注等所有详细信息，
**以便**我能够**回顾和验证每一笔钱的流入和流出**，查找特定的消费或收入事件，确保我的记录准确无误。

#### 用户故事 2：获取不同时间范围的收支统计概览

**作为**一名个人用户，
**我希望**能够**灵活地选择任意一个时间范围**（例如：指定某一个完整的年份，或者指定某一年内的某一个具体月份），
**并能快速查看该选定时间范围内的**：
* 总收入合计金额
* 总支出合计金额
**以便**我能够**迅速了解我在特定时期（年度或月度）的整体财务表现**，对我的收入和支出水平有一个宏观的认识。

#### 用户故事 3：可视化年度收支变化趋势

**作为**一名个人用户，
**我希望**能够**选择一个特定的年份**，
**并能看到一张直观的折线图或柱状图**，展示该年度内**每个月**的总收入和总支出金额如何随时间变化的趋势，
**以便**我能够**图形化地分析我的收支 pattern（模式）和周期性**，更容易发现季节性的消费高峰或收入波动，从而更好地规划。

#### 用户故事 4：分析收支的类别构成

**作为**一名个人用户，
**我希望**能够**选择一个特定的时间范围**（例如，当前月、过去一年或任意指定月份/年份），
**并能看到按类别划分的收入和支出统计结果**，最好能以图表（如饼图或柱状图）的形式展示每个类别占总收入或总支出的比例或具体金额，
**以便**我能够**清晰地了解我的钱主要花在了哪些方面**（例如餐饮、交通、娱乐），**以及我的收入主要来源**，这能帮助我识别过度消费的领域或优化我的收入结构。

#### 用户故事 5：深入查看某月详细财务分析

**作为**一名个人用户，
**当我已经选择了查看特定某一年某个月的数据时**（用户故事 2），
**我希望**能够在一个集中的界面上，**同时看到该月的总收入和总支出汇总**，**以及该月按详细类别划分的收入和支出图表分析**（用户故事 4 的月度细化版本），
**以便**我能够**在一个页面内获得对任意一个月的全面、深入的财务回顾**，既了解总额，也明白构成细节，方便进行月度消费总结和反思。

## 使用的技术

* **Java:** 核心编程语言
* **Swing:** 用于构建桌面应用程序的用户界面
* **CSV:** 用于数据持久化存储（账户信息和交易记录）
* **Maven:** 项目构建和依赖管理工具
* **SLF4J:** 日志门面，结合 Logback 等实现日志记录
* **Apache Commons CSV:** 用于更健壮地解析 CSV 文件
* **Java Time API (`java.time`):** 用于现代化的日期和时间处理



## Author

胡杨 (Hu Yang)

## Project Description

This project is a desktop financial management system built with Java Swing, featuring two types of users: personal users and administrator users. Personal users can record income and expenses, view bills, and perform data analysis, while administrator users are responsible for managing user accounts. Data is primarily persisted using CSV files.

## Features

### Administrator System Functions

* User Account Query
* User Account Modification
* Delete Users
* Account Data Import
* Transaction Data Import

### Personal User Functions

* Record Income/Expense
* Transfer (Transfer-in/Transfer-out)
* View Current Balance
* View Historical Transaction Details
* View Financial Statistics
    * View total income and expenditure for a selected time range (year, month).
    * View trend charts of income and expenditure over time for a specific year.
    * View category-based analysis of income and expenditure composition.
    * Delve into detailed financial reports for a specific year and month.

### Specific UI Component Roles

* `PersonalMainPlane.java`: The project's **main interface**, the primary window for personal users after logging in.
* `PersonalCenterPanel.java`: A panel within the main interface used for **intuitive display of personal fund account data** and for data statistics and organization.
* `IncomeExpenseChart.java` (or refactored View/Controller): Responsible for providing multiple types of **bill query options** and **statistical chart** displays.

## Solved User Stories: Personal Financial Data Viewing and Analysis Series

### Overview

By providing historical transaction details, multi-dimensional statistical overviews, trend charts, and classified analysis functions, we have addressed the issue that individual users find it difficult to fully grasp their financial situation, analyze consumption habits, and make effective budgets and plans.

### User Stories

#### User Story 1: View historical transaction details

**As** an individual user,
**I hope** to be able to conveniently **browse the complete list of all my historical transaction records**, including all the detailed information such as the date, time, type (income/expenditure), specific operation, amount, merchant/source, category, remarks, etc., of each transaction,
**So that** I can **review and verify the inflow and outflow of every sum of money**, look for specific consumption or income events, and ensure the accuracy of my records.

#### User Story 2: Obtain an overview of income and expenditure statistics for different time periods

**As** an individual user,
**I hope** to be able to **flexibly choose any time range** (for example: specifying a certain complete year, or specifying a specific month within a certain year),
**And be able to quickly view within the selected time range**:
* Total income amount
* The total amount of expenditure
**So that** I can **quickly understand my overall financial performance during a specific period** (annual or monthly), and have a macroscopic understanding of my income and expenditure levels.

#### User Story 3: Visualize the annual income and expenditure change trend

**As** an individual user,
**I hope** to be able to **choose a specific year**,
**And** an intuitive **line chart or bar chart can be seen**, showing the trend of how the total income and total expenditure amount of **each month** within the year change over time,
**So that** I can **graphically analyze my income and expenditure pattern and cyclicality**, and it is easier to spot seasonal consumption peaks or income fluctuations and plan better.

#### User Story 4: Analyze the category composition of income and expenditure

**As** an individual user,
**I hope** to be able to **choose a specific time range** (for example, the current month, the past year or any specified month/year),
**It is also possible to view the statistics of income and expenditure classified by category**, preferably in the form of a chart (such as a pie chart or bar chart) showing the proportion or specific amount of each category in the total income or total expenditure,
**So that** I can **clearly understand where my money is mainly spent** (such as catering, transportation, entertainment), **and the main sources of my income**, this can help me identify areas of excessive consumption or optimize my income structure.

#### User Story 5: Delve into the detailed financial analysis of a certain month

**As** an individual user,
**When I have chosen to view the data of a specific month of a particular year** (User Story 2),
**I hope** to be able to see the **summary of total income and total expenditure** for the month simultaneously on a centralized interface, **as well as the chart analysis of income and expenditure by detailed categories** for the month (the monthly detailed version of User Story 4),
**So that** I can **obtain a comprehensive and in-depth financial review of any month within one page**, understand both the total amount and the constituent details, and facilitate the monthly consumption summary and reflection.

## Technologies Used

* **Java:** Core programming language
* **Swing:** Used for building the desktop application's user interface
* **CSV:** Used for data persistence (account information and transaction records)
* **Maven:** Project build and dependency management tool
* **SLF4J:** Logging facade, implemented with Logback etc. for logging.
* **Apache Commons CSV:** Used for more robust CSV file parsing.
* **Java Time API (`java.time`):** Used for modern date and time handling.


