package Authentication;

import Database.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;

public class UserLoginManager {

    public ArrayList<Users> users;
    private Connection connection;

    public UserLoginManager() {
        users = new ArrayList<>();
        try {
            connection = DBConnection.getConnection();
            if (connection != null) {
                transferData();
            }
        } catch (SQLException e) {
            System.out.println("Database connection failed!");
            e.printStackTrace();
        }
    }

    // ================= SAVE USERS =================
    public void saveUsers() {

        if (connection == null) {
            System.out.println("Database connection not available!");
            return;
        }

        try {
            for (Users user : users) {

                // Check if user already exists by Email
                String checkQuery =
                        "SELECT ApplicantID FROM dbo.Applicant WHERE Email = ?";
                PreparedStatement checkStmt =
                        connection.prepareStatement(checkQuery);
                checkStmt.setString(1, user.getEmail());
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    // ========== UPDATE ==========
                    int applicantId = rs.getInt("ApplicantID");

                    String updateQuery = """
                        UPDATE dbo.Applicant
                        SET FirstName = ?, LastName = ?, Password = ?,
                            SecurityQuestion = ?, IDNumber = ?, DOB = ?,
                            Gender = ?, PhoneNumber = ?
                        WHERE ApplicantID = ?
                    """;

                    PreparedStatement updateStmt =
                            connection.prepareStatement(updateQuery);

                    updateStmt.setString(1, user.getFirstName());
                    updateStmt.setString(2, user.getLastName());
                    updateStmt.setString(3, user.getPassword());
                    updateStmt.setString(4, user.getSecurityAnswer());
                    updateStmt.setString(5, user.getCnic());
                    updateStmt.setDate(6,
                            Date.valueOf(user.getDateOfBirth()));
                    updateStmt.setString(7,
                            user.getGender().toString());
                    updateStmt.setString(8, user.getPhone());
                    updateStmt.setInt(9, applicantId);

                    updateStmt.executeUpdate();
                    updateStmt.close();

                } else {
                    // ========== INSERT ==========
                    String insertQuery = """
                        INSERT INTO dbo.Applicant
                        (FirstName, LastName, Email, Password,
                         SecurityQuestion, IDNumber, DOB,
                         Gender, PhoneNumber)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """;

                    PreparedStatement insertStmt =
                            connection.prepareStatement(
                                    insertQuery,
                                    Statement.RETURN_GENERATED_KEYS);

                    insertStmt.setString(1, user.getFirstName());
                    insertStmt.setString(2, user.getLastName());
                    insertStmt.setString(3, user.getEmail());
                    insertStmt.setString(4, user.getPassword());
                    insertStmt.setString(5, user.getSecurityAnswer());
                    insertStmt.setString(6, user.getCnic());
                    insertStmt.setDate(7,
                            Date.valueOf(user.getDateOfBirth()));
                    insertStmt.setString(8,
                            user.getGender().toString());
                    insertStmt.setString(9, user.getPhone());

                    insertStmt.executeUpdate();

                    // Get auto-generated ApplicantID
                    ResultSet keys = insertStmt.getGeneratedKeys();
                    if (keys.next()) {
                        int generatedId = keys.getInt(1);
                        user.setUserID("USER" + generatedId);
                    }

                    insertStmt.close();
                }

                rs.close();
                checkStmt.close();
            }

            System.out.println("Users saved to database successfully!");

        } catch (SQLException e) {
            System.out.println("Error saving users to database!");
            e.printStackTrace();
        }
    }

    // ================= LOAD USERS =================
    public void transferData() {

        if (connection == null) return;

        try {
            String query =
                    "SELECT * FROM dbo.Applicant";

            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            int maxId = 0;

            while (rs.next()) {
                Users user = new Users(
                        rs.getString("FirstName"),
                        rs.getString("LastName"),
                        rs.getString("Email"),
                        rs.getString("Password"),
                        rs.getString("SecurityQuestion"),
                        rs.getString("IDNumber"),
                        rs.getDate("DOB").toLocalDate(),
                        Gender.valueOf(rs.getString("Gender")),
                        rs.getString("PhoneNumber"),
                        "USER" + rs.getInt("ApplicantID")
                );

                users.add(user);

                int id = rs.getInt("ApplicantID");
                if (id >= maxId) {
                    maxId = id + 1;
                }
            }

            Users.idCounter = maxId;

            rs.close();
            stmt.close();

            System.out.println("Users loaded from database successfully!");

        } catch (SQLException e) {
            System.out.println("Error loading users from database!");
            e.printStackTrace();
        }
    }

    // ================= FORGET PASSWORD =================
    public String forgetPassword(String email,
                                 String newPassword,
                                 String securityAnswer) {

        if (connection == null) {
            return "Database connection failed.";
        }

        try {
            String selectQuery =
                    "SELECT ApplicantID, SecurityQuestion FROM dbo.Applicant WHERE Email = ?";

            PreparedStatement selectStmt =
                    connection.prepareStatement(selectQuery);

            selectStmt.setString(1, email);
            ResultSet rs = selectStmt.executeQuery();

            if (rs.next()) {
                String storedAnswer =
                        rs.getString("SecurityQuestion");

                if (!storedAnswer.equals(securityAnswer)) {
                    return "Incorrect security answer.";
                }

                String updateQuery =
                        "UPDATE dbo.Applicant SET Password = ? WHERE Email = ?";

                PreparedStatement updateStmt =
                        connection.prepareStatement(updateQuery);

                updateStmt.setString(1, newPassword);
                updateStmt.setString(2, email);
                updateStmt.executeUpdate();

                // Update memory
                for (Users user : users) {
                    if (user.getEmail().equals(email)) {
                        user.setPassword(newPassword);
                        break;
                    }
                }

                updateStmt.close();
                rs.close();
                selectStmt.close();

                return "Password reset successful!";
            }

            rs.close();
            selectStmt.close();
            return "Email not found.";

        } catch (SQLException e) {
            e.printStackTrace();
            return "Error during password reset.";
        }
    }
}