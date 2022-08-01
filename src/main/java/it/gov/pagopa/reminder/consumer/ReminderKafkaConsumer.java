package it.gov.pagopa.reminder.consumer;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;

import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.event.RetryOnErrorEvent;
import it.gov.pagopa.reminder.dto.NotificationMessage;
import it.gov.pagopa.reminder.dto.SenderMetadata;
import it.gov.pagopa.reminder.dto.request.NotificationDTO;
import it.gov.pagopa.reminder.model.Reminder;
import it.gov.pagopa.reminder.service.ReminderService;
import it.gov.pagopa.reminder.util.ApplicationContextProvider;
import it.gov.pagopa.reminder.util.Constants;
import it.gov.pagopa.reminder.util.RestTemplateUtils;
import it.gov.pagopa.reminder.util.TelemetryCustomEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReminderKafkaConsumer{


	@Value("${notification.notifyEndpoint}")
	private String notifyEndpoint;
	@Value("${interval.function}")
	private int intervalFunction;
	@Value("${attempts.max}")
	private int attemptsMax;

	@Autowired
	ReminderService reminderService;
	@Autowired
	RestTemplateUtils restTemplateUtils;

	private CountDownLatch latch = new CountDownLatch(1);
	private String payload = "";

	@KafkaListener(topics = "${kafka.send}", groupId = "reminder-message-send", containerFactory = "kafkaListenerContainerFactoryNotify", autoStartup = "${messagesend.auto.start}")
	public void reminderKafkaListener(Reminder reminder) {		

		log.info("Received message-send: {}", reminder);
		if(Objects.nonNull(reminder)) {
			log.info("I'm processing the reminder with id: {} ", reminder.getId());

			String createdAt = DateFormatUtils.format(reminder.getCreatedAt(), Constants.DATE_FORMAT);
			NotificationMessage notificationMessage =  new NotificationMessage(
					reminder.getId(), reminder.getFiscal_code(), 
					createdAt, reminder.getSenderServiceId(), 
					reminder.getTimeToLiveSeconds());

			SenderMetadata senderMetadata = (SenderMetadata) ApplicationContextProvider.getBean("reminderEventSenderMetadata");
			NotificationDTO notification = new NotificationDTO(notificationMessage, senderMetadata);
			try {
				sendNotificationWithRetry(notification);
			}
			catch(Exception e) {
				log.error(e.getMessage());
			}

			latch.countDown();
		}
	}


	private String callNotify(NotificationDTO notification) {
		log.info("Attempt to send reminder with id: {} ", notification.getMessage().getId());
		restTemplateUtils.sendNotification(notifyEndpoint, notification);
		return "";
	}

	private void sendNotificationWithRetry(NotificationDTO notification) {
		IntervalFunction intervalFn = IntervalFunction.of(intervalFunction);
		RetryConfig retryConfig = RetryConfig.custom()
				.maxAttempts(attemptsMax)
				.intervalFunction(intervalFn)
				.build();
		Retry retry = Retry.of("sendNotificationWithRetry", retryConfig);
		Function<Object, Object> sendNotificationFn = Retry.decorateFunction(retry, 
				notObj -> callNotify((NotificationDTO)notObj));
		Retry.EventPublisher publisher = retry.getEventPublisher();
		publisher.onError(event -> {
			if (event.getNumberOfRetryAttempts() == attemptsMax) {
				TelemetryCustomEvent.writeTelemetry("ErrorInPostNotification", new HashMap<>(), getErrorMap(notification, event));
			}
		});
		sendNotificationFn.apply(notification);
	}

	public CountDownLatch getLatch() {
		return latch;
	}

	public String getPayload() {
		return payload;
	}
	
	private Map<String, String> getErrorMap(NotificationDTO notification, RetryOnErrorEvent event ) {
		Map<String, String> properties = new HashMap<>();
		properties.put(notification.getMessage().getId(), "Call failed after maximum number of attempts");
		properties.put("time", event.getCreationTime().toString());
		if (Objects.nonNull(event.getLastThrowable().getMessage())) 
				properties.put("message", event.getLastThrowable().getMessage());
		if (Objects.nonNull(event.getLastThrowable().getCause())) 
				properties.put("cause", event.getLastThrowable().getCause().toString());		
		return properties;
	}
}
