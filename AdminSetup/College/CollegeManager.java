package AdminSetup.College;

import AdminSetup.Program.Program;
import Database.DBConnection;

import java.sql.*;
import java.util.ArrayList;

public class CollegeManager {
    private final ArrayList<College> colleges;
    private Connection connection;

    public CollegeManager() {
        // Initialize college list and load all colleges from database
        colleges = new ArrayList<>();
        try {
            connection = DBConnection.getConnection();
            loadFromDatabase();
        } catch (SQLException e) {
            System.out.println("Database connection failed!");
            e.printStackTrace();
        }
    }

    public void addCollege(String name) {
        // Insert new college into database and add to in-memory list
        // SQL: INSERT INTO dbo.College (college_name) VALUES (?)
        if (connection == null) {
            System.out.println("Database connection not available!");
            return;
        }

        String insertQuery = "INSERT INTO dbo.College (college_name) VALUES (?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
            // Retrieve auto-generated college ID from database
            ResultSet keys = pstmt.getGeneratedKeys();
            if (keys.next()) {
                int collegeId = keys.getInt(1);
                College college = new College(name);
                college.setCollegeId(collegeId);
                colleges.add(college);
            }
            keys.close();
        } catch (SQLException e) {
            System.out.println("Error adding college to database!");
            e.printStackTrace();
        }
    }

    public boolean removeCollegeByName(String name) {
        // Delete college from database by name and remove from in-memory list
        // SQL: DELETE FROM dbo.College WHERE college_name = ?
        if (connection == null) {
            System.out.println("Database connection not available!");
            return false;
        }

        String deleteQuery = "DELETE FROM dbo.College WHERE college_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(deleteQuery)) {
            pstmt.setString(1, name);
            int rowsAffected = pstmt.executeUpdate();
            // If deletion successful, remove college from in-memory list
            if (rowsAffected > 0) {
                colleges.removeIf(c -> c.getName().equalsIgnoreCase(name));
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.out.println("Error removing college from database!");
            e.printStackTrace();
            return false;
        }
    }

    public ArrayList<College> getAllColleges() {
        return colleges;
    }

    public College getCollegeByName(String collegeName) {
        // Search for college by name (case-insensitive)
        for (College c : colleges) {
            if (c.getName().equalsIgnoreCase(collegeName)) {
                return c;
            }
        }
        return null;
    }

    public ArrayList<College> getCollegesByProgramName(String programName) {
        // Find all colleges that offer a specific program
        // Avoid duplicates if program exists in multiple colleges
        ArrayList<College> result = new ArrayList<>();

        for (College college : colleges) {
            for (Program program : college.getPrograms()) {
                if (program.getName().equalsIgnoreCase(programName)) {
                    boolean alreadyExists = false;

                    for (College existing : result) {
                        if (existing.getName().equalsIgnoreCase(college.getName())) {
                            alreadyExists = true;
                            break;
                        }
                    }

                    if (!alreadyExists) {
                        result.add(college);
                    }

                    break;
                }
            }
        }

        return result;
    }

    // Get all programs that allow a specific stream
    public ArrayList<Program> getProgramsByStream(String stream) {
        // Filter all programs from all colleges by allowed stream
        // Prevents duplicate programs across colleges
        ArrayList<Program> result = new ArrayList<>();

        for (College college : colleges) {
            for (Program program : college.getPrograms()) {
                if (program.getAllowedStreams().contains(stream)) {
                    boolean alreadyExists = false;

                    for (Program existing : result) {
                        if (existing.getName().equalsIgnoreCase(program.getName())) {
                            alreadyExists = true;
                            break;
                        }
                    }

                    if (!alreadyExists) {
                        result.add(program);
                    }
                }
            }
        }
        return result;
    }

    public void saveToDatabase() {
        // This method is now mostly handled by addCollege and ProgramManager
        // Can be used for batch updates if needed
        System.out.println("Colleges are automatically saved to database.");
    }

    public void loadFromDatabase() {
        // Load all colleges, programs, and allowed streams from database
        // SQL: 3-level join: College -> Program -> ProgramStream
        colleges.clear();

        if (connection == null) {
            System.out.println("ERROR: Database connection not available for loading colleges!");
            return;
        }

        System.out.println("Loading colleges from database...");

        // SQL Queries for hierarchical data load
        // collegeQuery: SELECT all colleges from College table
        String collegeQuery = "SELECT college_id, college_name FROM dbo.College";
        // programQuery: SELECT all programs for a specific college (filtered by College_ID)
        String programQuery = "SELECT ProgramID, ProgramName, Seats, Eligibility, Fee FROM dbo.Program WHERE College_ID = ?";
        // streamQuery: SELECT all allowed streams for a specific program via join table
        String streamQuery = "SELECT s.name FROM dbo.ProgramStream ps JOIN dbo.Stream s ON ps.stream_id = s.stream_id WHERE ps.programid = ?";

        try (Statement stmt = connection.createStatement();
             ResultSet collegeRs = stmt.executeQuery(collegeQuery)) {

            int collegeCount = 0;
            while (collegeRs.next()) {
                collegeCount++;
                // Retrieve college ID and name from database
                int collegeId = collegeRs.getInt("college_id");
                String collegeName = collegeRs.getString("college_name");
                
                System.out.println("Loading college: " + collegeName + " (ID: " + collegeId + ")");
                
                College college = new College(collegeName);
                college.setCollegeId(collegeId);
                // Load programs associated with this college
                try (PreparedStatement programStmt = connection.prepareStatement(programQuery)) {
                    programStmt.setInt(1, collegeId);
                    try (ResultSet programRs = programStmt.executeQuery()) {
                        while (programRs.next()) {
                            int programId = programRs.getInt("ProgramID");
                            String programName = programRs.getString("ProgramName");
                            int seats = programRs.getInt("Seats");
                            int eligibility = programRs.getInt("Eligibility");
                            double fee = programRs.getDouble("Fee");

                            Program program = new Program(programName, seats, eligibility, fee);
                            program.setProgramId(programId);
                            // Load allowed streams (e.g., Science, Commerce) for this program
                            try (PreparedStatement streamStmt = connection.prepareStatement(streamQuery)) {
                                streamStmt.setInt(1, programId);
                                try (ResultSet streamRs = streamStmt.executeQuery()) {
                                    while (streamRs.next()) {
                                        String streamName = streamRs.getString("StreamName");
                                        program.addAllowedStream(streamName);
                                    }
                                }
                            }

                            college.addProgram(program);
                        }
                    } catch (SQLException e) {
                        System.out.println("Error loading programs for college " + collegeName + ": " + e.getMessage());
                    }
                }

                colleges.add(college);
            }

            System.out.println("Successfully loaded " + collegeCount + " colleges from database!");
            System.out.println("Total colleges in memory: " + colleges.size());

        } catch (SQLException e) {
            System.out.println("ERROR loading colleges from database!");
            System.out.println("SQL Error: " + e.getMessage());
            System.out.println("SQL State: " + e.getSQLState());
            e.printStackTrace();
        }
    }
}







