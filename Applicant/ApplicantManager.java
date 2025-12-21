package Applicant;

import Database.DBConnection;
import Authentication.Gender;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ApplicantManager {

    // Insert a new application into the database
    public static int saveApplication(ApplicationFormData app) {
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

            if (app.getUsers() != null) {
                ps.setInt(1, app.getUsers().getApplicantID());
            } else {
                ps.setNull(1, java.sql.Types.INTEGER);
            }
            ps.setObject(2, (Object) null); // AdminID optional
            ps.setString(3, app.getEmail());
            ps.setString(4, app.getUsers().getFirstName());
            ps.setString(5, app.getUsers().getLastName());
            ps.setDate(6, app.getUsers().getDateOfBirth() != null ? java.sql.Date.valueOf(app.getUsers().getDateOfBirth()) : null);
            ps.setString(7, app.getUsers().getGender() != null ? app.getUsers().getGender().name() : null);

            ps.setBigDecimal(8, app.getPercent12() != null && !app.getPercent12().isEmpty() ? new java.math.BigDecimal(app.getPercent12()) : null);
            ps.setObject(9, app.getYear12() != null && !app.getYear12().isEmpty() ? Integer.parseInt(app.getYear12()) : null, java.sql.Types.INTEGER);
            ps.setString(10, app.getStream12());

            ps.setString(11, app.getSelectedCollege()); // university_name
            ps.setDate(12, null); // test_schedule not set yet
            ps.setObject(13, app.getTestScore() != null && !app.getTestScore().isEmpty() ? Integer.parseInt(app.getTestScore()) : null, java.sql.Types.INTEGER);
            ps.setString(14, app.getStatus() != null ? app.getStatus().name() : Status.SUBMITTED.name());
            ps.setString(15, app.getFeeStatus() != null ? app.getFeeStatus().name() : FeeStatus.UNPAID.name());
            ps.setBoolean(16, app.isSubmitted());
            ps.setBoolean(17, app.isScholarshipSubmitted());

            // Need programid: resolve from selected program + college
            Integer programId = null;
            if (app.getSelectedProgram() != null && app.getSelectedCollege() != null) {
                programId = findProgramId(app.getSelectedProgram(), app.getSelectedCollege());
            }
            if (programId == null) {
                ps.setNull(18, java.sql.Types.INTEGER);
            } else {
                ps.setInt(18, programId);
            }

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }


    public boolean hasAppliedBefore(ApplicationFormData newApp) {
        String sql = """
            SELECT 1
            FROM dbo.ApplicationForm
            WHERE ApplicantID = ? AND programid = ?
        """;

        Integer programId = null;
        if (newApp.getSelectedProgram() != null && newApp.getSelectedCollege() != null) {
            programId = findProgramId(newApp.getSelectedProgram(), newApp.getSelectedCollege());
        }
        if (programId == null) {
            return false;
        }

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (newApp.getUsers() != null) {
                ps.setInt(1, newApp.getUsers().getApplicantID());
            } else {
                ps.setNull(1, java.sql.Types.INTEGER);
            }
            ps.setInt(2, programId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public static ArrayList<ApplicationFormData> loadAllApplications() {
        ArrayList<ApplicationFormData> applications = new ArrayList<>();
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

            while (rs.next()) {
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

                Applicant user = new Applicant();
                user.setApplicantID(rs.getInt("ApplicantID"));
                user.setEmail(email);
                user.setFirstName(rs.getString("first_name"));
                user.setLastName(rs.getString("last_name"));
                if (rs.getDate("date_of_birth") != null) {
                    user.setDateOfBirth(rs.getDate("date_of_birth").toLocalDate());
                }
                // gender as string
                if (rs.getString("gender") != null) {
                    try {
                        user.setGender(Gender.valueOf(rs.getString("gender")));
                    } catch (IllegalArgumentException ignored) {}
                }

                ApplicationFormData app = new ApplicationFormData(
                        applicationId,
                        user,
                        year12, percent12, stream12,
                        selectedProgramName,
                        selectedCollegeName,
                        email
                );

                app.setTestSchedule(testSchedule);
                app.setTestScore(testScore);

                try {
                    app.setStatus(Status.valueOf(statusStr != null ? statusStr.toUpperCase() : Status.SUBMITTED.name()));
                } catch (IllegalArgumentException | NullPointerException e) {
                    app.setStatus(Status.SUBMITTED);
                }

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


    public static ArrayList<ApplicationFormData> getApplicationsByUserEmail(String email) {
        ArrayList<ApplicationFormData> apps = new ArrayList<>();
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
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
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

                    Applicant user = new Applicant();
                    user.setApplicantID(rs.getInt("ApplicantID"));
                    user.setEmail(email);
                    user.setFirstName(rs.getString("first_name"));
                    user.setLastName(rs.getString("last_name"));
                    if (rs.getDate("date_of_birth") != null) {
                        user.setDateOfBirth(rs.getDate("date_of_birth").toLocalDate());
                    }
                    if (rs.getString("gender") != null) {
                        try {
                            user.setGender(Gender.valueOf(rs.getString("gender")));
                        } catch (IllegalArgumentException ignored) {}
                    }

                    ApplicationFormData app = new ApplicationFormData(
                            applicationId,
                            user,
                            year12, percent12, stream12,
                            selectedProgramName,
                            selectedCollegeName,
                            email
                    );

                    app.setTestSchedule(testSchedule);
                    app.setTestScore(testScore);
                    try {
                        app.setStatus(Status.valueOf(statusStr != null ? statusStr.toUpperCase() : Status.SUBMITTED.name()));
                    } catch (IllegalArgumentException | NullPointerException e) {
                        app.setStatus(Status.SUBMITTED);
                    }
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
    public static ApplicationFormData getApplicationByAppId(String id) {
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
            ps.setInt(1, Integer.parseInt(id));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
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

                    Applicant user = new Applicant();
                    user.setApplicantID(rs.getInt("ApplicantID"));
                    user.setEmail(email);
                    user.setFirstName(rs.getString("first_name"));
                    user.setLastName(rs.getString("last_name"));
                    if (rs.getDate("date_of_birth") != null) {
                        user.setDateOfBirth(rs.getDate("date_of_birth").toLocalDate());
                    }
                    if (rs.getString("gender") != null) {
                        try {
                            user.setGender(Gender.valueOf(rs.getString("gender")));
                        } catch (IllegalArgumentException ignored) {}
                    }

                    ApplicationFormData app = new ApplicationFormData(
                            applicationId,
                            user,
                            year12, percent12, stream12,
                            selectedProgramName,
                            selectedCollegeName,
                            email
                    );

                    app.setTestSchedule(testSchedule);
                    app.setTestScore(testScore);
                    try {
                        app.setStatus(Status.valueOf(statusStr != null ? statusStr.toUpperCase() : Status.SUBMITTED.name()));
                    } catch (IllegalArgumentException | NullPointerException e) {
                        app.setStatus(Status.SUBMITTED);
                    }
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
        return null; // Not found
    }

    public static void updateApplicationStatus(String applicationId, Status newStatus) {
        String sql = "UPDATE dbo.ApplicationForm SET status = ? WHERE application_form_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newStatus.name());
            ps.setInt(2, Integer.parseInt(applicationId));
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<String> getAllApplicantIds() {
        List<String> ids = new ArrayList<>();
        String sql = "SELECT CAST(application_form_id AS VARCHAR(50)) AS id FROM dbo.ApplicationForm";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ids.add(rs.getString("id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ids;
    }

    public static Status getApplicationStatus(String applicationId) {
        String sql = "SELECT status FROM dbo.ApplicationForm WHERE application_form_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(applicationId));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getString("status") != null) {
                    try {
                        return Status.valueOf(rs.getString("status").toUpperCase());
                    } catch (IllegalArgumentException ignored) {}
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Status.SUBMITTED; // Default fallback
    }

    // Persist test schedule on ApplicationForm for admin visibility
    public static void updateTestSchedule(String applicationId, java.time.LocalDateTime dateTime) {
        String sql = "UPDATE dbo.ApplicationForm SET test_schedule = ? WHERE application_form_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (dateTime != null) {
                ps.setTimestamp(1, java.sql.Timestamp.valueOf(dateTime));
            } else {
                ps.setNull(1, java.sql.Types.TIMESTAMP);
            }
            ps.setInt(2, Integer.parseInt(applicationId));
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Status getApplicationStatusByEmail(String email) {
        String sql = "SELECT TOP 1 status FROM dbo.ApplicationForm WHERE email = ? ORDER BY application_form_id DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getString("status") != null) {
                    try {
                        return Status.valueOf(rs.getString("status").toUpperCase());
                    } catch (IllegalArgumentException ignored) {}
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Status.SUBMITTED;
    }

    // Helper to resolve program id from names
    private static Integer findProgramId(String programName, String collegeName) {
        String sql = """
            SELECT p.ProgramID
            FROM dbo.Program p
            JOIN dbo.College c ON c.college_id = p.College_ID
            WHERE p.ProgramName = ? AND c.college_name = ?
        """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, programName);
            ps.setString(2, collegeName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ProgramID");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }




}
