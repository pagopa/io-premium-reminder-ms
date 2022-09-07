package it.gov.pagopa.reminder.service;

import java.util.List;

import it.gov.pagopa.reminder.model.Reminder;

public interface ReminderService {

	Reminder findById(String id);	
	void save(Reminder reminder);
	void updateReminder(String reminderId, boolean isRead);
	String healthCheck();
	Reminder getPaymentByNoticeNumberAndFiscalCode(String noticeNumber, String payeeFiscalCode);
	void getMessageToNotify();
	void deleteMessage();
	List<Reminder> getPaymentsByRptid(String rptId);
}
