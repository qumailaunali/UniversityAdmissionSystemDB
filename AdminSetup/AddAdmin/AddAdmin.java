package AdminSetup.AddAdmin;
import Authentication.AdminLogin;
import Authentication.Admins;
import Database.DBConnection;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

public class AddAdmin extends AdminLogin {

    private Connection connection;

    public AddAdmin() {
        // Initialize connection to database (inherited from AdminLogin)
        super();
        try {
            connection = DBConnection.getConnection();
        } catch (SQLException e) {
            System.out.println("Database connection failed!");
            e.printStackTrace();
        }
    }

    public String setAdmin(Admins currentAdmin,String email, String password){
        // Verify current user is a super admin (only super admins can add new admins)
        if (currentAdmin == null || !currentAdmin.isSuperAdmin()) {
            return "Only a super admin can add new admins.";
        }
        // Validate required fields are not empty
        if (email.isEmpty() || password.isEmpty()) {
            return "All fields are required.";
        }

        // Check for invalid characters (commas not allowed)
        if ( email.contains(",") || password.contains(",")) {
            return "Fields cannot contain commas.";
        }
        // Validate email format using regex pattern
        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.\\w{2,}$")) {
            return "Invalid email address.";
        }

        // Enforce minimum password length
        if (password.length() < 6) {
            return "Password must be at least 6 characters.";
        }
        // Verify database connection is available
        if (connection == null) {
            return "Database connection not available.";
        }

        try {
            // Step 1: Check if email already exists using stored procedure sp_CheckAdminEmailExists
            try (CallableStatement checkStmt = connection.prepareCall("{call sp_CheckAdminEmailExists(?, ?)}")) {
                checkStmt.setString(1, email);
                checkStmt.registerOutParameter(2, Types.BIT);
                checkStmt.execute();
                
                // If output parameter is 1 (true), email already exists
                if (checkStmt.getBoolean(2)) {
                    return "email already exists.";
                }
            }
            
            // Step 2: Insert new admin record using stored procedure sp_InsertNewAdmin
            try (CallableStatement insertStmt = connection.prepareCall("{call sp_InsertNewAdmin(?, ?, ?)}")) {
                insertStmt.setString(1, email);
                insertStmt.setString(2, password);
                // New admins are not super admin by default (security measure)
                insertStmt.setBoolean(3, false);
                insertStmt.execute();
            }
            
            // Step 3: Refresh in-memory admin list from database
            transferData();
            return "new admin added";

        } catch (SQLException e) {
            System.out.println("Error adding admin to database!");
            e.printStackTrace();
            return "Error adding admin.";
        }

    }

}
