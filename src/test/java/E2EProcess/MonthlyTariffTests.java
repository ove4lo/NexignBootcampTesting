package E2EProcess;

import E2EProcess.Utils.DBUtil;
import org.junit.jupiter.api.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Properties;
import static org.junit.jupiter.api.Assertions.*;
import static E2EProcess.TestConstants.*;

public class MonthlyTariffTests {
    private static RabbitMqCl rabbitClient;
    private static LocalDateTime testStartTime;

    @BeforeAll
    public static void setup() throws Exception {
        //инициализация RabbitMQ клиента
        Properties props = new Properties();
        props.load(Files.newInputStream(Paths.get("src/test/resources/config.properties")));

        rabbitClient = new RabbitMqCl(
                props.getProperty("rabbitmq.host"),
                Integer.parseInt(props.getProperty("rabbitmq.port")),
                props.getProperty("rabbitmq.username"),
                props.getProperty("rabbitmq.password"),
                props.getProperty("rabbitmq.request.queue"),
                props.getProperty("rabbitmq.response.queue")
        );

        DBUtil.updateSubscriber(ROMASHKA_NUMBER_1, INITIAL_BALANCE, MONTHLY_TARIFF_ID);
        DBUtil.updateSubscriber(ROMASHKA_NUMBER_2, INITIAL_BALANCE, MONTHLY_TARIFF_ID);

        DBUtil.resetUsedMinutes(ROMASHKA_NUMBER_1);
    }

    @BeforeEach
    public void setUpTest() {
        testStartTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }

    @AfterAll
    public static void tearDown() throws Exception {
        rabbitClient.close();
    }

    private String generateCdr(String callType, String caller, String receiver, int durationMinutes) {
        LocalDateTime startTime = testStartTime;
        LocalDateTime endTime = startTime.plusMinutes(durationMinutes);

        return String.format("%s,%s,%s,%s,%s",
                callType, caller, receiver,
                startTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                endTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }

    private void sendCdrToProcessing(String cdr) throws Exception {
        String response = rabbitClient.sendAndReceive(cdr, "test-" + System.currentTimeMillis());
        assertEquals("{\"status\":\"PROCESSED\"}", response);
    }


    //Тарификация исходящего звонка абонента оператора "Ромашки" с тарифом "Помесячный" к абоненту другого оператора с превышением лимита на тарифе
    @Test
    public void testOutgoingCallOtherNetworkOverLimit() throws Exception {
        DBUtil.setUsedMinutes(ROMASHKA_NUMBER_1, MONTHLY_PACKAGE_MINUTES);

        double initialBalance = DBUtil.getSubscriberBalance(ROMASHKA_NUMBER_1);
        int callDuration = OVER_CALL_DURATION;
        double expectedCost = callDuration * MONTHLY_OVER_RATE;

        String cdr = generateCdr(OUTGOING_CALL_TYPE, ROMASHKA_NUMBER_1, OTHER_NETWORK_NUMBER, callDuration);
        sendCdrToProcessing(cdr);

        double newBalance = DBUtil.getSubscriberBalance(ROMASHKA_NUMBER_1);
        assertEquals(initialBalance - expectedCost, newBalance, 0.001);

        assertTrue(DBUtil.isCallRecorded(ROMASHKA_NUMBER_1, OTHER_NETWORK_NUMBER,
                OUTGOING_CALL_TYPE, testStartTime));
    }

    //Тест - Тарификация исходящего звонка абонента оператора "Ромашки" с тарифом "Помесячный" к другому абоненту оператора "Ромашки"
    @Test
    public void testOutgoingCallSameNetwork() throws Exception {
        DBUtil.resetUsedMinutes(ROMASHKA_NUMBER_1);

        double initialBalance = DBUtil.getSubscriberBalance(ROMASHKA_NUMBER_1);
        int callDuration = MEDIUM_CALL_DURATION;
        double expectedCost = 0.0;

        String cdr = generateCdr(OUTGOING_CALL_TYPE, ROMASHKA_NUMBER_1, ROMASHKA_NUMBER_2, callDuration);
        sendCdrToProcessing(cdr);

        double newBalance = DBUtil.getSubscriberBalance(ROMASHKA_NUMBER_1);
        assertEquals(initialBalance - expectedCost, newBalance, 0.001);

        assertTrue(DBUtil.isCallRecorded(ROMASHKA_NUMBER_1, ROMASHKA_NUMBER_2,
                OUTGOING_CALL_TYPE, testStartTime));

        int remainingMinutes = DBUtil.getRemainingMinutes(ROMASHKA_NUMBER_1);
        assertEquals(MONTHLY_PACKAGE_MINUTES - callDuration, remainingMinutes);
    }

    //Тест - Тарификация входящего звонка абонента оператора "Ромашки" с тарифом "Помесячный" к абоненту другого оператора с превышением лимита на тарифе
    @Test
    public void testIncomingCallOverLimit() throws Exception {
        DBUtil.setUsedMinutes(ROMASHKA_NUMBER_1, MONTHLY_PACKAGE_MINUTES);

        double initialBalance = DBUtil.getSubscriberBalance(ROMASHKA_NUMBER_1);
        int callDuration = OVER_CALL_DURATION;
        double expectedCost = 0.0;

        String cdr = generateCdr(INCOMING_CALL_TYPE, ROMASHKA_NUMBER_1, OTHER_NETWORK_NUMBER, callDuration);
        sendCdrToProcessing(cdr);

        double newBalance = DBUtil.getSubscriberBalance(ROMASHKA_NUMBER_1);
        assertEquals(initialBalance - expectedCost, newBalance, 0.001);

        assertTrue(DBUtil.isCallRecorded(ROMASHKA_NUMBER_1, OTHER_NETWORK_NUMBER,
                INCOMING_CALL_TYPE, testStartTime));

        int remainingMinutes = DBUtil.getRemainingMinutes(ROMASHKA_NUMBER_1);
        assertEquals(0, remainingMinutes);
    }
}