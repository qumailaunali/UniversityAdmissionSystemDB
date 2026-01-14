package Applicant;

import Database.DBConnection;
import Authentication.Gender;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// Manages applicant application data and related database operations
// Handles application creation, retrieval, status updates, and validation
public class ApplicantManager {

    // Insert new application form into database and return generated application ID
    // Uses stored procedure to insert new application record
    public static int saveApplication(ApplicationFormData app) {
        String procedureCall = "{call sp_InsertApplication(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";

        try (Connection con = DBConnection.getConnection();
             java.sql.CallableStatement cs = con.prepareCall(procedureCall)) {

            // Bind applicant ID (from Users object or null if not set)
            if (app.getUsers() != null) {
                cs.setInt(1, app.getUsers().getApplicantID());
            } else {
                cs.setNull(1, java.sql.Types.INTEGER);
            }
            cs.setNull(2, java.sql.Types.INTEGER); // AdminID optional (not assigned at creation)
            cs.setString(3, app.getEmail());  // Bind email address
            cs.setString(4, app.getUsers().getFirstName());  // Bind first name
            cs.setString(5, app.getUsers().getLastName());   // Bind last name
            // Convert LocalDate to SQL Date for database storage
            cs.setDate(6, app.getUsers().getDateOfBirth() != null ? java.sql.Date.valueOf(app.getUsers().getDateOfBirth()) : null);
            // Convert Gender enum to string for database
            cs.setString(7, app.getUsers().getGender() != null ? app.getUsers().getGender().name() : null);

            // Bind 12th grade percentage as decimal
            cs.setBigDecimal(8, app.getPercent12() != null && !app.getPercent12().isEmpty() ? new java.math.BigDecimal(app.getPercent12()) : null);
            // Bind 12th grade year as integer
            cs.setObject(9, app.getYear12() != null && !app.getYear12().isEmpty() ? Integer.parseInt(app.getYear12()) : null, java.sql.Types.INTEGER);
            cs.setString(10, app.getStream12());  // Bind 12th stream (Science/Commerce/Arts)

            cs.setString(11, app.getSelectedCollege());  // Bind selected college name
            cs.setNull(12, java.sql.Types.TIMESTAMP);  // test_schedule not set at application creation
            // Bind test score if provided
            cs.setObject(13, app.getTestScore() != null && !app.getTestScore().isEmpty() ? Integer.parseInt(app.getTestScore()) : null, java.sql.Types.INTEGER);
            // Bind application status (default SUBMITTED)
            cs.setString(14, app.getStatus() != null ? app.getStatus().name() : Status.SUBMITTED.name());
            // Bind fee status (default UNPAID)
            cs.setString(15, app.getFeeStatus() != null ? app.getFeeStatus().name() : FeeStatus.UNPAID.name());
            cs.setBoolean(16, app.isSubmitted());  // Bind submission flag
            cs.setBoolean(17, app.isScholarshipSubmitted());  // Bind scholarship form submission flag

            // Resolve program ID from program name and college name via lookup
            Integer programId = null;
            if (app.getSelectedProgram() != null && app.getSelectedCollege() != null) {
                programId = findProgramId(app.getSelectedProgram(), app.getSelectedCollege());
            }
            // Bind program ID (null if not resolved)
            if (programId == null) {
                cs.setNull(18, java.sql.Types.INTEGER);
            } else {
                cs.setInt(18, programId);
            }
            
            // Register output parameter for auto-generated application ID
            cs.registerOutParameter(19, java.sql.Types.INTEGER);
            cs.execute();
            
            // Return the auto-generated application form ID
            return cs.getInt(19);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;  // Return -1 if insertion failed
    }


    // Check if applicant has already applied for the same program using stored procedure
    // Prevents duplicate applications for the same program-applicant combination
    public boolean hasAppliedBefore(ApplicationFormData newApp) {
        // Resolve program ID from program name and college name
        Integer programId = null;
        if (newApp.getSelectedProgram() != null && newApp.getSelectedCollege() != null) {
            programId = findProgramId(newApp.getSelectedProgram(), newApp.getSelectedCollege());
        }
        // Return false if program ID cannot be resolved
        if (programId == null) {
            return false;
        }

        String procedureCall = "{call sp_CheckDuplicateApplication(?, ?, ?)}";
        try (Connection con = DBConnection.getConnection();
             java.sql.CallableStatement cs = con.prepareCall(procedureCall)) {
            // Bind applicant ID (null if not set)
            if (newApp.getUsers() != null) {
                cs.setInt(1, newApp.getUsers().getApplicantID());
            } else {
                cs.setNull(1, java.sql.Types.INTEGER);
            }
            cs.setInt(2, programId);  // Bind program ID for lookup
            cs.registerOutParameter(3, java.sql.Types.BIT);  // Register output parameter
            cs.execute();
            
            // Return true if at least one record found (has applied before)
            return cs.getBoolean(3);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    // Load all applications from database with associated program and user data
    public static ArrayList<ApplicationFormData> loadAllApplications() {
        ArrayList<ApplicationFormData> applications = new ArrayList<>();
        // Use view to fetch all application records with program information
        // vw_AllApplications: Joins ApplicationForm with Program table
        String sql = "SELECT * FROM dbo.vw_AllApplications";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            // Process each application record from database
            while (rs.next()) {
                // Extract all fields from result set
                String applicationId = String.valueOf(rs.getInt("application_form_id"));
                String year12 = rs.getObject("twelfth_year") != null ? rs.getObject("twelfth_year").toString() : null;
                String percent12 = rs.getBigDecimal("twelfth_percentage") != null ? rs.getBigDecimal("twelfth_percentage").toPlainString() : null;
                String stream12 = rs.getString("twelfth_stream");
                String selectedProgramName = rs.getString("ProgramName");
                String selectedCollegeName = rs.getString("university_name");
                String email = rs.getString("email");
                String statusStr = rs.getString("status");
                String testSchedule = rs.getDate("test_schedule") != null ? rs.getDate("test_schedule").toString() : null;
                String testScore = rs.getObject("test_score") != null ? rs.getObject("test_score").toString() : null;
                String feeStatusStr = rs.getString("fee_status");

                // Create Applicant user object with personal information
                Applicant user = new Applicant();
                user.setApplicantID(rs.getInt("ApplicantID"));
                user.setEmail(email);
                user.setFirstName(rs.getString("first_name"));
                user.setLastName(rs.getString("last_name"));
                // Convert SQL Date to LocalDate for date field
                if (rs.getDate("date_of_birth") != null) {
                    user.setDateOfBirth(rs.getDate("date_of_birth").toLocalDate());
                }
                // Convert gender string to Gender enum
                if (rs.getString("gender") != null) {
                    try {
                        user.setGender(Gender.valueOf(rs.getString("gender")));
                    } catch (IllegalArgumentException ignored) {}
                }

                // Create ApplicationFormData with user and academic information
                ApplicationFormData app = new ApplicationFormData(
                        applicationId,
                        user,
                        year12, percent12, stream12,
                        selectedProgramName,
                        selectedCollegeName,
                        email
                );

                // Set test-related fields
                app.setTestSchedule(testSchedule);
                app.setTestScore(testScore);

                // Convert status string to Status enum (with fallback to SUBMITTED)
                try {
                    app.setStatus(Status.valueOf(statusStr != null ? statusStr.toUpperCase() : Status.SUBMITTED.name()));
                } catch (IllegalArgumentException | NullPointerException e) {
                    app.setStatus(Status.SUBMITTED);
                }

                // Convert fee status string to FeeStatus enum (with fallback to UNPAID)
                try {
                    app.setFeeStatus(FeeStatus.valueOf(feeStatusStr != null ? feeStatusStr.toUpperCase() : FeeStatus.UNPAID.name()));
                } catch (IllegalArgumentException | NullPointerException e) {
                    app.setFeeStatus(FeeStatus.UNPAID);
                }

                app.setSubmitted(rs.getBoolean("is_submitted"));
                app.setScholarshipSubmitted(rs.getBoolean("is_scholarship_submitted"));

                applications.add(app);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return applications;
    }


    // Retrieve all applications by applicant email address using view
    public static ArrayList<ApplicationFormData> getApplicationsByUserEmail(String email) {
        ArrayList<ApplicationFormData> apps = new ArrayList<>();
        // Use view with WHERE filter to fetch applications by email
        String sql = "SELECT * FROM dbo.vw_AllApplications WHERE email = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);  // Bind email parameter to filter applications
            try (ResultSet rs = ps.executeQuery()) {
                // Process each application record matching the email
                while (rs.next()) {
                    // Extract all fields from result set
                    String applicationId = String.valueOf(rs.getInt("application_form_id"));
                    String year12 = rs.getObject("twelfth_year") != null ? rs.getObject("twelfth_year").toString() : null;
                    String percent12 = rs.getBigDecimal("twelfth_percentage") != null ? rs.getBigDecimal("twelfth_percentage").toPlainString() : null;
                    String stream12 = rs.getString("twelfth_stream");
                    String selectedProgramName = rs.getString("ProgramName");
                    String selectedCollegeName = rs.getString("university_name");
                    String statusStr = rs.getString("status");
                    String testSchedule = rs.getDate("test_schedule") != null ? rs.getDate("test_schedule").toString() : null;
                    String testScore = rs.getObject("test_score") != null ? rs.getObject("test_score").toString() : null;
                    String feeStatusStr = rs.getString("fee_status");

                    // Create Applicant user object with personal information
                    Applicant user = new Applicant();
                    user.setApplicantID(rs.getInt("ApplicantID"));
                    user.setEmail(email);
                    user.setFirstName(rs.getString("first_name"));
                    user.setLastName(rs.getString("last_name"));
                    // Convert SQL Date to LocalDate
                    if (rs.getDate("date_of_birth") != null) {
                        user.setDateOfBirth(rs.getDate("date_of_birth").toLocalDate());
                    }
                    // Convert gender string to Gender enum
                    if (rs.getString("gender") != null) {
                        try {
                            user.setGender(Gender.valueOf(rs.getString("gender")));
                        } catch (IllegalArgumentException ignored) {}
                    }

                    // Create ApplicationFormData with user and academic information
                    ApplicationFormData app = new ApplicationFormData(
                            applicationId,
                            user,
                            year12, percent12, stream12,
                            selectedProgramName,
                            selectedCollegeName,
                            email
                    );

                    // Set test-related fields
                    app.setTestSchedule(testSchedule);
                    app.setTestScore(testScore);
                    // Convert status string to Status enum (with fallback to SUBMITTED)
                    try {
                        app.setStatus(Status.valueOf(statusStr != null ? statusStr.toUpperCase() : Status.SUBMITTED.name()));
                    } catch (IllegalArgumentException | NullPointerException e) {
                        app.setStatus(Status.SUBMITTED);
                    }
                    // Convert fee status string to FeeStatus enum (with fallback to UNPAID)
                    try {
                        app.setFeeStatus(FeeStatus.valueOf(feeStatusStr != null ? feeStatusStr.toUpperCase() : FeeStatus.UNPAID.name()));
                    } catch (IllegalArgumentException | NullPointerException e) {
                        app.setFeeStatus(FeeStatus.UNPAID);
                    }

                    app.setSubmitted(rs.getBoolean("is_submitted"));
                    app.setScholarshipSubmitted(rs.getBoolean("is_scholarship_submitted"));

                    apps.add(app);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return apps;
    }

    // Retrieve single application by application form ID using view
    public static ApplicationFormData getApplicationByAppId(String id) {
        // Use view with WHERE filter to fetch single application by ID
        String sql = "SELECT * FROM dbo.vw_AllApplications WHERE application_form_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(id));  // Bind application ID parameter
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Extract all fields from result set
                    String applicationId = String.valueOf(rs.getInt("application_form_id"));
                    String year12 = rs.getObject("twelfth_year") != null ? rs.getObject("twelfth_year").toString() : null;
                    String percent12 = rs.getBigDecimal("twelfth_percentage") != null ? rs.getBigDecimal("twelfth_percentage").toPlainString() : null;
                    String stream12 = rs.getString("twelfth_stream");
                    String selectedProgramName = rs.getString("ProgramName");
                    String selectedCollegeName = rs.getString("university_name");
                    String email = rs.getString("email");
                    String statusStr = rs.getString("status");
                    String testSchedule = rs.getDate("test_schedule") != null ? rs.getDate("test_schedule").toString() : null;
                    String testScore = rs.getObject("test_score") != null ? rs.getObject("test_score").toString() : null;
                    String feeStatusStr = rs.getString("fee_status");

                    // Create Applicant user object with personal information
                    Applicant user = new Applicant();
                    user.setApplicantID(rs.getInt("ApplicantID"));
                    user.setEmail(email);
                    user.setFirstName(rs.getString("first_name"));
                    user.setLastName(rs.getString("last_name"));
                    // Convert SQL Date to LocalDate
                    if (rs.getDate("date_of_birth") != null) {
                        user.setDateOfBirth(rs.getDate("date_of_birth").toLocalDate());
                    }
                    // Convert gender string to Gender enum
                    if (rs.getString("gender") != null) {
                        try {
                            user.setGender(Gender.valueOf(rs.getString("gender")));
                        } catch (IllegalArgumentException ignored) {}
                    }

                    // Create ApplicationFormData with user and academic information
                    ApplicationFormData app = new ApplicationFormData(
                            applicationId,
                            user,
                            year12, percent12, stream12,
                            selectedProgramName,
                            selectedCollegeName,
                            email
                    );

                    // Set test-related fields
                    app.setTestSchedule(testSchedule);
                    app.setTestScore(testScore);
                    // Convert status string to Status enum (with fallback to SUBMITTED)
                    try {
                        app.setStatus(Status.valueOf(statusStr != null ? statusStr.toUpperCase() : Status.SUBMITTED.name()));
                    } catch (IllegalArgumentException | NullPointerException e) {
                        app.setStatus(Status.SUBMITTED);
                    }
                    // Convert fee status string to FeeStatus enum (with fallback to UNPAID)
                    try {
                        app.setFeeStatus(FeeStatus.valueOf(feeStatusStr != null ? feeStatusStr.toUpperCase() : FeeStatus.UNPAID.name()));
                    } catch (IllegalArgumentException | NullPointerException e) {
                        app.setFeeStatus(FeeStatus.UNPAID);
                    }

                    app.setSubmitted(rs.getBoolean("is_submitted"));
                    app.setScholarshipSubmitted(rs.getBoolean("is_scholarship_submitted"));

                    return app;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;  // Return null if application not found
    }

    // Update application status to new value using stored procedure
    public static void updateApplicationStatus(String applicationId, Status newStatus) {
        String procedureCall = "{call sp_UpdateApplicationStatusOnly(?, ?)}";
        try (Connection con = DBConnection.getConnection();
             java.sql.CallableStatement cs = con.prepareCall(procedureCall)) {
            cs.setInt(1, Integer.parseInt(applicationId));  // Bind application ID parameter
            cs.setString(2, newStatus.name());              // Bind new status as enum name
            cs.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Update application status and admin ID (called when admin approves/rejects application)
    // Persists both the new status and the admin who made the decision
    public static void updateApplicationStatusWithAdmin(String applicationId, Status newStatus, int adminId) {
        // Use stored procedure to UPDATE both status and AdminID fields for specific application
        String procedureCall = "{call sp_UpdateApplicationStatus(?, ?, ?)}";
        try (Connection con = DBConnection.getConnection();
             java.sql.CallableStatement cs = con.prepareCall(procedureCall)) {
            cs.setInt(1, Integer.parseInt(applicationId));  // Bind application ID parameter
            cs.setString(2, newStatus.name());              // Bind new status as enum name
            cs.setInt(3, adminId);                          // Bind admin ID who made this decision
            cs.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Retrieve all application IDs from database
    public static List<String> getAllApplicantIds() {
        List<String> ids = new ArrayList<>();
        // SQL to fetch all application form IDs, cast to string for consistent return type
        String sql = "SELECT CAST(application_form_id AS VARCHAR(50)) AS id FROM dbo.ApplicationForm";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            // Process each application ID from result set
            while (rs.next()) {
                ids.add(rs.getString("id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ids;
    }

    // Retrieve application status by application ID using stored procedure
    public static Status getApplicationStatus(String applicationId) {
        // Use stored procedure with output parameter to fetch status value
        String procedureCall = "{call sp_GetApplicationStatus(?, ?)}";
        try (Connection con = DBConnection.getConnection();
             java.sql.CallableStatement cs = con.prepareCall(procedureCall)) {
            cs.setInt(1, Integer.parseInt(applicationId));  // Bind application ID parameter
            cs.registerOutParameter(2, java.sql.Types.NVARCHAR);  // Register output parameter for status
            cs.execute();
            
            String statusStr = cs.getString(2);  // Get output parameter value
            if (statusStr != null) {
                // Convert status string to Status enum
                try {
                    return Status.valueOf(statusStr.toUpperCase());
                } catch (IllegalArgumentException ignored) {}
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Status.SUBMITTED;  // Return SUBMITTED as default fallback if not found or error
    }

    // Update test schedule using stored procedure
    // Called when admin sets test date/time via SetTestDatePanel
    public static void updateTestSchedule(String applicationId, java.time.LocalDateTime dateTime) {
        String procedureCall = "{call sp_UpdateTestSchedule(?, ?)}";
        try (Connection con = DBConnection.getConnection();
             java.sql.CallableStatement cs = con.prepareCall(procedureCall)) {
            cs.setInt(1, Integer.parseInt(applicationId));  // Bind application ID parameter
            // Convert LocalDateTime to SQL Timestamp for database storage
            if (dateTime != null) {
                cs.setTimestamp(2, java.sql.Timestamp.valueOf(dateTime));
            } else {
                cs.setNull(2, java.sql.Types.TIMESTAMP);  // Set null if no date provided
            }
            cs.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Retrieve application status by applicant email (gets most recent application)
    public static Status getApplicationStatusByEmail(String email) {
        // SQL to fetch TOP 1 status ordered by application ID descending (most recent first)
        String sql = "SELECT TOP 1 status FROM dbo.ApplicationForm WHERE email = ? ORDER BY application_form_id DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);  // Bind email parameter to filter applications
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getString("status") != null) {
                    // Convert status string to Status enum
                    try {
                        return Status.valueOf(rs.getString("status").toUpperCase());
                    } catch (IllegalArgumentException ignored) {}
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Status.SUBMITTED;  // Return SUBMITTED as default fallback
    }

    // Helper method to resolve program ID from program name and college name
    // Performs lookup via JOIN to find matching program in specific college
    private static Integer findProgramId(String programName, String collegeName) {
        // SQL with JOIN to find ProgramID matching both program name and college name
        String sql = """
            SELECT p.ProgramID
            FROM dbo.Program p
            JOIN dbo.College c ON c.college_id = p.College_ID
            WHERE p.ProgramName = ? AND c.college_name = ?
        """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, programName);  // Bind program name to search for
            ps.setString(2, collegeName);  // Bind college name to filter by
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ProgramID");  // Return the found program ID
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;  // Return null if no matching program found
    }

}  // End of ApplicantManager class
