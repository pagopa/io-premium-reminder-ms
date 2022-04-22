package it.gov.pagopa.reminder.consumer;

import java.io.IOException;

import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.messaging.eventhubs.EventProcessorClientBuilder;
import com.azure.storage.blob.BlobContainerAsyncClient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Getter
@NoArgsConstructor
@Slf4j
public class EventHubConsumer {

	protected EventProcessorClientBuilder eventProcessorClientBuilder;
	protected BlobContainerAsyncClient blobContainerAsyncClient;
	
	protected void consume() {
		EventProcessorClient eventProcessorClient = this.eventProcessorClientBuilder.buildEventProcessorClient();

		log.info("Starting event processor");
		eventProcessorClient.start();

		log.info("Press enter to stop.");
		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}

		log.info("Stopping event processor");
		eventProcessorClient.stop();
		log.info("Event processor stopped.");

		log.info("Exiting process");
	}
}
