package it.gov.pagopa.reminder;

import static org.mockito.Mockito.times;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpServerErrorException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.gov.pagopa.reminder.model.Reminder;
import it.gov.pagopa.reminder.producer.ReminderProducer;
import it.gov.pagopa.reminder.service.ReminderService;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
@ExtendWith(OutputCaptureExtension.class)
public class ReminderServiceTest extends AbstractMock {

	@Autowired
	ReminderService reminderService;

	@MockBean
	ReminderProducer remProdMock;

	@Before
	public void setUp() {
		before();
	}

	@Test
	public void test_producerThrowsJsonProcessingException() throws JsonProcessingException {
		List<Reminder> reminders = new ArrayList<>();
		reminders.add(selectReminderMockObject("type", "1", "GENERIC", "AAABBB77Y66A444A", "123456", 3));
		mockGetReadMessageToNotifyWithResponse(reminders);
		mockGetPaidMessageToNotifyWithResponse(new ArrayList<>());
		Mockito.doThrow(JsonProcessingException.class).doNothing().when(remProdMock).sendReminder(
				Mockito.any(Reminder.class),
				Mockito.any(KafkaTemplate.class), Mockito.any(ObjectMapper.class),
				Mockito.anyString());
		reminderService.getMessageToNotify("0");
		Mockito.verify(mockRepository, times(0)).save(Mockito.any(Reminder.class));
	}

	@Test
	public void test_producerThrowsHttpServerErrorException() throws JsonProcessingException {
		List<Reminder> reminders = new ArrayList<>();
		reminders.add(selectReminderMockObject("type", "1", "PAYMENT", "AAABBB77Y66A444A", "123456", 3));
		mockGetReadMessageToNotifyWithResponse(reminders);
		mockGetPaidMessageToNotifyWithResponse(new ArrayList<>());
		proxyKo(null);
		reminderService.getMessageToNotify("0");
		Assertions.assertThrows(HttpServerErrorException.class,
				() -> mockDefaultApi.getPaymentInfo(Mockito.anyString(), Mockito.anyString()));
	}

}