package it.gov.pagopa.reminder.consumer;

import java.util.function.Consumer;
import org.apache.commons.lang.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventProcessorClientBuilder;
import com.azure.messaging.eventhubs.checkpointstore.blob.BlobCheckpointStore;
import com.azure.messaging.eventhubs.models.ErrorContext;
import com.azure.messaging.eventhubs.models.EventContext;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.google.gson.Gson;
import it.gov.pagopa.reminder.dto.NotificationMessage;
import it.gov.pagopa.reminder.dto.SenderMetadata;
import it.gov.pagopa.reminder.dto.avro.MessageContentType;
import it.gov.pagopa.reminder.dto.request.NotificationDTO;
import it.gov.pagopa.reminder.model.Reminder;
import it.gov.pagopa.reminder.service.ReminderService;
import it.gov.pagopa.reminder.util.ApplicationContextProvider;
import it.gov.pagopa.reminder.util.Constants;
import it.gov.pagopa.reminder.util.RestTemplateUtils;
import it.gov.pagopa.reminder.util.WebClientUtils;

public class ReminderConsumer extends EventHubConsumer {

	@Value("${azure.eventhub.notification.connectionString}")
	private String connectionString;
	@Value("${azure.eventhub.notification.name}")
	private String eventHubName;
	@Value("${azure.eventhub.notification.storageConnectionString}")
	private String storageConnectionString;
	@Value("${azure.eventhub.notification.storageContainerName}")
	private String storageContainerName;
	
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
	
	private final Consumer<EventContext> PARTITION_PROCESSOR = eventContext -> {
		EventData eventData = eventContext.getEventData();
		
		if (eventData != null) {
			Reminder reminderToSend = new Gson().fromJson(new String(eventData.getBody()), Reminder.class);
			if(reminderToSend != null && reminderToSend.getContent_type().equals(MessageContentType.PAYMENT)) {
				//TODO: inviare request a servizio dei pagamenti
			}
			if(reminderToSend != null && !reminderToSend.getContent_type().equals(MessageContentType.PAYMENT)) {
				String created_at = DateFormatUtils.format(reminderToSend.getCreatedAt(), Constants.DATE_FORMAT);
				NotificationMessage notificationMessage =  new NotificationMessage(
																reminderToSend.getId(), reminderToSend.getFiscalCode(), 
																created_at, reminderToSend.getSenderServiceId(), 
																reminderToSend.getTimeToLiveSeconds());
				SenderMetadata senderMetadata = (SenderMetadata) ApplicationContextProvider.getBean("ReminderEventSenderMetadata"); //new SenderMetadata();
				NotificationDTO notification = new NotificationDTO(notificationMessage, senderMetadata);
				RestTemplateUtils.sendNotification("https://io-d-mock-app-backend.azurewebsites.net/api/v1/notify", notification);
//				WebClientUtils.sendPostRequest("https://io-d-mock-app-backend.azurewebsites.net/api/v1/notify", 
//												MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, notification);
			}
			// Every 10 events received, it will update the checkpoint stored in Azure Blob Storage.
			if (eventData.getSequenceNumber() % 10 == 0) {
				eventContext.updateCheckpoint();
			}
		}
	};

	private final Consumer<ErrorContext> ERROR_HANDLER = errorContext -> {
		System.out.printf("Error occurred in partition processor for partition %s, %s.%n",
				errorContext.getPartitionContext().getPartitionId(),
				errorContext.getThrowable());
	};
}
