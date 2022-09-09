package it.gov.pagopa.reminder.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
//@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PaymentMessage {

	String noticeNumber;
	String payeeFiscalCode;
	boolean paid;
	LocalDateTime dueDate;
	double amount;
	String source;
	String fiscalCode;
}
