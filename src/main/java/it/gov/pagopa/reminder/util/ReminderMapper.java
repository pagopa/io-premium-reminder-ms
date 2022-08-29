package it.gov.pagopa.reminder.util;

import dto.message;
import it.gov.pagopa.reminder.model.Reminder;

public class ReminderMapper {

    public static Reminder messageToReminder(message msg) {
    	Reminder reminder = new Reminder();
        reminder.setId(msg.getId());
        reminder.setDueDate(msg.getDueDate());
        reminder.setFiscalCode(msg.getFiscalCode());
        reminder.setFeature_level_type(msg.getFeatureLevelType());
        reminder.setContent_type(msg.getContentType());
        reminder.setContent_subject(msg.getContentSubject());
        reminder.setCreatedAt(msg.getCreatedAt());
        reminder.setContent_paymentData_amount(msg.getContentPaymentDataAmount());
        reminder.setContent_paymentData_invalidAfterDueDate(msg.getContentPaymentDataInvalidAfterDueDate());
        reminder.setContent_paymentData_noticeNumber(msg.getContentPaymentDataNoticeNumber());
        reminder.setContent_paymentData_payeeFiscalCode(msg.getContentPaymentDataPayeeFiscalCode());
        reminder.setSenderServiceId(msg.getSenderServiceId());
        reminder.setSenderUserId(msg.getSenderUserId());
        reminder.setTimeToLiveSeconds(msg.getTimeToLiveSeconds());
        reminder.setPending(msg.getIsPending());
        return reminder;
    }
}
