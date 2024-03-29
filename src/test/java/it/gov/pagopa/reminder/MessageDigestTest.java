package it.gov.pagopa.reminder;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import it.gov.pagopa.reminder.util.ShaUtils;

public class MessageDigestTest {

    @Test
    public void testShard() throws Exception {
        String fiscalCode = "EEEEEE00E00E000B";
        Assertions.assertEquals(ShaUtils.getHexString(fiscalCode).substring(0, 1), "4");
    }
}
