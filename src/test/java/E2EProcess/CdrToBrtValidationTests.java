package E2EProcess;

import org.junit.jupiter.api.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import static org.junit.jupiter.api.Assertions.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CdrToBrtValidationTests {

    private static RabbitMqCl rabbitClient;
    private static Properties props;
    private static final String CDR_DIR = "src/test/resources/cdr/";

    @BeforeAll
    public static void setup() throws Exception {
        props = new Properties();
        props.load(Files.newInputStream(Paths.get("src/test/resources/config.properties")));

        rabbitClient = new RabbitMqCl(
                props.getProperty("rabbitmq.host"),
                Integer.parseInt(props.getProperty("rabbitmq.port")),
                props.getProperty("rabbitmq.username"),
                props.getProperty("rabbitmq.password"),
                "cdr.validation.queue",
                "cdr.validation.response.queue"
        );
    }

    @AfterAll
    public static void tearDown() throws Exception {
        rabbitClient.close();
    }

    private String sendCdrAndGetResponse(String filename) throws Exception {
        String cdrContent = new String(Files.readAllBytes(Paths.get(CDR_DIR + filename)));
        return rabbitClient.sendAndReceive(cdrContent, "test-" + filename);
    }

    //Тест на корректный CDR
    @Test
    public void validCdrTest() throws Exception {
        String response = sendCdrAndGetResponse("cdr_valid.txt");
        assertEquals("{\"status\":\"VALID\"}", response);
    }

    //Тест на звонки, пересекающие полночь
    @Test
    public void callCrossingMidnightCdrTest() throws Exception {
        String response = sendCdrAndGetResponse("cdr_midnight.txt");
        assertTrue(response.contains("\"status\":\"SPLIT\""));
    }

    //Тест на некорректный тип звонка
    @Test
    public void invalidCallTypeCdrTest() throws Exception {
        String response = sendCdrAndGetResponse("cdr_invalid_call_type.txt");
        assertEquals("{\"status\":\"INVALID\",\"reason\":\"Некорректный тип звонка\"}", response);
    }

    //Тест на некорректный номер телефона
    @Test
    public void invalidMsisdnCdrTest() throws Exception {
        String response = sendCdrAndGetResponse("cdr_invalid_msisdn.txt");
        assertEquals("{\"status\":\"INVALID\",\"reason\":\"Некорректный номер телефона\"}", response);
    }

    //Тест на некорректное время
    @Test
    public void invalidTimeCdrTest() throws Exception {
        String response = sendCdrAndGetResponse("cdr_invalid_time.txt");
        assertEquals("{\"status\":\"INVALID\",\"reason\":\"Некорректное время\"}", response);
    }

    //Тест на время начала > времени окончания
    @Test
    public void invalidOverlapCallTimeCdrTest() throws Exception {
        String response = sendCdrAndGetResponse("cdr_invalid_time_overlap.txt");
        assertEquals("{\"status\":\"INVALID\",\"reason\":\"время начала позже времени завершения\"}", response);
    }

    //Тест на пустой файл
    @Test
    public void emptyCdrTest() throws Exception {
        String response = sendCdrAndGetResponse("cdr_empty.txt");
        assertEquals("{\"status\":\"INVALID\",\"reason\":\"Пустой CDR\"}", response);
    }
}
