package it.gov.pagopa.reminder.deserializer;

import org.apache.kafka.common.serialization.Deserializer;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.gov.pagopa.reminder.model.JsonLoader;
import it.gov.pagopa.reminder.model.Reminder;
import tech.allegro.schema.json2avro.converter.JsonAvroConverter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AvroMessageDeserializer implements Deserializer<Reminder> {

	ObjectMapper mapper;
	JsonLoader schema;

	public AvroMessageDeserializer(JsonLoader jsSchema, ObjectMapper obMapper) {
		schema = jsSchema;
		mapper = obMapper;
	}

	@Override
	public Reminder deserialize(String topic, byte[] bytes) {	
		Reminder returnObject = null;
		JsonAvroConverter converter = new JsonAvroConverter();
		if (bytes != null) {
			try {
				byte[] binaryJson = converter.convertToJson(bytes, schema.getJsonString());
				String avroJson = new String(binaryJson);
				returnObject = mapper.readValue(avroJson, Reminder.class);
			}catch(Exception e) {
				log.error("Error in deserializing the Reminder for consumer message");
				log.error(e.getMessage());
			}
		}			
			return returnObject;
		}
	}
