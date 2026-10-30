# Smart Barangay Service Management System (SBSMS)

## Requirements
- JDK 17 or later
- NetBeans IDE with the Swing AbsoluteLayout library available
- XAMPP with MySQL/MariaDB running
- MySQL Connector/J 9.4.0

## Database setup
1. Start MySQL in XAMPP.
2. In NetBeans, add `lib/mysql-connector-j-9.4.0.jar` to the project (create the `lib` folder and copy the Connector/J JAR there). The project classpath uses this relative path so it is not tied to a specific Windows user folder.
3. Open phpMyAdmin and import `database/sbsms_setup.sql`, or run the script in the SQL tab. The application also attempts to initialize its tables on first database connection.
4. The default local connection is `localhost:3306`, database `sbsms`, user `root`, empty password. If your XAMPP MySQL account uses a password or a different port, update `src/database/DatabaseConnection.java`.
5. Open the project in NetBeans and Clean and Build.

## Demo login accounts
- Administrator: ID `301`, password `admin123!`
- Staff: ID `201`, password `staff123!`
- Resident: register through Sign Up, then wait for the account to be approved.

## Important
The demo administrator and staff credentials are hard-coded for classroom demonstration. Change them before any real deployment. Resident passwords are currently stored as entered; this is not suitable for production use.
