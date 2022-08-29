package it.gov.pagopa.reminder;

import java.io.IOException;

import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
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
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.applicationinsights.core.dependencies.apachecommons.io.output.ByteArrayOutputStream;

import dto.message;
import dto.messageStatus;
import it.gov.pagopa.reminder.deserializer.AvroMessageDeserializer;
import it.gov.pagopa.reminder.deserializer.AvroMessageStatusDeserializer;
import it.gov.pagopa.reminder.deserializer.PaymentMessageDeserializer;
import it.gov.pagopa.reminder.deserializer.ReminderDeserializer;
import it.gov.pagopa.reminder.dto.PaymentMessage;
import it.gov.pagopa.reminder.exception.AvroDeserializerException;
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
	public void test_messageDeserialize_ok() throws IOException {
		
		avroMessageDeserializer = new AvroMessageDeserializer();
		message mess = selectMessageMockObject("", "1","GENERIC","AAABBB77Y66A444A", "123456");
		DatumWriter<message> writer = new SpecificDatumWriter<>(
				message.class);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		Encoder encoder = EncoderFactory.get().binaryEncoder(bos, null);
		writer.write(mess, encoder);
		encoder.flush();
		Reminder rem = avroMessageDeserializer.deserialize(null, bos.toByteArray());
		Assertions.assertNotNull(rem);
	}
	
	@Test
	public void test_messageDeserialize_ko() {
		Assertions.assertThrows(AvroDeserializerException.class,
				() -> avroMessageDeserializer.deserialize(null, messageSchema.getJsonString().getBytes()));
	}


	@Test
	public void test_messageStatusDeserialize_ok() throws IOException {
		
		avroMessageStatusDeserializer = new AvroMessageStatusDeserializer();
		messageStatus messStatus = selectMessageStatusMockObject("1", true);
		DatumWriter<messageStatus> writer = new SpecificDatumWriter<>(
				messageStatus.class);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		Encoder encoder = EncoderFactory.get().binaryEncoder(bos, null);
		writer.write(messStatus, encoder);
		encoder.flush();
		messageStatus rem = avroMessageStatusDeserializer.deserialize(null, bos.toByteArray());
		Assertions.assertNotNull(rem);
	}
	
	@Test
	public void test_messageStatusDeserialize_ko() {
		Assertions.assertThrows(AvroDeserializerException.class,
				() -> avroMessageStatusDeserializer.deserialize(null, messageStatusSchema.getJsonString().getBytes()));
	}

	
	@Test
	public void test_paymentDeserialize_OK() throws StreamReadException, DatabindException, IOException {
		byte[] byteArrray = "".getBytes();
		paymentMessageDeserializer = new PaymentMessageDeserializer(mapper);
		Mockito.when(converter.convertToJson(Mockito.any(), Mockito.anyString())).thenReturn(byteArrray);
		Mockito.when(mapper.readValue(byteArrray, PaymentMessage.class)).thenReturn(new PaymentMessage());
		paymentMessageDeserializer.deserialize(null, byteArrray);
		Assertions.assertTrue(true);
	}
	
	@Test
	public void test_paymentDeserialize_KO() throws StreamReadException, DatabindException, IOException {
		paymentMessageDeserializer = new PaymentMessageDeserializer(null);
		Assertions.assertThrows(DeserializationException.class,
				() -> paymentMessageDeserializer.deserialize(null, "".getBytes()));
	}
	
	@Test
	public void test_reminderDeserialize_OK() throws StreamReadException, DatabindException, IOException {
		byte[] byteArrray = "".getBytes();
		reminderDeserializer = new ReminderDeserializer(mapper);
		Mockito.when(converter.convertToJson(Mockito.any(), Mockito.anyString())).thenReturn(byteArrray);
		Mockito.when(mapper.readValue(byteArrray, Reminder.class)).thenReturn(new Reminder());
		reminderDeserializer.deserialize(null, byteArrray);
		Assertions.assertTrue(true);
	}

}
