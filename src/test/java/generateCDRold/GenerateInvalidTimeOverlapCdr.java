package generateCDRold;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class GenerateInvalidTimeOverlapCdr {

    private static final String ROMASHKA_PREFIX = "79";
    private static final String OTHER_PREFIXES = "77,78,70,75";

    public static void main(String[] args) {
        try {
            generateInvalidCdrFile("cdr_invalid_time_overlap.txt", 10);
            System.out.println("Файл cdr_invalid_time_overlap.txt успешно создан с 10 невалидными записями");
        } catch (IOException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    //генерация файла с невалидными данными
    public static void generateInvalidCdrFile(String filename, int recordCount) throws IOException {
        try (FileWriter writer = new FileWriter(filename)) {
            for (int i = 0; i < recordCount; i++) {
                writer.write(generateInvalidDateRecord() + "\n");
            }
        }
    }

    //проверка на номер
    private static String generateInvalidDateRecord() {
        Random random = new Random();

        String callType = random.nextBoolean() ? "01" : "02";

        boolean firstIsRomashka = random.nextBoolean();
        String number1 = firstIsRomashka ? generateRomashkaNumber() : generateOtherNumber();
        String number2 = !firstIsRomashka ? generateRomashkaNumber() : generateOtherNumber();

        LocalDateTime endTime = LocalDateTime.now()
                .minusDays(random.nextInt(365))
                .minusHours(random.nextInt(24))
                .minusMinutes(random.nextInt(60));

        LocalDateTime startTime = endTime.plusMinutes(1 + random.nextInt(120));

        return String.format("%s,%s,%s,%s,%s",
                callType,
                number1,
                number2,
                startTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                endTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }

    //генераия Ромашки
    private static String generateRomashkaNumber() {
        return ROMASHKA_PREFIX + generateRandomDigits(9);
    }

    //генерация абонента другого оператора
    private static String generateOtherNumber() {
        String[] prefixes = OTHER_PREFIXES.split(",");
        String prefix = prefixes[new Random().nextInt(prefixes.length)];
        return prefix + generateRandomDigits(9);
    }

    private static String generateRandomDigits(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(ThreadLocalRandom.current().nextInt(0, 10));
        }
        return sb.toString();
    }
}