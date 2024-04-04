package it.gov.pagopa.reminder.service;

import java.util.List;
import java.util.Optional;

import it.gov.pagopa.reminder.model.Reminder;

public interface ReminderService {

	Optional<Reminder> findById(String id);

	void save(Reminder reminder);

	void updateReminder(String reminderId, boolean isRead);

	String healthCheck();

	void getMessageToNotify(String shard);

	void deleteMessage();

	List<Reminder> getPaymentsByRptid(String rptId);

	int countById(String shard, String id);

	void sendReminderNotification(Reminder reminder);
}
