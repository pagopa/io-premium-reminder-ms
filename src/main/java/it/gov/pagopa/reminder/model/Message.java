package it.gov.pagopa.reminder.model;

import java.time.LocalDate;

import org.springframework.data.annotation.Id;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;

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
	protected String content_subject="undefined";
	protected MessageContentType content_type;
	protected double content_paymentData_amount;
	protected String content_paymentData_noticeNumber;
	protected boolean content_paymentData_invalidAfterDueDate;
	protected String content_paymentData_payeeFiscalCode;
	protected String fiscalCode;
	protected Long dueDate;
	protected FeatureLevelType feature_level_type;
}
