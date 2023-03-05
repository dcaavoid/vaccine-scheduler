package scheduler;

import scheduler.db.ConnectionManager;
import scheduler.model.*;
import scheduler.util.Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;

public class Scheduler {

    // objects to keep track of the currently logged-in user
    // Note: it is always true that at most one of currentCaregiver and currentPatient is not null
    //       since only one user can be logged-in at a time
    private static Caregiver currentCaregiver = null;
    private static Patient currentPatient = null;

    public static void main(String[] args) {
        // printing greetings text
        System.out.println();
        System.out.println("Welcome to the COVID-19 Vaccine Reservation Scheduling Application!");
        System.out.println("*** Please enter one of the following commands ***");
        System.out.println("> create_patient <username> <password>");  //TODO: implement create_patient (Part 1)
        System.out.println("> create_caregiver <username> <password>");
        System.out.println("> login_patient <username> <password>");  // TODO: implement login_patient (Part 1)
        System.out.println("> login_caregiver <username> <password>");
        System.out.println("> search_caregiver_schedule <date>");  // TODO: implement search_caregiver_schedule (Part 2)
        System.out.println("> reserve <date> <vaccine>");  // TODO: implement reserve (Part 2)
        System.out.println("> upload_availability <date>");
        System.out.println("> cancel <appointment_id>");  // TODO: implement cancel (extra credit)
        System.out.println("> add_doses <vaccine> <number>");
        System.out.println("> show_appointments");  // TODO: implement show_appointments (Part 2)
        System.out.println("> logout");  // TODO: implement logout (Part 2)
        System.out.println("> quit");
        System.out.println();

        // read input from user
        BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.print("> ");
            String response = "";
            try {
                response = r.readLine();
            } catch (IOException e) {
                System.out.println("Please try again!");
            }
            // split the user input by spaces
            String[] tokens = response.split(" ");
            // check if input exists
            if (tokens.length == 0) {
                System.out.println("Please try again!");
                continue;
            }
            // determine which operation to perform
            String operation = tokens[0];
            if (operation.equals("create_patient")) {
                createPatient(tokens);
            } else if (operation.equals("create_caregiver")) {
                createCaregiver(tokens);
            } else if (operation.equals("login_patient")) {
                loginPatient(tokens);
            } else if (operation.equals("login_caregiver")) {
                loginCaregiver(tokens);
            } else if (operation.equals("search_caregiver_schedule")) {
                searchCaregiverSchedule(tokens);
            } else if (operation.equals("reserve")) {
                reserve(tokens);
            } else if (operation.equals("upload_availability")) {
                uploadAvailability(tokens);
            } else if (operation.equals("cancel")) {
                cancel(tokens);
            } else if (operation.equals("add_doses")) {
                addDoses(tokens);
            } else if (operation.equals("show_appointments")) {
                showAppointments(tokens);
            } else if (operation.equals("logout")) {
                logout(tokens);
            } else if (operation.equals("quit")) {
                System.out.println("Bye!");
                return;
            } else {
                System.out.println("Invalid operation name!");
            }
        }
    }

    private static void createPatient(String[] tokens) {
        // TODO: Part 1
        if (tokens.length != 3) {
            System.out.println("Failed to create users.");
            return;
        }

        String username = tokens[1];
        String password = tokens[2];

        if (usernameExistsPatient(username)) {
            System.out.println("Username taken, try again!");
            return;
        }
        byte[] salt = Util.generateSalt();
        byte[] hash = Util.generateHash(password, salt);
        // create the patient
        try {
            currentPatient = new Patient.PatientBuilder(username, salt, hash).build();
            // save to patient information to our database
            currentPatient.saveToDB();
            System.out.println("Created user " + username);
        } catch (SQLException e) {
            System.out.println("Failed to create user.");
            e.printStackTrace();
        }
    }

    private static boolean usernameExistsPatient(String username) {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String selectName = "SELECT * FROM Patients WHERE Name = ?";
        try {
            PreparedStatement statement = con.prepareStatement(selectName);
            statement.setString(1, username);
            ResultSet resultSet  = statement.executeQuery();
            return resultSet.isBeforeFirst();
        } catch (SQLException e) {
            System.out.println("Error occured when checking username.");
            e.printStackTrace();
        } finally {
            cm.closeConnection();
        }
        return true;
    }

    private static void createCaregiver(String[] tokens) {
        // create_caregiver <username> <password>
        // check 1: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Failed to create user.");
            return;
        }
        String username = tokens[1];
        String password = tokens[2];
        // check 2: check if the username has been taken already
        if (usernameExistsCaregiver(username)) {
            System.out.println("Username taken, try again!");
            return;
        }
        byte[] salt = Util.generateSalt();
        byte[] hash = Util.generateHash(password, salt);
        // create the caregiver
        try {
            currentCaregiver = new Caregiver.CaregiverBuilder(username, salt, hash).build();
            // save to caregiver information to our database
            currentCaregiver.saveToDB();
            System.out.println("Created user " + username);
        } catch (SQLException e) {
            System.out.println("Failed to create user.");
            e.printStackTrace();
        }
    }
    private static boolean usernameExistsCaregiver(String username) {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String selectUsername = "SELECT * FROM Caregivers WHERE Username = ?";
        try {
            PreparedStatement statement = con.prepareStatement(selectUsername);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.isBeforeFirst();
        } catch (SQLException e) {
            System.out.println("Error occurred when checking username");
            e.printStackTrace();
        } finally {
            cm.closeConnection();
        }
        return true;
    }

    private static void loginPatient(String[] tokens) {
        // TODO: Part 1
        if(currentCaregiver != null || currentPatient != null) {
            System.out.println("User already logged in.");
            return;
        }

        if(tokens.length != 3) {
            System.out.println("Login failed.");
            return;
        }
        String username = tokens[1];
        String password = tokens[2];

        Patient patient = null;
        try {
            patient = new Patient.PatientGetter(username, password).get();
        } catch(SQLException e) {
            System.out.println("Login failed.");
            e.printStackTrace();
        }
        if(patient == null) {
            System.out.println("Login failed.");
        } else {
            System.out.println("Logged in as: " + username);
            currentPatient = patient;
        }
    }

    private static void loginCaregiver(String[] tokens) {
        // login_caregiver <username> <password>
        // check 1: if someone's already logged-in, they need to log out first
        if (currentCaregiver != null || currentPatient != null) {
            System.out.println("User already logged in.");
            return;
        }
        // check 2: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Login failed.");
            return;
        }
        String username = tokens[1];
        String password = tokens[2];

        Caregiver caregiver = null;
        try {
            caregiver = new Caregiver.CaregiverGetter(username, password).get();
        } catch (SQLException e) {
            System.out.println("Login failed.");
            e.printStackTrace();
        }
        // check if the login was successful
        if (caregiver == null) {
            System.out.println("Login failed.");
        } else {
            System.out.println("Logged in as: " + username);
            currentCaregiver = caregiver;
        }
    }

    private static void searchCaregiverSchedule(String[] tokens) {
        // TODO: Part 2
        // If no user is logged in, print “Please login first!”.
        if (currentCaregiver == null && currentPatient == null) {
            System.out.println("Please login first!");
            return;
        }

        // If the input is invalid, print "Please try again!".
        if (tokens.length != 2) {
            System.out.println("Please try again!");
            return;
        }

        String date = tokens[1];

        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String username = "SELECT Username FROM Availabilities WHERE Time = ? ORDER BY Username ASC";
        String doses = "SELECT * FROM Vaccines";

        try {
            PreparedStatement statementAvailable = con.prepareStatement(username);
            PreparedStatement statementDoses = con.prepareStatement(doses);
            Date temp = Date.valueOf(date);
            statementAvailable.setDate(1, temp);
            ResultSet resultSetUsername = statementAvailable.executeQuery();
            ResultSet resultSetDoses = statementDoses.executeQuery();

            while (resultSetUsername.next()) {
                System.out.print(resultSetUsername.getString("Username") + " ");
                System.out.println();
                // ?
            }
            while (resultSetDoses.next()) {
                System.out.print(resultSetDoses.getString("Name") + " " + resultSetDoses.getString("Doses"));
            }
        } catch (SQLException e) {
            System.out.println("Please try again!");
            e.printStackTrace();
        } catch (IllegalArgumentException i) {
            System.out.println("Please select a valid date!");
        } finally {
            cm.closeConnection();
        }
    }

    private static void reserve(String[] tokens) {
        // TODO: Part 2
        // If no user is logged in, print “Please login first!”.
        if (currentCaregiver == null && currentPatient == null) {
            System.out.println("Please login first!");
            return;
        }
        // If the current user logged in is not a patient, print "Please login as a patient!".
        if (currentCaregiver != null && currentPatient == null) {
            System.out.println("Please login as a patient!");
            return;
        }
        // If the input is invalid, print "Please try again!".
        if (tokens.length != 3) {
            System.out.println("Please try again!");
            return;
        }
        String inputDate = tokens[1];
        String inputVaccine = tokens[2];


        try {
            Date date = Date.valueOf(inputDate);
            Availabilities availabilities = new Availabilities.AvailabilitiesGetter(date).get();
            // If there’s no available caregiver, print “No Caregiver is available!”.
            if (availabilities == null) {
                System.out.println("No Caregiver is available!");
                return;
            }

            Vaccine vaccine = new Vaccine.VaccineGetter(inputVaccine).get();
            // If vaccine is not valid, print "This vaccine is not valid!".
            if (vaccine == null) {
                System.out.println("This vaccine is not valid!");
                return;
            }

            // If not enough vaccine doses are available, print "Not enough available doses!".
            if (vaccine.getAvailableDoses() <= 0) {
                System.out.println("Not enough available doses!");
                return;
            }
            String availCaregiver = availabilities.getName();
            Appointment appointment = new Appointment.AppointmentBuilder(date, availCaregiver, inputVaccine, currentPatient.getName()).build();
            appointment.makeAppointment();
            availabilities.delete();
            vaccine.decreaseAvailableDoses(1);
            // If there are available caregivers, choose the caregiver by alphabetical order
            // and print “Appointment ID: {appointment_id}, Caregiver username: {username}”.
            System.out.println("Appointment ID: {" + appointment.getID() + "}, Caregiver username: {" + availCaregiver + "}");
        } catch (SQLException e) {
            System.out.println("Please try again!");
            e.printStackTrace();
        }

    }

    private static void uploadAvailability(String[] tokens) {
        // upload_availability <date>
        // check 1: check if the current logged-in user is a caregiver
        if (currentCaregiver == null) {
            System.out.println("Please login as a caregiver first!");
            return;
        }
        // check 2: the length for tokens need to be exactly 2 to include all information (with the operation name)
        if (tokens.length != 2) {
            System.out.println("Please try again!");
            return;
        }
        String date = tokens[1];
        try {
            Date d = Date.valueOf(date);
            currentCaregiver.uploadAvailability(d);
            System.out.println("Availability uploaded!");
        } catch (IllegalArgumentException e) {
            System.out.println("Please enter a valid date!");
        } catch (SQLException e) {
            System.out.println("Error occurred when uploading availability");
            e.printStackTrace();
        }
    }

    private static void cancel(String[] tokens) {
        // TODO: Extra credit
        // If the user is not login, print "Please login first!"
        if (currentCaregiver == null && currentPatient == null) {
            System.out.println("Please login first!");
            return;
        }

        // If the input is invalid, print "Please try again!".
        if (tokens.length != 2) {
            System.out.println("Please try again!");
            return;
        }

        try {
            int id = Integer.parseInt(tokens[1]);
            Appointment appointment = new Appointment.AppointmentGetter(id).get();
            // If there is no appointment on this date, print "Please try again!".
            if (appointment == null) {
                System.out.println("Please try again!");
                return;
            }

            if (!currentPatient.getName().equals(appointment.getPatient())) {
                System.out.println("Please try again!");
                return;
            }
            if (!currentCaregiver.getUsername().equals(appointment.getCaregiver())) {
                System.out.println("Please try again!");
                return;
            }

            appointment.deleteAppointment(id);
            Vaccine vaccine = new Vaccine.VaccineGetter(appointment.getVaccine()).get();
            vaccine.increaseAvailableDoses(1);
            System.out.println(id + "has cancelled the appointment!");
        } catch (SQLException e) {
            System.out.println("Please try again!");
            e.printStackTrace();
        }
    }

    private static void addDoses(String[] tokens) {
        // add_doses <vaccine> <number>
        // check 1: check if the current logged-in user is a caregiver
        if (currentCaregiver == null) {
            System.out.println("Please login as a caregiver first!");
            return;
        }
        // check 2: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Please try again!");
            return;
        }
        String vaccineName = tokens[1];
        int doses = Integer.parseInt(tokens[2]);
        Vaccine vaccine = null;
        try {
            vaccine = new Vaccine.VaccineGetter(vaccineName).get();
        } catch (SQLException e) {
            System.out.println("Error occurred when adding doses");
            e.printStackTrace();
        }
        // check 3: if getter returns null, it means that we need to create the vaccine and insert it into the Vaccines
        //          table
        if (vaccine == null) {
            try {
                vaccine = new Vaccine.VaccineBuilder(vaccineName, doses).build();
                vaccine.saveToDB();
            } catch (SQLException e) {
                System.out.println("Error occurred when adding doses");
                e.printStackTrace();
            }
        } else {
            // if the vaccine is not null, meaning that the vaccine already exists in our table
            try {
                vaccine.increaseAvailableDoses(doses);
            } catch (SQLException e) {
                System.out.println("Error occurred when adding doses");
                e.printStackTrace();
            }
        }
        System.out.println("Doses updated!");
    }

    private static void showAppointments(String[] tokens) {
        // TODO: Part 2
        // If no user is logged in, print “Please login first!”.
        if (currentCaregiver == null && currentPatient == null) {
            System.out.println("Please login first!");
            return;
        }
        // If the input is invalid, print "Please try again!".
        if (tokens.length != 1) {
            System.out.println("Please try again!");
            return;
        }

        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();
        String selectCaregiver = "SELECT id, Vaccines, date, Patients FROM Appointment WHERE Patients = ? ORDER BY id ASC";
        String selectPatient = "SELECT id, Vaccines, date, Caregivers FROM Appointment WHERE Caregivers = ? ORDER BY id ASC";

        try {
            if (currentCaregiver != null) {
                PreparedStatement statementCaregiver = con.prepareStatement(selectCaregiver);
                statementCaregiver.setString(1, currentCaregiver.getUsername());
                ResultSet resultSetCaregiver = statementCaregiver.executeQuery();
                while (resultSetCaregiver.next()) {
                    System.out.println(resultSetCaregiver.getInt("id") + " " +
                            resultSetCaregiver.getString("Vaccines") + " " +
                            resultSetCaregiver.getString("Time") + " " +
                            resultSetCaregiver.getString("Patients"));
                }

            } else /* ? if (currentPatient != null) */ {
                PreparedStatement statementPatient = con.prepareStatement(selectPatient);
                statementPatient.setString(1, currentPatient.getName());
                ResultSet resultSetPatient = statementPatient.executeQuery();
                while (resultSetPatient.next()) {}
                System.out.println(resultSetPatient.getInt("id") + " " +
                        resultSetPatient.getString("Vaccines") + " " +
                        resultSetPatient.getString("Time") + " " +
                        resultSetPatient.getString("Caregivers"));
                }
            } catch (SQLException e) {
                System.out.println("Please try again!");
                e.printStackTrace();
        } finally {
            cm.closeConnection();
        }
    }

    private static void logout(String[] tokens) {
        // TODO: Part 2
        // If no user is logged in, print “Please login first!”.
        if (currentCaregiver == null && currentPatient == null) {
            System.out.println("Please login first!");
            return;
        }

        // If the input is invalid, print "Please try again!".
        if (tokens.length != 1) {
            System.out.println("Please try again!");
            return;
        }

        if (currentPatient != null) {
            currentPatient = null;
            System.out.println("Successfully logged out!");
            // ? return;
        } else /* ? if (currentCaregiver != null) */ {
            currentCaregiver = null;
            System.out.println("Successfully logged out!");
            // ? return;
        }
    }
}
