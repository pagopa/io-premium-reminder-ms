package it.gov.pagopa.reminder.consumer;

import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.commons.lang.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventProcessorClientBuilder;
import com.azure.messaging.eventhubs.checkpointstore.blob.BlobCheckpointStore;
import com.azure.messaging.eventhubs.models.ErrorContext;
import com.azure.messaging.eventhubs.models.EventContext;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.google.gson.Gson;

import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import it.gov.pagopa.reminder.dto.NotificationMessage;
import it.gov.pagopa.reminder.dto.SenderMetadata;
import it.gov.pagopa.reminder.dto.request.NotificationDTO;
import it.gov.pagopa.reminder.model.Reminder;
import it.gov.pagopa.reminder.service.ReminderService;
import it.gov.pagopa.reminder.util.ApplicationContextProvider;
import it.gov.pagopa.reminder.util.Constants;
import it.gov.pagopa.reminder.util.RestTemplateUtils;
import lombok.extern.slf4j.Slf4j;
import static it.gov.pagopa.reminder.util.ReminderUtil.checkNullInMessage;
@Slf4j
public class ReminderConsumer extends EventHubConsumer {

	@Value("${azure.eventhub.notification.connectionString}")
	private String connectionString;
	@Value("${azure.eventhub.notification.name}")
	private String eventHubName;
	@Value("${azure.eventhub.notification.storageConnectionString}")
	private String storageConnectionString;
	@Value("${azure.eventhub.notification.storageContainerName}")
	private String storageContainerName;
	@Value("${notification.notifyEndpoint}")
	private String notifyEndpoint;
	@Value("${checkpoint.size}")
	private int checkpointSize;
	
	@Autowired
	ReminderService reminderService;
	
	public void init() {
		this.blobContainerAsyncClient = new BlobContainerClientBuilder()
				.connectionString(this.storageConnectionString)
				.containerName(this.storageContainerName)
				.buildAsyncClient();
		this.eventProcessorClientBuilder = new EventProcessorClientBuilder()
				.connectionString(this.connectionString, this.eventHubName)
				.consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
				.processEvent(PARTITION_PROCESSOR)
				.processError(ERROR_HANDLER)
				.checkpointStore(new BlobCheckpointStore(blobContainerAsyncClient));
		consume();
	}
	
	private  Consumer<EventContext> PARTITION_PROCESSOR = eventContext -> {
		EventData eventData = eventContext.getEventData();

		if (eventData != null) {
			Reminder reminderToSend = new Gson().fromJson(new String(eventData.getBody()), Reminder.class);
			checkNullInMessage(reminderToSend);
			if(reminderToSend != null) {
				log.info("I'm processing the reminder with id: {} ", reminderToSend.getId());
				String created_at = DateFormatUtils.format(reminderToSend.getCreatedAt(), Constants.DATE_FORMAT);
				NotificationMessage notificationMessage =  new NotificationMessage(
																reminderToSend.getId(), reminderToSend.getFiscalCode(), 
																created_at, reminderToSend.getSenderServiceId(), 
																reminderToSend.getTimeToLiveSeconds());
				SenderMetadata senderMetadata = (SenderMetadata) ApplicationContextProvider.getBean("ReminderEventSenderMetadata");
				NotificationDTO notification = new NotificationDTO(notificationMessage, senderMetadata);
				log.info("I'm sending the notification with id: {} ", notification.getMessage().getId());
				try {
					sendNotificationWithRetry(notification);
					eventContext.updateCheckpoint();
				}
				catch(Exception e) {
					eventContext.updateCheckpoint();
				}
			}
		}
	};

	private final Consumer<ErrorContext> ERROR_HANDLER = errorContext -> {
		log.error("Error occurred in partition processor for partition {}, {}", errorContext.getPartitionContext().getPartitionId(), errorContext.getThrowable());

	};
	
	private String callNotify(NotificationDTO notification) {
		log.info("Attempt to send reminder with id: {} ", notification.getMessage().getId());
		RestTemplateUtils.sendNotification(notifyEndpoint, notification);
		return null;
	}
	private void sendNotificationWithRetry(NotificationDTO notification) {
		IntervalFunction intervalFn = IntervalFunction.of(10000);
		RetryConfig retryConfig = RetryConfig.custom()
		  .maxAttempts(3)
		  .intervalFunction(intervalFn)
		  .build();
		Retry retry = Retry.of("sendNotificationWithRetry", retryConfig);
		Function<Object, Object> sendNotificationFn = Retry.decorateFunction(retry, 
				notObj -> callNotify((NotificationDTO)notObj));
		sendNotificationFn.apply(notification);
	}
}
