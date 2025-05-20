package validateCDRtoBRTnew.generateCDR;

import validateCDRtoBRTnew.additionally.CdrDTO;

import java.io.FileWriter;
import java.io.IOException;

public class GenerateValidCdr {
    public static void main(String[] args) {
        try {
            generateValidCdrFile("cdr_valid.txt", 10);
            System.out.println("Файл cdr_valid.txt успешно создан с 10 валидными записями");
        } catch (IOException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    public static void generateValidCdrFile(String filename, int recordCount) throws IOException {
        try (FileWriter writer = new FileWriter(filename)) {
            for (int i = 0; i < recordCount; i++) {
                writer.write(new CdrBase().build() + "\n");
            }
        }
    }

    // Метод для использования в тестах
    public static CdrDTO generateSingleValidRecord() {
        return new CdrBase().build();
    }
}