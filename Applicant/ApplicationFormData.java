package Applicant;

// import java.time.LocalDateTime;

public class ApplicationFormData {
    private String applicationId;
    private Applicant applicant;
    private String year12;
    private String percent12;
    private String stream12;
    private String selectedProgram;
    private String selectedCollege;
    private String email;
    private String testSchedule;
    private String testScore;
    private Status status;
    private FeeStatus feeStatus;
    private Boolean submitted;
    private Boolean scholarshipSubmitted;

    public ApplicationFormData(String applicationId, Applicant userInfo,
                               String year12, String percent12, String stream12,
                              String selectedProgram, String selectedCollege,String email) {
        this.applicationId = applicationId;
        this.applicant = userInfo;
        this.year12 = year12;
        this.percent12 = percent12;
        this.stream12 = stream12;
        this.selectedProgram = selectedProgram;
        this.selectedCollege = selectedCollege;
        this.testSchedule=null;
        this.testScore="N/A";
        this.email = email;         // can be set later via setter
        this.status = Status.SUBMITTED; // default initial status//
        this.feeStatus=FeeStatus.UNPAID;
    }

//


    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public Applicant getUsers() {
        return applicant;
    }

    public void setApplicant(Applicant users) {
        this.applicant = users;
    }

    public String getYear12() {
        return year12;
    }

    public void setYear12(String year12) {
        this.year12 = year12;
    }

    public String getPercent12() {
        return percent12;
    }

    public void setPercent12(String percent12) {
        this.percent12 = percent12;
    }

    public String getStream12() {
        return stream12;
    }

    public void setStream12(String stream12) {
        this.stream12 = stream12;
    }

    public String getSelectedProgram() {
        return selectedProgram;
    }

    public void setSelectedProgram(String selectedProgram) {
        this.selectedProgram = selectedProgram;
    }

    public String getSelectedCollege() {
        return selectedCollege;
    }

    public void setSelectedCollege(String selectedCollege) {
        this.selectedCollege = selectedCollege;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTestSchedule() {
        return testSchedule;
    }

    public void setTestSchedule(String testSchedule) {
        this.testSchedule = String.valueOf(testSchedule);
    }

    public String getTestScore() {
        return testScore;
    }

    public void setTestScore(String testScore) {
        this.testScore = testScore;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public boolean isSubmitted() { return submitted; }
    public void setSubmitted(boolean submitted) {
        this.submitted = submitted;
    }

    public void setFeeStatus(FeeStatus feeStatus) {
        this.feeStatus = feeStatus;
    }
    public FeeStatus getFeeStatus() {
        return feeStatus;
    }

    public boolean isScholarshipSubmitted() { return scholarshipSubmitted; }
    public void setScholarshipSubmitted(boolean scholarshipSubmitted) {
        this.scholarshipSubmitted = scholarshipSubmitted;
    }
}