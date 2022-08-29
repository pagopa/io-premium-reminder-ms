package it.gov.pagopa.reminder.deserializer;

import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.kafka.common.serialization.Deserializer;
import org.springframework.kafka.support.serializer.DeserializationException;

import dto.messageStatus;
import it.gov.pagopa.reminder.exception.AvroDeserializerException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AvroMessageStatusDeserializer implements Deserializer<messageStatus> {

	private final DatumReader<messageStatus> reader = new SpecificDatumReader<>(messageStatus.class);

	@Override
	public messageStatus deserialize(String topic, byte[] bytes) {
		messageStatus returnObject = null;
		if (bytes != null) {
			try {
				Decoder decoder = DecoderFactory.get().binaryDecoder(bytes, null);
				returnObject = reader.read(null, decoder);
			}catch(Exception e) {
				log.error("Error in deserializing the MessageStatus for consumer message-status|ERROR=" + e.getMessage());
				throw new AvroDeserializerException(
						"Error in deserializing the MessageStatus for consumer message-status|ERROR=" + e.getMessage(),
						bytes);
			}
		}	
		return returnObject;
	}
}
