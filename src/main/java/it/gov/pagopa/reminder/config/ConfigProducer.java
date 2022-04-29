package it.gov.pagopa.reminder.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import it.gov.pagopa.reminder.producer.ReminderProducer;

@Configuration
public class ConfigProducer {
	
	@Value("${azure.eventhub.notification.connectionString}")
	private String connectionString;
	@Value("${azure.eventhub.notification.name}")
	private String eventHubName;
	
	@Bean
	public ReminderProducer getReminderProducer() {	
		return new ReminderProducer(connectionString, eventHubName);
	}
}
