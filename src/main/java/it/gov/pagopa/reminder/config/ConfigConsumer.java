package it.gov.pagopa.reminder.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import it.gov.pagopa.reminder.consumer.MessageConsumer;
import it.gov.pagopa.reminder.consumer.MessageStatusConsumer;
import it.gov.pagopa.reminder.consumer.ReminderConsumer;
import it.gov.pagopa.reminder.dto.SenderMetadata;


@Configuration
public class ConfigConsumer {
	
	@Bean
	public MessageConsumer MessageEventConsumer() {
		return new MessageConsumer();
	}
	
	@Bean
	public MessageStatusConsumer MessageStatusEventConsumer() {
		return new MessageStatusConsumer();
	}
	
	@Bean
	public ReminderConsumer ReminderEventConsumer() {
		return new ReminderConsumer();
	}
	
	@Bean SenderMetadata ReminderEventSenderMetadata() {
		return new SenderMetadata();
	}
}
