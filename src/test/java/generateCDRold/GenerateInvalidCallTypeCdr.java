package generateCDRold;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class GenerateInvalidCallTypeCdr{

    private static final String ROMASHKA_PREFIX = "79";
    private static final String OTHER_PREFIXES = "77,78,70,75"; //другие операторы

    public static void main(String[] args) {
        try {
            generateInvalidCdrFile("cdr_invalid_call_type.txt", 10);
            System.out.println("Файл cdr_invalid_call_type.txt успешно создан с 10 невалидными записями");
        } catch (IOException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    //генерация CDR файла
    public static void generateInvalidCdrFile(String filename, int recordCount) throws IOException {
        try (FileWriter writer = new FileWriter(filename)) {
            for (int i = 0; i < recordCount; i++) {
                writer.write(generateValidRecord() + "\n");
            }
        }
    }

    //генерация валидных записей
    private static String generateValidRecord() {
        Random random = new Random();

        //Входящий/исходящий звонок
        String invalidCallType = generateInvalidCallType();

        //генерация номера (один обязательно Ромашка)
        boolean firstIsRomashka = random.nextBoolean();
        String number1 = firstIsRomashka ? generateRomashkaNumber() : generateOtherNumber();
        String number2 = !firstIsRomashka ? generateRomashkaNumber() : generateOtherNumber();

        //генерация дат
        LocalDateTime startTime = LocalDateTime.now()
                .minusDays(random.nextInt(365))
                .minusHours(random.nextInt(24))
                .minusMinutes(random.nextInt(60));

        LocalDateTime endTime = startTime.plusMinutes(1 + random.nextInt(120));

        return String.format("%s,%s,%s,%s,%s",
                invalidCallType,
                number1,
                number2,
                startTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                endTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }
    //генерация инвалидного типа звонка
    private static String generateInvalidCallType() {
        // Генерируем случайный НЕправильный тип звонка
        String[] invalidTypes = {"00", "-1", "AB", "XX", "1"};
        return invalidTypes[ThreadLocalRandom.current().nextInt(invalidTypes.length)];
    }

    //генерация номера Ромашка
    private static String generateRomashkaNumber() {
        return ROMASHKA_PREFIX + generateRandomDigits(9);
    }

    //генерация другого номера
    private static String generateOtherNumber() {
        String[] prefixes = OTHER_PREFIXES.split(",");
        String prefix = prefixes[new Random().nextInt(prefixes.length)];
        return prefix + generateRandomDigits(9);
    }

    //генерация цифр для номера
    private static String generateRandomDigits(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(ThreadLocalRandom.current().nextInt(0, 10));
        }
        return sb.toString();
    }
}