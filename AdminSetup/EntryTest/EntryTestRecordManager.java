package AdminSetup.EntryTest;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import Applicant.Status;
import Database.DBConnection;

// Manages entry test records and related database operations
// Handles persistence, retrieval, and subject tracking for applicant entry tests
public class EntryTestRecordManager {

    // Inner class representing a single entry test record with all metadata and test results
    public static class EntryTestRecord {
        // Test record identifiers and timing
        private String applicantId;              // Reference to applicant's application form ID
        private LocalDateTime testDateTime;      // When the test was scheduled/taken
        private boolean attempted;               // Whether applicant attempted the test
        private int score;                       // Total score obtained in the test
        private ArrayList<String> subjects;      // List of subjects taken in the test (English, Math, etc.)

        // Test tracking and status
        private Status status;                   // Application status (SUBMITTED, APPROVED, REJECTED, etc.)
        private ArrayList<String> attemptedSubjects = new ArrayList<>();  // Subjects attempted by applicant
        private int totalScore = 0;              // Accumulated score across all subjects
        // Individual subject completion flags for tracking which tests applicant completed
        private boolean englishTaken = false;    // Flag for English test completion
        private boolean mathTaken = false;       // Flag for Mathematics test completion
        private boolean biologyTaken = false;    // Flag for Biology test completion
        private boolean advMathTaken = false;    // Flag for Advanced Mathematics test completion



        // Constructor: Initialize test record with core test metadata
        public EntryTestRecord(String applicantId, LocalDateTime testDateTime, boolean attempted, int score) {
            this.applicantId = applicantId;          // Set applicant reference
            this.testDateTime = testDateTime;        // Set test date/time
            this.attempted = attempted;              // Set test attempt status
            this.score = score;                      // Set test score
        }


        public ArrayList<String> getAttemptedSubjects() {
            return attemptedSubjects;
        }



        public int getTotalScore() {
            return totalScore;
        }

        public void setTotalScore(int totalScore) {
            this.totalScore = totalScore;
        }

        public void addToScore(int subjectScore) {
            this.totalScore += subjectScore;
        }

        public Status getStatus() {
            return status;
        }

        public void setStatus(Status status) {
            this.status = status;
        }
        public boolean isEnglishTaken() {
            return englishTaken;
        }

        public void setEnglishTaken(boolean englishTaken) {
            this.englishTaken = englishTaken;
        }

        public boolean isMathTaken() {
            return mathTaken;
        }

        public void setMathTaken(boolean mathTaken) {
            this.mathTaken = mathTaken;
        }

        public boolean isBiologyTaken() {
            return biologyTaken;
        }

        public void setBiologyTaken(boolean biologyTaken) {
            this.biologyTaken = biologyTaken;
        }

        public boolean isAdvMathTaken() {
            return advMathTaken;
        }

        public void setAdvMathTaken(boolean advMathTaken) {
            this.advMathTaken = advMathTaken;
        }

        public String getApplicantId() {
            return applicantId;
        }

        public LocalDateTime getTestDateTime() {
            return testDateTime;
        }

        public boolean isAttempted() {
            return attempted;
        }

        public int getScore() {
            return score;
        }

        public void setTestDateTime(LocalDateTime testDateTime) {
            this.testDateTime = testDateTime;
        }

        public void setAttempted(boolean attempted) {
            this.attempted = attempted;
        }

        public void setScore(int score) {
            this.score = score;
        }

        @Override
        public String toString() {
            return applicantId + "," + testDateTime + "," + attempted + "," + score + "," +
                    (subjects != null ? String.join(";", subjects) : "Not Set");
        }

        public ArrayList<String> getSubjects() { return subjects; }
        public void setSubjects(ArrayList<String> subjects) { this.subjects = subjects; }

        public boolean isAllSubjectsCompleted() {
            return (isEnglishTaken() || !subjects.contains("English")) &&
                    (isMathTaken() || !subjects.contains("Math")) &&
                    (isBiologyTaken() || !subjects.contains("Biology")) &&
                    (isAdvMathTaken() || !subjects.contains("Add Math") && !subjects.contains("Advanced Math"));
        }
    }


