package it.gov.pagopa.reminder.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;

import dto.FeatureLevelType;
import dto.MessageContentType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Message {

	@Id
	protected String id;
	protected String senderServiceId;
	protected String senderUserId;
	protected int timeToLiveSeconds;
	protected long createdAt;
	protected boolean isPending = true;
	protected String content_subject = "undefined";
	protected MessageContentType content_type;
	protected double content_paymentData_amount;
	protected String content_paymentData_noticeNumber;
	protected boolean content_paymentData_invalidAfterDueDate;
	protected String content_paymentData_payeeFiscalCode;
	protected String fiscalCode;
	protected String shard = "A";
	protected LocalDateTime dueDate;
	protected FeatureLevelType feature_level_type;
}
