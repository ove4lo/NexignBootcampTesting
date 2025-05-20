package validateCDRtoBRTnew.generateCDR;

import validateCDRtoBRTnew.additionally.CdrDTO;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class CdrBase {
    protected static final String ROMASHKA_PREFIX = "79";
    protected static final String[] OTHER_PREFIXES = {"77", "78", "70", "75"};
    protected static final String[] VALID_CALL_TYPES = {"1", "2"};
    protected static final String[] INVALID_CALL_TYPES = {"00", "-1", "AB", "XX", "3"};

    protected String callType;
    protected String servicedMsisdn;
    protected String otherMsisdn;
    protected LocalDateTime startDateTime;
    protected LocalDateTime finishDateTime;
    protected Random random = new Random();

    public CdrBase() {
        this.callType = VALID_CALL_TYPES[random.nextInt(VALID_CALL_TYPES.length)];
        this.servicedMsisdn = generateRomashkaNumber();
        this.otherMsisdn = generateOtherNumber();
        this.startDateTime = generateRandomDateTime();
        this.finishDateTime = startDateTime.plusMinutes(1 + random.nextInt(120));
    }

    public CdrBase withCallType(String callType) {
        this.callType = callType;
        return this;
    }

    public CdrBase withServicedMsisdn(String msisdn) {
        this.servicedMsisdn = msisdn;
        return this;
    }

    public CdrBase withOtherMsisdn(String msisdn) {
        this.otherMsisdn = msisdn;
        return this;
    }

    public CdrBase withStartDateTime(LocalDateTime start) {
        this.startDateTime = start;
        return this;
    }

    public CdrBase withFinishDateTime(LocalDateTime finish) {
        this.finishDateTime = finish;
        return this;
    }

    public CdrBase withDurationMinutes(int minutes) {
        this.finishDateTime = this.startDateTime.plusMinutes(minutes);
        return this;
    }

    public CdrDTO build() {
        return CdrDTO.create(callType, servicedMsisdn, otherMsisdn,
                startDateTime, finishDateTime);
    }

    protected LocalDateTime generateRandomDateTime() {
        return LocalDateTime.now()
                .minusDays(random.nextInt(365))
                .minusHours(random.nextInt(24))
                .minusMinutes(random.nextInt(60));
    }

    protected static String generateRomashkaNumber() {
        return ROMASHKA_PREFIX + generateRandomDigits(9);
    }

    protected static String generateOtherNumber() {
        String prefix = OTHER_PREFIXES[new Random().nextInt(OTHER_PREFIXES.length)];
        return prefix + generateRandomDigits(9);
    }

    protected static String generateInvalidNumber(int length) {
        return generateRandomDigits(length);
    }

    protected static String generateRandomDigits(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(ThreadLocalRandom.current().nextInt(0, 10));
        }
        return sb.toString();
    }
}