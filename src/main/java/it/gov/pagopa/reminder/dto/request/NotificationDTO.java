package it.gov.pagopa.reminder.dto.request;

import it.gov.pagopa.reminder.dto.NotificationMessage;
import it.gov.pagopa.reminder.dto.SenderMetadata;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
//@Setter
//@NoArgsConstructor	
@AllArgsConstructor
public class NotificationDTO {

	NotificationMessage message;
	SenderMetadata sender_metadata;
}
