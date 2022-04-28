package it.gov.pagopa.reminder.util;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import it.gov.pagopa.reminder.model.Reminder;

public class ReminderUtil {

	private final static String UNDEFINED = "undefined";

	public static void checkNullInMessage(Reminder reminder) {
		if (Objects.nonNull(reminder)) {
			if (StringUtils.isEmpty(reminder.getSenderServiceId())){
				reminder.setSenderServiceId(UNDEFINED);
			}
			if (StringUtils.isEmpty(reminder.getSenderUserId())){
				reminder.setSenderUserId(UNDEFINED);
			}
			if (StringUtils.isEmpty(reminder.getContent_paymentData_payeeFiscalCode())){
				reminder.setContent_paymentData_payeeFiscalCode(UNDEFINED);
			}
			if (StringUtils.isEmpty(reminder.getContent_paymentData_noticeNumber())){
				reminder.setContent_paymentData_noticeNumber(UNDEFINED);
			}
		}
	}
}
