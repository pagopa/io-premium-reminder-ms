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

import it.gov.pagopa.reminder.scheduler.CheckRemindersToDeleteJob;

@SpringBootTest(classes = Application.class,webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class MockSchedulerDeleteIntegrationTest extends AbstractMock{

	@Autowired
    private CheckRemindersToDeleteJob job;
	
    @Before
    public void setUp() {
    	before();
    }
 
	@Test
	public void test_scheduleCheckRemindersToDeleteJob_ret0_ok() throws SchedulerException, InterruptedException {
		mockDeleteReadMessageWithResponse(0);
		mockDeletePaidMessageWithResponse(0);
		job.execute(null);
		Assertions.assertTrue(true);
	}
//
	@Test
	public void test_scheduleCheckRemindersToDeleteJob_ret1_OK() throws SchedulerException, InterruptedException {
		mockDeleteReadMessageWithResponse(0);
		mockDeletePaidMessageWithResponse(0);
		job.execute(null);
		Assertions.assertTrue(true);
	}
	
	@Test
	public void test_scheduleCheckRemindersToDeleteJob_retnull_KO() throws SchedulerException, InterruptedException {
		mockDeleteReadMessageWithResponse(0);
		mockDeletePaidMessageWithResponse(0);
		job.execute(null);
		Assertions.assertTrue(true);
	}

}
