package it.gov.pagopa.reminder.consumer;

import java.util.function.Consumer;

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

import it.gov.pagopa.reminder.consumer.utils.JsonAvroConverter;
import it.gov.pagopa.reminder.model.Reminder;
import it.gov.pagopa.reminder.service.ReminderService;
import it.gov.pagopa.reminder.util.Constants;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class MessageConsumer extends EventHubConsumer {

	@Value("${azure.eventhub.message.connectionString}")
	private String connectionString;
	@Value("${azure.eventhub.message.name}")
	private String eventHubName;
	@Value("${azure.eventhub.message.storageConnectionString}")
	private String storageConnectionString;
	@Value("${azure.eventhub.message.storageContainerName}")
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
		JsonAvroConverter converter = new JsonAvroConverter();
		EventData eventData = eventContext.getEventData();
		
		if (eventData != null) {
			byte[] binaryJson = converter.convertToJson(eventData.getBody(), Constants.MESSAGE_SCHEMA);
			Reminder newReminder = new Gson().fromJson(new String(binaryJson), Reminder.class);
			reminderService.save(newReminder);
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
