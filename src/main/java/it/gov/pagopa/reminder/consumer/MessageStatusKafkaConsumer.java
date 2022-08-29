package it.gov.pagopa.reminder.consumer;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;

import dto.messageStatus;
import it.gov.pagopa.reminder.service.ReminderService;
import lombok.extern.slf4j.Slf4j;
@Slf4j
public class MessageStatusKafkaConsumer{

	@Autowired
	ReminderService reminderService;
	
	private CountDownLatch latch = new CountDownLatch(1);
    private String payload = null;
	
	@KafkaListener(topics = "${kafka.status}", groupId = "reminder-message-status", containerFactory = "kafkaListenerContainerFactoryMessStat", autoStartup = "${messagestatus.auto.start}")
    public void messageStatusKafkaListener(messageStatus messageStatus) {

		log.info("Received message-status: {}", messageStatus);
		
		if (Objects.nonNull(messageStatus)) {

	        payload = messageStatus.toString();
	        //TODO check flag
			reminderService.updateReminder(messageStatus.getMessageId(), messageStatus.getIsRead(), false);
		}	
		latch.countDown();
    }
	
    public CountDownLatch getLatch() {
        return latch;
    }

    public String getPayload() {
        return payload;
    }
}
