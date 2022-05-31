package it.gov.pagopa.reminder.service;

import it.gov.pagopa.reminder.model.Reminder;

public interface ReminderService {

	Reminder findById(String id);	
	void save(Reminder reminder);
	void updateReminder(String reminderId, boolean isRead, boolean isPaid);
	String healthCheck();
	Reminder getPaymentByNoticeNumberAndFiscalCode(String noticeNumber, String payeeFiscalCode);
	void getMessageToNotify();
	void deleteMessage();
}
