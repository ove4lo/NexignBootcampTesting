package validateCDRtoBRTnew.generateCDR;

import validateCDRtoBRTnew.additionally.CdrDTO;
import java.util.concurrent.ThreadLocalRandom;

public class GenerateInvalidCallTypeCdr extends CdrBase {

    public CdrDTO generateInvalidCallTypeCdrRecord() {
        String invalidType = INVALID_CALL_TYPES[
                ThreadLocalRandom.current().nextInt(INVALID_CALL_TYPES.length)
                ];
        return this.withCallType(invalidType).build();
    }

    public static CdrDTO generate() {
        return new GenerateInvalidCallTypeCdr().generateInvalidCallTypeCdrRecord();
    }
}