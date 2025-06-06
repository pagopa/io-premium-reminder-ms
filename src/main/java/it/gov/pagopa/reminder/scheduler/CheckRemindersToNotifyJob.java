package it.gov.pagopa.reminder.scheduler;

import java.time.Duration;
import java.time.Instant;

import javax.transaction.Transactional;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.gov.pagopa.reminder.service.ReminderService;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@DisallowConcurrentExecution
public class CheckRemindersToNotifyJob implements Job {

	private static final String JOB_LOG_NAME = "Reminders to NOTIFY Job ";

	private final ReminderService reminderService;

	@Autowired
	public CheckRemindersToNotifyJob(ReminderService reminderService) {
		this.reminderService = reminderService;
	}

	@Transactional(Transactional.TxType.NOT_SUPPORTED)
	public void execute(JobExecutionContext context) {
		log.warn(JOB_LOG_NAME + "started for instance " + context.getFireInstanceId() + " and jobKey "
				+ context.getJobDetail().getKey().getName());
		Instant start = Instant.now();
		reminderService.getMessageToNotify(context.getMergedJobDataMap().getString("shard"));
		Instant end = Instant.now();
		log.warn(JOB_LOG_NAME + "ended for instance " + context.getFireInstanceId() + " and jobKey "
				+ context.getJobDetail().getKey().getName() + " in "
				+ Duration.between(start, end).getSeconds() + " seconds");
	}

}
