package validateCDRtoBRTnew.additionally;

import java.time.LocalDateTime;

public record CdrDTO(
        String callType,
        String servicedMsisdn,
        String otherMsisdn,
        LocalDateTime startDateTime,
        LocalDateTime finishDateTime
) {
    public static CdrDTO create(String callType, String msisdn1, String msisdn2,
                                LocalDateTime start, LocalDateTime end) {
        return new CdrDTO(callType, msisdn1, msisdn2, start, end);
    }


}