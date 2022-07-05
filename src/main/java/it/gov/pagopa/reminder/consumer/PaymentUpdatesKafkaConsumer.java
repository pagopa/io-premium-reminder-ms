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
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PaymentUpdatesKafkaConsumer {

	@Autowired
	ReminderService reminderService;

	private CountDownLatch latch = new CountDownLatch(1);
	private String payload = "";

	@KafkaListener(topics = "${kafka.payment}", groupId = "payment-updates-shared", containerFactory = "kafkaListenerContainerFactoryPaymentMessage")
	public void paymentUpdatesKafkaListener(PaymentMessage message) {		
		if(Objects.nonNull(message)) {		
			log.info("Received payment-updates: {}", message);
			payload = message.toString();

			Reminder oldReminder = reminderService.getPaymentByNoticeNumberAndFiscalCode(message.getNoticeNumber(), 
					message.getPayeeFiscalCode());

			if(Objects.nonNull(oldReminder)) {

				switch (message.getSource()) {
				case "payments":
					oldReminder.setPaidFlag(true);
					oldReminder.setPaidDate(LocalDateTime.now());
					break;
				default:
					break;
				}
				reminderService.save(oldReminder);
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
