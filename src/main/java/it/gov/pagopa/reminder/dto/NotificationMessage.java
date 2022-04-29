package it.gov.pagopa.reminder.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage {

	private String id;
	private String fiscal_code;
	private String created_at;
	private String sender_service_id;
	private int time_to_live;
}
