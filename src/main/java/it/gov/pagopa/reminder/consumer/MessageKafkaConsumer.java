package it.gov.pagopa.reminder.consumer;

import static it.gov.pagopa.reminder.util.ReminderUtil.checkNullInMessage;

import java.util.concurrent.CountDownLatch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import dto.FeatureLevelType;
import dto.MessageContentType;
import it.gov.pagopa.reminder.model.Reminder;
import it.gov.pagopa.reminder.service.ReminderService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MessageKafkaConsumer {

	@Autowired
	ReminderService reminderService;


	private CountDownLatch latch = new CountDownLatch(1);
	private String payload = null;

	@KafkaListener(topics = "${kafka.message}", groupId = "reminder-message", autoStartup = "${message.auto.start}")
	public void messageKafkaListener(Reminder message) {		
		log.info("Received message: {}", message);
		checkNullInMessage(message);

		if(FeatureLevelType.ADVANCED.toString().equalsIgnoreCase(message.getFeature_level_type().toString())) {

			if(MessageContentType.PAYMENT.toString().equalsIgnoreCase(message.getContent_type().toString())) {
				Reminder reminder = reminderService.getPaymentByNoticeNumberAndFiscalCode(message.getContent_paymentData_noticeNumber(), message.getContent_paymentData_payeeFiscalCode());
				if(reminder == null) 
					message.setRptId(message.getContent_paymentData_payeeFiscalCode().concat(message.getContent_paymentData_noticeNumber()));
				reminderService.save(message);
			} else {
				reminderService.save(message);
			}
		}
		payload = message.toString();
		latch.countDown();
	}


	public CountDownLatch getLatch() {
		return latch;
	}

	public String getPayload() {
		return payload;
	}
}
