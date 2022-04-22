package it.gov.pagopa.reminder.model;

import org.springframework.data.annotation.Id;
import it.gov.pagopa.reminder.dto.avro.MessageContentType;
import it.gov.pagopa.reminder.dto.avro.MessageCrudOperation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Message {

	@Id
	protected String id;
	protected MessageCrudOperation op;
	protected String senderServiceId="undefined";
	protected String senderUserId="undefined";
	protected int timeToLiveSeconds;
	protected long createdAt;
	protected boolean isPending = true;
	protected String content_subject="undefined";
	protected MessageContentType content_type;
	protected int content_paymentData_amount;
	protected String content_paymentData_noticeNumber="undefined";
	protected boolean content_paymentData_invalidAfterDueDate;
	protected String content_paymentData_payeeFiscalCode="undefined";
	protected long timestamp;
	protected String fiscalCode;
	
}
