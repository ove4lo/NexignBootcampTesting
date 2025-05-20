package validateCDRtoBRTnew;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import validateCDRtoBRTnew.additionally.CdrDTO;
import validateCDRtoBRTnew.additionally.RabbitMqClient;
import validateCDRtoBRTnew.generateCDR.GenerateInvalidCallTypeCdr;
import validateCDRtoBRTnew.generateCDR.GenerateInvalidPhoneNumberCdr;
import validateCDRtoBRTnew.generateCDR.GenerateInvalidTimeOverlapCdr;
import validateCDRtoBRTnew.generateCDR.GenerateValidCdr;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

public class cdrToBrtValidationTests {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    //Успешная валидация CDR файла
    @Test
    public void validCdrTest() throws IOException, TimeoutException {
        CdrDTO validCdr = GenerateValidCdr.generateSingleValidRecord();
        String jsonMessage = objectMapper.writeValueAsString(validCdr);

        RabbitMqClient rabbitMqClient = new RabbitMqClient();

        try {
            rabbitMqClient.channel.queueDeclare(RabbitMqClient.getResponseQueue(), true, false, false, null);
            rabbitMqClient.channel.queuePurge(RabbitMqClient.getResponseQueue());

            rabbitMqClient.publishToExchange(jsonMessage);
            System.out.println("Отправлено сообщение CDR:\n" + jsonMessage);

            String responseBody = rabbitMqClient.waitForResponseMessage(60);
            System.out.println("Получен ответ от BRT:\n" + responseBody);
            Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);

            assertEquals("SUCCESS", responseMap.get("status"), "Ожидался статус SUCCESS");

        } finally {
            rabbitMqClient.close();
        }
    }

    //Тест на некорректный тип номера
    @Test
    public void invalidCallTypeCdrTest() throws IOException, TimeoutException {
        CdrDTO invalidCdr = GenerateInvalidCallTypeCdr.generate();
        String jsonMessage = objectMapper.writeValueAsString(invalidCdr);

        RabbitMqClient rabbitMqClient = new RabbitMqClient();

        try {
            rabbitMqClient.channel.queueDeclare(RabbitMqClient.getResponseQueue(), true, false, false, null);
            rabbitMqClient.channel.queuePurge(RabbitMqClient.getResponseQueue());

            rabbitMqClient.publishToExchange(jsonMessage);
            System.out.println("Отправлен невалидный CDR (callType):\n" + jsonMessage);

            String responseBody = rabbitMqClient.waitForResponseMessage(10);
            System.out.println("Получен ответ от BRT:\n" + responseBody);

            assertNull(responseBody, "Ожидалось отсутствие ответа для невалидного callType");

        } finally {
            rabbitMqClient.close();
        }
    }

    //Тест на некорректный номер телефона
    @Test
    public void invalidPhoneNumberCdrTest() throws IOException, TimeoutException {
        CdrDTO invalidCdr = GenerateInvalidPhoneNumberCdr.generate();
        String jsonMessage = objectMapper.writeValueAsString(invalidCdr);

        RabbitMqClient rabbitMqClient = new RabbitMqClient();

        try {
            rabbitMqClient.channel.queueDeclare(RabbitMqClient.getResponseQueue(), true, false, false, null);
            rabbitMqClient.channel.queuePurge(RabbitMqClient.getResponseQueue());

            rabbitMqClient.publishToExchange(jsonMessage);
            System.out.println("Отправлен CDR:\n" + jsonMessage);

            String responseBody = rabbitMqClient.waitForResponseMessage(60);
            System.out.println("Получен ответ от BRT:\n" + responseBody);

            Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);

            assertEquals("ERROR", responseMap.get("status"), "Ожидался статус ERROR из-за некорректного номера");

        } finally {
            rabbitMqClient.close();
        }
    }

    //Тест на пересечение полночи
    @Test
    public void invalidTimeOverlapCdrTest() throws IOException, TimeoutException {
        CdrDTO invalidCdr = GenerateInvalidTimeOverlapCdr.generate();
        String jsonMessage = objectMapper.writeValueAsString(invalidCdr);

        RabbitMqClient rabbitMqClient = new RabbitMqClient();

        try {
            rabbitMqClient.channel.queueDeclare(RabbitMqClient.getResponseQueue(), true, false, false, null);
            rabbitMqClient.channel.queuePurge(RabbitMqClient.getResponseQueue());

            rabbitMqClient.publishToExchange(jsonMessage);
            System.out.println("Отправлен CDR:\n" + jsonMessage);

            String responseBody = rabbitMqClient.waitForResponseMessage(60);
            System.out.println("Получен ответ от BRT:\n" + responseBody);

            Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);

            assertEquals("ERROR", responseMap.get("status"), "Ожидался статус ERROR из-за пересечния полночи");

        } finally {
            rabbitMqClient.close();
        }
    }

    //Тест на собраннеы вместе невалидные CDR
    @Test
    public void mixedValidAndInvalidCdrTest() throws IOException, TimeoutException {
        CdrDTO validCdr1 = GenerateValidCdr.generateSingleValidRecord();
        CdrDTO invalidCdr = GenerateInvalidPhoneNumberCdr.generate();
        CdrDTO validCdr2 = GenerateValidCdr.generateSingleValidRecord();

        List<CdrDTO> records = List.of(validCdr1, invalidCdr, validCdr2);
        String jsonMessage = objectMapper.writeValueAsString(records);

        RabbitMqClient rabbitMqClient = new RabbitMqClient();

        try {
            rabbitMqClient.channel.queueDeclare(RabbitMqClient.getResponseQueue(), true, false, false, null);
            rabbitMqClient.channel.queuePurge(RabbitMqClient.getResponseQueue());

            rabbitMqClient.publishToExchange(jsonMessage);
            System.out.println("Отправлен CDR:\n" + jsonMessage);

            String responseBody = rabbitMqClient.waitForResponseMessage(60);
            System.out.println("Получен ответ от BRT:\n" + responseBody);

            Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);

            assertEquals("ERROR", responseMap.get("status"), "Ожидался статус ERROR из-за одной невалидной записи");

        } finally {
            rabbitMqClient.close();
        }
    }

}
