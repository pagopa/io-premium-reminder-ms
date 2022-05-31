package it.gov.pagopa.reminder.dto;

import org.springframework.beans.factory.annotation.Value;

import lombok.NoArgsConstructor;

//@Getter
//@Setter
@NoArgsConstructor
public class SenderMetadata {

	@Value("${notification.senderMetadata.serviceName}")
	private String service_name;
	@Value("${notification.senderMetadata.organizationName}")
	private String organization_name;
	@Value("${notification.senderMetadata.departmentName}")
	private String department_name;
}
