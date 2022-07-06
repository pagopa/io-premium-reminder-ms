package it.gov.pagopa.reminder.service;

import it.gov.pagopa.reminder.model.Reminder;

public interface ReminderService {
	/**
	 * Method that retrieves a Reminder corresponding to the input id
	 * @param id The Reminder id
	 * @return Reminder
	 */
	Reminder findById(String id);	
	/**
	 * Method that save a new Reminder
	 * @param reminder The Reminder to save
	 * @return The Reminder saved
	 */
	Reminder save(Reminder reminder);
	/**
	 * Method that updates a Reminder corresponding to the input id
	 * @param reminderId The Reminder id
	 * @param isRead
	 * @param isPaid
	 * @return The Reminder updated
	 */
	Reminder updateReminder(String reminderId, boolean isRead);
	/**
	 * Method to verify that the service is active
	 * @return sample string
	 */
	String healthCheck();
	/**
	 * Method that retrieves a Reminder corresponding to the notice number and payer fiscal code
	 * @param noticeNumber
	 * @param payeeFiscalCode
	 * @return Reminder
	 */
	Reminder getPaymentByNoticeNumberAndFiscalCode(String noticeNumber, String payeeFiscalCode);
	/**
	 * Method that retrieves the Reminders message to notify 
	 * 
	 */
	void getMessageToNotify();
	/**
	 * Method that deletes non-notifiable messages
	 * @return Number of deleted messages
	 */
	int deleteMessage();
}
