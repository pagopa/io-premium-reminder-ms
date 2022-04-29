package it.gov.pagopa.reminder.consumer;

import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
import it.gov.pagopa.reminder.dto.MessageStatus;
import it.gov.pagopa.reminder.model.JsonLoader;
import it.gov.pagopa.reminder.service.ReminderService;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor
@Slf4j
public class MessageStatusConsumer extends EventHubConsumer {

	@Value("${azure.eventhub.messageStatus.connectionString}")
	private String connectionString;
	@Value("${azure.eventhub.messageStatus.name}")
	private String eventHubName;
	@Value("${azure.eventhub.messageStatus.storageConnectionString}")
	private String storageConnectionString;
	@Value("${azure.eventhub.messageStatus.storageContainerName}")
	private String storageContainerName;
	@Value("${checkpoint.size}")
	private int checkpointSize;
	
	@Autowired
	ReminderService reminderService;
	
	@Autowired
	@Qualifier("messageStatusSchema")
	JsonLoader schema;
	
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
			byte[] binaryJson = converter.convertToJson(eventData.getBody(), schema.getJsonString());
			String avroJson = new String(binaryJson);
			MessageStatus reminderToUpdate = new Gson().fromJson(avroJson, MessageStatus.class);
			log.debug("Message Statuts riceved with idMsg: {} readFlag: {} paidFlad {}", reminderToUpdate.getMessageId(), reminderToUpdate.isRead(), reminderToUpdate.isPaid());
			reminderService.updateReminder(reminderToUpdate.getMessageId(), reminderToUpdate.isRead(), reminderToUpdate.isPaid());
			eventContext.updateCheckpoint();
		}
	};

	private final Consumer<ErrorContext> ERROR_HANDLER = errorContext -> {
		log.error("Error occurred in partition processor for partition {}, {}", errorContext.getPartitionContext().getPartitionId(), errorContext.getThrowable());
	};
}
