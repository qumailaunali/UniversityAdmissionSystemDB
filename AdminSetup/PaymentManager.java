package AdminSetup;

import java.sql.*;
import Database.DBConnection;

public class PaymentManager {

    public static boolean isFeePaid(String applicantId) {
        String sql = "SELECT fee_status FROM dbo.ApplicationForm WHERE application_form_id = ?";
        
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, Integer.parseInt(applicantId));
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String feeStatus = rs.getString("fee_status");
                    return feeStatus != null && feeStatus.equalsIgnoreCase("PAID");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }

}
