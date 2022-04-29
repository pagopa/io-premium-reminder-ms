package it.gov.pagopa.reminder.consumer.utils;

import org.apache.avro.AvroRuntimeException;

public class AvroConversionException extends AvroRuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = -5679199295509444208L;

	public AvroConversionException(String message) {
        super(message);
    }

    public AvroConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}
