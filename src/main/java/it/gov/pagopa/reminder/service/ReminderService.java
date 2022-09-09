package it.gov.pagopa.reminder.service;

import java.util.List;

import it.gov.pagopa.reminder.model.Reminder;

public interface ReminderService {

	Reminder findById(String id);	
	void save(Reminder reminder);
	void updateReminder(String reminderId, boolean isRead);
	String healthCheck();
	void getMessageToNotify();
	void deleteMessage();
	List<Reminder> getPaymentsByRptid(String rptId);
	int countFindById(String id);
}
