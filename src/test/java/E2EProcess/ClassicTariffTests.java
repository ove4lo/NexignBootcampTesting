package E2EProcess;

import E2EProcess.Utils.DBUtil;
import org.junit.jupiter.api.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static E2EProcess.TestConstants.*;

public class ClassicTariffTests {
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

        //начальный баланс для тестовых абонентов
        DBUtil.updateSubscriberBalance(ROMASHKA_NUMBER_1, INITIAL_BALANCE);
        DBUtil.updateSubscriberBalance(ROMASHKA_NUMBER_2, INITIAL_BALANCE);
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

    private double calculateExpectedCost(String callType, String receiver, int duration) {
        if (callType.equals(INCOMING_CALL_TYPE)) return 0.0;

        boolean isSameNetwork = receiver.startsWith("777");
        double rate = isSameNetwork ? CLASSIC_SAME_NETWORK_RATE : CLASSIC_OTHER_NETWORK_RATE;
        return rate * duration;
    }

    //Тест - Тарификация исходящего звонка абонента оператора "Ромашки" с тарифом "Классика" к другому абоненту оператора "Ромашки"
    @Test
    public void testOutgoingCallSameNetwork() throws Exception {
        double initialBalance = DBUtil.getSubscriberBalance(ROMASHKA_NUMBER_1);
        int callDuration = MEDIUM_CALL_DURATION;
        double expectedCost = calculateExpectedCost(OUTGOING_CALL_TYPE, ROMASHKA_NUMBER_2, callDuration);

        String cdr = generateCdr(OUTGOING_CALL_TYPE, ROMASHKA_NUMBER_1, ROMASHKA_NUMBER_2, callDuration);
        sendCdrToProcessing(cdr);

        double newBalance = DBUtil.getSubscriberBalance(ROMASHKA_NUMBER_1);
        assertEquals(initialBalance - expectedCost, newBalance, 0.001);

        assertTrue(DBUtil.isCallRecorded(ROMASHKA_NUMBER_1, ROMASHKA_NUMBER_2,
                OUTGOING_CALL_TYPE, testStartTime));
    }

    //Тест - Тарификация исходящего звонка абонента оператора "Ромашки" с тарифом "Классика" к абоненту другого оператора
    @Test
    public void testOutgoingCallOtherNetwork() throws Exception {

        double initialBalance = DBUtil.getSubscriberBalance(ROMASHKA_NUMBER_1);
        int callDuration = SHORT_CALL_DURATION;
        double expectedCost = calculateExpectedCost(OUTGOING_CALL_TYPE, OTHER_NETWORK_NUMBER, callDuration);

        String cdr = generateCdr(OUTGOING_CALL_TYPE, ROMASHKA_NUMBER_1, OTHER_NETWORK_NUMBER, callDuration);
        sendCdrToProcessing(cdr);

        double newBalance = DBUtil.getSubscriberBalance(ROMASHKA_NUMBER_1);
        assertEquals(initialBalance - expectedCost, newBalance, 0.001);

        assertTrue(DBUtil.isCallRecorded(ROMASHKA_NUMBER_1, OTHER_NETWORK_NUMBER,
                OUTGOING_CALL_TYPE, testStartTime));
    }

    //Тест - Тарификация входящего звонка абонента оператора "Ромашки" с тарифом "Классика" к абоненту другого оператора
    @Test
    public void testIncomingCall() throws Exception {

        double initialBalance = DBUtil.getSubscriberBalance(ROMASHKA_NUMBER_1);
        int callDuration = LONG_CALL_DURATION;
        double expectedCost = calculateExpectedCost(INCOMING_CALL_TYPE, OTHER_NETWORK_NUMBER, callDuration);

        String cdr = generateCdr(INCOMING_CALL_TYPE, ROMASHKA_NUMBER_1, OTHER_NETWORK_NUMBER, callDuration);
        sendCdrToProcessing(cdr);

        double newBalance = DBUtil.getSubscriberBalance(ROMASHKA_NUMBER_1);
        assertEquals(initialBalance - expectedCost, newBalance, 0.001);

        assertTrue(DBUtil.isCallRecorded(ROMASHKA_NUMBER_1, OTHER_NETWORK_NUMBER,
                INCOMING_CALL_TYPE, testStartTime));
    }
}