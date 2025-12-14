package AdminSetup.College;

import AdminSetup.Program.Program;
import Database.DBConnection;

import java.sql.*;
import java.util.ArrayList;

public class CollegeManager {
    private final ArrayList<College> colleges;
    private Connection connection;

    public CollegeManager() {
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
        if (connection == null) {
            System.out.println("Database connection not available!");
            return;
        }

        String insertQuery = "INSERT INTO dbo.College (college_name) VALUES (?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();

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
        if (connection == null) {
            System.out.println("Database connection not available!");
            return false;
        }

        String deleteQuery = "DELETE FROM dbo.College WHERE college_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(deleteQuery)) {
            pstmt.setString(1, name);
            int rowsAffected = pstmt.executeUpdate();
            
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
        for (College c : colleges) {
            if (c.getName().equalsIgnoreCase(collegeName)) {
                return c;
            }
        }
        return null;
    }

    public ArrayList<College> getCollegesByProgramName(String programName) {
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
        colleges.clear();

        if (connection == null) {
            System.out.println("ERROR: Database connection not available for loading colleges!");
            return;
        }

        System.out.println("Loading colleges from database...");

        // Try both possible column name formats (college_id vs CollegeID)
        String collegeQuery = "SELECT * FROM dbo.College";
        String programQuery = "SELECT * FROM dbo.Program WHERE CollegeID = ?";
        String streamQuery = "SELECT StreamName FROM dbo.Stream WHERE ProgramID = ?";

        try (Statement stmt = connection.createStatement();
             ResultSet collegeRs = stmt.executeQuery(collegeQuery)) {

            int collegeCount = 0;
            while (collegeRs.next()) {
                collegeCount++;
                
                // Try both column name formats
                int collegeId;
                String collegeName;
                
                try {
                    collegeId = collegeRs.getInt("college_id");
                    collegeName = collegeRs.getString("college_name");
                } catch (SQLException e) {
                    // Try alternate column names
                    collegeId = collegeRs.getInt("CollegeID");
                    collegeName = collegeRs.getString("CollegeName");
                }
                
                System.out.println("Loading college: " + collegeName + " (ID: " + collegeId + ")");
                
                College college = new College(collegeName);
                college.setCollegeId(collegeId);

                // Load programs for this college
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

                            // Load streams for this program
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







