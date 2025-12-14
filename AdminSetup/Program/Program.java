package AdminSetup.Program;

import java.util.ArrayList;

public class Program {
    private int programId;
    private String name;
    private int seats;
    private int eligibility;
    private double fee;
    private ArrayList<String> allowedStreams;
    private String collegeName;

    public Program(String name, int seats, int eligibility, double fee) {
        this.name = name;
        this.seats = seats;
        this.eligibility = eligibility;
        this.fee = fee;
        this.allowedStreams = new ArrayList<>();
    }

    public int getProgramId() {
        return programId;
    }

    public void setProgramId(int programId) {
        this.programId = programId;
    }

    public Program(String name, int seats, int eligibility) {
        this(name, seats, eligibility, 0.0);
    }

    public String getName() {
        return name;
    }

    public int getSeats() {
        return seats;
    }

    public int getEligibility() {
        return eligibility;
    }

    public double getFee() {
        return fee;
    }

    public ArrayList<String> getAllowedStreams() {
        return allowedStreams;
    }

    public void setSeats(int seats) {
        this.seats = seats;
    }

    public void setEligibility(int eligibility) {
        this.eligibility = eligibility;
    }

    public void setFee(double fee) {
        this.fee = fee;
    }

    public void setAllowedStreams(ArrayList<String> streams) {
        this.allowedStreams = streams;
    }

    public void addAllowedStream(String stream) {
        if (!allowedStreams.contains(stream)) {
            allowedStreams.add(stream);
        }
    }

    public boolean isStreamAllowed(String stream) {
        return allowedStreams.contains(stream);
    }

    public String getProgramDetails() {
        return getName() + " (Seats: " + getSeats() +
                ", Min Score: " + getEligibility() +
                ", Fee: Rs." + getFee() + " Stream:  "+getAllowedStreams() +" )";
    }
    public void setCollegeName(String collegeName) {
        this.collegeName = collegeName;
    }

    public String getCollegeName() {
        return collegeName;
    }

    @Override
    public String toString() {
        return this.name;
    }
}



