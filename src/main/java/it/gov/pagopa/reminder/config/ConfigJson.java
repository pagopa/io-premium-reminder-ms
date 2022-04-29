package it.gov.pagopa.reminder.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import it.gov.pagopa.reminder.model.JsonLoader;

@Configuration
public class ConfigJson {
	
	@Value("classpath:data/messageSchema.json")
	private Resource messageSchema;
	
	@Value("classpath:data/messageStatusSchema.json")
	private Resource messageStatusSchema;
	
	@Bean(name="messageSchema")
	public JsonLoader getMessageSchema() throws IOException {
		return new JsonLoader(messageSchema);
	}
	
	@Bean(name="messageStatusSchema")
	public JsonLoader getMessageStatusSchema() throws IOException {
		return new JsonLoader(messageStatusSchema);
	}

}
