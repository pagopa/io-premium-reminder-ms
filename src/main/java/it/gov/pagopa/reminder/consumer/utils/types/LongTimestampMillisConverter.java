package it.gov.pagopa.reminder.consumer.utils.types;

import static it.gov.pagopa.reminder.consumer.utils.PathsPrinter.print;
import static it.gov.pagopa.reminder.consumer.utils.types.AvroTypeConverter.isLogicalType;
import static org.apache.avro.Schema.Type.LONG;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.Deque;

import org.apache.avro.AvroTypeException;
import org.apache.avro.Schema;

public class LongTimestampMillisConverter implements AvroTypeConverter {
    public static final AvroTypeConverter INSTANCE = new LongTimestampMillisConverter(DateTimeFormatter.ISO_DATE_TIME);
    public static final String VALID_JSON_FORMAT = "date time string, timestamp number";

    private final DateTimeFormatter dateTimeFormatter;

    public LongTimestampMillisConverter(DateTimeFormatter dateTimeFormatter) {
        this.dateTimeFormatter = dateTimeFormatter;
    }

    @Override
    public Object convert(Schema.Field field, Schema schema, Object jsonValue, Deque<String> path, boolean silently) {
        if (jsonValue instanceof String) {
            String dateString = (String) jsonValue;
            try {
                long dateTimestamp = dateTimeFormatter.parse(dateString).getLong(ChronoField.INSTANT_SECONDS);
                return dateTimestamp * 1000;
            } catch (DateTimeParseException exception) {
                if (silently) {
                    return new Incompatible(VALID_JSON_FORMAT);
                } else {
                    throw new AvroTypeException("Field " + print(path) + " should be a valid date time.");
                }
            }
        } else if (jsonValue instanceof Number) {
            return ((Number) jsonValue).longValue();
        }

        if (silently) {
            return new Incompatible(VALID_JSON_FORMAT);
        } else {
            throw new AvroTypeException("Field " + print(path) + " is expected to be type: java.lang.String or java.lang.Number.");
        }
    }

    @Override
    public boolean canManage(Schema schema, Deque<String> deque) {
        return LONG.equals(schema.getType()) && isLogicalType(schema, "timestamp-millis");
    }
}
