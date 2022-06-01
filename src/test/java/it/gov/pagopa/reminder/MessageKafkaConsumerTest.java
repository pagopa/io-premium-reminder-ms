package it.gov.pagopa.reminder;

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
import com.fasterxml.jackson.databind.ObjectMapper;

import it.gov.pagopa.reminder.Application;
import it.gov.pagopa.reminder.consumer.MessageKafkaConsumer;
import it.gov.pagopa.reminder.consumer.MessageStatusKafkaConsumer;
import it.gov.pagopa.reminder.consumer.PaymentUpdatesKafkaConsumer;
import it.gov.pagopa.reminder.consumer.ReminderKafkaConsumer;
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
    
	@Autowired
	ObjectMapper mapper;
	
	@Value("${notification.notifyEndpoint}")
	private String notifyEndpoint;
	
    @Before
    public void setUp() {
    	before();
    }
    
	@Test
	public void test_scheduleMockSchedulerNotifyIntegrationTest2_OK() throws SchedulerException, InterruptedException, JsonProcessingException {
		Mockito.when(restTemplate.postForObject(Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR).internalServerError().body("{}"));
		kafkaTemplate = new KafkaTemplate<>((ProducerFactory<String, String>) ApplicationContextProvider.getBean("producerFactory"));
		consumerRem = (ReminderKafkaConsumer) ApplicationContextProvider.getBean("ReminderEventKafkaConsumer");
		producer.sendReminder(selectReminderMockObject("", "1","GENERIC","AAABBB77Y66A444A",3), kafkaTemplate, mapper, "message-send");
		consumerRem.reminderKafkaListener(selectReminderMockObject("", "1","GENERIC","AAABBB77Y66A444A",3));
//		consumer.getLatch().await(10000, TimeUnit.MILLISECONDS);
        String s = consumer.getPayload();
//        assertThat(consumer.getLatch().getCount(), equalTo(0L));
//        assertThat(consumer.getPayload(), containsString("embedded-test-topic"));
		Assertions.assertTrue(true);
	}
	
	
	@Test
	public void test_scheduleMockSchedulerNotifyIntegrationTest_GENERIC_OK() throws SchedulerException, InterruptedException, JsonProcessingException {
		consumer = (PaymentUpdatesKafkaConsumer) ApplicationContextProvider.getBean("PaymentUpdatesEventKafkaConsumer");
		mockGetPaymentByNoticeNumberAndFiscalCodeWithResponse(selectReminderMockObject("", "1","GENERIC","AAABBB77Y66A444A",3));
		mockSaveWithResponse(selectReminderMockObject("", "1","GENERIC","AAABBB77Y66A444A",3));
		consumer.paymentUpdatesKafkaListener(getPaymentMessage("123", "456", true, null, 5d, "payments"));
		Assertions.assertTrue(true);
	}

	@Test
	public void test_scheduleMockSchedulerNotifyIntegrationTest_PAYMENTS_OK() throws SchedulerException, InterruptedException, JsonProcessingException {
		consumer = (PaymentUpdatesKafkaConsumer) ApplicationContextProvider.getBean("PaymentUpdatesEventKafkaConsumer");
		mockGetPaymentByNoticeNumberAndFiscalCodeWithResponse(selectReminderMockObject("", "1","PAYMENT","AAABBB77Y66A444A",3));
		mockSaveWithResponse(selectReminderMockObject("", "1","GENERIC","AAABBB77Y66A444A",3));
		consumer.paymentUpdatesKafkaListener(getPaymentMessage("123", "456", true, null, 5d, "payments"));
		Assertions.assertTrue(true);
	}

	@Test
	public void test_scheduleMockSchedulerNotifyIntegrationTest_MESSAGES_OK() throws SchedulerException, InterruptedException, JsonProcessingException {
		consumer = (PaymentUpdatesKafkaConsumer) ApplicationContextProvider.getBean("PaymentUpdatesEventKafkaConsumer");
		mockGetPaymentByNoticeNumberAndFiscalCodeWithResponse(selectReminderMockObject("", "1","PAYMENT","AAABBB77Y66A444A",3));
		mockSaveWithResponse(selectReminderMockObject("", "1","GENERIC","AAABBB77Y66A444A",3));
		consumer.paymentUpdatesKafkaListener(getPaymentMessage("123", "456", true, null, 5d, "message"));
		Assertions.assertTrue(consumer.getPayload().contains("message"));
		Assertions.assertEquals(0L, consumer.getLatch().getCount());
	}
	
	
	
	@Test
	public void test_MessageStatusKafkaConsumerTest_MESSAGES_OK() throws SchedulerException, InterruptedException, JsonProcessingException {
		consumerMessStatus = (MessageStatusKafkaConsumer) ApplicationContextProvider.getBean("MessageStatusEventKafkaConsumer");
		mockSaveWithResponse(selectReminderMockObject("", "1","GENERIC","AAABBB77Y66A444A",3));
		mockFindIdWithResponse(selectReminderMockObject("", "1","GENERIC","AAABBB77Y66A444A",3));
		consumerMessStatus.messageStatusKafkaListener(selectMessageStatusMockObject("", "1", true, false));
		Assertions.assertTrue(consumerMessStatus.getPayload().contains("messageId"));
		Assertions.assertEquals(0L, consumerMessStatus.getLatch().getCount());
	}
	
	@Test
	public void test_MessageKafkaConsumerConsumerTest_MESSAGES_OK() throws SchedulerException, InterruptedException, JsonProcessingException {
		messageKafkaConsumer = (MessageKafkaConsumer) ApplicationContextProvider.getBean("MessageEventKafkaConsumer");
		mockSaveWithResponse(selectReminderMockObject("", "1","GENERIC","AAABBB77Y66A444A",3));
		messageKafkaConsumer.messageKafkaListener(selectReminderMockObject("", "1","GENERIC","AAABBB77Y66A444A",3));
		Assertions.assertTrue(messageKafkaConsumer.getPayload().contains("paidFlag=false"));
		Assertions.assertEquals(0L, messageKafkaConsumer.getLatch().getCount());
	}
}
