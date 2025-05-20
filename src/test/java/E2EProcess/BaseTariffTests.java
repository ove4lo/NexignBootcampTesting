package E2EProcess;

import com.rabbitmq.client.ConnectionFactory;
import org.junit.jupiter.api.BeforeAll;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.nio.file.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BaseTariffTests {
    protected static Connection dbConnection;
    protected static RabbitMqCl rabbitClient;
    protected static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @BeforeAll
    public static void setup() throws Exception {
        Properties props = new Properties();
        props.load(Files.newInputStream(Paths.get("src/test/resources/config.properties")));

        rabbitClient = new RabbitMqCl(
                props.getProperty("rabbitmq.host"),
                Integer.parseInt(props.getProperty("rabbitmq.port")),
                props.getProperty("rabbitmq.username"),
                props.getProperty("rabbitmq.password"),
                "cdr.processing.queue",
                "cdr.processing.response.queue"
        );

        dbConnection = DriverManager.getConnection(
                props.getProperty("db.url"),
                props.getProperty("db.username"),
                props.getProperty("db.password"));
    }

    protected SubscriberInfo getSubscriberInfo(String msisdn) throws SQLException {
        String sql = "SELECT balance FROM subscribers WHERE msisdn = ?";
        try (PreparedStatement stmt = dbConnection.prepareStatement(sql)) {
            stmt.setString(1, msisdn);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? new SubscriberInfo(rs.getDouble("balance")) : null;
        }
    }

    protected void sendCall(String callType, String fromNumber, String toNumber,
                            LocalDateTime start, LocalDateTime end) throws Exception {
        String cdr = String.format("%s,%s,%s,%s,%s",
                callType, fromNumber, toNumber,
                start.format(dtf), end.format(dtf));
        rabbitClient.sendAndReceive(cdr, "test-" + callType);
    }

    protected void assertBalance(String msisdn, double expectedBalance) throws SQLException {
        double actualBalance = getSubscriberInfo(msisdn).balance;
        assertEquals(expectedBalance, actualBalance, 0.001,
                String.format("Ожидаемый баланс: %.2f, фактический: %.2f",
                        expectedBalance, actualBalance));
    }

    protected static class SubscriberInfo {
        public final double balance;

        public SubscriberInfo(double balance) {
            this.balance = balance;
        }
    }
}