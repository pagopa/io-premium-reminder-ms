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
import it.gov.pagopa.reminder.util.ShaUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MessageKafkaConsumer {

	@Autowired
	ReminderService reminderService;

	private CountDownLatch latch = new CountDownLatch(1);
	private String payload = null;

	@KafkaListener(topics = "${kafka.message}", groupId = "reminder-message", autoStartup = "${message.auto.start}")
	public void messageKafkaListener(Reminder message) throws Exception {
		log.info("Received message: {}", message);
		checkNullInMessage(message);

		if (FeatureLevelType.ADVANCED.toString().equalsIgnoreCase(message.getFeature_level_type().toString())) {

			if (MessageContentType.PAYMENT.toString().equalsIgnoreCase(message.getContent_type().toString())) {
				message.setRptId(message.getContent_paymentData_payeeFiscalCode()
						.concat(message.getContent_paymentData_noticeNumber()));
			}

			if (reminderService.countById(message.getId()) == 0) {
				message.setShard(ShaUtils.getHexString(message.getFiscalCode()).substring(0, 1));
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
