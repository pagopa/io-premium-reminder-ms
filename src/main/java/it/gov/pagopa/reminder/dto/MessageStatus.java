package it.gov.pagopa.reminder.dto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MessageStatus {

	String messageId;
	boolean isRead;
	boolean isPaid;

}