package AdminSetup.EntryTest;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import Applicant.Status;
import Database.DBConnection;

public class EntryTestRecordManager {

    public static class EntryTestRecord {
        private String applicantId;
        private LocalDateTime testDateTime;
        private boolean attempted;
        private int score;
        private ArrayList<String> subjects;

        private Status status;
        private ArrayList<String> attemptedSubjects = new ArrayList<>();
        private int totalScore = 0;
        private boolean englishTaken = false;
        private boolean mathTaken = false;
        private boolean biologyTaken = false;
        private boolean advMathTaken = false;



        public EntryTestRecord(String applicantId, LocalDateTime testDateTime, boolean attempted, int score) {
            this.applicantId = applicantId;
            this.testDateTime = testDateTime;
            this.attempted = attempted;
            this.score = score;
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


    public void saveRecord(EntryTestRecord record) {
        String checkSql = "SELECT COUNT(*) FROM dbo.EntryTestRecord WHERE Application_Form_ID = ?";
        // Use identity/auto-increment on EntryTestID (omit explicit ID to avoid identity insert errors)
        String insertSql = """
            INSERT INTO dbo.EntryTestRecord (Application_Form_ID, TestDateTime, Passed, Score)
            VALUES (?, ?, ?, ?)
        """;
        String updateSql = """
            UPDATE dbo.EntryTestRecord 
            SET TestDateTime = ?, Passed = ?, Score = ?
            WHERE Application_Form_ID = ?
        """;

        String deleteSubjectsSql = "DELETE FROM dbo.EntryTestSubjects WHERE Application_Form_ID = ?";
        // EntryTestSubjectID assumed identity; omit explicit ID
        String insertSubjectSql = "INSERT INTO dbo.EntryTestSubjects (Application_Form_ID, SubjectName) VALUES (?, ?)";

        try (Connection con = DBConnection.getConnection()) {
            // Check if record exists
            boolean exists = false;
            try (PreparedStatement checkPs = con.prepareStatement(checkSql)) {
                checkPs.setInt(1, Integer.parseInt(record.getApplicantId()));
                try (ResultSet rs = checkPs.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        exists = true;
                    }
                }
            }

            if (exists) {
                try (PreparedStatement ps = con.prepareStatement(updateSql)) {
                    Timestamp ts = Timestamp.valueOf(record.getTestDateTime() != null ? record.getTestDateTime() : LocalDateTime.now());
                    ps.setTimestamp(1, ts);
                    ps.setBoolean(2, record.isAttempted()); // map attempted -> Passed
                    ps.setInt(3, record.getScore());
                    ps.setInt(4, Integer.parseInt(record.getApplicantId()));
                    ps.executeUpdate();
                }
            } else {
                try (PreparedStatement ps = con.prepareStatement(insertSql)) {
                    ps.setInt(1, Integer.parseInt(record.getApplicantId()));
                    Timestamp ts = Timestamp.valueOf(record.getTestDateTime() != null ? record.getTestDateTime() : LocalDateTime.now());
                    ps.setTimestamp(2, ts);
                    ps.setBoolean(3, record.isAttempted());
                    ps.setInt(4, record.getScore());
                    ps.executeUpdate();
                }
            }

            // Replace subjects set
            try (PreparedStatement delPs = con.prepareStatement(deleteSubjectsSql)) {
                delPs.setInt(1, Integer.parseInt(record.getApplicantId()));
                delPs.executeUpdate();
            }
            if (record.getSubjects() != null && !record.getSubjects().isEmpty()) {
                for (String subj : record.getSubjects()) {
                    try (PreparedStatement insSub = con.prepareStatement(insertSubjectSql)) {
                        insSub.setInt(1, Integer.parseInt(record.getApplicantId()));
                        insSub.setString(2, subj);
                        insSub.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to save test records: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public EntryTestRecord getRecordById(String applicantId) {
        String sql = """
            SELECT Application_Form_ID, TestDateTime, Passed, Score
            FROM dbo.EntryTestRecord
            WHERE Application_Form_ID = ?
        """;

        String subjectsSql = "SELECT SubjectName FROM dbo.EntryTestSubjects WHERE Application_Form_ID = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, Integer.parseInt(applicantId));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String appId = String.valueOf(rs.getInt("Application_Form_ID"));
                    Timestamp timestamp = rs.getTimestamp("TestDateTime");
                    LocalDateTime dateTime = timestamp != null ? timestamp.toLocalDateTime() : null;
                    boolean passed = rs.getBoolean("Passed");
                    int score = rs.getInt("Score");

                    EntryTestRecord record = new EntryTestRecord(appId, dateTime, passed, score);

                    try (PreparedStatement ps2 = con.prepareStatement(subjectsSql)) {
                        ps2.setInt(1, Integer.parseInt(applicantId));
                        try (ResultSet rs2 = ps2.executeQuery()) {
                            ArrayList<String> subjectList = new ArrayList<>();
                            while (rs2.next()) {
                                subjectList.add(rs2.getString("SubjectName"));
                            }
                            if (!subjectList.isEmpty()) record.setSubjects(subjectList);
                        }
                    }

                    return record;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching test record: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }



    public ArrayList<EntryTestRecord> loadAllRecords() {
        ArrayList<EntryTestRecord> list = new ArrayList<>();

        String sql = """
            SELECT et.Application_Form_ID, et.TestDateTime, et.Passed, et.Score
            FROM dbo.EntryTestRecord et
            INNER JOIN dbo.ApplicationForm af ON et.Application_Form_ID = af.application_form_id
            WHERE af.fee_status = 'PAID'
        """;

        String subjectsSql = "SELECT SubjectName FROM dbo.EntryTestSubjects WHERE Application_Form_ID = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String appId = String.valueOf(rs.getInt("Application_Form_ID"));
                Timestamp timestamp = rs.getTimestamp("TestDateTime");
                LocalDateTime dateTime = timestamp != null ? timestamp.toLocalDateTime() : null;
                boolean passed = rs.getBoolean("Passed");
                int score = rs.getInt("Score");

                EntryTestRecord record = new EntryTestRecord(appId, dateTime, passed, score);

                try (PreparedStatement ps2 = con.prepareStatement(subjectsSql)) {
                    ps2.setInt(1, Integer.parseInt(appId));
                    try (ResultSet rs2 = ps2.executeQuery()) {
                        ArrayList<String> subjectList = new ArrayList<>();
                        while (rs2.next()) {
                            subjectList.add(rs2.getString("SubjectName"));
                        }
                        if (!subjectList.isEmpty()) record.setSubjects(subjectList);
                    }
                }

                list.add(record);
            }
        } catch (SQLException e) {
            System.err.println("Error loading test records: " + e.getMessage());
            e.printStackTrace();
        }

        return list;
    }



    // Removed unused getNextId() helper after switching to IDENTITY inserts

}
