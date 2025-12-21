package AdminSetup;

import java.sql.*;
import Database.DBConnection;

// Utility class for managing fee payment status and related database operations
// Provides methods to check if an applicant has paid their application fee
public class PaymentManager {

    // Check if an applicant has paid their application fee by querying fee_status field
    // Returns true if fee_status is PAID, false otherwise or if applicant not found
    public static boolean isFeePaid(String applicantId) {
        // SQL to fetch fee_status for a specific applicant from ApplicationForm table
        String sql = "SELECT fee_status FROM dbo.ApplicationForm WHERE application_form_id = ?";
        
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            // Bind applicant ID parameter to query (convert String to int for database)
            ps.setInt(1, Integer.parseInt(applicantId));
            
            try (ResultSet rs = ps.executeQuery()) {
                // Check if record exists for this applicant
                if (rs.next()) {
                    // Extract fee_status value from database result
                    String feeStatus = rs.getString("fee_status");
                    // Return true if status is PAID (case-insensitive comparison)
                    return feeStatus != null && feeStatus.equalsIgnoreCase("PAID");
                }
            }
        } catch (SQLException e) {
            // Log any database errors encountered during query
            e.printStackTrace();
        }
        
        // Return false if applicant not found, fee status is null, or error occurred
        return false;
    }

}  // End of PaymentManager class
