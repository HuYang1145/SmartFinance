Author
胡杨 (Hu Yang)

Project Description
This project is a desktop financial management system built with Java Swing, featuring two types of users: personal users and administrator users. Personal users can record income and expenses, view bills, and perform data analysis, while administrator users are responsible for managing user accounts. Data is primarily persisted using CSV files.

Features
Administrator System Functions
User Account Query
User Account Modification
Delete Users
Account Data Import
Transaction Data Import
Personal User Functions
Record Income/Expense
Transfer (Transfer-in/Transfer-out)
View Current Balance
View Historical Transaction Details
View Financial Statistics
View total income and expenditure for a selected time range (year, month).
View trend charts of income and expenditure over time for a specific year.
View category-based analysis of income and expenditure composition.
Delve into detailed financial reports for a specific year and month.
Specific UI Component Roles
PersonalMainPlane.java: The project's main interface, the primary window for personal users after logging in.
PersonalCenterPanel.java: A panel within the main interface used for intuitive display of personal fund account data and for data statistics and organization.
IncomeExpenseChart.java (or refactored View/Controller): Responsible for providing multiple types of bill query options and statistical chart displays.
Solved User Stories: Personal Financial Data Viewing and Analysis Series
Overview
By providing historical transaction details, multi-dimensional statistical overviews, trend charts, and classified analysis functions, we have addressed the issue that individual users find it difficult to fully grasp their financial situation, analyze consumption habits, and make effective budgets and plans.

User Stories
User Story 1: View historical transaction details
As an individual user,
I hope to be able to conveniently browse the complete list of all my historical transaction records, including all the detailed information such as the date, time, type (income/expenditure), specific operation, amount, merchant/source, category, remarks, etc., of each transaction,
So that I can review and verify the inflow and outflow of every sum of money, look for specific consumption or income events, and ensure the accuracy of my records.

User Story 2: Obtain an overview of income and expenditure statistics for different time periods
As an individual user,
I hope to be able to flexibly choose any time range (for example: specifying a certain complete year, or specifying a specific month within a certain year),
And be able to quickly view within the selected time range:

Total income amount
The total amount of expenditure So that I can quickly understand my overall financial performance during a specific period (annual or monthly), and have a macroscopic understanding of my income and expenditure levels.
User Story 3: Visualize the annual income and expenditure change trend
As an individual user,
I hope to be able to choose a specific year,
And an intuitive line chart or bar chart can be seen, showing the trend of how the total income and total expenditure amount of each month within the year change over time,
So that I can graphically analyze my income and expenditure pattern and cyclicality, and it is easier to spot seasonal consumption peaks or income fluctuations and plan better.

User Story 4: Analyze the category composition of income and expenditure
As an individual user,
I hope to be able to choose a specific time range (for example, the current month, the past year or any specified month/year),
It is also possible to view the statistics of income and expenditure classified by category, preferably in the form of a chart (such as a pie chart or bar chart) showing the proportion or specific amount of each category in the total income or total expenditure,
So that I can clearly understand where my money is mainly spent (such as catering, transportation, entertainment), and the main sources of my income, this can help me identify areas of excessive consumption or optimize my income structure.

User Story 5: Delve into the detailed financial analysis of a certain month
As an individual user,
When I have chosen to view the data of a specific month of a particular year (User Story 2),
I hope to be able to see the summary of total income and total expenditure for the month simultaneously on a centralized interface, as well as the chart analysis of income and expenditure by detailed categories for the month (the monthly detailed version of User Story 4),
So that I can obtain a comprehensive and in-depth financial review of any month within one page, understand both the total amount and the constituent details, and facilitate the monthly consumption summary and reflection.
