package it.gov.pagopa.reminder.config;

import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.BytesDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.reminder.consumer.MessageKafkaConsumer;
import it.gov.pagopa.reminder.consumer.MessageStatusKafkaConsumer;
import it.gov.pagopa.reminder.consumer.PaymentUpdatesKafkaConsumer;
import it.gov.pagopa.reminder.consumer.ReminderKafkaConsumer;
import it.gov.pagopa.reminder.deserializer.AvroMessageDeserializer;
import it.gov.pagopa.reminder.deserializer.AvroMessageStatusDeserializer;
import it.gov.pagopa.reminder.deserializer.PaymentMessageDeserializer;
import it.gov.pagopa.reminder.deserializer.ReminderDeserializer;
import it.gov.pagopa.reminder.dto.MessageStatus;
import it.gov.pagopa.reminder.dto.PaymentMessage;
import it.gov.pagopa.reminder.dto.SenderMetadata;
import it.gov.pagopa.reminder.model.JsonLoader;
import it.gov.pagopa.reminder.model.Reminder;


@Configuration
public class ConfigConsumer extends ConfigKafka{
	
	@Autowired
	@Qualifier("messageSchema")
	JsonLoader messageSchema;
	
	@Autowired
	@Qualifier("messageStatusSchema")
	JsonLoader mesagesStatusSchema;
	
	@Autowired
	ObjectMapper mapper;
	
	@Value("${kafka.topic.message}")
	private String urlMessage;
	@Value("${kafka.topic.messagestatus}")
	private String urlMessageStatus;
	@Value("${bootstrap.servers.message}")
	private String serverMessage;
	@Value("${bootstrap.servers.messagestatus}")
	private String serverMessageStatus;
	@Value("${bootstrap.servers.messagesend}")
	protected String serverMessageSend;
	@Value("${kafka.topic.messagesend}")
	protected String urlMessageSend;
	
	@Bean
	public MessageKafkaConsumer MessageEventKafkaConsumer() {
		return new MessageKafkaConsumer();		
	}
	
	@Bean
	public MessageStatusKafkaConsumer MessageStatusEventKafkaConsumer() {
		return new MessageStatusKafkaConsumer();
	}
	
	@Bean
	public PaymentUpdatesKafkaConsumer PaymentUpdatesEventKafkaConsumer() {
		return new PaymentUpdatesKafkaConsumer();
	}
	
	@Bean
	public ReminderKafkaConsumer ReminderEventKafkaConsumer() {
		return new ReminderKafkaConsumer();
	}
	
	@Bean SenderMetadata ReminderEventSenderMetadata() {
		return new SenderMetadata();
	}
	
	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, Reminder> kafkaListenerContainerFactory() {
		ConcurrentKafkaListenerContainerFactory<String, Reminder> factory = new ConcurrentKafkaListenerContainerFactory<>();
		Map<String, Object> props = createProps(urlMessage, serverMessage);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, AvroMessageDeserializer.class.getName());
		DefaultKafkaConsumerFactory<String, Reminder> dkc = new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new AvroMessageDeserializer(messageSchema, mapper));
		factory.setConsumerFactory(dkc);
		return factory;
	}

	
	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, MessageStatus> kafkaListenerContainerFactoryMessStat() {
		ConcurrentKafkaListenerContainerFactory<String, MessageStatus> factoryStatus = new ConcurrentKafkaListenerContainerFactory<>();
		Map<String, Object> props1 = createProps(urlMessageStatus, serverMessageStatus);
		props1.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, AvroMessageStatusDeserializer.class.getName());
		DefaultKafkaConsumerFactory<String, MessageStatus> dkc = new DefaultKafkaConsumerFactory<>(props1, new StringDeserializer(), new AvroMessageStatusDeserializer(mesagesStatusSchema, mapper));
		factoryStatus.setConsumerFactory(dkc);
		return factoryStatus;
	}

	
	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, PaymentMessage> kafkaListenerContainerFactoryPaymentMessage() {
		ConcurrentKafkaListenerContainerFactory<String, PaymentMessage> factoryStatus = new ConcurrentKafkaListenerContainerFactory<>();
		Map<String, Object> props = createPropsShared();
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, BytesDeserializer.class);
		DefaultKafkaConsumerFactory<String, PaymentMessage> dkc = new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new PaymentMessageDeserializer(mapper));
		factoryStatus.setConsumerFactory(dkc);
		return factoryStatus;
	}
	
	
	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, Reminder> kafkaListenerContainerFactoryNotify() {
		ConcurrentKafkaListenerContainerFactory<String, Reminder> factory = new ConcurrentKafkaListenerContainerFactory<>();
		Map<String, Object> props = createProps(urlMessageSend, serverMessageSend);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, BytesDeserializer.class.getName());
		DefaultKafkaConsumerFactory<String, Reminder> dkc = new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new ReminderDeserializer(mapper));
		factory.setConsumerFactory(dkc);
		return factory;
	}
	
}
