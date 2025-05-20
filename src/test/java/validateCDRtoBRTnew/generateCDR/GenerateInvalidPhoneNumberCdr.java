package validateCDRtoBRTnew.generateCDR;

import validateCDRtoBRTnew.additionally.CdrDTO;

import java.util.Random;

public class GenerateInvalidPhoneNumberCdr {

    public static CdrDTO generate() {
        Random random = new Random();
        boolean firstInvalid = random.nextBoolean();

        CdrBase cdrBase = new CdrBase();

        if (firstInvalid) {
            cdrBase.withServicedMsisdn(CdrBase.generateInvalidNumber(6));
        } else {
            cdrBase.withOtherMsisdn(CdrBase.generateInvalidNumber(2));
        }

        return cdrBase.build();
    }

    public static CdrDTO generateInvalidPhoneNumberRecord() {
        return generate();
    }
}