    // Save or update a test record: insert new record or update existing one, manage associated subjects
    public void saveRecord(EntryTestRecord record) {
        // SQL to check if test record already exists for this applicant
        String checkSql = "SELECT COUNT(*) FROM dbo.EntryTestRecord WHERE Application_Form_ID = ?";
        // SQL to INSERT new test record with auto-increment EntryTestID (omit ID to let database generate)
        String insertSql = """
            INSERT INTO dbo.EntryTestRecord (Application_Form_ID, TestDateTime, Passed, Score)
            VALUES (?, ?, ?, ?)
        """;
        // SQL to UPDATE existing test record with new date/time and score
        String updateSql = """
            UPDATE dbo.EntryTestRecord 
            SET TestDateTime = ?, Passed = ?, Score = ?
            WHERE Application_Form_ID = ?
        """;

        // SQL to DELETE all existing subjects for this applicant (to refresh with new list)
        String deleteSubjectsSql = "DELETE FROM dbo.EntryTestSubjects WHERE Application_Form_ID = ?";
        // SQL to INSERT individual subject records (EntryTestSubjectID auto-incremented)
        String insertSubjectSql = "INSERT INTO dbo.EntryTestSubjects (Application_Form_ID, SubjectName) VALUES (?, ?)";

        try (Connection con = DBConnection.getConnection()) {
            // Check if test record already exists for this applicant
            boolean exists = false;
            try (PreparedStatement checkPs = con.prepareStatement(checkSql)) {
                // Bind applicant ID parameter to check if record exists
                checkPs.setInt(1, Integer.parseInt(record.getApplicantId()));
                try (ResultSet rs = checkPs.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        exists = true;  // Record found, will use UPDATE
                    }
                }
            }

            if (exists) {
                // Record exists, UPDATE it with new test date/time, pass status, and score
                try (PreparedStatement ps = con.prepareStatement(updateSql)) {
                    // Convert test date/time to SQL Timestamp format
                    Timestamp ts = Timestamp.valueOf(record.getTestDateTime() != null ? record.getTestDateTime() : LocalDateTime.now());
                    ps.setTimestamp(1, ts);                                           // Bind test date/time
                    ps.setBoolean(2, record.isAttempted());                           // Bind attempt status (mapped to Passed column)
                    ps.setInt(3, record.getScore());                                  // Bind test score
                    ps.setInt(4, Integer.parseInt(record.getApplicantId()));          // Bind applicant ID for WHERE clause
                    ps.executeUpdate();
                }
            } else {
                // Record doesn't exist, INSERT new test record
                try (PreparedStatement ps = con.prepareStatement(insertSql)) {
                    ps.setInt(1, Integer.parseInt(record.getApplicantId()));          // Bind applicant ID
                    Timestamp ts = Timestamp.valueOf(record.getTestDateTime() != null ? record.getTestDateTime() : LocalDateTime.now());
                    ps.setTimestamp(2, ts);                                           // Bind test date/time
                    ps.setBoolean(3, record.isAttempted());                           // Bind attempt status
                    ps.setInt(4, record.getScore());                                  // Bind test score
                    ps.executeUpdate();
                }
            }

            // Replace subjects: DELETE old ones and INSERT updated list
            try (PreparedStatement delPs = con.prepareStatement(deleteSubjectsSql)) {
                delPs.setInt(1, Integer.parseInt(record.getApplicantId()));  // Delete all subjects for this applicant
                delPs.executeUpdate();
            }
            // INSERT all new subjects if the list is not empty
            if (record.getSubjects() != null && !record.getSubjects().isEmpty()) {
                for (String subj : record.getSubjects()) {
                    try (PreparedStatement insSub = con.prepareStatement(insertSubjectSql)) {
                        insSub.setInt(1, Integer.parseInt(record.getApplicantId()));  // Bind applicant ID
                        insSub.setString(2, subj);                                     // Bind subject name (English, Math, etc.)
                        insSub.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            // Log any database errors encountered during save operation
            System.err.println("Failed to save test records: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Retrieve a single test record by applicant ID with all associated subject data
    public EntryTestRecord getRecordById(String applicantId) {
        // SQL to fetch test record metadata (date/time, pass status, score)
        String sql = """
            SELECT Application_Form_ID, TestDateTime, Passed, Score
            FROM dbo.EntryTestRecord
            WHERE Application_Form_ID = ?
        """;

        // SQL to fetch all subjects taken in this test record
        String subjectsSql = "SELECT SubjectName FROM dbo.EntryTestSubjects WHERE Application_Form_ID = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Bind applicant ID to fetch their test record
            ps.setInt(1, Integer.parseInt(applicantId));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Extract test record fields from database row
                    String appId = String.valueOf(rs.getInt("Application_Form_ID"));
                    Timestamp timestamp = rs.getTimestamp("TestDateTime");
                    LocalDateTime dateTime = timestamp != null ? timestamp.toLocalDateTime() : null;  // Convert SQL Timestamp to LocalDateTime
                    boolean passed = rs.getBoolean("Passed");
                    int score = rs.getInt("Score");

                    // Create new EntryTestRecord object with retrieved test data
                    EntryTestRecord record = new EntryTestRecord(appId, dateTime, passed, score);

                    // Fetch all subjects associated with this test record
                    try (PreparedStatement ps2 = con.prepareStatement(subjectsSql)) {
                        ps2.setInt(1, Integer.parseInt(applicantId));  // Bind applicant ID to get their subjects
                        try (ResultSet rs2 = ps2.executeQuery()) {
                            ArrayList<String> subjectList = new ArrayList<>();
                            // Accumulate all subject names from query results
                            while (rs2.next()) {
                                subjectList.add(rs2.getString("SubjectName"));  // Add subject (English, Math, Biology, etc.)
                            }
                            // Update record with subjects if list is not empty
                            if (!subjectList.isEmpty()) record.setSubjects(subjectList);
                        }
                    }

                    return record;  // Return populated record object
                }
            }
        } catch (SQLException e) {
            // Log any database errors encountered during fetch operation
            System.err.println("Error fetching test record: " + e.getMessage());
            e.printStackTrace();
        }

        return null;  // Return null if record not found or error occurred
    }



    // Load all test records for applicants who have PAID their fees along with their subjects
    public ArrayList<EntryTestRecord> loadAllRecords() {
        ArrayList<EntryTestRecord> list = new ArrayList<>();

        // SQL to fetch all test records with INNER JOIN to ApplicationForm table
        // INNER JOIN ensures only test records with PAID fee status are returned
        String sql = """
            SELECT et.Application_Form_ID, et.TestDateTime, et.Passed, et.Score
            FROM dbo.EntryTestRecord et
            INNER JOIN dbo.ApplicationForm af ON et.Application_Form_ID = af.application_form_id
            WHERE af.fee_status = 'PAID'
        """;

        // SQL to fetch all subjects for each test record
        String subjectsSql = "SELECT SubjectName FROM dbo.EntryTestSubjects WHERE Application_Form_ID = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            // Iterate through each test record from the main query
            while (rs.next()) {
                // Extract test record fields from database row
                String appId = String.valueOf(rs.getInt("Application_Form_ID"));
                Timestamp timestamp = rs.getTimestamp("TestDateTime");
                LocalDateTime dateTime = timestamp != null ? timestamp.toLocalDateTime() : null;  // Convert SQL Timestamp to LocalDateTime
                boolean passed = rs.getBoolean("Passed");
                int score = rs.getInt("Score");

                // Create new EntryTestRecord object with retrieved test data
                EntryTestRecord record = new EntryTestRecord(appId, dateTime, passed, score);

                // Fetch all subjects associated with this specific test record
                try (PreparedStatement ps2 = con.prepareStatement(subjectsSql)) {
                    ps2.setInt(1, Integer.parseInt(appId));  // Bind applicant ID to get their subjects
                    try (ResultSet rs2 = ps2.executeQuery()) {
                        ArrayList<String> subjectList = new ArrayList<>();
                        // Accumulate all subject names from subject query results
                        while (rs2.next()) {
                            subjectList.add(rs2.getString("SubjectName"));  // Add subject (English, Math, Biology, etc.)
                        }
                        // Update record with subjects if list is not empty
                        if (!subjectList.isEmpty()) record.setSubjects(subjectList);
                    }
                }

                // Add populated record to result list
                list.add(record);
            }
        } catch (SQLException e) {
            // Log any database errors encountered during load operation
            System.err.println("Error loading test records: " + e.getMessage());
            e.printStackTrace();
        }

        return list;  // Return list of all loaded records (may be empty if error occurred)
    }


    // Removed unused getNextId() helper after switching to IDENTITY inserts

}
