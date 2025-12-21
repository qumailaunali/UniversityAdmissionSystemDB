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
    // Maps ApplicationFormData to database columns and resolves program ID from college/program names
    public static int saveApplication(ApplicationFormData app) {
        // SQL to INSERT new application record with all applicant and academic information
        String sql = """
            INSERT INTO dbo.ApplicationForm (
                ApplicantID, AdminID, email, first_name, last_name, date_of_birth, gender,
                twelfth_percentage, twelfth_year, twelfth_stream,
                university_name, test_schedule, test_score, status, fee_status,
                is_submitted, is_scholarship_submitted, programid
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            // Bind applicant ID (from Users object or null if not set)
            if (app.getUsers() != null) {
                ps.setInt(1, app.getUsers().getApplicantID());
            } else {
                ps.setNull(1, java.sql.Types.INTEGER);
            }
            ps.setObject(2, (Object) null); // AdminID optional (not assigned at creation)
            ps.setString(3, app.getEmail());  // Bind email address
            ps.setString(4, app.getUsers().getFirstName());  // Bind first name
            ps.setString(5, app.getUsers().getLastName());   // Bind last name
            // Convert LocalDate to SQL Date for database storage
            ps.setDate(6, app.getUsers().getDateOfBirth() != null ? java.sql.Date.valueOf(app.getUsers().getDateOfBirth()) : null);
            // Convert Gender enum to string for database
            ps.setString(7, app.getUsers().getGender() != null ? app.getUsers().getGender().name() : null);

            // Bind 12th grade percentage as decimal
            ps.setBigDecimal(8, app.getPercent12() != null && !app.getPercent12().isEmpty() ? new java.math.BigDecimal(app.getPercent12()) : null);
            // Bind 12th grade year as integer
            ps.setObject(9, app.getYear12() != null && !app.getYear12().isEmpty() ? Integer.parseInt(app.getYear12()) : null, java.sql.Types.INTEGER);
            ps.setString(10, app.getStream12());  // Bind 12th stream (Science/Commerce/Arts)

            ps.setString(11, app.getSelectedCollege());  // Bind selected college name
            ps.setDate(12, null);  // test_schedule not set at application creation
            // Bind test score if provided
            ps.setObject(13, app.getTestScore() != null && !app.getTestScore().isEmpty() ? Integer.parseInt(app.getTestScore()) : null, java.sql.Types.INTEGER);
            // Bind application status (default SUBMITTED)
            ps.setString(14, app.getStatus() != null ? app.getStatus().name() : Status.SUBMITTED.name());
            // Bind fee status (default UNPAID)
            ps.setString(15, app.getFeeStatus() != null ? app.getFeeStatus().name() : FeeStatus.UNPAID.name());
            ps.setBoolean(16, app.isSubmitted());  // Bind submission flag
            ps.setBoolean(17, app.isScholarshipSubmitted());  // Bind scholarship form submission flag

            // Resolve program ID from program name and college name via lookup
            Integer programId = null;
            if (app.getSelectedProgram() != null && app.getSelectedCollege() != null) {
                programId = findProgramId(app.getSelectedProgram(), app.getSelectedCollege());
            }
            // Bind program ID (null if not resolved)
            if (programId == null) {
                ps.setNull(18, java.sql.Types.INTEGER);
            } else {
                ps.setInt(18, programId);
            }

            ps.executeUpdate();
            // Retrieve and return the auto-generated application form ID
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;  // Return -1 if insertion failed
    }


    // Check if applicant has already applied for the same program
    // Prevents duplicate applications for the same program-applicant combination
    public boolean hasAppliedBefore(ApplicationFormData newApp) {
        // SQL to check if record exists with matching ApplicantID and ProgramID
        String sql = """
            SELECT 1
            FROM dbo.ApplicationForm
            WHERE ApplicantID = ? AND programid = ?
        """;

        // Resolve program ID from program name and college name
        Integer programId = null;
        if (newApp.getSelectedProgram() != null && newApp.getSelectedCollege() != null) {
            programId = findProgramId(newApp.getSelectedProgram(), newApp.getSelectedCollege());
        }
        // Return false if program ID cannot be resolved
        if (programId == null) {
            return false;
        }

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            // Bind applicant ID (null if not set)
            if (newApp.getUsers() != null) {
                ps.setInt(1, newApp.getUsers().getApplicantID());
            } else {
                ps.setNull(1, java.sql.Types.INTEGER);
            }
            ps.setInt(2, programId);  // Bind program ID for lookup
            try (ResultSet rs = ps.executeQuery()) {
                // Return true if at least one record found (has applied before)
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    // Load all applications from database with associated program and user data
    public static ArrayList<ApplicationFormData> loadAllApplications() {
        ArrayList<ApplicationFormData> applications = new ArrayList<>();
        // SQL to fetch all application records with LEFT JOIN to Program table to get program names
        String sql = """
            SELECT af.application_form_id, af.ApplicantID, af.email, af.first_name, af.last_name, af.date_of_birth, af.gender,
                   af.twelfth_percentage, af.twelfth_year, af.twelfth_stream, af.university_name, af.test_schedule,
                   af.test_score, af.status, af.fee_status, af.is_submitted, af.is_scholarship_submitted, af.programid,
                   p.ProgramName
            FROM dbo.ApplicationForm af
            LEFT JOIN dbo.Program p ON af.programid = p.ProgramID
        """;

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


    // Retrieve all applications by applicant email address
    public static ArrayList<ApplicationFormData> getApplicationsByUserEmail(String email) {
        ArrayList<ApplicationFormData> apps = new ArrayList<>();
        // SQL to fetch applications filtered by email with LEFT JOIN to Program table
        String sql = """
            SELECT af.application_form_id, af.ApplicantID, af.email, af.first_name, af.last_name, af.date_of_birth, af.gender,
                   af.twelfth_percentage, af.twelfth_year, af.twelfth_stream, af.university_name, af.test_schedule,
                   af.test_score, af.status, af.fee_status, af.is_submitted, af.is_scholarship_submitted, af.programid,
                   p.ProgramName
            FROM dbo.ApplicationForm af
            LEFT JOIN dbo.Program p ON af.programid = p.ProgramID
            WHERE af.email = ?
        """;

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

    // Retrieve single application by application form ID
    public static ApplicationFormData getApplicationByAppId(String id) {
        // SQL to fetch single application record by ID with LEFT JOIN to Program table
        String sql = """
            SELECT af.application_form_id, af.ApplicantID, af.email, af.first_name, af.last_name, af.date_of_birth, af.gender,
                   af.twelfth_percentage, af.twelfth_year, af.twelfth_stream, af.university_name, af.test_schedule,
                   af.test_score, af.status, af.fee_status, af.is_submitted, af.is_scholarship_submitted, af.programid,
                   p.ProgramName
            FROM dbo.ApplicationForm af
            LEFT JOIN dbo.Program p ON af.programid = p.ProgramID
            WHERE af.application_form_id = ?
        """;

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

    // Update application status to new value in database
    public static void updateApplicationStatus(String applicationId, Status newStatus) {
        // SQL to UPDATE status field for specific application
        String sql = "UPDATE dbo.ApplicationForm SET status = ? WHERE application_form_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newStatus.name());        // Bind new status as enum name
            ps.setInt(2, Integer.parseInt(applicationId));  // Bind application ID for WHERE clause
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Update application status and admin ID (called when admin approves/rejects application)
    // Persists both the new status and the admin who made the decision
    public static void updateApplicationStatusWithAdmin(String applicationId, Status newStatus, int adminId) {
        // SQL to UPDATE both status and AdminID fields for specific application
        String sql = "UPDATE dbo.ApplicationForm SET status = ?, AdminID = ? WHERE application_form_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newStatus.name());        // Bind new status as enum name
            ps.setInt(2, adminId);                    // Bind admin ID who made this decision
            ps.setInt(3, Integer.parseInt(applicationId));  // Bind application ID for WHERE clause
            ps.executeUpdate();
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

    // Retrieve application status by application ID
    public static Status getApplicationStatus(String applicationId) {
        // SQL to fetch status value for specific application
        String sql = "SELECT status FROM dbo.ApplicationForm WHERE application_form_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(applicationId));  // Bind application ID parameter
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
        return Status.SUBMITTED;  // Return SUBMITTED as default fallback if not found or error
    }

    // Persist test schedule date/time on ApplicationForm for admin visibility
    // Called when admin sets test date/time via SetTestDatePanel
    public static void updateTestSchedule(String applicationId, java.time.LocalDateTime dateTime) {
        // SQL to UPDATE test_schedule field with date/time value
        String sql = "UPDATE dbo.ApplicationForm SET test_schedule = ? WHERE application_form_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            // Convert LocalDateTime to SQL Timestamp for database storage
            if (dateTime != null) {
                ps.setTimestamp(1, java.sql.Timestamp.valueOf(dateTime));
            } else {
                ps.setNull(1, java.sql.Types.TIMESTAMP);  // Set null if no date provided
            }
            ps.setInt(2, Integer.parseInt(applicationId));  // Bind application ID for WHERE clause
            ps.executeUpdate();
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
