package it.gov.pagopa.reminder.deserializer;

import org.apache.kafka.common.serialization.Deserializer;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.gov.pagopa.reminder.dto.PaymentMessage;
import lombok.extern.slf4j.Slf4j;
@Slf4j
public class PaymentMessageDeserializer implements Deserializer<PaymentMessage> {
	
	ObjectMapper mapper;

	public PaymentMessageDeserializer(ObjectMapper mapper) {
		this.mapper = mapper;
	}
	

	@Override
	public PaymentMessage deserialize(String s, byte[] bytes) {
			
		PaymentMessage paymentMessage = null;
		try {
			paymentMessage = mapper.readValue(bytes, PaymentMessage.class);
		} catch (Exception e) {
			log.error("Error in deserializing the PaymentMessage for consumer payment-updates");
			log.error(e.getMessage());
		}
		return paymentMessage;
	}
}
