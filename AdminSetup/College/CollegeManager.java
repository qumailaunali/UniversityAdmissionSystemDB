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
        // Insert new college into database using stored procedure and add to in-memory list
        if (connection == null) {
            System.out.println("Database connection not available!");
            return;
        }

        String procedureCall = "{call sp_InsertCollege(?, ?)}";
        try (CallableStatement cs = connection.prepareCall(procedureCall)) {
            cs.setString(1, name);  // Input parameter: college name
            cs.registerOutParameter(2, Types.INTEGER);  // Output parameter: college ID
            cs.execute();
            // Retrieve auto-generated college ID from output parameter
            int collegeId = cs.getInt(2);
            College college = new College(name);
            college.setCollegeId(collegeId);
            colleges.add(college);
        } catch (SQLException e) {
            System.out.println("Error adding college to database!");
            e.printStackTrace();
        }
    }

    public boolean removeCollegeByName(String name) {
        // Delete college from database using stored procedure and remove from in-memory list
        if (connection == null) {
            System.out.println("Database connection not available!");
            return false;
        }

        String procedureCall = "{call sp_DeleteCollegeByName(?, ?)}";
        try (CallableStatement cs = connection.prepareCall(procedureCall)) {
            cs.setString(1, name);  // Input parameter: college name
            cs.registerOutParameter(2, Types.INTEGER);  // Output parameter: rows affected
            cs.execute();
            int rowsAffected = cs.getInt(2);
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

        // Use view to fetch all colleges with their programs and streams in one query
        String viewQuery = "SELECT * FROM dbo.vw_CollegesProgramsStreams ORDER BY college_id, ProgramID";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(viewQuery)) {

            College currentCollege = null;
            Program currentProgram = null;
            int lastCollegeId = -1;
            int lastProgramId = -1;

            while (rs.next()) {
                int collegeId = rs.getInt("college_id");
                String collegeName = rs.getString("college_name");
                
                // Create new college if we encounter a different college ID
                if (collegeId != lastCollegeId) {
                    if (currentCollege != null) {
                        colleges.add(currentCollege);
                    }
                    System.out.println("Loading college: " + collegeName + " (ID: " + collegeId + ")");
                    currentCollege = new College(collegeName);
                    currentCollege.setCollegeId(collegeId);
                    lastCollegeId = collegeId;
                    lastProgramId = -1;
                }

                // Get program data (may be null if college has no programs)
                Integer programId = (Integer) rs.getObject("ProgramID");
                if (programId != null && programId != lastProgramId) {
                    String programName = rs.getString("ProgramName");
                    int seats = rs.getInt("Seats");
                    int eligibility = rs.getInt("Eligibility");
                    double fee = rs.getDouble("Fee");

                    currentProgram = new Program(programName, seats, eligibility, fee);
                    currentProgram.setProgramId(programId);
                    currentCollege.addProgram(currentProgram);
                    lastProgramId = programId;
                }

                // Add stream to current program (may be null if program has no streams)
                String streamName = rs.getString("StreamName");
                if (streamName != null && currentProgram != null) {
                    currentProgram.addAllowedStream(streamName);
                }
            }

            // Add the last college to the list
            if (currentCollege != null) {
                colleges.add(currentCollege);
            }

            System.out.println("Successfully loaded " + colleges.size() + " colleges from database!");
            System.out.println("Total colleges in memory: " + colleges.size());

        } catch (SQLException e) {
            System.out.println("ERROR loading colleges from database!");
            System.out.println("SQL Error: " + e.getMessage());
            System.out.println("SQL State: " + e.getSQLState());
            e.printStackTrace();
        }
    }
}







