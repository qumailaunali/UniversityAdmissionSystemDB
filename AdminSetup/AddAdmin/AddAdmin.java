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
        super();
        try {
            connection = DBConnection.getConnection();
        } catch (SQLException e) {
            System.out.println("Database connection failed!");
            e.printStackTrace();
        }
    }

    public String setAdmin(Admins currentAdmin,String email, String password){
        if (currentAdmin == null || !currentAdmin.isSuperAdmin()) {
            return "Only a super admin can add new admins.";
        }
        if (email.isEmpty() || password.isEmpty()) {
            return "All fields are required.";
        }

        if ( email.contains(",") || password.contains(",")) {
            return "Fields cannot contain commas.";
        }

        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.\\w{2,}$")) {
            return "Invalid email address.";
        }

        if (password.length() < 6) {
            return "Password must be at least 6 characters.";
        }

        if (connection == null) {
            return "Database connection not available.";
        }

        String checkQuery = "SELECT AdminID FROM dbo.Admin WHERE Email = ?";
        String insertQuery = "INSERT INTO dbo.Admin (Email, Password, IsSuperAdmin) VALUES (?, ?, ?)";

        try {
            // Check if email already exists
            try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
                checkStmt.setString(1, email);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        return "email already exists.";
                    }
                }
            }

            // Insert new admin (IDs are auto-assigned)
            try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                insertStmt.setString(1, email);
                insertStmt.setString(2, password);
                insertStmt.setBoolean(3, false); // new admins are not super by default
                insertStmt.executeUpdate();
            }

            // Optional: refresh in-memory list for the session
            transferData();

            return "new admin added";

        } catch (SQLException e) {
            System.out.println("Error adding admin to database!");
            e.printStackTrace();
            return "Error adding admin.";
        }

    }

}
