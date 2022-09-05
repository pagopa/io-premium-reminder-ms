package it.gov.pagopa.reminder;

import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonProcessingException;

import dto.FeatureLevelType;
import it.gov.pagopa.reminder.consumer.MessageKafkaConsumer;
import it.gov.pagopa.reminder.consumer.MessageStatusKafkaConsumer;
import it.gov.pagopa.reminder.consumer.PaymentUpdatesKafkaConsumer;
import it.gov.pagopa.reminder.consumer.ReminderKafkaConsumer;
import it.gov.pagopa.reminder.model.Reminder;
import it.gov.pagopa.reminder.producer.ReminderProducer;
import it.gov.pagopa.reminder.util.ApplicationContextProvider;
import it.gov.pagopa.reminder.util.RestTemplateUtils;

@SpringBootTest(classes = Application.class,webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@Import(it.gov.pagopa.reminder.KafkaTestContainersConfiguration.class)
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
public class MessageKafkaConsumerTest extends AbstractMock{
	
	private static final String GENERIC = "GENERIC";
	private static final String PAYMENT = "PAYMENT";
	
    @Autowired
    private ReminderProducer producer;
    
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    
    @InjectMocks
    PaymentUpdatesKafkaConsumer consumer;
    
    @InjectMocks
    ReminderKafkaConsumer consumerRem;
    
    @InjectMocks
    MessageStatusKafkaConsumer consumerMessStatus;
    
    @InjectMocks
    MessageKafkaConsumer messageKafkaConsumer;
    
    @InjectMocks
    RestTemplateUtils util;
    
	
	@Value("${notification.notifyEndpoint}")
	private String notifyEndpoint;
	
    @Before
    public void setUp() {
    	before();
    }
    
    @SuppressWarnings("unchecked")
	public void MockSchedulerNotifyIntegrationReminderKafkaConsumerTest() throws JsonProcessingException {
    	kafkaTemplate = new KafkaTemplate<>((ProducerFactory<String, String>) ApplicationContextProvider.getBean("producerFactory"));
		consumerRem = (ReminderKafkaConsumer) ApplicationContextProvider.getBean("reminderEventKafkaConsumer");
		producer.sendReminder(selectReminderMockObject("", "1", GENERIC, "AAABBB77Y66A444A", "123456", 3), kafkaTemplate, mapper, "message-send");
		consumerRem.reminderKafkaListener(selectReminderMockObject("", "1", GENERIC, "AAABBB77Y66A444A", "123456", 3));
		Assertions.assertTrue(consumerRem.getPayload().contains(""));
		Assertions.assertEquals(0L, consumerRem.getLatch().getCount());
    }
    
    public void MockSchedulerNotifyIntegrationPaymentUpdatesKafkaConsumerTest(String contentType, String contentType2, String source) {
    	consumer = (PaymentUpdatesKafkaConsumer) ApplicationContextProvider.getBean("paymentUpdatesEventKafkaConsumer");
    	mockGetPaymentByNoticeNumberAndFiscalCodeWithResponse(selectReminderMockObject("", "1", contentType, "AAABBB77Y66A444A", "123456", 3));
		mockSaveWithResponse(selectReminderMockObject("", "1", contentType2, "AAABBB77Y66A444A", "123456", 3));
		consumer.paymentUpdatesKafkaListener(getPaymentMessage("123", "456", true, null, 5d, source, "BBBPPP77J99A888A"));
		Assertions.assertTrue(consumer.getPayload().contains("paid=true"));
		Assertions.assertEquals(0L, consumer.getLatch().getCount());
    }
    
    public void MockMessageStatusKafkaConsumerTest(boolean isRead) {
    	consumerMessStatus = (MessageStatusKafkaConsumer) ApplicationContextProvider.getBean("messageStatusEventKafkaConsumer");
		mockSaveWithResponse(selectReminderMockObject("", "1", GENERIC, "AAABBB77Y66A444A", "123456", 3));
		mockFindIdWithResponse(selectReminderMockObject("", "1", GENERIC, "AAABBB77Y66A444A", "123456", 3));
		consumerMessStatus.messageStatusKafkaListener(selectMessageStatusMockObject("1", isRead));
		Assertions.assertTrue(consumerMessStatus.getPayload().contains("messageId"));
		Assertions.assertEquals(0L, consumerMessStatus.getLatch().getCount());
    }
    
	@Test
	public void test_scheduleMockSchedulerNotifyIntegrationTest2_KO() throws SchedulerException, InterruptedException, JsonProcessingException {				
		Mockito.when(restTemplate.postForObject(Mockito.anyString(), Mockito.any(), Mockito.any())).thenThrow(new RuntimeException("500!"));
		MockSchedulerNotifyIntegrationReminderKafkaConsumerTest();
	}
	
	@SuppressWarnings({ "static-access", "rawtypes" })
	@Test
	public void test_scheduleMockSchedulerNotifyIntegrationTest2_OK() throws SchedulerException, InterruptedException, JsonProcessingException {
		Mockito.when(restTemplate.postForObject(Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(new ResponseEntity(HttpStatus.OK).accepted().body("{}"));
		MockSchedulerNotifyIntegrationReminderKafkaConsumerTest();
	}
	
	@Test
	public void test_scheduleMockSchedulerNotifyIntegrationTest_GENERIC_OK() throws SchedulerException, InterruptedException, JsonProcessingException {
		MockSchedulerNotifyIntegrationPaymentUpdatesKafkaConsumerTest(GENERIC, GENERIC, "payments");
	}

	@Test
	public void test_scheduleMockSchedulerNotifyIntegrationTest_PAYMENTS_OK() throws SchedulerException, InterruptedException, JsonProcessingException {
		MockSchedulerNotifyIntegrationPaymentUpdatesKafkaConsumerTest(PAYMENT, PAYMENT, "payments");
	}	
	
	@Test
	public void test_MessageStatusKafkaConsumerTest_Read_MESSAGES_OK() throws SchedulerException, InterruptedException, JsonProcessingException {
		MockMessageStatusKafkaConsumerTest(true);
	}
	
	public void MockMessageKafkaConsumerConsumerTest_MESSAGES(String contentType) {
		messageKafkaConsumer = (MessageKafkaConsumer) ApplicationContextProvider.getBean("messageEventKafkaConsumer");
		Reminder mockObj = selectReminderMockObject("", "1", contentType, "AAABBB77Y66A444A", "123456", 3);
		mockObj.setContent_paymentData_noticeNumber("12345");
		mockObj.setContent_paymentData_payeeFiscalCode("fiscal");
		mockObj.setInsertionDate(LocalDateTime.now());
		mockObj.setSenderServiceId("id");
		mockObj.setFeature_level_type(FeatureLevelType.ADVANCED);
		messageKafkaConsumer.messageKafkaListener(mockObj);
		mockSaveWithResponse(mockObj);
		Assertions.assertTrue(messageKafkaConsumer.getPayload().contains("paidFlag=false"));
		Assertions.assertEquals(0L, messageKafkaConsumer.getLatch().getCount());
	}
	
	@Test
	public void test_MessageKafkaConsumerConsumerTest_MESSAGES_OK() throws SchedulerException, InterruptedException, JsonProcessingException {
		MockMessageKafkaConsumerConsumerTest_MESSAGES(GENERIC);
	}
	
	@Test
	public void test_MessageKafkaConsumerConsumerTest_MESSAGES_PAYMENT() throws SchedulerException, InterruptedException, JsonProcessingException {
		mockGetPaymentByNoticeNumberAndFiscalCode();
		MockMessageKafkaConsumerConsumerTest_MESSAGES(PAYMENT);
	}
}

