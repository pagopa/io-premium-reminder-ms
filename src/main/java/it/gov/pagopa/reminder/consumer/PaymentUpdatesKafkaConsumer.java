package it.gov.pagopa.reminder.consumer;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import it.gov.pagopa.reminder.dto.PaymentMessage;
import it.gov.pagopa.reminder.model.Reminder;
import it.gov.pagopa.reminder.service.ReminderService;
import it.gov.pagopa.reminder.util.ReminderUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PaymentUpdatesKafkaConsumer {

	@Autowired
	ReminderService reminderService;

	private CountDownLatch latch = new CountDownLatch(1);
	private String payload = "";

	@KafkaListener(topics = "${kafka.payment}", groupId = "payment-updates-shared", containerFactory = "kafkaListenerContainerFactoryPaymentMessage", autoStartup = "${payment.auto.start}")
	public void paymentUpdatesKafkaListener(PaymentMessage message) {		
		if(Objects.nonNull(message)) {		
			log.info("Received payment-updates: {}", message);
			payload = message.toString();

			if("payments".equalsIgnoreCase(message.getSource())) {

				Reminder reminderToUpdate = reminderService.findById(message.getMessageId());
				if(reminderToUpdate!=null) {
					reminderToUpdate.setPaidFlag(true);
					reminderToUpdate.setPaidDate(ReminderUtil.getLocalDateTime(message.getPaymentDateTime()));
					reminderService.save(reminderToUpdate);
				}

			}
		}
		this.latch.countDown();
	}


	public CountDownLatch getLatch() {
		return latch;
	}

	public String getPayload() {
		return payload;
	}
}
