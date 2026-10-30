ADMIN-ASSISTED PASSWORD RESET

1. Back up your sbsms database in phpMyAdmin (Export > Quick > SQL).
2. Select sbsms in phpMyAdmin, open Import, and import password_reset_requests.sql.
   Or open the file and run its SQL from the database SQL tab.
3. Reopen this project in NetBeans and Clean and Build.
4. Resident: Login > Forgot Password > enter ID No. and Full Name > Submit Request.
5. Admin: open Admin Dashboard > Password Reset Requests. Verify identity outside the app, then Approve or Reject.
6. After approval, select the request and click Set New Password. Password is hashed before saving.

Notes: The admin screen currently records reviewer as ADM001 to match the project demo admin. Replace with the authenticated admin session ID before production. Identity verification is a manual barangay-office step; matching ID and name alone does not prove identity.
