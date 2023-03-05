package scheduler.model;

import scheduler.db.ConnectionManager;
import scheduler.util.Util;

import java.sql.*;
import java.util.Arrays;

public class Availabilities {
    private final Date date;
    private final String name;

    private Availabilities (AvailabilitiesGetter getter) {
        this.date = getter.date;
        this.name = getter.name;
    }

    public Date getDate() {
        return this.date;
    }

    public String getName() {
        return this.name;
    }

    public void delete() throws SQLException {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String deleteAvailabilities = "DELETE FROM Availabilities WHERE Date = ? AND Name = ?";
        try {
            PreparedStatement statement = con.prepareStatement(deleteAvailabilities);
            statement.setDate(1, this.date);
            statement.setString(2, this.name);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException();
        } finally {
            cm.closeConnection();
        }
    }

    public static class AvailabilitiesGetter {
        private final Date date;
        private String name;

        public AvailabilitiesGetter(Date date) {
            this.date = date;
        }

        public Availabilities get() throws SQLException {
            ConnectionManager cm = new ConnectionManager();
            Connection con = cm.createConnection();

            String getAvailabilities = "SELECT Date, Name FROM Availabilities WHERE Date = ? ORDER BY Name ASC";
            try {
                PreparedStatement statement = con.prepareStatement(getAvailabilities);
                statement.setDate(1, this.date);
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    this.name = resultSet.getString("Username");
                    return new Availabilities(this);
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
