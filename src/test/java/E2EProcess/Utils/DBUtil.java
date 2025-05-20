package E2EProcess.Utils;

import nexign.com.TestConstants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class DBUtil {
    private static String DB_URL;
    private static String DB_USER;
    private static String DB_PASSWORD;

    static {
        try {
            Properties props = new Properties();
            props.load(Files.newInputStream(Paths.get("src/test/resources/config.properties")));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load database configuration", e);
        }
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    //текущий баланс абонента
    public static double getSubscriberBalance(String msisdn) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT balance FROM subscribers WHERE msisdn = ?")) {
            stmt.setString(1, msisdn);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getDouble("balance") : -1;
        }
    }

    //обновление баланса абонента
    public static void updateSubscriberBalance(String msisdn, double newBalance) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE subscribers SET balance = ? WHERE msisdn = ?")) {
            stmt.setDouble(1, newBalance);
            stmt.setString(2, msisdn);
            stmt.executeUpdate();
        }
    }

    //проверка на наличие записи о звонке в CDR
    public static boolean isCallRecorded(String callerNumber, String receiverNumber,
                                         String callType, LocalDateTime startTime) throws SQLException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT COUNT(*) FROM cdrs WHERE serviced_msisdn = ? AND other_msisdn = ? " +
                             "AND call_type = ? AND start_date_time >= ?")) {
            stmt.setString(1, callerNumber);
            stmt.setString(2, receiverNumber);
            stmt.setString(3, callType);
            stmt.setString(4, startTime.format(formatter));

            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    //обновление данных абонента (баланс и тариф)
    public static void updateSubscriber(String msisdn, double balance, long tariffId) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE subscribers SET balance = ?, tariff_id = ? WHERE msisdn = ?")) {
            stmt.setDouble(1, balance);
            stmt.setLong(2, tariffId);
            stmt.setString(3, msisdn);
            stmt.executeUpdate();
        }
    }

    //получает количество оставшихся минут по тарифу
    public static int getRemainingMinutes(String msisdn) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT remaining_minutes FROM subscriber_packages WHERE msisdn = ?")) {
            stmt.setString(1, msisdn);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt("remaining_minutes") : -1;
        }
    }

    //устанавление количества использованных минут и пересчитывает оставшиеся
    public static void setUsedMinutes(String msisdn, int usedMinutes) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE subscriber_packages SET used_minutes = ?, remaining_minutes = ? WHERE msisdn = ?")) {
            stmt.setInt(1, usedMinutes);
            stmt.setInt(2, TestConstants.MONTHLY_PACKAGE_MINUTES - usedMinutes);
            stmt.setString(3, msisdn);
            stmt.executeUpdate();
        }
    }

    //сброс счетчики использованных минут
    public static void resetUsedMinutes(String msisdn) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE subscriber_packages SET used_minutes = 0, remaining_minutes = ? WHERE msisdn = ?")) {
            stmt.setInt(1, TestConstants.MONTHLY_PACKAGE_MINUTES);
            stmt.setString(2, msisdn);
            stmt.executeUpdate();
        }
    }
}