package AdminSetup.Program;

import Database.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProgramManager {

    public ProgramManager() {
    }

    // Add program to database with multiple streams
    public boolean addProgram(String name, int seats, int eligibility, double fees, int collegeId, List<Integer> streamIds) {
        String insertProgram = """
            INSERT INTO dbo.Program (ProgramName, College_ID, Seats, Eligibility, Fee)
            VALUES (?, ?, ?, ?, ?)
        """;
        
        String insertProgramStream = """
            INSERT INTO dbo.ProgramStream (programid, stream_id)
            VALUES (?, ?)
        """;

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            
            try (PreparedStatement psProgram = con.prepareStatement(insertProgram, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement psProgramStream = con.prepareStatement(insertProgramStream)) {

                // Step 1: Insert Program
                psProgram.setString(1, name);
                psProgram.setInt(2, collegeId);
                psProgram.setInt(3, seats);
                psProgram.setInt(4, eligibility);
                psProgram.setDouble(5, fees);
                psProgram.executeUpdate();

                // Step 2: Get generated ProgramID
                int programId;
                try (ResultSet rs = psProgram.getGeneratedKeys()) {
                    if (rs.next()) {
                        programId = rs.getInt(1);
                    } else {
                        throw new SQLException("Failed to get ProgramID");
                    }
                }

                // Step 3: Insert ProgramStream entries (batch)
                for (int streamId : streamIds) {
                    psProgramStream.setInt(1, programId);
                    psProgramStream.setInt(2, streamId);
                    psProgramStream.addBatch();
                }
                psProgramStream.executeBatch();

                con.commit();
                return true;
                
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get all programs for a specific college
    public ArrayList<Program> getProgramsByCollege(int collegeId) {
        ArrayList<Program> programs = new ArrayList<>();
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
            
            ps.setInt(1, collegeId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Program program = new Program(
                    rs.getString("ProgramName"),
                    rs.getInt("Seats"),
                    rs.getInt("Eligibility"),
                    rs.getDouble("Fee")
                );
                program.setProgramId(rs.getInt("ProgramID"));
                
                String streams = rs.getString("streams");
                if (streams != null && !streams.isEmpty()) {
                    for (String stream : streams.split(", ")) {
                        program.addAllowedStream(stream);
                    }
                }
                programs.add(program);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return programs;
    }

    // Get all programs
    public ArrayList<Program> getAllPrograms() {
        ArrayList<Program> programs = new ArrayList<>();
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

            while (rs.next()) {
                Program program = new Program(
                    rs.getString("ProgramName"),
                    rs.getInt("Seats"),
                    rs.getInt("Eligibility"),
                    rs.getDouble("Fee")
                );
                program.setProgramId(rs.getInt("ProgramID"));
                
                String streams = rs.getString("streams");
                if (streams != null && !streams.isEmpty()) {
                    for (String stream : streams.split(", ")) {
                        program.addAllowedStream(stream);
                    }
                }
                programs.add(program);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return programs;
    }

    // Remove program
    public boolean removeProgram(int programId) {
        String query = "DELETE FROM dbo.Program WHERE ProgramID = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            
            ps.setInt(1, programId);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get programs by stream
    public ArrayList<Program> getProgramsByStream(String streamName) {
        ArrayList<Program> programs = new ArrayList<>();
        String query = """
            SELECT p.ProgramID, p.ProgramName, p.Seats, p.Eligibility, p.Fee
            FROM dbo.Program p
            JOIN dbo.ProgramStream ps ON p.ProgramID = ps.programid
            JOIN dbo.Stream s ON ps.stream_id = s.stream_id
            WHERE s.name = ?
        """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            
            ps.setString(1, streamName);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Program program = new Program(
                    rs.getString("ProgramName"),
                    rs.getInt("Seats"),
                    rs.getInt("Eligibility"),
                    rs.getDouble("Fee")
                );
                program.setProgramId(rs.getInt("ProgramID"));
                programs.add(program);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return programs;
    }

    // Get all available streams
    public ArrayList<StreamData> getAllStreams() {
        ArrayList<StreamData> streams = new ArrayList<>();
        String query = "SELECT stream_id, name FROM dbo.Stream ORDER BY name";

        try (Connection con = DBConnection.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                streams.add(new StreamData(rs.getInt("stream_id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return streams;
    }

    // Inner class to hold stream data
    public static class StreamData {
        private int streamId;
        private String streamName;

        public StreamData(int streamId, String streamName) {
            this.streamId = streamId;
            this.streamName = streamName;
        }

        public int getStreamId() {
            return streamId;
        }

        public String getStreamName() {
            return streamName;
        }

        @Override
        public String toString() {
            return streamName;
        }
    }
}


