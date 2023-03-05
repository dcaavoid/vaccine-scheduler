package scheduler.model;

import scheduler.db.ConnectionManager;
import scheduler.util.Util;

import java.sql.*;
import java.util.Arrays;

public class Appointment {
    private final int id;
    private Date date;
    private String caregiver;
    private String vaccine;
    private String patient;

    public Appointment (AppointmentBuilder builder){
        this.id = builder.id;
        this.date = builder.date;
        this.caregiver = builder.caregiver;
        this.vaccine = builder.vaccine;
        this.patient = builder.patient;
    }

    public Appointment (AppointmentGetter getter) {
        this.id = getter.id;
        this.date = getter.date;
        this.caregiver = getter.caregiver;
        this.vaccine = getter.vaccine;
        this.patient = getter.patient;
    }

    public int getID() {
        return this.id;
    }

    public Date getDate() {
        return this.date;
    }

    public String getCaregiver() {
        return this.caregiver;
    }

    public String getVaccine() {
        return this.vaccine;
    }

    public String getPatient() {
        return this.patient;
    }

    public void makeAppointment() throws SQLException {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String makeAppointment = "INSERT INTO Appointment VALUES (?, ?, ?, ?, ?)";
        try {
            PreparedStatement statement = con.prepareStatement(makeAppointment);
            statement.setInt(1, id);
            statement.setDate(2, date);
            statement.setString(3, caregiver);
            statement.setString(4, vaccine);
            statement.setString(5, patient);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException();
        } finally {
            cm.closeConnection();
        }
    }

    public void deleteAppointment(int id) throws SQLException {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String deleteAppointment = "DELETE FROM Appointment WHERE id = ?";
        try {
            PreparedStatement statement = con.prepareStatement(deleteAppointment);
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException();
        } finally {
            cm.closeConnection();
        }
    }

    public static class AppointmentBuilder {
        private final int id;
        private final Date date;
        private final String caregiver;
        private final String vaccine;
        private final String patient;

        public AppointmentBuilder(Date date, String caregiver, String vaccine, String patient) throws SQLException {
            this.id = getID();
            this.date = date;
            this.caregiver = caregiver;
            this.vaccine = vaccine;
            this.patient = patient;
        }
        public int getID() throws SQLException {
            ConnectionManager cm = new ConnectionManager();
            Connection con = cm.createConnection();

            String ID = "SELECT MAX(id) AS id FROM Appointment";
            try {
                PreparedStatement statement = con.prepareStatement(ID);
                ResultSet resultSet = statement.executeQuery();
                resultSet.next();
                return resultSet.getInt("id") + 1;
            } catch (SQLException e) {
                throw new SQLException();
            } finally {
                cm.closeConnection();
            }
        }
        public Appointment build() {
            return new Appointment(this);
        }

    }

    public static class AppointmentGetter {
        private final int id;
        private Date date;
        private String caregiver;
        private String vaccine;
        private String patient;

        public AppointmentGetter(int id) {
            this.id = id;
        }

        public Appointment get() throws SQLException {
            ConnectionManager cm = new ConnectionManager();
            Connection con = cm.createConnection();

            String getAppointment = "SELECT * FROM Appointment WHERE id = ?";
            try {
                PreparedStatement statement = con.prepareStatement(getAppointment);
                statement.setInt(1, this.id);
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    this.date = resultSet.getDate("date");
                    this.caregiver = resultSet.getString("Caregiver");
                    this.vaccine = resultSet.getString("Vaccine");
                    this.patient = resultSet.getString("Patient");
                    return new Appointment(this);
                }
                return null;
            } catch (SQLException e) {
                throw new SQLException();
            } finally {
                cm.closeConnection();
            }
        }
    }
}
