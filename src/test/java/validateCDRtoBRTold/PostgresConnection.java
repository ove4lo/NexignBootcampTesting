package validateCDRtoBRTold;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class PostgresConnection {

    public static void main(String[] args) {
        String url = "jdbc:postgresql://de5a097a346a4e80240f0439a6b166fa.serveo.net/brt-db";
        String user = "postgres";
        String password = "postgres";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("Успешное подключение к PostgreSQL!");
        } catch (SQLException e) {
            System.err.println("Ошибка подключения: " + e.getMessage());
        }
    }
}