package it.gov.pagopa.reminder;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonProcessingException;

import dto.messageStatus;
import it.gov.pagopa.reminder.model.Reminder;
import it.gov.pagopa.reminder.producer.ReminderProducer;
import it.gov.pagopa.reminder.scheduler.CheckRemindersToNotifyJob;

@SpringBootTest(classes = Application.class,webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
public class TestSchedulerNotifyIntegrationTest extends AbstractMock{

    @Autowired
    private CheckRemindersToNotifyJob job;
	
    @MockBean
    ReminderProducer reminderProducer;
    
	@Value("${paymentupdater.url}")
	private String urlPayment;

    @Before
    public void setUp() {
    	before();
    }
 
	@Test
	public void test_CheckRemindersToNotifyJob_AllResponse_OK() throws SchedulerException, InterruptedException {
		List<Reminder> modifiedList = selectListReminderMockObject("full");
		Reminder newReminder = modifiedList.get(1);
		newReminder.setReadFlag(true);
		modifiedList.add(newReminder);
		mockGetReadMessageToNotifyWithResponse(selectListReminderMockObject("full"));
		mockGetPaidMessageToNotifyWithResponse(modifiedList);
		getMockRestGetForEntity(messageStatus.class, urlPayment.concat("123456"), selectMessageStatusMockObject("1", true), HttpStatus.OK);
		mockSaveWithResponse(selectReminderMockObject("", "1","GENERIC","AAABBB77Y66A444A", "123456", 3));
		mockFindIdWithResponse(selectReminderMockObject("", "1","GENERIC","AAABBB77Y66A444A", "123456", 3));
		job.execute(null);
		Assertions.assertTrue(true);
	}
	
	@Test
	public void test_CheckRemindersToNotifyJob_Payd_AllResponse_OK() throws SchedulerException, InterruptedException {
		mockGetReadMessageToNotifyWithResponse(selectListReminderMockObject("full"));
		mockGetPaidMessageToNotifyWithResponse(selectListReminderMockObject("full"));
		getMockRestGetForEntity(messageStatus.class, urlPayment.concat("123456"), selectMessageStatusMockObject("1", true), HttpStatus.OK);
		mockSaveWithResponse(selectReminderMockObject("", "1","GENERIC","AAABBB77Y66A444A", "123456", 3));
		mockFindIdWithResponse(selectReminderMockObject("", "1","GENERIC","AAABBB77Y66A444A", "123456", 3));
		job.execute(null);
		Assertions.assertTrue(true);
	}
	
	@Test
	public void test_CheckRemindersToNotifyJob_NoReadResponse_OK() throws SchedulerException, InterruptedException {
		mockGetReadMessageToNotifyWithResponse(selectListReminderMockObject("empty"));
		mockGetPaidMessageToNotifyWithResponse(selectListReminderMockObject("full"));
		getMockRestGetForEntity(messageStatus.class, urlPayment.concat("123456"),selectMessageStatusMockObject("1", true), HttpStatus.OK);
		mockSaveWithResponse(selectReminderMockObject("", "1","GENERIC","AAABBB77Y66A444A", "123456", 3));
		mockFindIdWithResponse(selectReminderMockObject("", "1","GENERIC","AAABBB77Y66A444A", "123456", 3));
		job.execute(null);
		Assertions.assertTrue(true);
	}
	
	@Test
	public void test_CheckRemindersToNotifyJob_NoPaidResponse_OK() throws SchedulerException, InterruptedException {
		mockGetReadMessageToNotifyWithResponse(selectListReminderMockObject("full"));
		mockGetPaidMessageToNotifyWithResponse(selectListReminderMockObject("empty"));
		getMockRestGetForEntity(messageStatus.class, urlPayment.concat("123456"), selectMessageStatusMockObject("1", true), HttpStatus.OK);
		mockSaveWithResponse(selectReminderMockObject("", "1","GENERIC","AAABBB77Y66A444A", "123456", 3));
		mockFindIdWithResponse(selectReminderMockObject("", "1","GENERIC","AAABBB77Y66A444A", "123456", 3));
		job.execute(null);
		Assertions.assertTrue(true);
	}
	
	@Test
	public void test_CheckRemindersToNotifyJob_NoResponse_OK() throws SchedulerException, InterruptedException {
		mockGetReadMessageToNotifyWithResponse(selectListReminderMockObject("empty"));
		mockGetPaidMessageToNotifyWithResponse(selectListReminderMockObject("empty"));
		getMockRestGetForEntity(messageStatus.class, urlPayment.concat("123456"), selectMessageStatusMockObject("1", true), HttpStatus.OK);
		mockSaveWithResponse(selectReminderMockObject("", "1","GENERIC","AAABBB77Y66A444A", "123456", 3));
		mockFindIdWithResponse(selectReminderMockObject("", "1","GENERIC","AAABBB77Y66A444A", "123456", 3));
		job.execute(null);
		Assertions.assertTrue(true);
	}
	
	@Test
	public void test_CheckRemindersToNotifyJob_AllResponse_Generic_OK() throws SchedulerException, InterruptedException {
		mockGetReadMessageToNotifyWithResponse(selectListReminderMockObject("full"));
		mockGetPaidMessageToNotifyWithResponse(selectListReminderMockObject("full"));
		getMockRestGetForEntity(messageStatus.class, urlPayment.concat("123456"), selectMessageStatusMockObject("1", true), HttpStatus.OK);
		mockSaveWithResponse(selectReminderMockObject("", "1","GENERIC","AAABBB77Y66A444A", "123456", 3));
		mockFindIdWithResponse(selectReminderMockObject("", "1","GENERIC","AAABBB77Y66A444A", "123456", 3));
		job.execute(null);
		Assertions.assertTrue(true);
	}
	
	@Test
	public void test_CheckRemindersToNotifyJob_AllResponse_Paid_OK() throws SchedulerException, InterruptedException {
		mockGetReadMessageToNotifyWithResponse(selectListReminderMockObject("full"));
		mockGetPaidMessageToNotifyWithResponse(selectListReminderMockObject("full"));
		getMockRestGetForEntity(messageStatus.class, urlPayment.concat("123456"), selectMessageStatusMockObject("1", true), HttpStatus.OK);
		mockSaveWithResponse(selectReminderMockObject("", "1","PAYMENT","AAABBB77Y66A444A", "123456", 3));
		mockFindIdWithResponse(selectReminderMockObject("", "1","PAYMENT","AAABBB77Y66A444A", "123456", 3));
		job.execute(null);
		Assertions.assertTrue(true);
	}
	
	@Test
	public void test_CheckRemindersToNotifyJob_AllResponse_Paid_WithProxy_KO() throws SchedulerException, InterruptedException, JsonProcessingException {
		proxyKo();
		mockGetReadMessageToNotifyWithResponse(selectListReminderMockObject("full"));
		mockGetPaidMessageToNotifyWithResponse(selectListReminderMockObject("full"));
		getMockRestGetForEntity(messageStatus.class, urlPayment.concat("123456"), selectMessageStatusMockObject("1", true), HttpStatus.OK);
		mockSaveWithResponse(selectReminderMockObject("", "1","PAYMENT","AAABBB77Y66A444A", "123456", 3));
		mockFindIdWithResponse(selectReminderMockObject("", "1","PAYMENT","AAABBB77Y66A444A", "123456", 3));
		job.execute(null);
		Assertions.assertTrue(true);
	}
}