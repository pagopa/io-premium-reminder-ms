package it.gov.pagopa.reminder.dto;

import org.springframework.beans.factory.annotation.Value;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class SenderMetadata {

	@Value("${notification.senderMetadata.serviceName}")
	private String service_name;
	@Value("${notification.senderMetadata.organizationName}")
	private String organization_name;
	@Value("${notification.senderMetadata.departmentName}")
	private String department_name;
}
