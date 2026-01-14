package AdminSetup.Program;

import Database.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Manages academic programs and their associated streams in the university system
// Handles program creation, retrieval, deletion, and stream associations
public class ProgramManager {

    // Constructor: Initialize program manager
    public ProgramManager() {
    }

    // Add new academic program to database with multiple associated streams using stored procedure
    public boolean addProgram(String name, int seats, int eligibility, double fees, int collegeId, List<Integer> streamIds) {
        String procedureCall = "{call sp_AddProgram(?, ?, ?, ?, ?, ?, ?)}";

        try (Connection con = DBConnection.getConnection();
             CallableStatement cs = con.prepareCall(procedureCall)) {

            // Bind input parameters
            cs.setString(1, name);                // Program name
            cs.setInt(2, collegeId);              // College ID
            cs.setInt(3, seats);                  // Available seats
            cs.setInt(4, eligibility);            // Eligibility criteria
            cs.setDouble(5, fees);                // Program fees
            
            // Convert stream IDs list to comma-separated string
            String streamIdsStr = "";
            if (streamIds != null && !streamIds.isEmpty()) {
                streamIdsStr = streamIds.stream()
                    .map(String::valueOf)
                    .reduce((a, b) -> a + "," + b)
                    .orElse("");
            }
            cs.setString(6, streamIdsStr);        // Comma-separated stream IDs
            
            // Register output parameter for generated program ID
            cs.registerOutParameter(7, Types.INTEGER);
            
            cs.execute();
            return true;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Retrieve all programs offered by a specific college using view
    public ArrayList<Program> getProgramsByCollege(int collegeId) {
        ArrayList<Program> programs = new ArrayList<>();
        // Use view with WHERE filter to get programs by college
        String query = "SELECT ProgramID, ProgramName, Seats, Eligibility, Fee, streams FROM dbo.vw_ProgramsWithStreams WHERE College_ID = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            
            ps.setInt(1, collegeId);  // Bind college ID to filter by specific college
            ResultSet rs = ps.executeQuery();

            // Process each program row from database
            while (rs.next()) {
                // Create program object with retrieved data
                Program program = new Program(
                    rs.getString("ProgramName"),        // Get program name
                    rs.getInt("Seats"),                 // Get available seats
                    rs.getInt("Eligibility"),           // Get eligibility criteria
                    rs.getDouble("Fee")                 // Get program fees
                );
                program.setProgramId(rs.getInt("ProgramID"));  // Set program ID
                
                // Parse and associate streams with program
                String streams = rs.getString("streams");
                if (streams != null && !streams.isEmpty()) {
                    // Split comma-separated stream names and add to program object
                    for (String stream : streams.split(", ")) {
                        program.addAllowedStream(stream);  // Add individual stream to program
                    }
                }
                programs.add(program);  // Add populated program to results list
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return programs;
    }

    // Retrieve all programs from database using view
    public ArrayList<Program> getAllPrograms() {
        ArrayList<Program> programs = new ArrayList<>();
        // Use view to fetch all programs with aggregated streams
        String query = "SELECT ProgramID, ProgramName, Seats, Eligibility, Fee, streams FROM dbo.vw_ProgramsWithStreams";

        try (Connection con = DBConnection.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            // Process each program row from database result set
            while (rs.next()) {
                // Create program object with retrieved data
                Program program = new Program(
                    rs.getString("ProgramName"),        // Get program name
                    rs.getInt("Seats"),                 // Get available seats
                    rs.getInt("Eligibility"),           // Get eligibility criteria
                    rs.getDouble("Fee")                 // Get program fees
                );
                program.setProgramId(rs.getInt("ProgramID"));  // Set program ID
                
                // Parse and associate streams with program
                String streams = rs.getString("streams");
                if (streams != null && !streams.isEmpty()) {
                    // Split comma-separated stream names and add to program object
                    for (String stream : streams.split(", ")) {
                        program.addAllowedStream(stream);  // Add individual stream to program
                    }
                }
                programs.add(program);  // Add populated program to results list
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return programs;
    }

    // Delete program from database using stored procedure
    public boolean removeProgram(int programId) {
        String procedureCall = "{call sp_RemoveProgram(?, ?)}";

        try (Connection con = DBConnection.getConnection();
             CallableStatement cs = con.prepareCall(procedureCall)) {
            
            cs.setInt(1, programId);  // Bind program ID to delete
            cs.registerOutParameter(2, Types.INTEGER);  // Register output for rows affected
            cs.execute();
            
            int rowsAffected = cs.getInt(2);
            return rowsAffected > 0;  // Return true if at least one row was deleted
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Retrieve all programs available for a specific educational stream using view
    public ArrayList<Program> getProgramsByStream(String streamName) {
        ArrayList<Program> programs = new ArrayList<>();
        // Use view with WHERE filter to get programs by stream
        String query = "SELECT ProgramID, ProgramName, Seats, Eligibility, Fee FROM dbo.vw_ProgramsByStream WHERE StreamName = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            
            ps.setString(1, streamName);  // Bind stream name to filter by (Science, Commerce, etc.)
            ResultSet rs = ps.executeQuery();

            // Process each program row that matches the specified stream
            while (rs.next()) {
                // Create program object with retrieved data
                Program program = new Program(
                    rs.getString("ProgramName"),        // Get program name
                    rs.getInt("Seats"),                 // Get available seats
                    rs.getInt("Eligibility"),           // Get eligibility criteria
                    rs.getDouble("Fee")                 // Get program fees
                );
                program.setProgramId(rs.getInt("ProgramID"));  // Set program ID
                programs.add(program);  // Add program to results list
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return programs;
    }

    // Retrieve all educational streams available in the system (Science, Commerce, Arts, etc.)
    public ArrayList<StreamData> getAllStreams() {
        ArrayList<StreamData> streams = new ArrayList<>();
        // SQL to fetch all stream records from database, ordered alphabetically
        String query = "SELECT stream_id, name FROM dbo.Stream ORDER BY name";

        try (Connection con = DBConnection.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            // Process each stream row from database
            while (rs.next()) {
                // Create StreamData object with stream ID and name
                streams.add(new StreamData(rs.getInt("stream_id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return streams;
    }

    // Inner class to represent educational stream data (Science, Commerce, Arts, etc.)
    public static class StreamData {
        private int streamId;        // Database ID for the stream
        private String streamName;   // Human-readable stream name

        // Constructor: Initialize stream data with ID and name
        public StreamData(int streamId, String streamName) {
            this.streamId = streamId;
            this.streamName = streamName;
        }

        // Getter for stream ID
        public int getStreamId() {
            return streamId;
        }

        // Getter for stream name
        public String getStreamName() {
            return streamName;
        }

        // String representation: return stream name for display
        @Override
        public String toString() {
            return streamName;
        }
    }
}  // End of ProgramManager class


