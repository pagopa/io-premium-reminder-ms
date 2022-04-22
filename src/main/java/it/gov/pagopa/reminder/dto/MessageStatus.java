package it.gov.pagopa.reminder.dto;
import lombok.Getter;
import lombok.NoArgsConstructor;



@Getter
@NoArgsConstructor
public class MessageStatus {

private String messageId;
private boolean isRead;
//TODO: AGGIUNGERE isPaid

}