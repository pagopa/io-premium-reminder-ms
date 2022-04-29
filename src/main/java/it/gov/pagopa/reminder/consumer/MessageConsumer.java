package it.gov.pagopa.reminder.consumer;

import java.time.LocalDateTime;
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
import com.google.gson.GsonBuilder;

import it.gov.pagopa.reminder.consumer.utils.JsonAvroConverter;
import it.gov.pagopa.reminder.model.JsonLoader;
import it.gov.pagopa.reminder.model.Reminder;
import it.gov.pagopa.reminder.service.ReminderService;
import it.gov.pagopa.reminder.util.LocalDateTimeDeserializer;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static it.gov.pagopa.reminder.util.ReminderUtil.checkNullInMessage;

@NoArgsConstructor
@Slf4j
public class MessageConsumer extends EventHubConsumer {

	@Value("${azure.eventhub.message.connectionString}")
	private String connectionString;
	@Value("${azure.eventhub.message.name}")
	private String eventHubName;
	@Value("${azure.eventhub.message.storageConnectionString}")
	private String storageConnectionString;
	@Value("${azure.eventhub.message.storageContainerName}")
	private String storageContainerName;
	@Value("${checkpoint.size}")
	private int checkpointSize;
	
	@Autowired
	ReminderService reminderService;
	
	@Autowired
	@Qualifier("messageSchema")
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
			try {
				byte[] binaryJson = converter.convertToJson(eventData.getBody(), schema.getJsonString());
				String avroJson = new String(binaryJson);
				GsonBuilder gsonBuilder = new GsonBuilder();
				gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer());
				Gson gson = gsonBuilder.setPrettyPrinting().create();
				Reminder newReminder = gson.fromJson(avroJson, Reminder.class);
				log.debug("Message riceved with id: {}", newReminder.getId());
				checkNullInMessage(newReminder);
				reminderService.save(newReminder);
				eventContext.updateCheckpoint();
			} catch(Exception e) {
				eventContext.updateCheckpoint();
			}
		}
	};
	
	private final Consumer<ErrorContext> ERROR_HANDLER = errorContext -> {
		log.error("Error occurred in partition processor for partition {}, {}", errorContext.getPartitionContext().getPartitionId(), errorContext.getThrowable());
	};
}
