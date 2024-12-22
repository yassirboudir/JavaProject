# Inventory Management System

Welcome to the **Inventory Management System**, a robust solution designed to streamline inventory management processes. This project allows administrators and users to efficiently manage product information in a database while maintaining a secure and user-friendly interface.

Developed as part of a school project for **SupMTI**, this application demonstrates the practical implementation of Java RMI, MySQL database integration, and an object-oriented programming approach to solving real-world problems.

---

## Features

### User Roles
- **Admin Users**: Full control to add, update, and delete products.
- **Regular Users**: View-only access to the product inventory.

### Functionalities
- **View All Products**: Browse the complete inventory with details like name, category, quantity, and price.
- **Add Products** (Admin-only): Add new items to the inventory with ease.
- **Update Products** (Admin-only): Modify product details, such as name, category, quantity, and price.
- **Delete Products** (Admin-only): Remove products that are no longer needed.

### Secure Authentication
- Username and password authentication using a MySQL database to ensure secure access.

### Logging
- Comprehensive logging of user activities (e.g., login, product addition, updates, deletions) stored in a log file (`inventory_operations.log`).

---

## Installation and Setup

Follow these steps to set up and run the application on your system:

### Prerequisites
- **Java**: Ensure Java is installed on your machine (JDK 11 or higher is recommended).
- **MySQL Server**: Set up MySQL to host the database.

---

### Step 1: Setting up the Database

1. **Import the SQL Dump**:  
   The database dump file (`inventory_system.sql`) is included in this package.  
   Run the following commands to set up the database:
   ```bash
   mysql -u root -p
   CREATE DATABASE inventory_system;
   USE inventory_system;
   SOURCE inventory_system.sql;
Update Database Credentials (if needed):
Update the connection credentials in the InventoryClient.java file to match your local MySQL setup:

connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/inventory_system", "your-username", "your-password");

Step 2: Running the Application

Navigate to the rmi folder in the project directory.
Locate the Main file.
Run the project by executing the Main file:
java rmi.Main
Usage Instructions
Launch the application.
Log in using your username and password (admin users have elevated privileges).
Use the menu to navigate through the available options:
View Products
Add Products (Admin only)
Update Products (Admin only)
Delete Products (Admin only)
Exit
Contributors
This project was developed collaboratively by:

Yassir Boudir
Anass El Garti
Ahmed El Hachimi
As part of the engineering program at SupMTI, Rabat.

License
This project is for educational purposes and is not intended for commercial use. All rights reserved by the authors.

Contact
For any questions or issues, feel free to reach out to the contributors via






