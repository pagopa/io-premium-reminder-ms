package it.gov.pagopa.reminder.util;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class LocalDateTimeDeserializer implements JsonDeserializer<LocalDateTime> {

	private static final String UNDEFINED = "undefined";
	
	@Override
    public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return null != json.getAsString() && !UNDEFINED.equalsIgnoreCase(json.getAsString()) ?
        		LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ofPattern(Constants.DATE_FORMAT)) : null;
    }
}
