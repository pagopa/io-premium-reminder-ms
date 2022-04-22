package it.gov.pagopa.reminder.model;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@JsonIgnoreProperties
@Document
public class Reminder extends Message{

	private boolean readFlag;
	private boolean paidFlag;
	private boolean reminderFlag=true;
	@CreatedDate
	private LocalDateTime dataInserimento;
	private LocalDateTime dataScadenza;
	private List<LocalDateTime> dateReminder;
	private LocalDateTime dataUltimoReminder;
	private int numReminder;
	
	@Override
	public String toString() {
		return String.format("%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s", 
				readFlag, paidFlag, reminderFlag, dataInserimento, dataScadenza, dateReminder, dataUltimoReminder, numReminder,
				id, op, senderServiceId, senderUserId, timeToLiveSeconds, createdAt, isPending, 
				content_subject, content_type, content_paymentData_amount, content_paymentData_noticeNumber,
				content_paymentData_invalidAfterDueDate, content_paymentData_payeeFiscalCode, timestamp, fiscalCode);
	}

}
