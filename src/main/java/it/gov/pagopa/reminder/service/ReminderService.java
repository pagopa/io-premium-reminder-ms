package it.gov.pagopa.reminder.service;

import it.gov.pagopa.reminder.model.Reminder;

public interface ReminderService {

	void deleteReminders();

	void getRemindersToNotify();

	Reminder findById(String id);
	
	void save(Reminder reminder);
	
	void updateReminder(String reminderId, boolean isRead, boolean isPaid);

}
