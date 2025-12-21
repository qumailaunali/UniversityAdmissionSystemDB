package AdminSetup.AddAdmin;
import Authentication.AdminLogin;
import Authentication.Admins;
import Database.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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

        // SQL Queries: Check if email exists and insert new admin record
        // checkQuery: SELECT AdminID from Admin table where Email matches input
        // (returns 1 record if email exists, 0 if new email)
        String checkQuery = "SELECT AdminID FROM dbo.Admin WHERE Email = ?";
        
        // insertQuery: INSERT new admin with email, password, and super-admin flag
        // IsSuperAdmin defaults to false for new admins (only super admins can create super admins)
        String insertQuery = "INSERT INTO dbo.Admin (Email, Password, IsSuperAdmin) VALUES (?, ?, ?)";

        try {
            // Step 1: Check if email already exists to prevent duplicates
            try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
                checkStmt.setString(1, email);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    // If query returns a result, email already exists
                    if (rs.next()) {
                        return "email already exists.";
                    }
                }
            }
            // Step 2: Insert new admin record into database
            try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                insertStmt.setString(1, email);
                insertStmt.setString(2, password);
                // New admins are not super admin by default (security measure)
                insertStmt.setBoolean(3, false);
                insertStmt.executeUpdate();
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
