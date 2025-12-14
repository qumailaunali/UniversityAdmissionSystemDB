package Applicant;



import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ApplicantManager {

    private static final String APPLICATION_FILE = "all_applications.txt";

    public static void saveToFile(ApplicationFormData app) {
        try (FileWriter writer = new FileWriter(APPLICATION_FILE, true)) {
            String line = String.join(",",
                    app.getApplicationId() != null ? app.getApplicationId() : "",
                    app.getUsers() != null && app.getUsers().getFirstName() != null ? app.getUsers().getFirstName() : "Unknown",
                    app.getYear12() != null ? app.getYear12() : "",
                    app.getPercent12() != null ? app.getPercent12() : "",
                    app.getStream12() != null ? app.getStream12() : "",
                    app.getSelectedProgram() != null ? app.getSelectedProgram() : "N/A",
                    app.getSelectedCollege() != null ? app.getSelectedCollege() : "N/A",
                    app.getUsers() != null && app.getUsers().getEmail() != null ? app.getUsers().getEmail() : "n/a",
                    app.getStatus() != null ? app.getStatus().name() : "UNKNOWN",
                    app.getTestSchedule() != null ? app.getTestSchedule() : "N/A",
                    app.getTestScore() != null ? app.getTestScore() : "null",
                    app.getFeeStatus() != null ? app.getFeeStatus().name() : "PENDING"
            );

            writer.write(line + System.lineSeparator());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public boolean hasAppliedBefore(ApplicationFormData newApp) {
        ArrayList<ApplicationFormData> existingApplications = loadAllApplications();

        for (ApplicationFormData app : existingApplications) {
            boolean sameUser = app.getUsers().equals(newApp.getUsers());
            boolean sameProgram = app.getSelectedProgram().equalsIgnoreCase(newApp.getSelectedProgram());
            boolean sameCollege = app.getSelectedCollege().equalsIgnoreCase(newApp.getSelectedCollege());

            if (sameUser && (sameProgram || sameCollege)) {
                return true;
            }
        }

        return false;
    }


    public static ArrayList<ApplicationFormData> loadAllApplications() {
        ArrayList<ApplicationFormData> applications = new ArrayList<>();
        File file = new File(APPLICATION_FILE);

        if (!file.exists()) return applications;

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(",");

                if (parts.length < 11) {
                    continue;
                }

                String applicationID = parts[0];
                String fullName = parts[1];
                String year12 = parts[2];
                String percent12 = parts[3];
                String stream12 = parts[4];
                String selectedProgramName = parts[5];
                String selectedCollegeName = parts[6];
                String email = parts[7];
                String statusStr = parts[8];
                String testSchedule = parts[9];
                String testScore = parts[10];
                String feeStatusStr = parts[11];

                Applicant user = null;

                ApplicationFormData app = new ApplicationFormData(
                        applicationID,
                        user,
                        year12, percent12, stream12,
                        selectedProgramName,
                        selectedCollegeName,
                        email
                );

                app.setTestSchedule(testSchedule);
                app.setTestScore(testScore);

                try {
                    app.setStatus(Status.valueOf(statusStr.toUpperCase()));
                } catch (IllegalArgumentException | NullPointerException e) {
                    app.setStatus(Status.SUBMITTED);
                }

                try {
                    app.setFeeStatus(FeeStatus.valueOf(feeStatusStr.toUpperCase()));
                } catch (IllegalArgumentException | NullPointerException e) {
                    app.setFeeStatus(FeeStatus.UNPAID);
                }

                applications.add(app);
            }
        } catch (IOException e) {
            System.out.println("Error reading application file: " + e.getMessage());
        }

        return applications;
    }


    public static ArrayList<ApplicationFormData> getApplicationsByUserEmail(String email) {
        ArrayList<ApplicationFormData> allApps = loadAllApplications();
        ArrayList<ApplicationFormData> userApps = new ArrayList<>();

        for (ApplicationFormData app : allApps) {
            if (app.getEmail() != null && app.getEmail().equalsIgnoreCase(email)) {
                userApps.add(app);
            }
        }

        return userApps;
    }
    public static ApplicationFormData getApplicationByAppId(String id) {
        ArrayList<ApplicationFormData> allApps = loadAllApplications();

        for (ApplicationFormData app : allApps) {
            if (app.getApplicationId() != null && app.getApplicationId().equalsIgnoreCase(id)) {
                return app;
            }
        }

        return null; // Not found
    }
//
    public static void updateApplicationStatus(String applicationId, Status newStatus) {
        ArrayList<ApplicationFormData> allApps = loadAllApplications();
        System.out.println("Applications loaded: " + allApps.size());

        for (ApplicationFormData app : allApps) {
            if (app.getApplicationId().equals(applicationId)) {
                app.setStatus(newStatus);
                break;
            }
        }

        try (FileWriter writer = new FileWriter(APPLICATION_FILE, false)) { // overwrite entire file
            for (ApplicationFormData app : allApps) {
                String applicationId1 = app.getApplicationId() != null ? app.getApplicationId() : "";
                String firstName = (app.getUsers() != null && app.getUsers().getFirstName() != null)
                        ? app.getUsers().getFirstName() : "Unknown";
                String address = app.getAddress() != null ? app.getAddress() : "";
                String board10 = app.getBoard10() != null ? app.getBoard10() : "";
                String year10 = app.getYear10() != null ? app.getYear10() : "";
                String percent10 = app.getPercent10() != null ? app.getPercent10() : "";
                String stream10 = app.getStream10() != null ? app.getStream10() : "";
                String board12 = app.getBoard12() != null ? app.getBoard12() : "";
                String year12 = app.getYear12() != null ? app.getYear12() : "";
                String percent12 = app.getPercent12() != null ? app.getPercent12() : "";
                String stream12 = app.getStream12() != null ? app.getStream12() : "";
                String program = app.getSelectedProgram() != null ? app.getSelectedProgram() : "N/A";
                String college = app.getSelectedCollege() != null ? app.getSelectedCollege() : "N/A";
                String email = app.getEmail() != null ? app.getEmail() : "n/a";
                String status = app.getStatus() != null ? app.getStatus().name() : "UNKNOWN";
                String testSchedule = app.getTestSchedule() != null ? app.getTestSchedule() : "null";
                String testScore = app.getTestScore() != null ? app.getTestScore() : "null";
                String feeStatusStr = app.getFeeStatus() != null ? app.getFeeStatus().name() : "PENDING";

                String line = String.join(",",
                        applicationId1,
                        firstName,
                        address,
                        board10,
                        year10,
                        percent10,
                        stream10,
                        board12,
                        year12,
                        percent12,
                        stream12,
                        program,
                        college,
                        email,
                        status,
                        testSchedule,
                        testScore,
                        feeStatusStr

                );
                writer.write(line + System.lineSeparator());
            }
        }
        catch (IOException e) {
            System.out.println("Error updating application status: " + e.getMessage());
        }

    }

    public static List<String> getAllApplicantIds() {
        List<String> ids = new ArrayList<>();
        try {
            ArrayList<ApplicationFormData> apps = loadAllApplications();
            for (ApplicationFormData app : apps) {
                ids.add(app.getApplicationId());
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return ids;
    }

    public static Status getApplicationStatus(String applicationId) {
        // Example: Look up from saved applications
        ArrayList<ApplicationFormData> all = loadAllApplications();
        for (ApplicationFormData app : all) {
            if (app.getApplicationId().equals(applicationId)) {
                return app.getStatus();
            }
        }
        return Status.SUBMITTED; // Default fallback
    }
    public static Status getApplicationStatusByEmail(String email) {
        // Example: Look up from saved applications
        ArrayList<ApplicationFormData> all = loadAllApplications();
        for (ApplicationFormData app : all) {
            if (app.getEmail().equalsIgnoreCase(email)) {
                return app.getStatus(); //
            }
        }
        return Status.SUBMITTED;
    }




}
