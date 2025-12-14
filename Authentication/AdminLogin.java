package Authentication;

import Database.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class AdminLogin {
    public ArrayList<Admins> adminsArrayList;
    private static Admins currentAdmin;
    private Connection connection;

    public static void setCurrentAdmin(Admins admin) {
        currentAdmin = admin;
    }

    public static Admins getCurrentAdmin() {
        return currentAdmin;
    }

    public AdminLogin() {
        adminsArrayList = new ArrayList<>();
        try {
            connection = DBConnection.getConnection();
            transferData();
        } catch (SQLException e) {
            System.out.println("Database connection failed!");
            e.printStackTrace();
        }
    }

    public boolean login(String email, String password) {
        if (connection == null) {
            System.out.println("Database connection not available!");
            return false;
        }

        String query = "SELECT Email, Password, IsSuperAdmin FROM dbo.Admin WHERE Email = ? AND Password = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Admins admin = new Admins(rs.getString("Email"), rs.getString("Password"));
                    admin.setSuperAdmin(rs.getBoolean("IsSuperAdmin"));
                    setCurrentAdmin(admin);
                    return true;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error validating admin login!");
            e.printStackTrace();
        }

        return false;
    }


    public void saveAdmin() {
        if (connection == null) {
            System.out.println("Database connection not available!");
            return;
        }

        String checkQuery = "SELECT AdminID FROM dbo.Admin WHERE Email = ?";
        String insertQuery = "INSERT INTO dbo.Admin (Email, Password, IsSuperAdmin) VALUES (?, ?, ?)";
        String updateQuery = "UPDATE dbo.Admin SET Password = ?, IsSuperAdmin = ? WHERE Email = ?";

        try {
            for (Admins admin : adminsArrayList) {
                try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
                    checkStmt.setString(1, admin.getEmail());
                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (rs.next()) {
                            try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                                updateStmt.setString(1, admin.getPassword());
                                updateStmt.setBoolean(2, admin.isSuperAdmin());
                                updateStmt.setString(3, admin.getEmail());
                                updateStmt.executeUpdate();
                            }
                        } else {
                            try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                                insertStmt.setString(1, admin.getEmail());
                                insertStmt.setString(2, admin.getPassword());
                                insertStmt.setBoolean(3, admin.isSuperAdmin());
                                insertStmt.executeUpdate();
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error saving admins to database!");
            e.printStackTrace();
        }
    }

    public void transferData() {
        adminsArrayList.clear();

        if (connection == null) {
            System.out.println("Database connection not available!");
            return;
        }

        String query = "SELECT Email, Password, IsSuperAdmin FROM dbo.Admin";

        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Admins admin = new Admins(rs.getString("Email"), rs.getString("Password"));
                admin.setSuperAdmin(rs.getBoolean("IsSuperAdmin"));
                adminsArrayList.add(admin);
            }

        } catch (SQLException e) {
            System.out.println("Error loading admins from database!");
            e.printStackTrace();
        }

        if (adminsArrayList.isEmpty()) {
            Admins superAdmin = new Admins("superadmin@gmail.com", "superpassword");
            superAdmin.setSuperAdmin(true);
            adminsArrayList.add(superAdmin);
            saveAdmin();
            System.out.println("Default super admin created in database.");
        }
    }






}
