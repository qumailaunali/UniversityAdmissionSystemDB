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

    // Add new academic program to database with multiple associated streams
    // Uses transaction to ensure atomicity: insert program first, then associate streams
    public boolean addProgram(String name, int seats, int eligibility, double fees, int collegeId, List<Integer> streamIds) {
        // SQL to INSERT new program record with college association
        String insertProgram = """
            INSERT INTO dbo.Program (ProgramName, College_ID, Seats, Eligibility, Fee)
            VALUES (?, ?, ?, ?, ?)
        """;
        
        // SQL to INSERT program-stream associations (many-to-many relationship)
        String insertProgramStream = """
            INSERT INTO dbo.ProgramStream (programid, stream_id)
            VALUES (?, ?)
        """;

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);  // Disable auto-commit for transaction management
            
            try (PreparedStatement psProgram = con.prepareStatement(insertProgram, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement psProgramStream = con.prepareStatement(insertProgramStream)) {

                // Step 1: Insert program record with provided details (name, college, seats, eligibility, fees)
                psProgram.setString(1, name);           // Bind program name
                psProgram.setInt(2, collegeId);         // Bind college ID foreign key
                psProgram.setInt(3, seats);             // Bind available seats in program
                psProgram.setInt(4, eligibility);       // Bind eligibility criteria (12th score cutoff)
                psProgram.setDouble(5, fees);           // Bind program fees
                psProgram.executeUpdate();

                // Step 2: Retrieve auto-generated ProgramID from database identity column
                int programId;
                try (ResultSet rs = psProgram.getGeneratedKeys()) {
                    if (rs.next()) {
                        programId = rs.getInt(1);  // Get generated program ID
                    } else {
                        throw new SQLException("Failed to get ProgramID");
                    }
                }

                // Step 3: Insert stream associations (batch insert for efficiency)
                for (int streamId : streamIds) {
                    psProgramStream.setInt(1, programId);  // Bind program ID
                    psProgramStream.setInt(2, streamId);   // Bind stream ID (Science, Commerce, etc.)
                    psProgramStream.addBatch();            // Add to batch for bulk insert
                }
                psProgramStream.executeBatch();  // Execute all stream inserts as a batch

                con.commit();  // Commit transaction if all operations succeed
                return true;
                
            } catch (SQLException ex) {
                con.rollback();  // Rollback all changes if any error occurs
                throw ex;
            } finally {
                con.setAutoCommit(true);  // Re-enable auto-commit for normal operation
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Retrieve all programs offered by a specific college with their stream information
    public ArrayList<Program> getProgramsByCollege(int collegeId) {
        ArrayList<Program> programs = new ArrayList<>();
        // SQL with LEFT JOIN to get programs and associated streams even if no streams exist
        // STRING_AGG aggregates multiple streams into comma-separated list per program
        String query = """
            SELECT p.ProgramID, p.ProgramName, p.Seats, p.Eligibility, p.Fee,
                   STRING_AGG(s.name, ', ') as streams
            FROM dbo.Program p
            LEFT JOIN dbo.ProgramStream ps ON p.ProgramID = ps.programid
            LEFT JOIN dbo.Stream s ON ps.stream_id = s.stream_id
            WHERE p.College_ID = ?
            GROUP BY p.ProgramID, p.ProgramName, p.Seats, p.Eligibility, p.Fee
        """;

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

    // Retrieve all programs from database regardless of college, with stream associations
    public ArrayList<Program> getAllPrograms() {
        ArrayList<Program> programs = new ArrayList<>();
        // SQL to fetch all programs with their associated streams using aggregation and joins
        String query = """
            SELECT p.ProgramID, p.ProgramName, p.Seats, p.Eligibility, p.Fee,
                   STRING_AGG(s.name, ', ') as streams
            FROM dbo.Program p
            LEFT JOIN dbo.ProgramStream ps ON p.ProgramID = ps.programid
            LEFT JOIN dbo.Stream s ON ps.stream_id = s.stream_id
            GROUP BY p.ProgramID, p.ProgramName, p.Seats, p.Eligibility, p.Fee
        """;

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

    // Delete program from database (cascade deletes associated program-stream records)
    public boolean removeProgram(int programId) {
        // SQL to DELETE program record by ID (foreign key constraints may cascade delete)
        String query = "DELETE FROM dbo.Program WHERE ProgramID = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            
            ps.setInt(1, programId);  // Bind program ID to delete
            int rowsAffected = ps.executeUpdate();  // Execute deletion
            return rowsAffected > 0;  // Return true if at least one row was deleted
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Retrieve all programs that are available for a specific educational stream (Science/Commerce/etc.)
    public ArrayList<Program> getProgramsByStream(String streamName) {
        ArrayList<Program> programs = new ArrayList<>();
        // SQL with INNER JOIN to get programs associated with specific stream via ProgramStream table
        String query = """
            SELECT p.ProgramID, p.ProgramName, p.Seats, p.Eligibility, p.Fee
            FROM dbo.Program p
            JOIN dbo.ProgramStream ps ON p.ProgramID = ps.programid
            JOIN dbo.Stream s ON ps.stream_id = s.stream_id
            WHERE s.name = ?
        """;

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


