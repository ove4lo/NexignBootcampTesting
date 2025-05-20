package validateCDRtoBRTnew.generateCDR;

import validateCDRtoBRTnew.additionally.CdrDTO;

import java.time.LocalDateTime;
import java.util.Random;

public class GenerateInvalidTimeOverlapCdr {

    private static CdrDTO generateInvalidRecord() {
        Random random = new Random();
        LocalDateTime finishTime = LocalDateTime.now()
                .minusDays(random.nextInt(365))
                .minusHours(random.nextInt(24))
                .minusMinutes(random.nextInt(60));

        return new CdrBase()
                .withFinishDateTime(finishTime)
                .withStartDateTime(finishTime.plusMinutes(1 + random.nextInt(120)))
                .build();
    }

    public static CdrDTO generate() {
        return generateInvalidRecord();
    }

    public static CdrDTO generateInvalidTimeOverlapRecord() {
        return generate();
    }
}
