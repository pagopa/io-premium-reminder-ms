package it.gov.pagopa.reminder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import it.gov.pagopa.reminder.consumer.MessageConsumer;
import it.gov.pagopa.reminder.consumer.MessageStatusConsumer;
import it.gov.pagopa.reminder.consumer.ReminderConsumer;
import it.gov.pagopa.reminder.util.ApplicationContextProvider;

@SpringBootApplication
public class Application{
	
    public static void main(String[] args) {
    	SpringApplication.run(Application.class, args);
    	
//    	MessageConsumer messageConsumer = (MessageConsumer)ApplicationContextProvider.getBean("MessageEventConsumer");
//    	messageConsumer.init();
    	
//    	MessageStatusConsumer messageStatusConsumer = (MessageStatusConsumer)ApplicationContextProvider.getBean("MessageStatusEventConsumer");
//    	messageStatusConsumer.init();
//    	
    	ReminderConsumer notificationConsumer = (ReminderConsumer)ApplicationContextProvider.getBean("ReminderEventConsumer");
    	notificationConsumer.init();
    }
}