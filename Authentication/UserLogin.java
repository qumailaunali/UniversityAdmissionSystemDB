package Authentication;

import Database.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserLogin extends UserLoginManager {
    private Connection connection;

    public UserLogin() {
        super();
        try {
            connection = DBConnection.getConnection();
        } catch (SQLException e) {
            System.out.println("Database connection failed!");
            e.printStackTrace();
        }
    }

    public Users login(String email, String pass) {
        try {
            if (connection == null) {
                System.out.println("Database connection not available!");
                return null;
            }

            // Query database for user credentials
            String query = "SELECT ApplicantID, FirstName, LastName, Email, Password, SecurityQuestion, IDNumber, DOB, Gender, PhoneNumber FROM dbo.Applicant WHERE Email = ? AND Password = ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setString(1, email);
            pstmt.setString(2, pass);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // User found - create Users object with database data
                Users user = new Users(
                        rs.getString("FirstName"),
                        rs.getString("LastName"),
                        rs.getString("Email"),
                        rs.getString("Password"),
                        rs.getString("SecurityQuestion"),
                        rs.getString("IDNumber"),
                        rs.getDate("DOB") != null ? rs.getDate("DOB").toLocalDate() : null,
                        Gender.valueOf(rs.getString("Gender")),
                        rs.getString("PhoneNumber"),
                        "USER" + rs.getInt("ApplicantID")
                );
                rs.close();
                pstmt.close();
                return user;
            } else {
                rs.close();
                pstmt.close();
                return null;
            }

        } catch (SQLException e) {
            System.out.println("Error during login: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        new LoginFrame();
    }
}
