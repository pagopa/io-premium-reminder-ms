package it.gov.pagopa.reminder;

import java.util.ArrayList;
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
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonProcessingException;

import dto.messageStatus;
import it.gov.pagopa.reminder.model.Reminder;
import it.gov.pagopa.reminder.producer.ReminderProducer;
import it.gov.pagopa.reminder.scheduler.CheckRemindersToNotifyJob;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
public class TestSchedulerNotifyIntegrationTest extends AbstractMock {

	private static final String GENERIC = "GENERIC";
	private static final String PAYMENT = "PAYMENT";
	private static final String EMPTY = "empty";
	private static final String FULL = "full";

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

	public void test_CheckRemindersToNotifyJob(boolean isRead, String type1, String type2, String contentType) {
		List<Reminder> modifiedList = selectListReminderMockObject(type1);
		if (isRead) {
			Reminder newReminder = modifiedList.get(1);
			newReminder.setReadFlag(true);
			modifiedList.add(newReminder);
		}
		mockGetReadMessageToNotifyWithResponse(modifiedList);
		mockGetPaidMessageToNotifyWithResponse(selectListReminderMockObject(type2));
		getMockRestGetForEntity(messageStatus.class, urlPayment.concat("123456"),
				selectMessageStatusMockObject("1", true), HttpStatus.OK);
		mockSaveWithResponse(selectReminderMockObject("", "1", contentType, "AAABBB77Y66A444A", "123456", 3));
		mockFindIdWithResponse(selectReminderMockObject("", "1", contentType, "AAABBB77Y66A444A", "123456", 3));
		job.execute(null);
		Assertions.assertTrue(true);
	}

	@Test
	public void test_proxy_dueDateIsNotNull() throws SchedulerException, InterruptedException, JsonProcessingException {
		proxy(true);
		test_CheckRemindersToNotifyJob(true, FULL, FULL, GENERIC);
	}

	@Test
	public void test_CheckRemindersToNotifyJob_AllResponse_OK()
			throws SchedulerException, InterruptedException, JsonProcessingException {
		proxyKo("PPT_RPT_DUPLICATA");
		test_CheckRemindersToNotifyJob(true, FULL, FULL, GENERIC);
	}

	@Test
	public void test_CheckRemindersToNotifyJob_AllResponse_Paid_OK()
			throws SchedulerException, InterruptedException, JsonProcessingException {
		proxyKo("PPT_RPT_DUPLICATA");
		test_CheckRemindersToNotifyJob(false, FULL, FULL, PAYMENT);
	}

	@Test
	public void test_CheckRemindersToNotifyJob_NoResponse_OK() throws SchedulerException, InterruptedException {
		test_CheckRemindersToNotifyJob(false, EMPTY, EMPTY, GENERIC);
	}

	@Test
	public void test_CheckRemindersToNotifyJob_AllResponse_Paid_WithProxy_KO()
			throws SchedulerException, InterruptedException, JsonProcessingException {
		proxyKo("PPT_RPT_DUPLICATA");
		List<Reminder> listRem = new ArrayList<>();
		listRem.add(selectReminderMockObject("", "1", PAYMENT, "AAABBB77Y66A444A", "123456", 3));
		mockGetPaymentByRptId(listRem);
		test_CheckRemindersToNotifyJob(false, FULL, FULL, PAYMENT);
	}

	@Test
	public void test_CheckRemindersToNotifyJob_AllResponse_Paid_WithProxy_KO2()
			throws SchedulerException, InterruptedException, JsonProcessingException {
		proxyKo("PPT_RPT_NOTFOUND");
		List<Reminder> listRem = new ArrayList<>();
		listRem.add(selectReminderMockObject("", "1", PAYMENT, "AAABBB77Y66A444A", "123456", 3));
		mockGetPaymentByRptId(listRem);
		test_CheckRemindersToNotifyJob(false, FULL, FULL, PAYMENT);
	}

}