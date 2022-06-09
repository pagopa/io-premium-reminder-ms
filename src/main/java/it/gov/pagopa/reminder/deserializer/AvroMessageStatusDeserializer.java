package it.gov.pagopa.reminder.deserializer;

import org.apache.kafka.common.serialization.Deserializer;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.gov.pagopa.reminder.dto.MessageStatus;
import it.gov.pagopa.reminder.model.JsonLoader;
import tech.allegro.schema.json2avro.converter.JsonAvroConverter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AvroMessageStatusDeserializer implements Deserializer<MessageStatus> {

	JsonLoader schema;
	ObjectMapper mapper;
	JsonAvroConverter converter;

	public void setConverter(JsonAvroConverter converter) {
		this.converter = converter;
	}
	
	public AvroMessageStatusDeserializer(JsonLoader js, ObjectMapper obMapper) {
		schema = js;
		mapper = obMapper;
	}

	@Override
	public MessageStatus deserialize(String topic, byte[] bytes) {
		MessageStatus returnObject = null;
		if (bytes != null) {
			try {
				converter = new JsonAvroConverter();
				byte[] binaryJson = converter.convertToJson(bytes, schema.getJsonString());
				String avroJson = new String(binaryJson);
				returnObject = mapper.readValue(avroJson, MessageStatus.class);
			}catch(Exception e) {
				log.error("Error in deserializing the MessageStatusDao for consumer message-status");
				log.error(e.getMessage());
			}
		}	
		return returnObject;
	}
}
