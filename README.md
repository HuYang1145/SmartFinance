# coursework

**Name:** 胡杨
**Student ID:** 2022213111

## Features Implemented:

### User Authentication & Registration System:

* **User Registration:** Provides a user registration interface, supporting the registration of Administrator Accounts and Ordinary User Accounts.
* **Registration Information & Validation:** The registration process includes detailed personal information input and necessary validity validation.
* **User Login:** Implements user login authentication, verifying user identity and directing to the corresponding functional interface.

### Ordinary User Module:

* **View Balance:** Users can query their current account balance.
* **Deposit:** Supports depositing funds into the user's account.
* **Withdrawal:** Supports withdrawing funds from the user's account.
* **Transfer / Pay:** Provides the function to transfer funds to specified other accounts.
* **View Personal Info:** Allows users to view and manage their detailed personal information.
* **Transaction History:** Provides a complete account transaction history query function.
* **Logout:** Supports users safely logging out of the current session.

### Administrator Module:

* **Administrator Information Management:** View and manage the administrator's own personal account information.
* **Customer Account Management:** Provides comprehensive management functions for ordinary user accounts:
    * **Customer Information Inquiry:** Can query all registered ordinary customers' detailed personal information.
    * **Modify Customer Account Information:** Can modify all customer information except the username. Has the authority to change account status, for example, can change the status of abnormal accounts from 'ACTIVE' to 'FROZEN' to restrict or prevent their use.
    * **Delete Customer Information:** Supports deleting specified ordinary user accounts.
* **Data Import:** Supports batch data processing by importing CSV format files:
    * **Import Customer Accounts:** Supports batch input of ordinary user account information.
    * **Import Transaction Records:** Supports batch input of transaction records.
* **Logout:** Supports administrators safely logging out of the current session.
