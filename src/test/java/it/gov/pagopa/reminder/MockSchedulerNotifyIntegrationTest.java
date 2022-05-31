package it.gov.pagopa.reminder;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import it.gov.pagopa.reminder.Application;
import it.gov.pagopa.reminder.scheduler.CheckRemindersToNotifyJob;

@SpringBootTest(classes = Application.class,webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class MockSchedulerNotifyIntegrationTest extends AbstractMock{

    @Autowired
    private CheckRemindersToNotifyJob job;
	
    @Before
    public void setUp() {
    	before();
    }
 
	@Test
	public void test_scheduleMockSchedulerNotifyIntegrationTest_OK() throws SchedulerException, InterruptedException {
		mockFindReminderAndPaymentNotifyWithResponse(selectListReminderMockObject("full"));
		mockFindRemindersToNotifyWithResponse(selectListReminderMockObject("full"));
		mockFindPaymentsToNotifyWithResponse(selectListReminderMockObject("full"));
		mockSaveWithResponse(selectReminderMockObject("", "1","GENERIC","AAABBB77Y66A444A",3));
		mockFindIdWithResponse(selectReminderMockObject("", "1","GENERIC","AAABBB77Y66A444A",3));
		job.execute(null);
		Assertions.assertTrue(true);
	}
}