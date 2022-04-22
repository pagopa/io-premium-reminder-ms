package it.gov.pagopa.reminder.consumer.utils;

import org.apache.avro.AvroRuntimeException;

public class AvroConversionException extends AvroRuntimeException {

    public AvroConversionException(String message) {
        super(message);
    }

    public AvroConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}
