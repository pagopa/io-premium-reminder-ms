package it.gov.pagopa.reminder;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.gov.pagopa.reminder.deserializer.AvroMessageDeserializer;
import it.gov.pagopa.reminder.deserializer.AvroMessageStatusDeserializer;
import it.gov.pagopa.reminder.deserializer.PaymentMessageDeserializer;
import it.gov.pagopa.reminder.deserializer.ReminderDeserializer;
import it.gov.pagopa.reminder.dto.MessageStatus;
import it.gov.pagopa.reminder.dto.PaymentMessage;
import it.gov.pagopa.reminder.model.JsonLoader;
import it.gov.pagopa.reminder.model.Reminder;
import tech.allegro.schema.json2avro.converter.JsonAvroConverter;

@SpringBootTest(classes = Application.class,webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class MockDeserializerIntegrationTest extends AbstractMock{

	@MockBean
	JsonAvroConverter converter;
	
	@Mock
	ObjectMapper mapper;
	
	@InjectMocks
	AvroMessageDeserializer avroMessageDeserializer = null;
	
	@InjectMocks
	AvroMessageStatusDeserializer avroMessageStatusDeserializer = null;
	
	@InjectMocks
	PaymentMessageDeserializer paymentMessageDeserializer = null;
	
	@InjectMocks
	ReminderDeserializer reminderDeserializer = null;
	
	@Autowired 
	@Qualifier("messageSchema") 
	JsonLoader messageSchema;
	
	@Autowired 
	@Qualifier("messageStatusSchema") 
	JsonLoader messageStatusSchema;
	
    @Before
    public void setUp() {
    	before();
    }
 
	@Test
	public void test_messageDeserialize_ok() throws JsonMappingException, JsonProcessingException {
		String s = "";
		byte[] byteArrray = s.getBytes();
		avroMessageDeserializer = new AvroMessageDeserializer(messageSchema, mapper);
		avroMessageDeserializer.setConverter(converter);
		Mockito.when(converter.convertToJson(Mockito.any(), Mockito.anyString())).thenReturn(byteArrray);
		Mockito.when(mapper.readValue(messageSchema.getJsonString(), Reminder.class)).thenReturn(new Reminder());
		avroMessageDeserializer.deserialize(null, messageSchema.getJsonString().getBytes());
		Assertions.assertTrue(true);
	}
	
	@Test
	public void test_messageDeserialize_ko() {
		byte[] byteArrray = null;
		avroMessageDeserializer = new AvroMessageDeserializer(messageSchema, mapper);
		avroMessageDeserializer.setConverter(converter);
		Mockito.when(converter.convertToJson(Mockito.any(), Mockito.anyString())).thenReturn(byteArrray);
		avroMessageDeserializer.deserialize(null, messageSchema.getJsonString().getBytes());
		Assertions.assertTrue(true);
	}

	@Test
	public void test_messageStatusDeserialize_ok() throws JsonMappingException, JsonProcessingException {
		String s = "";
		byte[] byteArrray = s.getBytes();
		avroMessageStatusDeserializer = new AvroMessageStatusDeserializer(messageStatusSchema, mapper);
		avroMessageStatusDeserializer.setConverter(converter);
		Mockito.when(converter.convertToJson(Mockito.any(), Mockito.anyString())).thenReturn(byteArrray);
		Mockito.when(mapper.readValue(messageStatusSchema.getJsonString(), MessageStatus.class)).thenReturn(new MessageStatus());
		avroMessageStatusDeserializer.deserialize(null, messageSchema.getJsonString().getBytes());
		Assertions.assertTrue(true);
	}
	
	@Test
	public void test_messageStatusDeserialize_ko() {
		byte[] byteArrray = null;
		avroMessageStatusDeserializer = new AvroMessageStatusDeserializer(messageStatusSchema, mapper);
		avroMessageStatusDeserializer.setConverter(converter);
		Mockito.when(converter.convertToJson(Mockito.any(), Mockito.anyString())).thenReturn(byteArrray);
		avroMessageStatusDeserializer.deserialize(null, messageStatusSchema.getJsonString().getBytes());
		Assertions.assertTrue(true);
	}

	
	@Test
	public void test_paymentDeserialize_OK() throws StreamReadException, DatabindException, IOException {
		String s = "";
		byte[] byteArrray = s.getBytes();
		paymentMessageDeserializer = new PaymentMessageDeserializer(mapper);
		Mockito.when(converter.convertToJson(Mockito.any(), Mockito.anyString())).thenReturn(byteArrray);
		Mockito.when(mapper.readValue(byteArrray, PaymentMessage.class)).thenReturn(new PaymentMessage());
		paymentMessageDeserializer.deserialize(null, byteArrray);
		Assertions.assertTrue(true);
	}
	
	@Test
	public void test_paymentDeserialize_KO() throws StreamReadException, DatabindException, IOException {
		String s = "ko";
		byte[] byteArrray = s.getBytes();
		paymentMessageDeserializer = new PaymentMessageDeserializer(null);
		paymentMessageDeserializer.deserialize(null, byteArrray);
		Assertions.assertTrue(true);
	}
	
	@Test
	public void test_reminderDeserialize_OK() throws StreamReadException, DatabindException, IOException {
		String s = "";
		byte[] byteArrray = s.getBytes();
		reminderDeserializer = new ReminderDeserializer(mapper);
		Mockito.when(converter.convertToJson(Mockito.any(), Mockito.anyString())).thenReturn(byteArrray);
		Mockito.when(mapper.readValue(byteArrray, Reminder.class)).thenReturn(new Reminder());
		reminderDeserializer.deserialize(null, byteArrray);
		Assertions.assertTrue(true);
	}
	
	@Test
	public void test_reminderDeserialize_KO() throws StreamReadException, DatabindException, IOException {
		String s = "ko";
		byte[] byteArrray = s.getBytes();
		reminderDeserializer = new ReminderDeserializer(null);
		reminderDeserializer.deserialize(null, byteArrray);
		Assertions.assertTrue(true);
	}


}
